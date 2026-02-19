package taichi.walking.seniors.beginners.data.model.registeruser

import com.google.gson.annotations.SerializedName

data class RegisterUserResponse(
    @SerializedName("carId")  val carId: String
)
