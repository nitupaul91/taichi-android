package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WorkoutViewModel(
    val dayNumber: Int,
    val dayData: WorkoutDayDto,
    startingExerciseIndex: Int = 0
) : ViewModel() {
    enum class WorkoutAudioCue(val resId: Int) {
        PreviewCountdown(R.raw.countdown),
        KeepGoing(R.raw.keep_going),
        AlmostDone(R.raw.almost_done),
        TenMoreSeconds(R.raw.ten_more_seconds)
    }

    /** [cue] null means "silence any cue still playing" rather than "play nothing new". */
    data class WorkoutAudioCueEvent(
        val cue: WorkoutAudioCue?,
        val token: Int
    )

    private data class ScheduledCue(
        val atSecond: Int,
        val cue: WorkoutAudioCue
    )

    companion object {
        const val PREVIEW_DURATION_SECONDS = 10
        const val PREVIEW_COUNTDOWN_START = 5

        /** "Last 1-2 exercises" — the window in which exiting still counts as completing. */
        const val EXIT_COMPLETION_REMAINING_THRESHOLD = 2
    }

    var currentExerciseIndex by mutableIntStateOf(startingExerciseIndex)
        private set

    var isPaused by mutableStateOf(false)
        private set

    var isAudioEnabled by mutableStateOf(true)
        private set

    var isVideoPlaying by mutableStateOf(true)
        private set

    /**
     * The video is not actually rendering — it errored, or has been buffering past the grace period.
     * Kept separate from [isPaused] so recovery is automatic and does not fight a deliberate user
     * pause: the clock resumes on its own, but a user who paused stays paused.
     */
    var isVideoStalled by mutableStateOf(false)
        private set

    var exerciseElapsedSeconds by mutableIntStateOf(0)
        private set

    var videoResetTrigger by mutableIntStateOf(0)
        private set

    var isShowingPreview by mutableStateOf(true)
        private set

    var previewCountdown by mutableIntStateOf(0)
        private set

    var isWorkoutComplete by mutableStateOf(false)
        private set

    var audioCueEvent by mutableStateOf<WorkoutAudioCueEvent?>(null)
        private set

    var musicMode by mutableStateOf<WorkoutMusicMode>(WorkoutMusicMode.Off)
        private set

    /** The track actually sounding — resolved once per session when the mode is Shuffle. */
    var activeMusicTrack by mutableStateOf<WorkoutMusicTrack?>(null)
        private set

    /** True once the first preview has ended, i.e. the user actually began exercising. */
    private var hasStartedExercises = false

    private var previewJob: Job? = null
    private var timerJob: Job? = null
    private var cueTokenCounter = 0
    private var instructionAudioDurationSeconds: Int? = null
    private var scheduledExerciseCues: List<ScheduledCue> = emptyList()
    private val firedCueSeconds = mutableSetOf<Int>()

    /**
     * Nothing may advance while the user has paused OR the video is not playing. Without the second
     * condition the timer ran on wall clock alone, so a failed video still burned through the
     * exercise with audio cues firing over a frozen frame.
     */
    private val isClockBlocked get() = isPaused || isVideoStalled

    /** Exercises not yet finished, counting the one in progress. */
    val remainingExercises: Int
        get() = (dayData.exercises.size - currentExerciseIndex).coerceAtLeast(0)

    /**
     * Leaving with only the last exercise or two left counts the workout as done rather than
     * abandoned (iOS parity). Users routinely walk away during the final stretch, and recording
     * that as an abandon both loses their streak and understates completion in our analytics.
     */
    val shouldCompleteOnExit: Boolean
        get() = hasStartedExercises && remainingExercises <= EXIT_COMPLETION_REMAINING_THRESHOLD

    val currentExercise get() = dayData.exercises.getOrNull(currentExerciseIndex)

    val currentExerciseProgress: Float
        get() {
            if (isShowingPreview) return 0f
            val duration = (currentExercise?.duration ?: 0).coerceAtLeast(1)
            return (exerciseElapsedSeconds.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }

    val overallProgress: Float
        get() {
            val total = dayData.exercises.size.coerceAtLeast(1)
            return (currentExerciseIndex + currentExerciseProgress) / total.toFloat()
        }

    fun start(onTick: () -> Unit, onComplete: () -> Unit) {
        stop()
        exerciseElapsedSeconds = 0
        isWorkoutComplete = false
        startPreview(onTick = onTick, onComplete = onComplete)
    }

    private fun startExerciseTimer(onTick: () -> Unit, onComplete: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !isWorkoutComplete) {
                delay(1000)
                if (isClockBlocked) continue
                exerciseElapsedSeconds += 1
                triggerExerciseCueIfNeeded()
                onTick()
                val duration = currentExercise?.duration ?: 0
                if (duration > 0 && exerciseElapsedSeconds >= duration) {
                    val done = nextExercise(onTick = onTick, onComplete = onComplete)
                    if (done) {
                        isWorkoutComplete = true
                        onComplete()
                        break
                    }
                }
            }
        }
    }

    fun startPreview(onTick: () -> Unit, onComplete: () -> Unit) {
        previewJob?.cancel()
        timerJob?.cancel()
        cancelAudioCues()
        resetExerciseCueSchedule()
        isShowingPreview = true
        previewCountdown = 0
        exerciseElapsedSeconds = 0
        isPaused = false
        isVideoPlaying = true
        isVideoStalled = false

        previewJob = viewModelScope.launch {
            var elapsed = 0
            while (isActive && isShowingPreview && !isWorkoutComplete) {
                delay(1000)
                if (isClockBlocked) continue
                elapsed += 1
                val remaining = PREVIEW_DURATION_SECONDS - elapsed

                previewCountdown = if (remaining in 1..PREVIEW_COUNTDOWN_START) {
                    remaining
                } else {
                    0
                }

                if (remaining == PREVIEW_COUNTDOWN_START + 1) {
                    emitAudioCue(WorkoutAudioCue.PreviewCountdown)
                }

                if (remaining <= 0) {
                    finishPreview(onTick = onTick, onComplete = onComplete)
                }
            }
        }
    }

    fun finishPreview(onTick: () -> Unit, onComplete: () -> Unit) {
        previewJob?.cancel()
        cancelAudioCues()
        hasStartedExercises = true
        isShowingPreview = false
        previewCountdown = 0
        exerciseElapsedSeconds = 0
        isPaused = false
        isVideoPlaying = true
        videoResetTrigger += 1
        startExerciseTimer(onTick = onTick, onComplete = onComplete)
    }

    fun updateInstructionAudioDuration(durationSeconds: Int?) {
        val normalizedDuration = durationSeconds?.takeIf { it > 0 }
        if (instructionAudioDurationSeconds == normalizedDuration && normalizedDuration != null) return

        instructionAudioDurationSeconds = normalizedDuration
        firedCueSeconds.clear()
        scheduledExerciseCues = buildExerciseCueSchedule(
            totalDurationSeconds = currentExercise?.duration ?: 0,
            instructionDurationSeconds = normalizedDuration
        )
    }

    /**
     * Driven by the ExoPlayer listener, not by user intent. [stalled] true stops the clock and the
     * instruction audio; false resumes both unless the user has separately paused.
     */
    fun onVideoStalledChanged(stalled: Boolean) {
        if (isVideoStalled == stalled) return
        isVideoStalled = stalled
        if (stalled) cancelAudioCues()
    }

    fun togglePause() {
        isPaused = !isPaused
        isVideoPlaying = !isPaused
    }

    fun toggleVideoPlayback() {
        isVideoPlaying = !isVideoPlaying
        isPaused = !isVideoPlaying
    }

    /**
     * Shuffle resolves to a concrete track once and then stays put: re-rolling on every pause would
     * restart the music mid-session.
     */
    fun selectMusicMode(mode: WorkoutMusicMode) {
        musicMode = mode
        activeMusicTrack = when (mode) {
            WorkoutMusicMode.Off -> null
            WorkoutMusicMode.Shuffle -> WorkoutMusicTrack.entries.random()
            is WorkoutMusicMode.Track -> mode.track
        }
    }

    fun toggleVoiceGuidance() {
        isAudioEnabled = !isAudioEnabled
    }

    fun pauseForBackground() {
        isPaused = true
        isVideoPlaying = false
    }

    fun nextExercise(onTick: () -> Unit, onComplete: () -> Unit): Boolean {
        if (currentExerciseIndex < dayData.exercises.size - 1) {
            currentExerciseIndex += 1
            exerciseElapsedSeconds = 0
            videoResetTrigger += 1
            isPaused = false
            isVideoPlaying = true
            startPreview(onTick = onTick, onComplete = onComplete)
            return false
        }
        return true
    }

    fun previousOrRestart(onTick: () -> Unit, onComplete: () -> Unit) {
        if (isShowingPreview) {
            if (currentExerciseIndex > 0) {
                currentExerciseIndex -= 1
                videoResetTrigger += 1
            }
            startPreview(onTick = onTick, onComplete = onComplete)
            return
        }

        if (exerciseElapsedSeconds < 3 && currentExerciseIndex > 0) {
            currentExerciseIndex -= 1
            videoResetTrigger += 1
            startPreview(onTick = onTick, onComplete = onComplete)
            return
        }
        exerciseElapsedSeconds = 0
        videoResetTrigger += 1
        isPaused = false
        isVideoPlaying = true
        startExerciseTimer(onTick = onTick, onComplete = onComplete)
    }

    /** Ends the session as complete when the user exits during the final exercises. */
    fun completeEarly() {
        stop()
        isWorkoutComplete = true
    }

    fun stop() {
        previewJob?.cancel()
        timerJob?.cancel()
        cancelAudioCues()
        resetExerciseCueSchedule()
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    private fun triggerExerciseCueIfNeeded() {
        if (isShowingPreview) return
        val cue = scheduledExerciseCues.firstOrNull { it.atSecond == exerciseElapsedSeconds } ?: return
        if (!firedCueSeconds.add(cue.atSecond)) return
        emitAudioCue(cue.cue)
    }

    /**
     * Silences an in-flight cue. The countdown clip runs about five seconds, so tapping Start mid
     * "5-4-3-2-1" used to leave it playing underneath the exercise narration.
     */
    fun cancelAudioCues() {
        cueTokenCounter += 1
        audioCueEvent = WorkoutAudioCueEvent(cue = null, token = cueTokenCounter)
    }

    private fun emitAudioCue(cue: WorkoutAudioCue) {
        cueTokenCounter += 1
        audioCueEvent = WorkoutAudioCueEvent(cue = cue, token = cueTokenCounter)
    }

    private fun resetExerciseCueSchedule() {
        instructionAudioDurationSeconds = null
        scheduledExerciseCues = emptyList()
        firedCueSeconds.clear()
    }

    private fun buildExerciseCueSchedule(
        totalDurationSeconds: Int,
        instructionDurationSeconds: Int?
    ): List<ScheduledCue> {
        if (totalDurationSeconds <= 0) return emptyList()
        val instructionEnd = instructionDurationSeconds ?: return emptyList()
        val tenMoreAtSecond = totalDurationSeconds - 10

        if (tenMoreAtSecond <= instructionEnd) return emptyList()

        val gapBeforeTenMore = tenMoreAtSecond - instructionEnd
        val fillerCount = when {
            gapBeforeTenMore >= 15 -> 2
            gapBeforeTenMore >= 10 -> 1
            else -> 0
        }

        val scheduled = mutableListOf<ScheduledCue>()
        if (fillerCount > 0) {
            val step = gapBeforeTenMore.toDouble() / (fillerCount + 1).toDouble()
            scheduled += ScheduledCue(
                atSecond = (instructionEnd + step).roundToInt(),
                cue = WorkoutAudioCue.KeepGoing
            )
        }
        if (fillerCount > 1) {
            val step = gapBeforeTenMore.toDouble() / (fillerCount + 1).toDouble()
            scheduled += ScheduledCue(
                atSecond = (instructionEnd + (step * 2)).roundToInt(),
                cue = WorkoutAudioCue.AlmostDone
            )
        }
        scheduled += ScheduledCue(
            atSecond = tenMoreAtSecond,
            cue = WorkoutAudioCue.TenMoreSeconds
        )

        return scheduled
            .distinctBy { it.atSecond }
            .filter { it.atSecond > instructionEnd && it.atSecond < totalDurationSeconds }
            .sortedBy { it.atSecond }
    }
}
