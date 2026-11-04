package com.sageai.notifications.api.model

import com.google.gson.annotations.SerializedName

data class UploadConfigBody(
    @SerializedName("userId") val userId: String,
    @SerializedName("region") val region: String,
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("deviceLanguage") val deviceLanguage: String,
    @SerializedName("appVersion") val appVersion: Int,
    /** BCP-47 tag of the app's language ("de-DE"), as iOS sends it. */
    @SerializedName("preferredLanguage") val preferredLanguage: String?,
    /** Region format ("de_AT"); rarely equals the language. */
    @SerializedName("locale") val locale: String?,
    @SerializedName("deviceModel") val deviceModel: String?,
    @SerializedName("deviceOS") val deviceOS: String?,
)
