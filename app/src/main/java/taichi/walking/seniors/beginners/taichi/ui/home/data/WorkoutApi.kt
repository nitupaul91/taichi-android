package taichi.walking.seniors.beginners.taichi.ui.home.data

import taichi.walking.seniors.beginners.taichi.ui.home.model.GenerateWorkoutRequest
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Android is pinned to the **v1** media set.
 *
 * v2 serves Apple HEVC-with-alpha (an `almo`-signalled alpha layer). Android's MediaCodec cannot
 * decode that layer, so it renders the base layer and the instructor appears on the flat blue matte
 * instead of transparent. Moving back to v2 requires an Android-decodable rendition
 * (VP9-with-alpha in WebM) — see tasks/ios-parity.md.
 *
 * Both v1 endpoints hardcode `language = "en"` server-side and read no `Accept-Language`, so the
 * header is deliberately absent here; localized workout content only returns with v2.
 */
interface WorkoutApi {
    @POST("workouts/generate")
    suspend fun generateWorkout(@Body request: GenerateWorkoutRequest): WorkoutPlanDto

    @POST("workouts/walking/generate")
    suspend fun generateWalkingWorkout(@Body request: GenerateWorkoutRequest): WorkoutPlanDto

    /**
     * Seated program. v2-only, so it is currently unreachable — the Chair tab is hidden until an
     * Android-decodable rendition exists. Kept wired so re-enabling is a one-line UI change.
     */
    @POST("workouts/chair/generate")
    suspend fun generateChairWorkout(
        @Body request: GenerateWorkoutRequest,
        @Header("Accept-Language") acceptLanguage: String
    ): WorkoutPlanDto
}
