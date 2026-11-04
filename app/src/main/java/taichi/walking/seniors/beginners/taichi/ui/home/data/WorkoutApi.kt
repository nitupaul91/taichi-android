package taichi.walking.seniors.beginners.taichi.ui.home.data

import taichi.walking.seniors.beginners.taichi.ui.home.model.GenerateWorkoutRequest
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * The same v2 programs iOS plays (new videos, chair program, exercise speech in the user's
 * language, resolved from `Accept-Language`).
 *
 * The v2 media is Apple HEVC-with-alpha, which Android cannot decode (the instructor would sit on
 * the flat blue matte). `platform=android` makes the backend return its Android rendition instead:
 * the instructor already laid over the workout backdrop as plain H.264 (videos/hls-android/, see
 * the backend's ExerciseVideoRenditionResolver). [WorkoutBackdrop] draws the rest of that backdrop
 * around the video so the two meet seamlessly.
 */
interface WorkoutApi {
    @POST("workouts/v2/generate")
    suspend fun generateWorkout(
        @Body request: GenerateWorkoutRequest,
        @Header("Accept-Language") acceptLanguage: String,
        @Query("platform") platform: String = PLATFORM
    ): WorkoutPlanDto

    @POST("workouts/walking/v2/generate")
    suspend fun generateWalkingWorkout(
        @Body request: GenerateWorkoutRequest,
        @Header("Accept-Language") acceptLanguage: String,
        @Query("platform") platform: String = PLATFORM
    ): WorkoutPlanDto

    @POST("workouts/chair/generate")
    suspend fun generateChairWorkout(
        @Body request: GenerateWorkoutRequest,
        @Header("Accept-Language") acceptLanguage: String,
        @Query("platform") platform: String = PLATFORM
    ): WorkoutPlanDto

    companion object {
        const val PLATFORM = "android"
    }
}
