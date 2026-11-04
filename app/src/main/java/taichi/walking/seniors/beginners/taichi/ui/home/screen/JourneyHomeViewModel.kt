package taichi.walking.seniors.beginners.taichi.ui.home.screen

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.logAppEvent
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
import taichi.walking.seniors.beginners.taichi.music.WorkoutMusicService
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutMusicTrack
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import taichi.walking.seniors.beginners.util.RateAppDialogHandler
import javax.inject.Inject

open class BaseJourneyHomeViewModel(
    private val practiceKind: PracticeKind,
    private val workoutPlanService: WorkoutPlanService,
    private val practiceRepository: PracticeRepository,
    private val purchaseManager: PurchaseManager,
    private val rateAppDialogHandler: RateAppDialogHandler,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val exerciseProgressSyncService: ExerciseProgressSyncService,
    private val workoutMusicService: WorkoutMusicService
) : ViewModel() {

    /** CDN URL for a music track; null when the catalog is unavailable, which keeps music off. */
    suspend fun musicTrackUrl(track: WorkoutMusicTrack): String? =
        workoutMusicService.urlFor(track)

    data class UiState(
        val workoutPlan: WorkoutPlanDto? = null,
        val isLoadingPlan: Boolean = false,
        val currentDay: Int = 1,
        val selectedDayNumber: Int = 1,
        val selectedDayData: WorkoutDayDto? = null,
        val error: String? = null,
        val isPremium: Boolean = false,
        val musicOnByDefault: Boolean = false,
        val showRateDialog: Boolean = false,
        val completionResult: PracticeCompletionResult? = null,
        /** Stored level "1".."5"; null until the first plan sets it. */
        val workoutIntensity: String? = null,
        val showIntensityWidget: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Guards against overlapping restores when the screen recomposes or is reopened quickly. */
    private var isRestoringProgress = false

    init {
        // Pull server-side progress once per ViewModel so a reinstall or a new device recovers the
        // user's streak instead of silently restarting at day 1.
        viewModelScope.launch { restoreProgressFromBackend() }
        _state.update {
            it.copy(
                musicOnByDefault = remoteConfig.musicOnByDefault(),
                showIntensityWidget = remoteConfig.showWorkoutIntensityWidget()
            )
        }
        viewModelScope.launch {
            practiceRepository.workoutIntensity().collect { intensity ->
                _state.update { it.copy(workoutIntensity = intensity) }
            }
        }

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

    private suspend fun restoreProgressFromBackend() {
        if (isRestoringProgress) return
        isRestoringProgress = true
        try {
            exerciseProgressSyncService.restoreAndMergeProgress()
        } finally {
            isRestoringProgress = false
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

    val analyticsPracticeKind: String get() = practiceKind.storageValue

    /**
     * Days 1..N of every program are free (Remote Config `free_days_count`). Read on each use, like
     * iOS, so a value activated after launch applies the next time a day is opened.
     */
    fun freeDaysCount(): Int = remoteConfig.freeDaysCount()

    /** Remote Config `should_show_post_workout_paywall`, read when the completed workout is left. */
    fun shouldShowPostWorkoutPaywall(): Boolean = remoteConfig.shouldShowPostWorkoutPaywall()

    /** iOS applyWorkoutIntensity: store the level, then regenerate the plan with it. */
    fun applyWorkoutIntensity(intensity: String) {
        viewModelScope.launch {
            practiceRepository.setWorkoutIntensity(intensity)
            loadWorkoutPlan(intensityOverride = intensity)
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

    /** homeAnalyticsContext on iOS. */
    fun homeAnalyticsParams(): Bundle = bundleOf(
        "practice_kind" to practiceKind.storageValue,
        "is_premium" to _state.value.isPremium
    )

    /**
     * iOS home_view: the first time this practice's tab shows in the process. home_view_count is
     * kept per practice across launches and also counts silent returns from the background.
     */
    fun onHomeShown(context: Context) {
        if (!homeViewTracked.add(practiceKind)) return
        val count = bumpHomeViewCount(context)
        val absoluteDay = _state.value.currentDay.coerceAtLeast(1)
        val cycleLength = _state.value.workoutPlan?.allDays?.size?.takeIf { it > 0 } ?: DEFAULT_CYCLE_LENGTH
        logAppEvent("home_view", homeAnalyticsParams().apply {
            putInt("home_view_count", count)
            putInt("current_day", (absoluteDay - 1) % cycleLength + 1)
            putInt("cycle", (absoluteDay - 1) / cycleLength + 1)
        })
    }

    fun onReturnedToForeground(context: Context) {
        bumpHomeViewCount(context)
    }

    private fun bumpHomeViewCount(context: Context): Int {
        val prefs = context.getSharedPreferences(HOME_ANALYTICS_PREFS, Context.MODE_PRIVATE)
        val key = "home_view_count_${practiceKind.storageValue}"
        val count = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, count).apply()
        return count
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
            // iOS plan_fetch_failed; Android keeps no plan on disk, so there is never a fallback.
            logAppEvent(
                "plan_fetch_failed",
                bundleOf(
                    "practice_kind" to practiceKind.storageValue,
                    "offline_cache_used" to false,
                    "error" to "${throwable::class.java.simpleName}#${(throwable as? retrofit2.HttpException)?.code() ?: 0}"
                )
            )
            _state.update {
                it.copy(
                    isLoadingPlan = false,
                    error = throwable.message ?: "Unable to load workout plan"
                )
            }
        }
    }
}

/** Practice kinds whose home_view was logged in this process (iOS: once per tab instance). */
private val homeViewTracked: MutableSet<PracticeKind> = java.util.Collections.synchronizedSet(mutableSetOf())
private const val HOME_ANALYTICS_PREFS = "home_analytics"
private const val DEFAULT_CYCLE_LENGTH = 30

@HiltViewModel
class JourneyHomeViewModel @Inject constructor(
    workoutPlanService: WorkoutPlanService,
    practiceRepository: PracticeRepository,
    purchaseManager: PurchaseManager,
    rateAppDialogHandler: RateAppDialogHandler,
    remoteConfig: AiAssistantRemoteConfig,
    exerciseProgressSyncService: ExerciseProgressSyncService,
    workoutMusicService: WorkoutMusicService
) : BaseJourneyHomeViewModel(
    practiceKind = PracticeKind.TAI_CHI,
    workoutPlanService = workoutPlanService,
    practiceRepository = practiceRepository,
    purchaseManager = purchaseManager,
    rateAppDialogHandler = rateAppDialogHandler,
    remoteConfig = remoteConfig,
    exerciseProgressSyncService = exerciseProgressSyncService,
    workoutMusicService = workoutMusicService
)

@HiltViewModel
class WalkingHomeViewModel @Inject constructor(
    workoutPlanService: WorkoutPlanService,
    practiceRepository: PracticeRepository,
    purchaseManager: PurchaseManager,
    rateAppDialogHandler: RateAppDialogHandler,
    remoteConfig: AiAssistantRemoteConfig,
    exerciseProgressSyncService: ExerciseProgressSyncService,
    workoutMusicService: WorkoutMusicService
) : BaseJourneyHomeViewModel(
    practiceKind = PracticeKind.WALKING,
    workoutPlanService = workoutPlanService,
    practiceRepository = practiceRepository,
    purchaseManager = purchaseManager,
    rateAppDialogHandler = rateAppDialogHandler,
    remoteConfig = remoteConfig,
    exerciseProgressSyncService = exerciseProgressSyncService,
    workoutMusicService = workoutMusicService
)

@HiltViewModel
class ChairHomeViewModel @Inject constructor(
    workoutPlanService: WorkoutPlanService,
    practiceRepository: PracticeRepository,
    purchaseManager: PurchaseManager,
    rateAppDialogHandler: RateAppDialogHandler,
    remoteConfig: AiAssistantRemoteConfig,
    exerciseProgressSyncService: ExerciseProgressSyncService,
    workoutMusicService: WorkoutMusicService
) : BaseJourneyHomeViewModel(
    practiceKind = PracticeKind.CHAIR,
    workoutPlanService = workoutPlanService,
    practiceRepository = practiceRepository,
    purchaseManager = purchaseManager,
    rateAppDialogHandler = rateAppDialogHandler,
    remoteConfig = remoteConfig,
    exerciseProgressSyncService = exerciseProgressSyncService,
    workoutMusicService = workoutMusicService
)
