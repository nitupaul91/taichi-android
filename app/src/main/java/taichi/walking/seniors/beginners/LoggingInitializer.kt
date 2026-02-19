package taichi.walking.seniors.beginners

import com.sageai.id.IDService
import com.sageai.util.RemoteLogging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoggingInitializer @Inject constructor(
    private val idService: IDService,
    private val remoteLogging: RemoteLogging,
) {

    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            remoteLogging.init()

            GlobalScope.launch {
                idService.getUserIdFlow().collect {
                    remoteLogging.updateLoggingUserId(it)
                }
            }
        }
    }
}