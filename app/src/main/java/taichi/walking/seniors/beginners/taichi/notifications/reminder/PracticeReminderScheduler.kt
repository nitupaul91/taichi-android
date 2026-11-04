package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.await
import com.sageai.notifications.NotificationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The opt-in "same time tomorrow?" practice reminder offered after a completed session (iOS
 * `PracticeReminder`).
 *
 * Two reminders are scheduled at once: tomorrow, and a single second chance the day after. Both are
 * cancelled as soon as the user completes another session, so a user who returns never sees the
 * second chance, and a user who doesn't gets exactly one nudge more and then nothing. Scheduling
 * both up front matters: the second chance has to survive the user never opening the app again,
 * which is precisely the case it exists for.
 *
 * Delivery is a WorkManager job per reminder, like the conversion notification. It survives reboots
 * but is not exact: Doze can hold it back a little while the phone is idle.
 */
@Singleton
class PracticeReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: ReminderPreferences,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val notificationUtil: NotificationUtil,
    private val practiceRepository: PracticeRepository,
    private val analytics: ReminderAnalyticsTracker
) {

    enum class ScheduleResult { SCHEDULED, PERMISSION_DENIED, FAILED }

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    // region Suggested time

    suspend fun suggestedTime(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime =
        PracticeReminderTimes.suggestedTime(now, preferences.preferredTime())

    // endregion

    // region Prompt throttling

    /** Flag `show_post_workout_reminder_prompt`, offered once per day: two sessions in one day shouldn't ask twice. */
    suspend fun shouldOfferPrompt(now: ZonedDateTime = ZonedDateTime.now()): Boolean =
        remoteConfig.showPostWorkoutReminderPrompt() && !hasAnsweredPromptToday(now)

    suspend fun hasAnsweredPromptToday(now: ZonedDateTime = ZonedDateTime.now()): Boolean =
        preferences.lastPromptDay() == now.toLocalDate()

    suspend fun markPromptAnswered(now: ZonedDateTime = ZonedDateTime.now()) {
        preferences.setLastPromptDay(now.toLocalDate())
    }

    // endregion

    // region Scheduling

    /**
     * Schedules tomorrow's reminder and its second chance. The caller asks for the notification
     * permission first: on Android that needs an Activity, so it lives in the prompt screen.
     */
    suspend fun schedule(reminderTime: ZonedDateTime): ScheduleResult {
        if (!notificationUtil.areNotificationsEnabled()) return ScheduleResult.PERMISSION_DENIED

        preferences.setPreferredTime(reminderTime.hour, reminderTime.minute)
        cancelPending()

        if (!reminderTime.isAfter(ZonedDateTime.now())) return ScheduleResult.FAILED

        return enqueueReminders(firstFireAt = reminderTime, isFirstSession = false, trigger = TRIGGER_PROMPT)
    }

    /**
     * A/B test `trial_practice_reminder_enabled`: the same two reminders for a new trial user who
     * has never practised, without asking. It relies on the permission from onboarding and never
     * prompts.
     */
    suspend fun scheduleAfterTrialStart(now: ZonedDateTime = ZonedDateTime.now()) {
        if (!remoteConfig.trialPracticeReminderEnabled()) return
        if (practiceRepository.currentSnapshot().summaries.isNotEmpty()) return
        if (!notificationUtil.areNotificationsEnabled()) return

        enqueueReminders(
            firstFireAt = PracticeReminderTimes.timeAfterTrialStart(now, preferences.preferredTime()),
            isFirstSession = true,
            trigger = TRIGGER_TRIAL_START
        )
    }

    /** Called whenever the user completes a session, so a returning user never receives the second chance. */
    fun cancelPending() {
        workManager.cancelUniqueWork(ReminderNotification.PRACTICE_NEXT_DAY.identifier)
        workManager.cancelUniqueWork(ReminderNotification.PRACTICE_SECOND_CHANCE.identifier)
    }

    private suspend fun enqueueReminders(
        firstFireAt: ZonedDateTime,
        isFirstSession: Boolean,
        trigger: String
    ): ScheduleResult {
        val secondChanceAt = firstFireAt.plusDays(1)
        val extras = ReminderNotificationWorker.practiceExtras(isFirstSession)

        return try {
            enqueue(ReminderNotification.PRACTICE_NEXT_DAY, firstFireAt, extras)
            enqueue(ReminderNotification.PRACTICE_SECOND_CHANCE, secondChanceAt, extras)

            analytics.trackPracticeReminderScheduled(
                firstFireAt = firstFireAt,
                scheduledCount = SCHEDULED_COUNT,
                trigger = trigger
            )
            ScheduleResult.SCHEDULED
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Practice reminder scheduling failed")
            analytics.trackPracticeReminderScheduleFailed(throwable)
            ScheduleResult.FAILED
        }
    }

    private suspend fun enqueue(reminder: ReminderNotification, fireAt: ZonedDateTime, extras: Data) {
        workManager.enqueueUniqueWork(
            reminder.identifier,
            ExistingWorkPolicy.REPLACE,
            ReminderNotificationWorker.request(reminder, fireAt, extras = extras)
        ).await()
    }

    // endregion

    private companion object {
        const val TRIGGER_PROMPT = "prompt"
        const val TRIGGER_TRIAL_START = "trial_start"
        const val SCHEDULED_COUNT = 2
    }
}
