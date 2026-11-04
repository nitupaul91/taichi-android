package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import java.time.Duration
import java.time.ZonedDateTime
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
    PRACTICE_SECOND_CHANCE("practice_reminder_second_chance", notificationId = 522, requestCode = 512);

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
    private val notificationUtil: NotificationUtil
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val reminder = ReminderNotification.fromIdentifier(inputData.getString(KEY_REMINDER))
            ?: return Result.success()
        // Permission can be revoked after scheduling; posting would silently fail anyway.
        if (!notificationUtil.areNotificationsEnabled()) return Result.success()

        val (title, body) = content(reminder)

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

    private fun content(reminder: ReminderNotification): Pair<String, String> = when (reminder) {
        ReminderNotification.PRACTICE_NEXT_DAY -> Pair(
            applicationContext.getString(R.string.notification_practice_reminder_title),
            applicationContext.getString(R.string.notification_practice_reminder_body)
        )
        ReminderNotification.PRACTICE_SECOND_CHANCE -> Pair(
            applicationContext.getString(R.string.notification_practice_reminder_second_chance_title),
            applicationContext.getString(R.string.notification_practice_reminder_second_chance_body)
        )
    }

    companion object {
        /** Intent extra carrying [ReminderNotification.identifier]; read by [TaiChiHomeActivity] for tap analytics. */
        const val EXTRA_NOTIFICATION_ID = "extra_reminder_notification_id"

        private const val KEY_REMINDER = "reminder"

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
