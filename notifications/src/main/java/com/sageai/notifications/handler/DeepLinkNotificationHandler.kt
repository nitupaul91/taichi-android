package com.sageai.notifications.handler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.messaging.RemoteMessage
import com.sageai.notifications.buildDefaultNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeepLinkNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationHandler {

    override fun canHandleNotification(message: RemoteMessage): Boolean {
        val payload = message.data

        return !payload[KEY_REDIRECT_URL].isNullOrEmpty() &&
                !payload[KEY_NOTIFICATION_TITLE].isNullOrEmpty() &&
                !payload[KEY_NOTIFICATION_SUBTITLE].isNullOrEmpty()
    }

    override fun handleNotification(message: RemoteMessage) {
        val payload = message.data

        val url = requireNotNull(payload[KEY_REDIRECT_URL])
        val title = requireNotNull(payload[KEY_NOTIFICATION_TITLE])
        val subtitle = requireNotNull(payload[KEY_NOTIFICATION_SUBTITLE])

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_REDIRECT_NOTIFICATION,
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

        notificationManager.notify(NOTIFICATION_ID_REDIRECT, notification)
    }

    companion object {
        private const val REQUEST_CODE_REDIRECT_NOTIFICATION = 123

        private const val NOTIFICATION_ID_REDIRECT = 1

        const val KEY_NOTIFICATION_TITLE = "notification_title"
        const val KEY_NOTIFICATION_SUBTITLE = "notification_subtitle"
        const val KEY_REDIRECT_URL = "url_redirect"
    }
}