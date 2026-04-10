package taichi.walking.seniors.beginners.taichi.ui.home.data

import com.sageai.id.IDService
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseGoal
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseLimitation
import taichi.walking.seniors.beginners.taichi.ui.home.model.GenerateWorkoutRequest
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanService @Inject constructor(
    private val workoutApi: WorkoutApi,
    private val idService: IDService
) {
    suspend fun fetchWorkoutPlan(
        kind: PracticeKind,
        intensity: String? = null
    ): WorkoutPlanDto {
        val language = Locale.getDefault().language.ifBlank { "en" }
        val request = GenerateWorkoutRequest(
            userId = currentUserId(),
            intensity = normalizedIntensity(intensity),
            language = language,
            restrictions = emptyList(),
            goals = emptyList()
        )
        return when (kind) {
            PracticeKind.TAI_CHI -> workoutApi.generateWorkout(request)
            PracticeKind.WALKING -> workoutApi.generateWalkingWorkout(request)
        }
    }

    suspend fun generateWorkoutPlan(
        kind: PracticeKind,
        language: String,
        restrictions: Set<ExerciseLimitation>,
        goals: Set<ExerciseGoal>,
        intensity: String? = null
    ): WorkoutPlanDto {
        val request = GenerateWorkoutRequest(
            userId = currentUserId(),
            intensity = normalizedIntensity(intensity),
            language = language,
            restrictions = restrictions.toList(),
            goals = goals.toList()
        )
        return when (kind) {
            PracticeKind.TAI_CHI -> workoutApi.generateWorkout(request)
            PracticeKind.WALKING -> workoutApi.generateWalkingWorkout(request)
        }
    }

    private suspend fun currentUserId(): String? =
        runCatching { idService.getUserID().trim() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }

    private fun normalizedIntensity(intensity: String?): Int? =
        intensity
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it in 1..5 }
}
