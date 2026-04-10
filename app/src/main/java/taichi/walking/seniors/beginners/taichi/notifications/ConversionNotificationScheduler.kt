package taichi.walking.seniors.beginners.taichi.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mobteq.billing.datastore.DataStorePrefs
import com.sageai.notifications.NotificationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.notifications.ConversionNotificationWorker.Companion.KEY_NOTIFICATION_SUBTITLE
import taichi.walking.seniors.beginners.taichi.notifications.ConversionNotificationWorker.Companion.KEY_NOTIFICATION_TITLE
import taichi.walking.seniors.beginners.taichi.notifications.ConversionNotificationWorker.Companion.KEY_PAYWALL_SOURCE
import taichi.walking.seniors.beginners.taichi.notifications.ConversionNotificationWorker.Companion.KEY_PAYWALL_VARIANT
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepository
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversionNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStorePrefs: DataStorePrefs,
    private val onboardingRepository: OnboardingRepository,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val notificationUtil: NotificationUtil
) {

    suspend fun scheduleIfNeeded() {
        if (!onboardingRepository.observeCompletion().first()) return
        if (dataStorePrefs.isSubscribed().first()) return
        if (dataStorePrefs.hasScheduledConversionNotification().first()) return
        if (!notificationUtil.areNotificationsEnabled()) return

        val campaign = remoteConfig.localNotificationCampaign()
        val paywallVariant = campaign.paywallVariant ?: return

        val work = OneTimeWorkRequestBuilder<ConversionNotificationWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(KEY_NOTIFICATION_TITLE, context.getString(R.string.notification_conversion_title))
                    .putString(KEY_NOTIFICATION_SUBTITLE, context.getString(R.string.notification_conversion_body))
                    .putString(KEY_PAYWALL_SOURCE, campaign.paywallSource)
                    .putString(KEY_PAYWALL_VARIANT, paywallVariant.name)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            work
        )

        dataStorePrefs.setHasScheduledConversionNotification(true)
    }

    fun cancelPending() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "conversion_notification_work"
    }
}
