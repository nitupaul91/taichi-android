package taichi.walking.seniors.beginners.taichi.onboarding.viewmodel

import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepository
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.SingleSelect -> handleSingleSelect(action.questionId, action.optionId)
            is OnboardingAction.MultiToggle -> handleMultiToggle(action.questionId, action.optionId)
            is OnboardingAction.MeasurementSystemChange -> _state.update { it.copy(useMetric = action.value) }
            is OnboardingAction.AgeChange -> _state.update { it.copy(actualAge = action.value) }
            is OnboardingAction.HeightChange -> _state.update { it.copy(heightCm = action.value) }
            is OnboardingAction.WeightChange -> _state.update {
                it.copy(
                    weightKg = action.value,
                    targetWeightKg = autoTargetWeight(action.value)
                )
            }
            is OnboardingAction.TargetWeightChange -> _state.update { it.copy(targetWeightKg = action.value) }
            is OnboardingAction.ShapeUpEventDateChange -> _state.update {
                it.copy(shapeUpEventDateMillis = action.value, shapeUpEventDateSkipped = false)
            }
            is OnboardingAction.ShapeUpEventDateSkipped -> _state.update {
                it.copy(shapeUpEventDateSkipped = action.value)
            }
            is OnboardingAction.EmailChange -> _state.update { it.copy(email = action.value) }
            OnboardingAction.Complete -> markCompleted()
        }
    }

    private fun handleSingleSelect(questionId: String, optionId: String) {
        _state.update { current ->
            when (questionId) {
                "heard_about_us" -> current.copy(discoverySourceId = optionId)
                "gender" -> applyGenderDefaults(current, optionId)
                "familiarity" -> current.copy(taiChiFamiliarityId = optionId)
                "current_body" -> current.copy(currentBodyTypeId = optionId)
                "target_body" -> current.copy(targetBodyTypeId = optionId)
                "activity" -> current.copy(activityLevelId = optionId)
                "walk" -> current.copy(walkDailyId = optionId)
                "stairs" -> current.copy(stairsFeelingId = optionId)
                "squat" -> current.copy(squatAbilityId = optionId)
                "rotate_head" -> current.copy(rotateHeadId = optionId)
                "arms_forward" -> current.copy(armsForwardId = optionId)
                "tai_chi_level" -> current.copy(taiChiLevelId = optionId)
                "between_meals" -> current.copy(betweenMealsId = optionId)
                "sleep" -> current.copy(sleepAmountId = optionId)
                "water" -> current.copy(waterIntakeId = optionId)
                "shape_up_event" -> current.copy(
                    shapeUpEventId = optionId,
                    shapeUpEventDateSkipped = optionId == "none"
                )
                "motivation" -> current.copy(motivationLevelId = optionId)
                else -> current
            }
        }
    }

    private fun handleMultiToggle(questionId: String, optionId: String) {
        _state.update { current ->
            when (questionId) {
                "goals" -> current.copy(goals = toggleSet(current.goals, optionId))
                "zones" -> current.copy(targetZones = toggleSet(current.targetZones, optionId))
                "diet" -> current.copy(dietTypes = toggleSet(current.dietTypes, optionId))
                "blockers" -> current.copy(exerciseBlockers = toggleSet(current.exerciseBlockers, optionId))
                else -> current
            }
        }
    }

    private fun toggleSet(current: Set<String>, item: String): Set<String> =
        if (current.contains(item)) current - item else current + item

    private fun applyGenderDefaults(current: OnboardingState, genderId: String): OnboardingState {
        val defaults = when (genderId) {
            "female" -> Triple(163, 75, autoTargetWeight(75))
            "male" -> Triple(172, 85, autoTargetWeight(85))
            else -> Triple(current.heightCm, current.weightKg, current.targetWeightKg)
        }
        return current.copy(
            genderId = genderId,
            heightCm = defaults.first,
            weightKg = defaults.second,
            targetWeightKg = defaults.third,
            currentBodyTypeId = null,
            targetBodyTypeId = null
        )
    }

    private fun autoTargetWeight(currentWeightKg: Int): Int =
        (currentWeightKg * 0.95f).toInt()

    suspend fun completeOnboarding() {
        repository.saveAnswers(state.value)
        repository.markCompleted()
    }

    suspend fun persistSnapshot() {
        repository.persistSnapshot(state.value)
    }

    private fun markCompleted() {
        viewModelScope.launch { completeOnboarding() }
    }
}
