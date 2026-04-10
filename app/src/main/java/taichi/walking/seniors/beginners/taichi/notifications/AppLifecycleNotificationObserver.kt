package taichi.walking.seniors.beginners.taichi.notifications

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mobteq.billing.datastore.DataStorePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleNotificationObserver @Inject constructor(
    private val conversionNotificationScheduler: ConversionNotificationScheduler,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val dataStorePrefs: DataStorePrefs
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStart(owner: LifecycleOwner) {
        remoteConfig.fetchAndActivate()
        scope.launch {
            if (dataStorePrefs.isSubscribed().first()) {
                conversionNotificationScheduler.cancelPending()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        scope.launch {
            conversionNotificationScheduler.scheduleIfNeeded()
        }
    }
}
