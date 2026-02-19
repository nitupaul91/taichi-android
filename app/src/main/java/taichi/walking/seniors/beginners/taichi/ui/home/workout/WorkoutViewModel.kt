package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WorkoutViewModel(
    val dayNumber: Int,
    val dayData: WorkoutDayDto,
    startingExerciseIndex: Int = 0
) : ViewModel() {

    var currentExerciseIndex by mutableIntStateOf(startingExerciseIndex)
        private set

    var isPaused by mutableStateOf(false)
        private set

    var isAudioEnabled by mutableStateOf(true)
        private set

    var isVideoPlaying by mutableStateOf(true)
        private set

    var exerciseElapsedSeconds by mutableIntStateOf(0)
        private set

    var isWorkoutComplete by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    val currentExercise get() = dayData.exercises.getOrNull(currentExerciseIndex)

    val currentExerciseProgress: Float
        get() {
            val duration = (currentExercise?.duration ?: 0).coerceAtLeast(1)
            return (exerciseElapsedSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }

    val overallProgress: Float
        get() {
            val total = dayData.exercises.size.coerceAtLeast(1)
            return (currentExerciseIndex + currentExerciseProgress) / total.toFloat()
        }

    fun startTimer(onTick: () -> Unit, onComplete: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !isWorkoutComplete) {
                delay(1000)
                if (isPaused) continue
                exerciseElapsedSeconds += 1
                onTick()
                val duration = currentExercise?.duration ?: 0
                if (duration > 0 && exerciseElapsedSeconds >= duration) {
                    val done = nextExercise()
                    if (done) {
                        isWorkoutComplete = true
                        onComplete()
                        break
                    }
                }
            }
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        isVideoPlaying = !isPaused
    }

    fun toggleVideoPlayback() {
        isVideoPlaying = !isVideoPlaying
        isPaused = !isVideoPlaying
    }

    fun toggleVoiceGuidance() {
        isAudioEnabled = !isAudioEnabled
    }

    fun nextExercise(): Boolean {
        if (currentExerciseIndex < dayData.exercises.size - 1) {
            currentExerciseIndex += 1
            exerciseElapsedSeconds = 0
            isPaused = false
            isVideoPlaying = true
            return false
        }
        return true
    }

    fun previousOrRestart() {
        if (exerciseElapsedSeconds < 3 && currentExerciseIndex > 0) {
            currentExerciseIndex -= 1
        }
        exerciseElapsedSeconds = 0
        isPaused = false
        isVideoPlaying = true
    }

    fun stop() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }
}
