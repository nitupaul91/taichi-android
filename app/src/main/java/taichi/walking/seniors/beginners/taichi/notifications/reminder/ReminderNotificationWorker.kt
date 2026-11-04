package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.sageai.notifications.NotificationUtil
import com.sageai.notifications.buildDefaultNotification
import com.sageai.notifications.createNotificationChannel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.activity.TaiChiHomeActivity
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * The local reminders. [identifier] matches iOS's notification request identifiers and is both the
 * unique WorkManager work name and the `notification_id` reported when the notification is tapped.
 */
enum class ReminderNotification(
    val identifier: String,
    val notificationId: Int,
    val requestCode: Int
) {
    PRACTICE_NEXT_DAY("practice_reminder_next_day", notificationId = 521, requestCode = 511),
    PRACTICE_SECOND_CHANCE("practice_reminder_second_chance", notificationId = 522, requestCode = 512),
    TRIAL_ENDING("trial-ending-reminder", notificationId = 523, requestCode = 513);

    companion object {
        fun fromIdentifier(identifier: String?): ReminderNotification? =
            entries.firstOrNull { it.identifier == identifier }
    }
}

/**
 * Posts one reminder at its scheduled time. The copy is resolved here rather than when scheduling,
 * so it follows the language the phone is in when the reminder fires.
 */
@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationUtil: NotificationUtil,
    private val practiceRepository: PracticeRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val reminder = ReminderNotification.fromIdentifier(inputData.getString(KEY_REMINDER))
            ?: return Result.success()
        // Permission can be revoked after scheduling; posting would silently fail anyway.
        if (!notificationUtil.areNotificationsEnabled()) return Result.success()

        val (title, body) = content(reminder) ?: return Result.success()

        createNotificationChannel(applicationContext)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            reminder.requestCode,
            tapIntent(applicationContext, reminder),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = buildDefaultNotification(
            title = title,
            subtitle = body,
            pendingIntent = pendingIntent,
            context = applicationContext,
            expandable = true
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminder.notificationId, notification)

        return Result.success()
    }

    private suspend fun content(reminder: ReminderNotification): Pair<String, String>? = when (reminder) {
        ReminderNotification.PRACTICE_NEXT_DAY -> Pair(
            applicationContext.getString(R.string.notification_practice_reminder_title),
            applicationContext.getString(
                if (inputData.getBoolean(KEY_IS_FIRST_SESSION, false)) {
                    R.string.notification_practice_reminder_first_session_body
                } else {
                    R.string.notification_practice_reminder_body
                }
            )
        )
        ReminderNotification.PRACTICE_SECOND_CHANCE -> Pair(
            applicationContext.getString(R.string.notification_practice_reminder_second_chance_title),
            applicationContext.getString(R.string.notification_practice_reminder_second_chance_body)
        )
        ReminderNotification.TRIAL_ENDING -> trialEndingContent()
    }

    /** Built now rather than when scheduled, so the session count is this week's, not a stale one. */
    private suspend fun trialEndingContent(): Pair<String, String>? {
        if (!inputData.hasKeyWithValueOfType(KEY_TRIAL_END_EPOCH_MILLIS, Long::class.javaObjectType)) return null
        val trialEnd = Instant.ofEpochMilli(inputData.getLong(KEY_TRIAL_END_EPOCH_MILLIS, 0L))
            .atZone(ZoneId.systemDefault())

        val locale = Locale.getDefault()
        val dateText = DateTimeFormatter
            .ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMd"), locale)
            .format(trialEnd)

        val practiceDays = practiceRepository.currentSnapshot().summaries
            .filter { it.hasPractice() }
            .map { it.date }
        val completedSessions = TrialReminderTimes.practiceDayCount(practiceDays, LocalDate.now())

        val body = if (completedSessions > 0) {
            applicationContext.resources.getQuantityString(
                R.plurals.notification_trial_reminder_body_practiced,
                completedSessions,
                completedSessions,
                dateText
            )
        } else {
            applicationContext.getString(R.string.notification_trial_reminder_body_not_practiced, dateText)
        }

        return Pair(applicationContext.getString(R.string.notification_trial_reminder_title), body)
    }

    companion object {
        /** Intent extra carrying [ReminderNotification.identifier]; read by [TaiChiHomeActivity] for tap analytics. */
        const val EXTRA_NOTIFICATION_ID = "extra_reminder_notification_id"

        private const val KEY_REMINDER = "reminder"
        private const val KEY_IS_FIRST_SESSION = "is_first_session"
        private const val KEY_TRIAL_END_EPOCH_MILLIS = "trial_end_epoch_millis"

        /** A trial user who has not practised yet is invited to get started rather than to keep going. */
        fun practiceExtras(isFirstSession: Boolean): Data =
            Data.Builder().putBoolean(KEY_IS_FIRST_SESSION, isFirstSession).build()

        fun trialExtras(trialEnd: ZonedDateTime): Data =
            Data.Builder().putLong(KEY_TRIAL_END_EPOCH_MILLIS, trialEnd.toInstant().toEpochMilli()).build()

        fun request(
            reminder: ReminderNotification,
            fireAt: ZonedDateTime,
            now: ZonedDateTime = ZonedDateTime.now(),
            extras: Data = Data.EMPTY
        ): OneTimeWorkRequest {
            val delayMillis = Duration.between(now, fireAt).toMillis().coerceAtLeast(0L)
            val data = Data.Builder()
                .putAll(extras)
                .putString(KEY_REMINDER, reminder.identifier)
                .build()

            return OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(reminder.identifier)
                .build()
        }

        private fun tapIntent(context: Context, reminder: ReminderNotification): Intent =
            Intent(context, TaiChiHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_NOTIFICATION_ID, reminder.identifier)
            }
    }
}
