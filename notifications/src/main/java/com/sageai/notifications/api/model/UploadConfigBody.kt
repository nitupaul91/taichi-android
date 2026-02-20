package com.sageai.notifications.api.model

import com.google.gson.annotations.SerializedName

data class UploadConfigBody(
    @SerializedName("userId") val userId: String,
    @SerializedName("region") val region: String,
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("deviceLanguage") val deviceLanguage: String,
    @SerializedName("appVersion") val appVersion: Int,
)
