package com.sageai.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sageai.notifications.handler.DeepLinkNotificationHandler
import com.sageai.notifications.handler.DisplayNotificationHandler
import com.sageai.notifications.handler.UpdateLogLevelNotificationHandler
import com.sageai.notifications.worker.ConfigUploadWorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseNotificationsService : FirebaseMessagingService() {

    @Inject
    lateinit var configUploadWorkScheduler: ConfigUploadWorkScheduler

    @Inject
    lateinit var displayNotificationHandler: DisplayNotificationHandler

    @Inject
    lateinit var deepLinkNotificationHandler: DeepLinkNotificationHandler

    @Inject
    lateinit var updateLogLevelNotificationHandler: UpdateLogLevelNotificationHandler

    private val notificationHandlers by lazy {
        listOf(
            displayNotificationHandler,
            deepLinkNotificationHandler,
            updateLogLevelNotificationHandler
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val handler = notificationHandlers.firstOrNull { it.canHandleNotification(message) }

        if (handler != null) {
            handler.handleNotification(message)
        } else {
            Timber.e("Notification handler not found for notification ${message.data}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token updated $token")

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_DATA_MESSAGES)

        configUploadWorkScheduler.scheduleOnce(token)
    }

    private companion object {
        private const val TOPIC_DATA_MESSAGES = "dataMessages"
    }
}