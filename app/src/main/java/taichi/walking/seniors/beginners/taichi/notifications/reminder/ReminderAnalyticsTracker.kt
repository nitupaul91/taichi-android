package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Reminder analytics with iOS's event and parameter names, so one dashboard reads both platforms.
 * Logged through [FirebaseAnalytics], which also mirrors every event to the backend journey upload.
 */
class ReminderAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun trackPromptShown() = log(EVENT_PROMPT_SHOWN)

    fun trackPromptAccepted(didChangeTime: Boolean, reminderHour: Int) = log(
        EVENT_PROMPT_ACCEPTED,
        bundleOf(PARAM_DID_CHANGE_TIME to didChangeTime, PARAM_REMINDER_HOUR to reminderHour.toLong())
    )

    fun trackPromptDeclined(reminderHour: Int) = log(
        EVENT_PROMPT_DECLINED,
        bundleOf(PARAM_REMINDER_HOUR to reminderHour.toLong())
    )

    fun trackPermissionDenied() = log(EVENT_PERMISSION_DENIED)

    fun trackNotificationPermissionResult(context: String, granted: Boolean, wasPrompt: Boolean) = log(
        EVENT_NOTIFICATION_PERMISSION_RESULT,
        bundleOf(
            PARAM_CONTEXT to context,
            PARAM_GRANTED to granted,
            PARAM_WAS_PROMPT to wasPrompt
        )
    )

    fun trackPracticeReminderScheduled(firstFireAt: ZonedDateTime, scheduledCount: Int, trigger: String) = log(
        EVENT_PRACTICE_REMINDER_SCHEDULED,
        bundleOf(
            PARAM_FIRE_AT to firstFireAt.toIsoInstant(),
            PARAM_FIRE_HOUR to firstFireAt.hour.toLong(),
            PARAM_SCHEDULED_COUNT to scheduledCount.toLong(),
            PARAM_TRIGGER to trigger
        )
    )

    fun trackPracticeReminderScheduleFailed(error: Throwable) = log(
        EVENT_PRACTICE_REMINDER_SCHEDULE_FAILED,
        bundleOf(PARAM_ERROR to error.toString())
    )

    fun trackTrialReminderScheduled(trialEnd: ZonedDateTime, completedSessions: Int, isReschedule: Boolean) = log(
        EVENT_TRIAL_REMINDER_SCHEDULED,
        bundleOf(
            PARAM_TRIAL_END to trialEnd.toIsoInstant(),
            PARAM_COMPLETED_SESSIONS to completedSessions.toLong(),
            PARAM_IS_RESCHEDULE to isReschedule
        )
    )

    fun trackTrialReminderNotScheduled(reason: String, error: Throwable? = null) = log(
        EVENT_TRIAL_REMINDER_NOT_SCHEDULED,
        bundleOf(PARAM_REASON to reason).apply {
            if (error != null) putString(PARAM_ERROR, error.toString())
        }
    )

    /** iOS tracks every notification tap; here only the reminder notifications carry an id to report. */
    fun trackNotificationTapped(
        notificationId: String,
        appState: String,
        hasDeeplink: Boolean = false,
        deeplinkSource: String = ""
    ) = log(
        EVENT_NOTIFICATION_TAPPED,
        bundleOf(
            PARAM_NOTIFICATION_ID to notificationId,
            PARAM_HAS_DEEPLINK to hasDeeplink,
            PARAM_DEEPLINK_SOURCE to deeplinkSource,
            PARAM_APP_STATE to appState
        )
    )

    private fun log(name: String, params: Bundle = bundleOf()) {
        firebaseAnalytics.logEvent(name, params)
    }

    /** Same shape as iOS's `ISO8601DateFormatter` default: UTC, whole seconds, e.g. 2026-11-05T08:15:00Z. */
    private fun ZonedDateTime.toIsoInstant(): String =
        DateTimeFormatter.ISO_INSTANT.format(toInstant().truncatedTo(ChronoUnit.SECONDS))

    companion object {
        const val CONTEXT_PRACTICE_REMINDER = "practice_reminder"
        const val CONTEXT_PRACTICE_REMINDER_PROMPT = "practice_reminder_prompt"

        const val APP_STATE_ACTIVE = "active"
        const val APP_STATE_INACTIVE = "inactive"

        private const val EVENT_PROMPT_SHOWN = "practice_reminder_prompt_shown"
        private const val EVENT_PROMPT_ACCEPTED = "practice_reminder_prompt_accepted"
        private const val EVENT_PROMPT_DECLINED = "practice_reminder_prompt_declined"
        private const val EVENT_PERMISSION_DENIED = "practice_reminder_permission_denied"
        private const val EVENT_NOTIFICATION_PERMISSION_RESULT = "notification_permission_result"
        private const val EVENT_PRACTICE_REMINDER_SCHEDULED = "practice_reminder_scheduled"
        private const val EVENT_PRACTICE_REMINDER_SCHEDULE_FAILED = "practice_reminder_schedule_failed"
        private const val EVENT_NOTIFICATION_TAPPED = "notification_tapped"
        private const val EVENT_TRIAL_REMINDER_SCHEDULED = "trial_reminder_scheduled"
        private const val EVENT_TRIAL_REMINDER_NOT_SCHEDULED = "trial_reminder_not_scheduled"

        private const val PARAM_DID_CHANGE_TIME = "did_change_time"
        private const val PARAM_REMINDER_HOUR = "reminder_hour"
        private const val PARAM_CONTEXT = "context"
        private const val PARAM_GRANTED = "granted"
        private const val PARAM_WAS_PROMPT = "was_prompt"
        private const val PARAM_FIRE_AT = "fire_at"
        private const val PARAM_FIRE_HOUR = "fire_hour"
        private const val PARAM_SCHEDULED_COUNT = "scheduled_count"
        private const val PARAM_TRIGGER = "trigger"
        private const val PARAM_ERROR = "error"
        private const val PARAM_NOTIFICATION_ID = "notification_id"
        private const val PARAM_HAS_DEEPLINK = "has_deeplink"
        private const val PARAM_DEEPLINK_SOURCE = "deeplink_source"
        private const val PARAM_APP_STATE = "app_state"
        private const val PARAM_TRIAL_END = "trial_end"
        private const val PARAM_COMPLETED_SESSIONS = "completed_sessions"
        private const val PARAM_IS_RESCHEDULE = "is_reschedule"
        private const val PARAM_REASON = "reason"
    }
}
