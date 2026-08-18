package taichi.walking.seniors.beginners.taichi.ui.progress.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ExerciseProgressSyncUploadBody(
    val userId: String,
    val version: Int,
    val programVariant: String,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val chairCurrentDay: Int,
    val dailyPractice: List<PracticeSyncDay>
) {
    constructor(userId: String, payload: PracticeProgressSyncPayload) : this(
        userId = userId,
        version = payload.version,
        programVariant = payload.programVariant.storageValue,
        taiChiCurrentDay = payload.taiChiCurrentDay,
        walkingCurrentDay = payload.walkingCurrentDay,
        chairCurrentDay = payload.chairCurrentDay,
        dailyPractice = payload.dailyPractice
    )
}

/**
 * Server-side progress. `chair*` is carried even though Android has no seated program yet: the
 * backend max-merges every field on upload, so round-tripping through this client can never lower a
 * value set on iOS.
 */
data class ExerciseProgressRestoreResponse(
    val version: Int = 1,
    val programVariant: String? = null,
    val taiChiCurrentDay: Int = 1,
    val walkingCurrentDay: Int = 1,
    val chairCurrentDay: Int = 1,
    val dailyPractice: List<ExerciseProgressRestoreDay> = emptyList()
)

data class ExerciseProgressRestoreDay(
    val date: String,
    val taiChiMinutes: Int = 0,
    val walkingMinutes: Int = 0,
    val chairMinutes: Int = 0
)

interface ExerciseProgressApi {
    @POST("progress/iOS/upload")
    suspend fun uploadProgress(@Body body: ExerciseProgressSyncUploadBody): Response<Unit>

    /**
     * Shared with iOS — the route is named for the platform that shipped it first, not the caller.
     * Without this, reinstalling the Android app lost every day of practice.
     */
    @GET("progress/iOS/restore")
    suspend fun restoreProgress(@Query("userId") userId: String): Response<ExerciseProgressRestoreResponse>
}
