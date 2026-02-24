package taichi.walking.seniors.beginners

import taichi.walking.seniors.beginners.util.FirebaseAppCheckUtil
import taichi.walking.seniors.beginners.util.ThemeHelper
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mobteq.billing.datastore.DataStorePrefs
import com.mobteq.billing.domain.repository.PurchasesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        themeHelper.applySavedTheme()

        firebaseAppCheck.initFirebaseAppCheck(this)

        loggingInitializer.init()

        // Warm up BillingClient and product cache as early as possible.
        CoroutineScope(Dispatchers.IO).launch {
            purchasesRepository.queryPurchases()
        }
    }
}
