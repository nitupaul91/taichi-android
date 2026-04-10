package com.sageai.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "taichi.walking.seniors.beginners.notifications"
private const val NOTIFICATION_CHANNEL_NAME = "App updates"
private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Important app updates."

class NotificationUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun buildDefaultNotification(
    title: String,
    subtitle: String,
    pendingIntent: PendingIntent,
    context: Context
): Notification {
    return NotificationCompat.Builder(
        context,
        NOTIFICATION_CHANNEL_ID
    )
        .setContentTitle(title)
        .setContentText(subtitle)
        .setSmallIcon(R.drawable.ic_notification_small)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
}
