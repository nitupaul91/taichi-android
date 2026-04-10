package taichi.walking.seniors.beginners.taichi.ui.progress.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeDaySummary
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeTitleModel
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeTitleService
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val practiceRepository: PracticeRepository
) : ViewModel() {

    data class DayProgress(
        val dayName: String,
        val date: LocalDate,
        val isCompleted: Boolean
    )

    data class CalendarDayProgress(
        val date: LocalDate,
        val isCompleted: Boolean
    )

    data class UiState(
        val weeklyProgress: List<DayProgress> = emptyList(),
        val monthlyProgress: List<CalendarDayProgress> = emptyList(),
        val totalDaysPracticed: Int = 0,
        val totalMinutesPracticed: Int = 0,
        val currentStreak: Int = 0,
        val currentTitle: PracticeTitleModel? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            practiceRepository.practiceSnapshot.collect { snapshot ->
                val summaries = snapshot.summaries.filter { it.hasPractice() }
                _state.update {
                    it.copy(
                        weeklyProgress = buildWeeklyProgress(summaries),
                        monthlyProgress = buildMonthlyProgress(summaries),
                        totalDaysPracticed = snapshot.totalDaysPracticed,
                        totalMinutesPracticed = snapshot.totalMinutesPracticed,
                        currentStreak = practiceRepository.computeCurrentStreak(summaries),
                        currentTitle = PracticeTitleService.getTitle(snapshot.totalDaysPracticed)
                    )
                }
            }
        }
    }

    private fun buildWeeklyProgress(summaries: List<PracticeDaySummary>): List<DayProgress> {
        val practicedDates = summaries.map { it.date }.toSet()
        val today = LocalDate.now()
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeek))

        return (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            DayProgress(
                dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                date = date,
                isCompleted = date in practicedDates
            )
        }
    }

    private fun buildMonthlyProgress(summaries: List<PracticeDaySummary>): List<CalendarDayProgress> {
        val practicedDates = summaries.map { it.date }.toSet()
        val today = LocalDate.now()
        val startDate = today.minusDays(29)
        return (0L..29L).map { offset ->
            val date = startDate.plusDays(offset)
            CalendarDayProgress(
                date = date,
                isCompleted = date in practicedDates
            )
        }
    }
}
