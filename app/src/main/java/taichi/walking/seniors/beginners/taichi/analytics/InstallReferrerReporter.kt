package taichi.walking.seniors.beginners.taichi.analytics

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.gson.annotations.SerializedName
import com.mobteq.analytics.logAppEvent
import com.sageai.id.IDService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import taichi.walking.seniors.beginners.BuildConfig
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

interface InstallReferrerApi {
    @POST("config/android/installReferrer")
    suspend fun upload(@Body body: InstallReferrerBody): Response<Unit>
}

// @SerializedName everywhere: Gson serializes by reflection and release R8 renames fields.
data class InstallReferrerBody(
    @SerializedName("userId") val userId: String,
    @SerializedName("installReferrer") val installReferrer: String,
    @SerializedName("referrerClickTimestampSeconds") val referrerClickTimestampSeconds: Long,
    @SerializedName("installBeginTimestampSeconds") val installBeginTimestampSeconds: Long,
    @SerializedName("appVersion") val appVersion: Int
)

/**
 * Tells the server where this install came from, once per install. Our tracking links send Android
 * visitors to the Play Store with `click_id` in the install referrer; Play hands that referrer back
 * here on first launch, and the server ties the install to that exact click, which is how the
 * organic, YouTube and web-to-app dashboards see Android installs. Store searches and Google Ads
 * installs carry Play's own referrer (utm_source=google-play, gclid), so those are told apart too.
 *
 * Play is asked once and its answer kept, so a failed upload retries on later launches without
 * asking again. Nothing here blocks startup, and a phone without Play simply reports nothing.
 */
@Singleton
class InstallReferrerReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: InstallReferrerApi,
    private val idService: IDService
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)

    fun reportIfNeeded() {
        if (prefs.getBoolean(KEY_DONE, false) || !running.compareAndSet(false, true)) return
        scope.launch {
            try {
                val stored = storedReferrer() ?: readFromPlay()?.also { store(it) }
                if (stored != null) upload(stored)
            } finally {
                running.set(false)
            }
        }
    }

    private data class StoredReferrer(val referrer: String, val clickSeconds: Long, val installBeginSeconds: Long)

    private fun storedReferrer(): StoredReferrer? {
        val referrer = prefs.getString(KEY_REFERRER, null) ?: return null
        return StoredReferrer(referrer, prefs.getLong(KEY_CLICK_SECONDS, 0L), prefs.getLong(KEY_INSTALL_BEGIN_SECONDS, 0L))
    }

    private fun store(referrer: StoredReferrer) {
        prefs.edit()
            .putString(KEY_REFERRER, referrer.referrer)
            .putLong(KEY_CLICK_SECONDS, referrer.clickSeconds)
            .putLong(KEY_INSTALL_BEGIN_SECONDS, referrer.installBeginSeconds)
            .apply()
        val params = Uri.parse("https://referrer/?${referrer.referrer}")
        logAppEvent(
            EVENT_INSTALL_REFERRER,
            bundleOf(
                "utm_source" to params.query("utm_source"),
                "utm_medium" to params.query("utm_medium"),
                "has_click_id" to !params.query("click_id").isNullOrEmpty()
            )
        )
    }

    private fun Uri.query(name: String): String? = runCatching { getQueryParameter(name) }.getOrNull()

    /** Null when Play cannot answer now (try again next launch) or never will (marked done). */
    private suspend fun readFromPlay(): StoredReferrer? {
        val client = InstallReferrerClient.newBuilder(context).build()
        val outcome = withContext(Dispatchers.Main) {
            withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    client.startConnection(object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(responseCode: Int) {
                            if (continuation.isActive) continuation.resume(responseCode)
                        }

                        override fun onInstallReferrerServiceDisconnected() {
                            if (continuation.isActive) continuation.resume(InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED)
                        }
                    })
                }
            }
        }
        try {
            return when (outcome) {
                InstallReferrerClient.InstallReferrerResponse.OK -> {
                    val details: ReferrerDetails = runCatching { client.installReferrer }.getOrElse {
                        Timber.w(it, "InstallReferrer: could not read the referrer")
                        return null
                    }
                    StoredReferrer(
                        referrer = details.installReferrer.orEmpty(),
                        clickSeconds = details.referrerClickTimestampSeconds,
                        installBeginSeconds = details.installBeginTimestampSeconds
                    )
                }
                // No Play Store on this phone, or this app was not installed through it: nothing will ever come.
                InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR,
                InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                    Timber.d("InstallReferrer: unavailable (%s)", outcome)
                    prefs.edit().putBoolean(KEY_DONE, true).apply()
                    null
                }
                else -> null
            }
        } finally {
            runCatching { client.endConnection() }
        }
    }

    private suspend fun upload(referrer: StoredReferrer) {
        repeat(UPLOAD_ATTEMPTS) { attempt ->
            val userId = runCatching { idService.getUserID() }.getOrNull().orEmpty()
            if (userId.isNotBlank()) {
                val code = runCatching {
                    api.upload(
                        InstallReferrerBody(
                            userId = userId,
                            installReferrer = referrer.referrer,
                            referrerClickTimestampSeconds = referrer.clickSeconds,
                            installBeginTimestampSeconds = referrer.installBeginSeconds,
                            appVersion = BuildConfig.VERSION_CODE
                        )
                    ).code()
                }.getOrNull()
                // 2xx: stored. Any other 4xx but a throttle: retrying the same body cannot help.
                if (code != null && (code in 200..299 || (code in 400..499 && code != 408 && code != 429))) {
                    prefs.edit().putBoolean(KEY_DONE, true).apply()
                    return
                }
                Timber.d("InstallReferrer: upload failed (code=%s), attempt %d", code, attempt + 1)
            }
            if (attempt < UPLOAD_ATTEMPTS - 1) delay(RETRY_DELAY_MS * (attempt + 1))
        }
    }

    companion object {
        private const val PREFS_NAME = "install_referrer"
        private const val KEY_DONE = "installReferrer.reported"
        private const val KEY_REFERRER = "installReferrer.referrer"
        private const val KEY_CLICK_SECONDS = "installReferrer.clickSeconds"
        private const val KEY_INSTALL_BEGIN_SECONDS = "installReferrer.installBeginSeconds"

        private const val EVENT_INSTALL_REFERRER = "install_referrer"
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val UPLOAD_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 10_000L
    }
}
