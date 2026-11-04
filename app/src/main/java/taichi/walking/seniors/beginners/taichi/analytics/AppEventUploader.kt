package taichi.walking.seniors.beginners.taichi.analytics

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.sageai.id.IDService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import taichi.walking.seniors.beginners.BuildConfig
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

interface AppEventApi {
    @POST("events/android/upload")
    suspend fun upload(@Body body: AppEventBatch): Response<Unit>
}

// @SerializedName everywhere: Gson serializes by reflection and release R8 renames fields.
data class AppEventBatch(
    @SerializedName("userId") val userId: String,
    @SerializedName("sentAtMs") val sentAtMs: Long,
    @SerializedName("events") val events: List<AppEvent>
)

data class AppEvent(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("name") val name: String,
    /** JsonObject keeps ints as ints through the on-disk queue (a Map<String, Any> would turn 3 into 3.0). */
    @SerializedName("props") val props: JsonObject,
    @SerializedName("appVersion") val appVersion: String?,
    @SerializedName("build") val build: String?,
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("sessionIndex") val sessionIndex: Int,
    @SerializedName("daysSinceInstall") val daysSinceInstall: Int,
    @SerializedName("seq") val seq: Long,
    @SerializedName("clientTimestampMs") val clientTimestampMs: Long
)

