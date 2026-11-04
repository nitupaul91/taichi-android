package taichi.walking.seniors.beginners.taichi.account

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountApi {

    /**
     * Android counterpart of the iOS app's `account/iOS/delete`, with the same contract: idempotent,
     * `deleted = true` on success, 400 for a blank userId.
     */
    @POST("account/android/delete")
    suspend fun deleteAccount(@Body body: DeleteAccountBody): Response<DeleteAccountResponse>
}

// @SerializedName on every field: R8 renames unannotated fields in release builds and Gson would
// then send and read the wrong keys.
data class DeleteAccountBody(
    @SerializedName("userId") val userId: String
)

data class DeleteAccountResponse(
    @SerializedName("deleted") val deleted: Boolean
)
