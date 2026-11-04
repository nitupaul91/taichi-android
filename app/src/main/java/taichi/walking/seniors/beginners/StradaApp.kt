package taichi.walking.seniors.beginners

import taichi.walking.seniors.beginners.util.FirebaseAppCheckUtil
import taichi.walking.seniors.beginners.util.ThemeHelper
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.mobteq.analytics.AnalyticsMirror
import taichi.walking.seniors.beginners.taichi.analytics.AppEventUploader
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mobteq.analytics.FacebookAnalyticsTracker
import com.mobteq.analytics.TikTokAnalyticsTracker
import com.mobteq.billing.datastore.DataStorePrefs
import com.mobteq.billing.domain.repository.PurchasesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.notifications.AppLifecycleNotificationObserver
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallCache
import javax.inject.Inject


@HiltAndroidApp
class StradaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sharePrefs: DataStorePrefs

    @Inject
    lateinit var firebaseAppCheck: FirebaseAppCheckUtil

    @Inject
    lateinit var loggingInitializer: LoggingInitializer

    @Inject
    lateinit var themeHelper: ThemeHelper

    @Inject
    lateinit var purchasesRepository: PurchasesRepository

    @Inject
    lateinit var remoteConfig: AiAssistantRemoteConfig

    @Inject
    lateinit var remotePaywallCache: RemotePaywallCache

    @Inject
    lateinit var appLifecycleNotificationObserver: AppLifecycleNotificationObserver

    @Inject
    lateinit var tikTokAnalyticsTracker: TikTokAnalyticsTracker

    @Inject
    lateinit var facebookAnalyticsTracker: FacebookAnalyticsTracker

    @Inject
    lateinit var appEventUploader: AppEventUploader

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        themeHelper.applySavedTheme()

        firebaseAppCheck.initFirebaseAppCheck(this)

        loggingInitializer.init()

        // Our own copy of every analytics event, for the backend user-journey dashboard (iOS
        // AppEventUploader). Registered before anything logs, so no early event is missed.
        AnalyticsMirror.sink = appEventUploader::enqueue
        appEventUploader.startSession()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = appEventUploader.resumeSession()
            override fun onStop(owner: LifecycleOwner) = appEventUploader.enterBackground()
        })

        // Warm up BillingClient and product cache as early as possible.
        CoroutineScope(Dispatchers.IO).launch {
            purchasesRepository.queryPurchases()
            remotePaywallCache.prefetchAll()
        }

        remoteConfig.fetchAndActivate {
            appEventUploader.recordRemoteConfigSnapshotIfNeeded(remoteConfig.snapshotValues())
            CoroutineScope(Dispatchers.IO).launch {
                remotePaywallCache.prefetchAll()
            }
        }

        tikTokAnalyticsTracker.initialize(
            application = this,
            sdkAppId = BuildConfig.APPLICATION_ID,
            tikTokAppId = BuildConfig.TIKTOK_APP_ID
        )

        facebookAnalyticsTracker.initialize(application = this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleNotificationObserver)
    }
}