/**
 * Port of iOS AppEventUploader: mirrors every analytics event to our own backend
 * (`events/android/upload`) so the user-journey dashboard sees Android installs too. Events are
 * queued on disk, numbered per install and per session, and uploaded in batches with retries.
 * Nothing here ever blocks the UI and a failing upload never loses the queue.
 *
 * All state is touched on one single-threaded dispatcher, so `seq` order equals call order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AppEventUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AppEventApi,
    private val idService: IDService,
    private val gson: Gson
) {
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val storeFile = File(File(context.filesDir, "app-events").apply { mkdirs() }, "queue.json")

    private var pending = mutableListOf<AppEvent>()
    private var inFlight = false
    private var failures = 0
    private var nextAllowedAtMs = 0L
    private var sessionId = ""
    private var sessionIndex = 0
    private var snapshotSessionId: String? = null
    private var backgroundedAtMs: Long? = null
    private var persistJob: Job? = null

    init {
        scope.launch {
            load()
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flushLocked(FlushReason.TIMER)
            }
        }
    }

    /** Call once per process launch: rotates the session and uploads what the last visit left. */
    fun startSession() {
        scope.launch {
            beginSessionLocked()
            flushLocked(FlushReason.SESSION_START)
        }
    }

    /** App back in the foreground: only a return after [SESSION_TIMEOUT_MS] starts a new visit. */
    fun resumeSession() {
        scope.launch {
            val backgroundedAt = backgroundedAtMs
            if (backgroundedAt != null && System.currentTimeMillis() - backgroundedAt >= SESSION_TIMEOUT_MS) {
                beginSessionLocked()
            }
            backgroundedAtMs = null
            flushLocked(FlushReason.SESSION_START)
        }
    }

    /** App went to the background: remember when, and upload the queue. */
    fun enterBackground() {
        scope.launch {
            backgroundedAtMs = System.currentTimeMillis()
            flushLocked(FlushReason.BACKGROUND)
        }
    }

    fun enqueue(name: String, params: Bundle) {
        val props = params.toJson()
        scope.launch { enqueueLocked(name, props) }
    }

    /** Experiment arms live in Remote Config; one snapshot per session makes them visible server-side. */
    fun recordRemoteConfigSnapshotIfNeeded(values: Map<String, String>) {
        scope.launch {
            if (sessionId.isEmpty()) beginSessionLocked()
            if (snapshotSessionId == sessionId || values.isEmpty()) return@launch
            snapshotSessionId = sessionId
            val props = JsonObject().apply {
                values.forEach { (key, value) -> addProperty(key, value.take(MAX_STRING_LENGTH)) }
            }
            enqueueLocked(REMOTE_CONFIG_SNAPSHOT_EVENT, props)
        }
    }

    private fun beginSessionLocked() {
        sessionId = UUID.randomUUID().toString().lowercase()
        sessionIndex = prefs.getInt(KEY_SESSION_INDEX, 0) + 1
        prefs.edit().putInt(KEY_SESSION_INDEX, sessionIndex).apply()
        if (!prefs.contains(KEY_INSTALLED_AT)) {
            prefs.edit().putLong(KEY_INSTALLED_AT, System.currentTimeMillis()).apply()
        }
        failures = 0
        nextAllowedAtMs = 0L
    }

    private suspend fun enqueueLocked(name: String, props: JsonObject) {
        // Events can arrive before the first startSession (early init code); never leave them sessionless.
        if (sessionId.isEmpty()) beginSessionLocked()

        val seq = prefs.getLong(KEY_SEQ, 0L) + 1
        prefs.edit().putLong(KEY_SEQ, seq).apply()
        val now = System.currentTimeMillis()
        val installedAt = prefs.getLong(KEY_INSTALLED_AT, now)
        pending += AppEvent(
            eventId = UUID.randomUUID().toString().lowercase(),
            name = name,
            props = props,
            appVersion = BuildConfig.VERSION_NAME,
            build = BuildConfig.VERSION_CODE.toString(),
            sessionId = sessionId,
            sessionIndex = sessionIndex,
            daysSinceInstall = ((now - installedAt) / DAY_MS).toInt().coerceAtLeast(0),
            seq = seq,
            clientTimestampMs = now
        )
        if (pending.size > MAX_QUEUED) pending = pending.takeLast(MAX_QUEUED).toMutableList()
        schedulePersistLocked()
        if (pending.size >= FLUSH_THRESHOLD) flushLocked(FlushReason.THRESHOLD)
    }

    private suspend fun flushLocked(reason: FlushReason) {
        if (inFlight || pending.isEmpty()) return
        if (System.currentTimeMillis() < nextAllowedAtMs && reason != FlushReason.SESSION_START) return

        val batch = pending.take(BATCH_SIZE)
        val userId = runCatching { idService.getUserID() }.getOrNull().orEmpty()
        if (userId.isBlank()) return // the id service assigns one at first launch; try again later
        inFlight = true
        scope.launch {
            val result = runCatching {
                api.upload(AppEventBatch(userId = userId, sentAtMs = System.currentTimeMillis(), events = batch))
            }
            val code = result.getOrNull()?.code()
            when {
                code != null && code in 200..299 -> handleSuccessLocked(batch)
                // The server rejected the batch itself (not transient): retrying cannot help.
                code != null && code in 400..499 && code !in TRANSIENT_CODES -> {
                    Timber.w("AppEvents: batch of %d rejected with %d; dropping", batch.size, code)
                    inFlight = false
                    removeLocked(batch)
                }
                else -> handleFailureLocked(result.exceptionOrNull(), code)
            }
        }
    }

    private suspend fun handleSuccessLocked(batch: List<AppEvent>) {
        inFlight = false
        failures = 0
        nextAllowedAtMs = 0L
        removeLocked(batch)
        if (pending.size >= FLUSH_THRESHOLD) flushLocked(FlushReason.THRESHOLD)
    }

    private fun handleFailureLocked(error: Throwable?, code: Int?) {
        inFlight = false
        failures += 1
        val delayMs = min(MAX_BACKOFF_MS.toDouble(), 10_000.0 * 2.0.pow(failures - 1)).toLong()
        nextAllowedAtMs = System.currentTimeMillis() + delayMs
        Timber.d(error, "AppEvents: upload failed (code=%s); retrying in %ds, %d queued", code, delayMs / 1000, pending.size)
    }

    private fun removeLocked(batch: List<AppEvent>) {
        val ids = batch.mapTo(HashSet()) { it.eventId }
        pending.removeAll { it.eventId in ids }
        schedulePersistLocked()
    }

    private fun load() {
        pending = runCatching {
            if (!storeFile.exists()) return@runCatching mutableListOf()
            val type = object : TypeToken<List<AppEvent>>() {}.type
            gson.fromJson<List<AppEvent>>(storeFile.readText(), type).orEmpty().toMutableList()
        }.getOrElse { mutableListOf() }
    }

    private fun schedulePersistLocked() {
        persistJob?.cancel()
        persistJob = scope.launch {
            delay(PERSIST_DEBOUNCE_MS)
            runCatching {
                val tmp = File(storeFile.parentFile, "queue.json.tmp")
                tmp.writeText(gson.toJson(pending))
                tmp.renameTo(storeFile)
            }.onFailure { Timber.w(it, "AppEvents: could not persist queue") }
        }
    }

    private enum class FlushReason { TIMER, THRESHOLD, SESSION_START, BACKGROUND }

    companion object {
        private const val PREFS_NAME = "app_events"
        private const val KEY_SEQ = "appEvents.seq"
        private const val KEY_SESSION_INDEX = "appEvents.sessionIndex"
        private const val KEY_INSTALLED_AT = "appEvents.installedAtMs"

        private const val FLUSH_INTERVAL_MS = 10_000L
        private const val FLUSH_THRESHOLD = 20
        private const val BATCH_SIZE = 100
        private const val MAX_QUEUED = 2_000
        private const val MAX_BACKOFF_MS = 300_000L
        private const val PERSIST_DEBOUNCE_MS = 500L
        private const val DAY_MS = 86_400_000L
        private const val MAX_STRING_LENGTH = 200

        /** Coming back sooner than this continues the same session (system dialogs, quick trips). */
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L
        private val TRANSIENT_CODES = setOf(401, 408, 429)

        const val REMOTE_CONFIG_SNAPSHOT_EVENT = "remote_config_snapshot"

        /** iOS JSONValue: bools, whole numbers, doubles and strings (capped at 200 chars). */
        @Suppress("DEPRECATION")
        internal fun Bundle.toJson(): JsonObject = JsonObject().also { json ->
            keySet().forEach { key ->
                when (val value = get(key)) {
                    null -> Unit
                    is Boolean -> json.addProperty(key, value)
                    is Int, is Long, is Short, is Byte -> json.addProperty(key, (value as Number).toLong())
                    is Float, is Double -> json.addProperty(key, (value as Number).toDouble())
                    else -> json.addProperty(key, value.toString().take(MAX_STRING_LENGTH))
                }
            }
        }
    }
}
