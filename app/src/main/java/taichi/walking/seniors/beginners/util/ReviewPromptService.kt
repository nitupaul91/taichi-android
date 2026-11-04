package taichi.walking.seniors.beginners.util

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import taichi.walking.seniors.beginners.BuildConfig

/**
 * Single owner of the Play in-app review requests, ported from iOS `ReviewPromptService` (874528f).
 *
 * iOS moved its rating ask out of onboarding (App Review guideline 5.6.3; Google's in-app review
 * guidance likewise says to ask only after the user has experienced the app) to two engagement
 * points: finishing the first session and coming back to Home a second time. Each call site owns
 * its own gate; this owns what must never differ between them: at most one request per app version,
 * and never while the app is not in the foreground.
 */
object ReviewPromptService {

    /** Requests a review [delayMillis] from now unless this app version already asked. */
    suspend fun requestIfNeeded(activity: ComponentActivity, surface: String, delayMillis: Long = 0) {
        val prefs = activity.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_LAST_REQUEST_VERSION, null) == currentVersion) return

        delay(delayMillis)

        // Re-checked after the delay so two surfaces racing on the same launch cannot both prompt.
        if (prefs.getString(KEY_LAST_REQUEST_VERSION, null) == currentVersion) return
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return

        prefs.edit().putString(KEY_LAST_REQUEST_VERSION, currentVersion).apply()
        Firebase.analytics.logEvent(
            EVENT_REVIEW_PROMPT_REQUESTED,
            bundleOf(
                "surface" to surface,
                "app_version" to currentVersion,
                "delay_seconds" to delayMillis / 1000.0
            )
        )
        InAppReviewHelper.launchIfAvailable(activity)
    }

    /** Counts a visit to Home (iOS `journeyHomeViewCount`) and returns the new total. */
    fun recordHomeVisit(context: Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_HOME_VISIT_COUNT, 0) + 1
        prefs.edit().putInt(KEY_HOME_VISIT_COUNT, count).apply()
        return count
    }

    private val currentVersion: String get() = BuildConfig.VERSION_NAME

    private const val PREFS_NAME = "taichi_review_prompt"
    private const val KEY_LAST_REQUEST_VERSION = "lastReviewRequestVersion"
    private const val KEY_HOME_VISIT_COUNT = "homeVisitCount"
    private const val EVENT_REVIEW_PROMPT_REQUESTED = "review_prompt_requested"
}
