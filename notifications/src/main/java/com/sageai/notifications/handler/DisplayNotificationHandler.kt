package com.sageai.notifications.handler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.sageai.notifications.buildDefaultNotification
import com.sageai.notifications.createNotificationChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DisplayNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationHandler {

    init {
        createNotificationChannel(context)
    }

    override fun canHandleNotification(message: RemoteMessage): Boolean {
        return !message.notification?.title.isNullOrEmpty() &&
                !message.notification?.body.isNullOrEmpty() &&
                message.data.isEmpty()
    }

    override fun handleNotification(message: RemoteMessage) {
        val title = requireNotNull(message.notification?.title)
        val subtitle = requireNotNull(message.notification?.body)

        val intent = Intent(ACTION_OPEN_MAIN_ACTIVITY)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_DEFAULT_NOTIFICATION,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = buildDefaultNotification(
            title,
            subtitle,
            pendingIntent,
            context
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        notificationManager.notify(NOTIFICATION_ID_DEFAULT, notification)
    }

    private companion object {
        private const val NOTIFICATION_ID_DEFAULT = 2

        private const val REQUEST_CODE_DEFAULT_NOTIFICATION = 124

        private const val ACTION_OPEN_MAIN_ACTIVITY = "taichi.walking.seniors.beginners.OPEN_MAIN_ACTIVITY"
    }
}