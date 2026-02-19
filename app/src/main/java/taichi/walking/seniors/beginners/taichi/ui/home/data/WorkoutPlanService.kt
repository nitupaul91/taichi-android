package taichi.walking.seniors.beginners.taichi.ui.home.data

import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseGoal
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseLimitation
import taichi.walking.seniors.beginners.taichi.ui.home.model.GenerateWorkoutRequest
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanService @Inject constructor(
    private val workoutApi: WorkoutApi
) {
    suspend fun fetchWorkoutPlan(): WorkoutPlanDto {
        val language = Locale.getDefault().language.ifBlank { "en" }
        return workoutApi.generateWorkout(
            GenerateWorkoutRequest(
                language = language,
                restrictions = emptyList(),
                goals = emptyList()
            )
        )
    }

    suspend fun generateWorkoutPlan(
        language: String,
        restrictions: Set<ExerciseLimitation>,
        goals: Set<ExerciseGoal>
    ): WorkoutPlanDto {
        return workoutApi.generateWorkout(
            GenerateWorkoutRequest(
                language = language,
                restrictions = restrictions.toList(),
                goals = goals.toList()
            )
        )
    }
}
