package taichi.walking.seniors.beginners.taichi.analytics

import android.content.Context
import android.os.Bundle
import com.mobteq.analytics.logAppEvent
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutSessionAnalytics

/**
 * iOS AppOpenAnalyticsTracker: app_open on the first foreground of the process
 * ("initial_launch") and on every return from the background ("foreground"), app_background when
 * the app leaves, and the report of a workout the system killed in the background.
 */
class AppOpenAnalytics(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var hasTrackedInitialOpen = false

    fun onForeground() {
        val entryPoint = if (hasTrackedInitialOpen) ENTRY_FOREGROUND else ENTRY_INITIAL_LAUNCH
        val count = prefs.getLong(KEY_OPEN_COUNT, 0L) + 1
        prefs.edit().putLong(KEY_OPEN_COUNT, count).apply()
        logAppEvent("app_open", Bundle().apply {
            putLong("app_open_count", count)
            putString("entry_point", entryPoint)
            putBoolean("is_first_open", count == 1L)
        })
        if (!hasTrackedInitialOpen) {
            hasTrackedInitialOpen = true
            WorkoutSessionAnalytics.reportKilledSessionIfNeeded(context)
        }
    }

    fun onBackground() {
        if (!hasTrackedInitialOpen) return
        logAppEvent("app_background", Bundle().apply {
            putLong("app_open_count", prefs.getLong(KEY_OPEN_COUNT, 0L))
        })
    }

    private companion object {
        const val PREFS_NAME = "app_open_analytics"
        const val KEY_OPEN_COUNT = "analytics.app_open_count"
        const val ENTRY_INITIAL_LAUNCH = "initial_launch"
        const val ENTRY_FOREGROUND = "foreground"
    }
}
