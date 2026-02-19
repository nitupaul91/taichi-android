package taichi.walking.seniors.beginners.taichi.ui.progress.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
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

    data class UiState(
        val currentDay: Int = 1,
        val totalMinutes: Int = 0,
        val completionDates: List<LocalDate> = emptyList(),
        val weeklyProgress: List<DayProgress> = emptyList(),
        val streak: Int = 0
    ) {
        val totalDaysCompleted: Int get() = (currentDay - 1).coerceAtLeast(0)
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                practiceRepository.currentDay,
                practiceRepository.totalMinutes,
                practiceRepository.completionDates
            ) { day, mins, dates -> Triple(day, mins, dates) }
                .collect { (day, mins, dates) ->
                    _state.update {
                        it.copy(
                            currentDay = day,
                            totalMinutes = mins,
                            completionDates = dates,
                            weeklyProgress = buildWeek(dates),
                            streak = computeStreak(dates)
                        )
                    }
                }
        }
    }

    private fun buildWeek(dates: List<LocalDate>): List<DayProgress> {
        val today = LocalDate.now()
        val start = today.with(DayOfWeek.MONDAY)
        val map = dates.toSet()
        return (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            DayProgress(
                dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                date = date,
                isCompleted = map.contains(date)
            )
        }
    }

    private fun computeStreak(dates: List<LocalDate>): Int {
        val set = dates.toSet()
        if (set.isEmpty()) return 0
        var streak = 0
        var day = LocalDate.now()
        if (!set.contains(day)) day = day.minusDays(1)
        while (set.contains(day)) {
            streak += 1
            day = day.minusDays(1)
        }
        return streak
    }
}
