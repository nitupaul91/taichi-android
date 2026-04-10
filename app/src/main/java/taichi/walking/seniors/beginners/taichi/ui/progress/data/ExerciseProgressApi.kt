package taichi.walking.seniors.beginners.taichi.ui.progress.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ExerciseProgressSyncUploadBody(
    val userId: String,
    val version: Int,
    val programVariant: String,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val dailyPractice: List<PracticeSyncDay>
) {
    constructor(userId: String, payload: PracticeProgressSyncPayload) : this(
        userId = userId,
        version = payload.version,
        programVariant = payload.programVariant.storageValue,
        taiChiCurrentDay = payload.taiChiCurrentDay,
        walkingCurrentDay = payload.walkingCurrentDay,
        dailyPractice = payload.dailyPractice
    )
}

interface ExerciseProgressApi {
    @POST("progress/iOS/upload")
    suspend fun uploadProgress(@Body body: ExerciseProgressSyncUploadBody): Response<Unit>
}
