package com.sageai.notifications.handler

import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class UpdateLogLevelNotificationHandler @Inject constructor(
    @Named("log_level_shared_flow") private val logLevelFlow: MutableSharedFlow<String>
) : NotificationHandler {

    override fun canHandleNotification(message: RemoteMessage): Boolean {
        val payload = message.data

        return !payload[KEY_MIN_LOG_LEVEL].isNullOrEmpty()
    }

    override fun handleNotification(message: RemoteMessage) {
        val logLevel = requireNotNull(message.data[KEY_MIN_LOG_LEVEL])

        GlobalScope.launch {
            logLevelFlow.emit(logLevel)
        }
    }

    private companion object {
        private const val KEY_MIN_LOG_LEVEL = "min_log_level"
    }
}