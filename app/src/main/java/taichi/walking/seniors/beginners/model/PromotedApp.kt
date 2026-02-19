package taichi.walking.seniors.beginners.model

import com.google.gson.annotations.SerializedName

data class PromotedApp(
    @SerializedName("imgUrl") val imgUrl: String,
    @SerializedName("text") val text: String,
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String
)