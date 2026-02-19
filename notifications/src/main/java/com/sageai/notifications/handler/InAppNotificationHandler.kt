package com.sageai.notifications.handler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.sageai.notifications.buildDefaultNotification
import com.sageai.notifications.createNotificationChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InAppNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    init {
        createNotificationChannel(context)
    }

    fun sendInAppNotification(title: String, subtitle: String) {
        val intent = Intent(ACTION_OPEN_MAIN_ACTIVITY).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
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