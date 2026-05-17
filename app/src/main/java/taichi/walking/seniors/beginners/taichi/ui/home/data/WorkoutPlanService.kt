package taichi.walking.seniors.beginners.taichi.ui.home.data

import com.google.gson.Gson
import com.sageai.id.IDService
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingPrefs
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
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
    private val idService: IDService,
    private val onboardingPrefs: OnboardingPrefs,
    private val gson: Gson
) {
    suspend fun fetchWorkoutPlan(
        kind: PracticeKind,
        intensity: String? = null
    ): WorkoutPlanDto {
        val language = Locale.getDefault().language.ifBlank { "en" }
        val personalization = currentPersonalization()
        val request = GenerateWorkoutRequest(
            userId = currentUserId(),
            intensity = normalizedIntensity(intensity),
            language = language,
            restrictions = personalization.restrictions.toList(),
            goals = personalization.goals.toList()
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

    private suspend fun currentPersonalization(): WorkoutPersonalization {
        val onboardingState = onboardingPrefs.getAnswersJson()
            ?.takeIf { it.isNotBlank() }
            ?.let { rawJson -> runCatching { gson.fromJson(rawJson, OnboardingState::class.java) }.getOrNull() }
            ?: return WorkoutPersonalization()

        return WorkoutPersonalization(
            restrictions = onboardingState.toExerciseLimitations(),
            goals = onboardingState.toExerciseGoals()
        )
    }

    private fun OnboardingState.toExerciseGoals(): Set<ExerciseGoal> =
        goals.mapNotNull { goalId ->
            when (goalId) {
                "weight_loss" -> ExerciseGoal.WEIGHT_LOSS
                "improve_balance" -> ExerciseGoal.BALANCE_MOBILITY
                "increase_flexibility" -> ExerciseGoal.BALANCE_MOBILITY
                "more_energy" -> ExerciseGoal.ENERGY_VITALITY
                "reduce_stress" -> ExerciseGoal.WELLNESS_LONGEVITY
                "pain_relief" -> ExerciseGoal.BALANCE_MOBILITY
                else -> null
            }
        }.toSet()

    private fun OnboardingState.toExerciseLimitations(): Set<ExerciseLimitation> =
        buildSet {
            if (armsForwardId == "no") {
                add(ExerciseLimitation.SHOULDER)
            }
            if (stairsFeelingId == "need_to_rest" || stairsFeelingId == "avoid_stairs") {
                add(ExerciseLimitation.KNEE)
                add(ExerciseLimitation.ANKLE)
            }
            if (squatAbilityId == "none" || squatAbilityId == "one_to_five") {
                add(ExerciseLimitation.KNEE)
                add(ExerciseLimitation.HIP)
            }
        }

    private data class WorkoutPersonalization(
        val restrictions: Set<ExerciseLimitation> = emptySet(),
        val goals: Set<ExerciseGoal> = emptySet()
    )
}
