package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.billing.model.purchases.local.PurchaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.ui.home.data.WorkoutPlanService
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.ExerciseProgressSyncService
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeCompletionResult
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import taichi.walking.seniors.beginners.util.RateAppDialogHandler
import javax.inject.Inject

open class BaseJourneyHomeViewModel(
    private val practiceKind: PracticeKind,
    private val workoutPlanService: WorkoutPlanService,
    private val practiceRepository: PracticeRepository,
    private val purchaseManager: PurchaseManager,
    private val rateAppDialogHandler: RateAppDialogHandler,
    private val exerciseProgressSyncService: ExerciseProgressSyncService
) : ViewModel() {

    data class UiState(
        val workoutPlan: WorkoutPlanDto? = null,
        val isLoadingPlan: Boolean = false,
        val currentDay: Int = 1,
        val selectedDayNumber: Int = 1,
        val selectedDayData: WorkoutDayDto? = null,
        val error: String? = null,
        val isPremium: Boolean = false,
        val showRateDialog: Boolean = false,
        val completionResult: PracticeCompletionResult? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                practiceRepository.currentDay(practiceKind),
                purchaseManager.isUserPremiumSubscribed()
            ) { currentDay, isPremium -> currentDay to isPremium }
                .collect { (day, isPremium) ->
                    _state.update { it.copy(currentDay = day, isPremium = isPremium) }
                }
        }
    }

    fun fetchWorkoutPlan() {
        viewModelScope.launch {
            loadWorkoutPlan()
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
            val completionResult = practiceRepository.completeWorkout(
                kind = practiceKind,
                dayNumber = day.day,
                durationMinutes = day.durationInMinutes
            )
            val shouldShowRateDialog = rateAppDialogHandler.showEnjoyAppDialog()
            _state.update {
                it.copy(
                    showRateDialog = shouldShowRateDialog,
                    completionResult = completionResult
                )
            }
            exerciseProgressSyncService.uploadProgress()
        }
    }

    fun clearCompletionResult() {
        _state.update { it.copy(completionResult = null) }
    }

    fun dismissRateDialog() {
        _state.update { it.copy(showRateDialog = false) }
    }

    fun onRateDialogConfirmed() {
        viewModelScope.launch {
            rateAppDialogHandler.disableEnjoyAppDialog()
            _state.update { it.copy(showRateDialog = false) }
        }
    }

    private suspend fun loadWorkoutPlan(intensityOverride: String? = null) {
        if (_state.value.isLoadingPlan) return

        _state.update { it.copy(isLoadingPlan = true, error = null) }
        try {
            val workoutIntensity = intensityOverride ?: practiceRepository.currentWorkoutIntensity()
            val plan = workoutPlanService.fetchWorkoutPlan(
                kind = practiceKind,
                intensity = workoutIntensity
            )
            plan.intensity?.let { practiceRepository.setWorkoutIntensity(it) }
            _state.update { it.copy(workoutPlan = plan, isLoadingPlan = false) }
        } catch (throwable: Throwable) {
            _state.update {
                it.copy(
                    isLoadingPlan = false,
                    error = throwable.message ?: "Unable to load workout plan"
                )
            }
        }
    }
}

@HiltViewModel
class JourneyHomeViewModel @Inject constructor(
    workoutPlanService: WorkoutPlanService,
    practiceRepository: PracticeRepository,
    purchaseManager: PurchaseManager,
    rateAppDialogHandler: RateAppDialogHandler,
    exerciseProgressSyncService: ExerciseProgressSyncService
) : BaseJourneyHomeViewModel(
    practiceKind = PracticeKind.TAI_CHI,
    workoutPlanService = workoutPlanService,
    practiceRepository = practiceRepository,
    purchaseManager = purchaseManager,
    rateAppDialogHandler = rateAppDialogHandler,
    exerciseProgressSyncService = exerciseProgressSyncService
)

@HiltViewModel
class WalkingHomeViewModel @Inject constructor(
    workoutPlanService: WorkoutPlanService,
    practiceRepository: PracticeRepository,
    purchaseManager: PurchaseManager,
    rateAppDialogHandler: RateAppDialogHandler,
    exerciseProgressSyncService: ExerciseProgressSyncService
) : BaseJourneyHomeViewModel(
    practiceKind = PracticeKind.WALKING,
    workoutPlanService = workoutPlanService,
    practiceRepository = practiceRepository,
    purchaseManager = purchaseManager,
    rateAppDialogHandler = rateAppDialogHandler,
    exerciseProgressSyncService = exerciseProgressSyncService
)
