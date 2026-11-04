package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.await
import com.sageai.notifications.NotificationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The "your trial ends in 2 days" reminder (iOS `TrialReminder`), flag `show_trial_ending_reminder`.
 *
 * It fires two days before the trial ends at the hour the user usually practices, so it is re-armed
 * after every completed session ([rescheduleIfActive]) as that hour settles. The copy, including
 * this week's session count, is built when the reminder fires.
 */
@Singleton
class TrialReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: ReminderPreferences,
    private val notificationUtil: NotificationUtil,
    private val practiceRepository: PracticeRepository,
    private val analytics: ReminderAnalyticsTracker
) {

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    /**
     * Called once when a free trial starts. Unlike iOS it never asks for the notification
     * permission: that needs an Activity, and onboarding asks just before the paywall. The end date
     * is kept, so a session after notifications are turned on still arms the reminder.
     */
    suspend fun schedule(trialEnd: ZonedDateTime) {
        preferences.setTrialEnd(trialEnd.toInstant())
        submit(trialEnd, isReschedule = false, practicedToday = false)
    }

    /** Records the session and re-arms the reminder with the user's latest practice hour. Never prompts. */
    suspend fun onPracticeCompleted(now: ZonedDateTime = ZonedDateTime.now()) {
        preferences.recordPracticeHour(now.hour)
        rescheduleIfActive(now)
    }

    private suspend fun rescheduleIfActive(now: ZonedDateTime) {
        val trialEnd = preferences.trialEnd()?.atZone(ZoneId.systemDefault()) ?: return

        if (!trialEnd.isAfter(now)) {
            cancel()
            return
        }

        submit(trialEnd, isReschedule = true, practicedToday = true, now = now)
    }

    suspend fun cancel() {
        preferences.clearTrialEnd()
        workManager.cancelUniqueWork(ReminderNotification.TRIAL_ENDING.identifier)
    }

    /** `isReschedule` is analytics only: it tells a first scheduling from a post-session re-arm apart. */
    private suspend fun submit(
        trialEnd: ZonedDateTime,
        isReschedule: Boolean,
        practicedToday: Boolean,
        now: ZonedDateTime = ZonedDateTime.now()
    ) {
        val fireAt = TrialReminderTimes.reminderDate(
            trialEnd = trialEnd,
            after = now,
            typicalPracticeHour = TrialReminderTimes.typicalPracticeHour(preferences.recentPracticeHours())
        )

        if (!fireAt.isAfter(now)) {
            analytics.trackTrialReminderNotScheduled(REASON_NOT_IN_FUTURE)
            return
        }

        if (!notificationUtil.areNotificationsEnabled()) {
            analytics.trackTrialReminderNotScheduled(REASON_PERMISSION_UNAVAILABLE)
            return
        }

        val completedSessions = completedSessionsThisWeek(today = now.toLocalDate(), practicedToday = practicedToday)

        try {
            workManager.enqueueUniqueWork(
                ReminderNotification.TRIAL_ENDING.identifier,
                ExistingWorkPolicy.REPLACE,
                ReminderNotificationWorker.request(
                    reminder = ReminderNotification.TRIAL_ENDING,
                    fireAt = fireAt,
                    now = now,
                    extras = ReminderNotificationWorker.trialExtras(trialEnd)
                )
            ).await()

            analytics.trackTrialReminderScheduled(
                trialEnd = trialEnd,
                completedSessions = completedSessions,
                isReschedule = isReschedule
            )
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Trial reminder scheduling failed")
            analytics.trackTrialReminderNotScheduled(REASON_REQUEST_FAILED, throwable)
        }
    }

    /**
     * The completion screen re-arms this while the session is still being saved, so today is
     * counted from the session itself rather than from the store.
     */
    private suspend fun completedSessionsThisWeek(today: LocalDate, practicedToday: Boolean): Int {
        val practiceDays = practiceRepository.currentSnapshot().summaries
            .filter { it.hasPractice() }
            .map { it.date }
        return TrialReminderTimes.practiceDayCount(
            practiceDays = if (practicedToday) practiceDays + today else practiceDays,
            today = today
        )
    }

    private companion object {
        const val REASON_NOT_IN_FUTURE = "reminder_date_not_in_future"
        const val REASON_PERMISSION_UNAVAILABLE = "notification_permission_unavailable"
        const val REASON_REQUEST_FAILED = "request_failed"
    }
}
