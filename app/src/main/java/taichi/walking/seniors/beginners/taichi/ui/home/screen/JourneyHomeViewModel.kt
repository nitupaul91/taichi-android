package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import taichi.walking.seniors.beginners.taichi.ui.home.data.WorkoutPlanService
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyHomeViewModel @Inject constructor(
    private val workoutPlanService: WorkoutPlanService,
    private val practiceRepository: PracticeRepository
) : ViewModel() {

    data class UiState(
        val workoutPlan: WorkoutPlanDto? = null,
        val isLoadingPlan: Boolean = false,
        val currentDay: Int = 1,
        val selectedDayNumber: Int = 1,
        val selectedDayData: WorkoutDayDto? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                practiceRepository.currentDay,
                practiceRepository.totalMinutes
            ) { currentDay, _ -> currentDay }
                .collect { day -> _state.update { it.copy(currentDay = day) } }
        }
    }

    fun fetchWorkoutPlan() {
        if (_state.value.isLoadingPlan) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPlan = true, error = null) }
            runCatching { workoutPlanService.fetchWorkoutPlan() }
                .onSuccess { plan ->
                    _state.update { it.copy(workoutPlan = plan, isLoadingPlan = false) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoadingPlan = false, error = throwable.message ?: "Unable to load workout plan") }
                }
        }
    }

    fun openWorkoutFlow(dayNumber: Int) {
        val dayData = _state.value.workoutPlan?.dayAt(dayNumber) ?: return
        _state.update {
            it.copy(
                selectedDayNumber = dayNumber,
                selectedDayData = dayData
            )
        }
    }

    fun completeWorkoutFlow(day: WorkoutDayDto) {
        viewModelScope.launch {
            practiceRepository.recordCompletion(day.durationInMinutes)
        }
    }
}
