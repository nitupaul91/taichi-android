package taichi.walking.seniors.beginners.taichi.ui.home.workout

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobteq.analytics.logAppEvent
import com.mobteq.remoteconfig.RemoteConfig

/** What the screen hosting the workout knows and the workout itself does not. */
data class WorkoutAnalyticsContext(
    val isPremium: Boolean = false,
    /** Stored intensity "1".."5", or "" before the first plan set one. */
    val intensity: String = "",
    /** iOS values: "home_day_card", "preview_exercise_row", "onboarding_first_workout". */
    val source: String = "home_day_card",
    val startingExerciseIndex: Int = 0
)

/** Hooks the workout view model calls at the moments iOS logs its workout events. */
interface WorkoutSessionListener {
    fun onExerciseStarted(countdownSkipped: Boolean)
    fun onExerciseCompleted(endedBy: String)
    fun onWatchSecond()
    fun onUserPaused()
}

/**
 * The iOS WorkoutView session events, with the same names, triggers and parameters:
 * workout_started, workout_exercise_started / _completed, workout_video_load_failed,
 * workout_backgrounded / _resumed, workout_completed and workout_abandoned. On every background
 * the abandon parameters are saved with reason "background_killed"; if the process dies there,
 * [reportKilledSessionIfNeeded] logs them as workout_abandoned on the next launch.
 */
