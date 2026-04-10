package taichi.walking.seniors.beginners.taichi.ui.home.data

import taichi.walking.seniors.beginners.taichi.ui.home.model.GenerateWorkoutRequest
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import retrofit2.http.Body
import retrofit2.http.POST

interface WorkoutApi {
    @POST("workouts/generate")
    suspend fun generateWorkout(@Body request: GenerateWorkoutRequest): WorkoutPlanDto

    @POST("workouts/walking/generate")
    suspend fun generateWalkingWorkout(@Body request: GenerateWorkoutRequest): WorkoutPlanDto
}
