package taichi.walking.seniors.beginners.taichi.notifications

import android.app.PendingIntent
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobteq.billing.datastore.DataStorePrefs
import com.sageai.notifications.buildDefaultNotification
import com.sageai.notifications.createNotificationChannel
import com.sageai.notifications.NotificationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepository
import taichi.walking.seniors.beginners.taichi.ui.home.activity.PaywallLaunchRequest
import taichi.walking.seniors.beginners.taichi.ui.home.activity.TaiChiHomeActivity
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant

@HiltWorker
class ConversionNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataStorePrefs: DataStorePrefs,
    private val onboardingRepository: OnboardingRepository,
    private val notificationUtil: NotificationUtil
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!onboardingRepository.observeCompletion().first()) return Result.success()
        if (dataStorePrefs.isSubscribed().first()) return Result.success()
        if (!notificationUtil.areNotificationsEnabled()) return Result.success()

        val title = inputData.getString(KEY_NOTIFICATION_TITLE).orEmpty()
        val subtitle = inputData.getString(KEY_NOTIFICATION_SUBTITLE).orEmpty()
        val source = inputData.getString(KEY_PAYWALL_SOURCE)
        val variant = inputData.getString(KEY_PAYWALL_VARIANT)
            ?.let { runCatching { RemotePaywallVariant.valueOf(it) }.getOrNull() }
            ?: RemotePaywallVariant.PRIMARY

        if (title.isBlank() || subtitle.isBlank()) return Result.success()

        createNotificationChannel(applicationContext)

        val intent = TaiChiHomeActivity.createIntent(
            context = applicationContext,
            paywallRequest = PaywallLaunchRequest(
                source = source,
                variant = variant
            )
        )

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE_CONVERSION_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = buildDefaultNotification(
            title = title,
            subtitle = subtitle,
            pendingIntent = pendingIntent,
            context = applicationContext
        )

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID_CONVERSION, notification)

        return Result.success()
    }

    companion object {
        const val KEY_NOTIFICATION_TITLE = "notification_title"
        const val KEY_NOTIFICATION_SUBTITLE = "notification_subtitle"
        const val KEY_PAYWALL_SOURCE = "paywall_source"
        const val KEY_PAYWALL_VARIANT = "paywall_variant"

        private const val REQUEST_CODE_CONVERSION_NOTIFICATION = 501
        private const val NOTIFICATION_ID_CONVERSION = 502
    }
}