class WorkoutSessionAnalytics(
    context: Context,
    private val model: WorkoutViewModel,
    private val session: WorkoutAnalyticsContext
) : WorkoutSessionListener {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val total = model.dayData.exercises.size
    private val freeDaysCount = RemoteConfig.peekLong(FREE_DAYS_COUNT_KEY).takeIf { it > 0 } ?: DEFAULT_FREE_DAYS

    private var hasStarted = false
    private var isTerminal = false
    private var isBackgrounded = false
    private var startedAtMs = 0L
    private var watchSeconds = 0
    private var pauseCount = 0
    private var highestIndex = session.startingExerciseIndex

    private val exerciseId: String get() = model.currentExercise?.exerciseId.orEmpty()
    private val completedExercises: Int get() = maxOf(session.startingExerciseIndex, highestIndex)
    private val oneBasedIndex: Int get() = if (total == 0) 0 else (model.currentExerciseIndex + 1).coerceIn(1, total)
    private val wallClockSeconds: Int
        get() = if (startedAtMs == 0L) 0 else ((System.currentTimeMillis() - startedAtMs) / 1000).toInt()
    private val partialCompletionPercent: Int
        get() = if (total == 0) 0 else (completedExercises * 100 / total).coerceIn(0, 99)

    private fun Bundle.common(): Bundle = apply {
        putInt("day_number", model.dayNumber)
        putBoolean("is_free_day", model.dayNumber <= freeDaysCount)
        putString("subscription_status", if (session.isPremium) "paid" else "free")
        putString("source", session.source)
    }

    private fun Bundle.media(): Bundle = apply {
        putString("music_mode", when (model.musicMode) {
            WorkoutMusicMode.Off -> "off"
            WorkoutMusicMode.Shuffle -> "shuffle"
            is WorkoutMusicMode.Track -> "track"
        })
        putString("music_track_id", model.activeMusicTrack?.id.orEmpty())
        putBoolean("voice_enabled", model.isAudioEnabled)
        putString("intensity", session.intensity)
        putInt("wall_clock_seconds", wallClockSeconds)
        putInt("pause_count", pauseCount)
    }

    private fun snapshotParams(): Bundle = Bundle().common().apply {
        putInt("exercise_index", oneBasedIndex)
        putString("exercise_id", exerciseId)
        putInt("completion_percent", partialCompletionPercent)
        putInt("total_watch_time_seconds", watchSeconds)
        putBoolean("music_enabled", model.musicMode != WorkoutMusicMode.Off)
    }

    private fun abandonedParams(reason: String): Bundle = snapshotParams().media().apply {
        putInt("starting_exercise_index", session.startingExerciseIndex)
        putString("reason", reason)
        putInt("exercise_elapsed_seconds", model.exerciseElapsedSeconds)
    }

    /** The video rendered for the first time (during the first "UP NEXT" countdown, as on iOS). */
    fun onPlaybackStarted() {
        if (hasStarted || isTerminal) return
        hasStarted = true
        startedAtMs = System.currentTimeMillis()
        logAppEvent("workout_started", Bundle().common().media().apply {
            putInt("exercise_count", total)
            putInt("starting_exercise_index", session.startingExerciseIndex)
            putString("exercise_id", exerciseId)
        })
    }

    override fun onExerciseStarted(countdownSkipped: Boolean) {
        highestIndex = maxOf(highestIndex, model.currentExerciseIndex)
        logAppEvent("workout_exercise_started", Bundle().apply {
            putInt("day_number", model.dayNumber)
            putInt("exercise_index", model.currentExerciseIndex)
            putString("exercise_id", exerciseId)
            putInt("exercise_count", total)
            putInt("exercise_duration_seconds", model.currentExercise?.duration ?: 0)
            putBoolean("was_countdown_skipped", countdownSkipped)
            putString("source", session.source)
        })
    }

    override fun onExerciseCompleted(endedBy: String) {
        val duration = model.currentExercise?.duration ?: 0
        val elapsed = if (endedBy == ENDED_BY_AUTO && duration > 0) duration else model.exerciseElapsedSeconds
        logAppEvent("workout_exercise_completed", Bundle().apply {
            putInt("day_number", model.dayNumber)
            putInt("exercise_index", model.currentExerciseIndex)
            putString("exercise_id", exerciseId)
            putInt("duration_seconds", duration)
            putInt("elapsed_seconds", elapsed)
            putInt("completion_percent", if (duration > 0) minOf(100, elapsed * 100 / duration) else 0)
            putString("ended_by", endedBy)
        })
    }

    override fun onWatchSecond() {
        watchSeconds += 1
    }

    override fun onUserPaused() {
        pauseCount += 1
    }

    fun onVideoLoadFailed(error: String, isCached: Boolean) {
        logAppEvent("workout_video_load_failed", Bundle().apply {
            putInt("day_number", model.dayNumber)
            putInt("exercise_index", model.currentExerciseIndex)
            putString("exercise_id", exerciseId)
            putBoolean("is_cached", isCached)
            putString("error", error.take(100))
            putInt("elapsed_seconds", model.exerciseElapsedSeconds)
        })
    }

    fun onBackgrounded() {
        if (!hasStarted || isTerminal) return
        isBackgrounded = true
        logAppEvent("workout_backgrounded", snapshotParams())
        saveSnapshot(abandonedParams(REASON_BACKGROUND_KILLED))
    }

    fun onResumed() {
        if (!isBackgrounded) return
        isBackgrounded = false
        clearSnapshot()
        if (!isTerminal) logAppEvent("workout_resumed", snapshotParams())
    }

    /** [exitEarly]: closed during the last exercise or two, which counts as done (iOS parity). */
    fun onCompleted(exitEarly: Boolean) {
        if (isTerminal) return
        val remaining = (total - completedExercises).coerceAtLeast(0)
        finish()
        logAppEvent("workout_completed", Bundle().common().media().apply {
            putInt("exercise_count", total)
            putInt("total_watch_time_seconds", watchSeconds)
            putInt("completion_percent", 100)
            putBoolean("music_enabled", model.musicMode != WorkoutMusicMode.Off)
            putInt("starting_exercise_index", session.startingExerciseIndex)
            putString("exercise_id", exerciseId)
            putBoolean("exit_early", exitEarly)
            putInt("remaining_exercises", remaining)
        })
    }

    /** iOS reasons: close_button, offline_go_back, disappear. Ignored before the video ever played. */
    fun onAbandoned(reason: String) {
        if (isTerminal || !hasStarted) return
        val params = abandonedParams(reason)
        finish()
        logAppEvent("workout_abandoned", params)
    }

    private fun finish() {
        isTerminal = true
        clearSnapshot()
    }

    private fun saveSnapshot(params: Bundle) {
        prefs.edit().putString(KEY_SNAPSHOT, Gson().toJson(params.toJsonObject())).apply()
    }

    private fun clearSnapshot() {
        prefs.edit().remove(KEY_SNAPSHOT).apply()
    }

    companion object {
        const val ENDED_BY_AUTO = "auto"
        const val ENDED_BY_NEXT_TAP = "next_tap"
        const val REASON_CLOSE_BUTTON = "close_button"
        const val REASON_DISAPPEAR = "disappear"
        private const val REASON_BACKGROUND_KILLED = "background_killed"

        private const val PREFS_NAME = "workout_analytics"
        private const val KEY_SNAPSHOT = "analytics.workout_session_snapshot"
        private const val FREE_DAYS_COUNT_KEY = "free_days_count"
        private const val DEFAULT_FREE_DAYS = 3L

        /**
         * iOS AppOpenAnalyticsTracker.reportKilledSessionIfNeeded: a workout that was in the
         * background when the process died is reported once, as workout_abandoned with the
         * parameters saved at that moment.
         */
        fun reportKilledSessionIfNeeded(context: Context) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return
            prefs.edit().remove(KEY_SNAPSHOT).apply()
            val json = runCatching { Gson().fromJson(raw, JsonObject::class.java) }.getOrNull() ?: return
            val params = Bundle()
            json.entrySet().forEach { (key, value) ->
                val primitive = value.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return@forEach
                when {
                    primitive.isBoolean -> params.putBoolean(key, primitive.asBoolean)
                    primitive.isNumber -> params.putLong(key, primitive.asLong)
                    else -> params.putString(key, primitive.asString)
                }
            }
            logAppEvent("workout_abandoned", params)
        }

        @Suppress("DEPRECATION")
        private fun Bundle.toJsonObject(): JsonObject = JsonObject().also { json ->
            keySet().forEach { key ->
                when (val value = get(key)) {
                    is Boolean -> json.addProperty(key, value)
                    is Number -> json.addProperty(key, value)
                    null -> Unit
                    else -> json.addProperty(key, value.toString())
                }
            }
        }
    }
}
