package com.sageai.notifications.handler

import com.google.firebase.messaging.RemoteMessage

interface NotificationHandler {

    fun canHandleNotification(message: RemoteMessage): Boolean

    fun handleNotification(message: RemoteMessage)
}