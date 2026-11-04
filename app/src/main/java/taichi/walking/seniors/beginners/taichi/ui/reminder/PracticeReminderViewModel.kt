package taichi.walking.seniors.beginners.taichi.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.notifications.reminder.PracticeReminderScheduler
import taichi.walking.seniors.beginners.taichi.notifications.reminder.PracticeReminderScheduler.ScheduleResult
import taichi.walking.seniors.beginners.taichi.notifications.reminder.ReminderAnalyticsTracker
import taichi.walking.seniors.beginners.taichi.notifications.reminder.TrialReminderScheduler
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Reminder side of the workout completion screen and the reminder prompt. Holds no per-prompt
 * state: it is shared by every completion shown in the home screen, and the prompt's own state
 * lives in the composable.
 */
@HiltViewModel
class PracticeReminderViewModel @Inject constructor(
    private val scheduler: PracticeReminderScheduler,
    private val trialReminderScheduler: TrialReminderScheduler,
    private val analytics: ReminderAnalyticsTracker
) : ViewModel() {

    /**
     * The user just practiced, so any pending nudge (and its second chance) is obsolete. Cleared
     * when the completion screen appears, before the prompt, so it can never cancel the reminder
     * the user is about to accept.
     *
     * The session also re-arms the trial-ending reminder at the user's usual practice hour.
     */
    fun onWorkoutCompletionShown() {
        scheduler.cancelPending()
        viewModelScope.launch { trialReminderScheduler.onPracticeCompleted() }
    }

    /** The time to suggest, or null when the prompt should not be offered after this session. */
    suspend fun promptTimeIfOffered(): ZonedDateTime? =
        if (scheduler.shouldOfferPrompt()) scheduler.suggestedTime() else null

    fun trackPromptShown() = analytics.trackPromptShown()

    /** Result of the system permission dialog launched from the prompt. */
    fun trackSystemPermissionResult(granted: Boolean) = analytics.trackNotificationPermissionResult(
        context = ReminderAnalyticsTracker.CONTEXT_PRACTICE_REMINDER,
        granted = granted,
        wasPrompt = true
    )

    fun schedule(
        reminderTime: ZonedDateTime,
        didChangeTime: Boolean,
        onResult: (ScheduleResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = scheduler.schedule(reminderTime)
            if (result == ScheduleResult.SCHEDULED) {
                analytics.trackPromptAccepted(didChangeTime = didChangeTime, reminderHour = reminderTime.hour)
            }
            onResult(result)
        }
    }

    fun onPermissionDenied(wasPrompt: Boolean) {
        analytics.trackPermissionDenied()
        analytics.trackNotificationPermissionResult(
            context = ReminderAnalyticsTracker.CONTEXT_PRACTICE_REMINDER_PROMPT,
            granted = false,
            wasPrompt = wasPrompt
        )
    }

    fun decline(reminderHour: Int) = analytics.trackPromptDeclined(reminderHour)

    fun markPromptAnswered() {
        viewModelScope.launch { scheduler.markPromptAnswered() }
    }
}
