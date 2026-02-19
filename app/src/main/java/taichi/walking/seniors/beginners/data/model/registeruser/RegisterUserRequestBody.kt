package taichi.walking.seniors.beginners.data.model.registeruser

import com.google.gson.annotations.SerializedName

data class RegisterUserRequestBody(
    @SerializedName("userId") val userId: String,
    @SerializedName("fcmToken") val fcmToken: String?,
    @SerializedName("deviceLanguage") val deviceLanguage: String,
    @SerializedName("appVersion") val appVersion: Int,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("color") val color: String,
    @SerializedName("templateId") val templateId: String,
)
