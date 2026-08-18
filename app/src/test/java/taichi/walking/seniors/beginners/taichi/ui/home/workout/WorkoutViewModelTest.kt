package taichi.walking.seniors.beginners.taichi.ui.home.workout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDifficulty
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseStage
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun exercise(id: String, duration: Int) = ExerciseDto(
        exerciseId = id,
        name = id,
        limitations = emptyList(),
        steps = emptyList(),
        energyUse = 10,
        lengthSeconds = duration,
        intensity = ExerciseDifficulty.LIGHT,
        goals = emptyList(),
        stage = ExerciseStage.entries.first(),
        heroImageUrl = "https://example.test/cover.jpg",
        hintSpeechUrl = "https://example.test/hint.mp3",
        stepsSpeechUrl = "https://example.test/steps.mp3",
        nameSpeechUrl = "https://example.test/name.mp3",
        videoUrl = "https://example.test/video.mp4"
    )

    private fun model(exerciseCount: Int = 3, duration: Int = 60) = WorkoutViewModel(
        dayNumber = 1,
        dayData = WorkoutDayDto(
            day = 1,
            lengthSeconds = duration * exerciseCount,
            exercises = (1..exerciseCount).map { exercise("ex$it", duration) },
            description = "test day",
            heroImageUrl = "https://example.test/day.jpg"
        )
    )

    /** The reported bug: a video that never plays used to burn through the exercise anyway. */
    @Test
    fun `a stalled video freezes the workout clock`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})

        // Run out the preview so the exercise timer is the thing ticking.
        advanceTimeBy((WorkoutViewModel.PREVIEW_DURATION_SECONDS + 1) * 1000L)
        advanceTimeBy(3_000)
        val elapsedBeforeStall = model.exerciseElapsedSeconds
        assertTrue(elapsedBeforeStall > 0, "timer should run while the video plays")

        model.onVideoStalledChanged(true)
        advanceTimeBy(10_000)
        assertEquals(elapsedBeforeStall, model.exerciseElapsedSeconds, "clock advanced while stalled")

        model.onVideoStalledChanged(false)
        advanceTimeBy(3_000)
        assertTrue(model.exerciseElapsedSeconds > elapsedBeforeStall, "clock did not resume")

        model.stop()
    }

    @Test
    fun `recovering from a stall does not override a deliberate user pause`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})
        advanceTimeBy((WorkoutViewModel.PREVIEW_DURATION_SECONDS + 1) * 1000L)

        model.togglePause()
        assertTrue(model.isPaused)
        model.onVideoStalledChanged(true)
        model.onVideoStalledChanged(false)

        val elapsed = model.exerciseElapsedSeconds
        advanceTimeBy(5_000)
        assertEquals(elapsed, model.exerciseElapsedSeconds, "stall recovery resumed a paused workout")
        assertTrue(model.isPaused, "user pause was cleared by video recovery")

        model.stop()
    }

    @Test
    fun `a stall also freezes the preview countdown`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})
        advanceTimeBy(2_000)

        model.onVideoStalledChanged(true)
        val stillPreviewing = model.isShowingPreview
        advanceTimeBy(30_000)
        assertTrue(stillPreviewing && model.isShowingPreview, "preview advanced past a stalled video")

        model.stop()
    }

    @Test
    fun `exiting on the last two exercises counts as complete`() = runTest(dispatcher) {
        val model = model(exerciseCount = 3)
        model.start(onTick = {}, onComplete = {})

        // Still on the first of three: exiting is a genuine abandon.
        advanceTimeBy((WorkoutViewModel.PREVIEW_DURATION_SECONDS + 1) * 1000L)
        assertEquals(3, model.remainingExercises)
        assertFalse(model.shouldCompleteOnExit)

        // Advance to the final exercise.
        model.nextExercise(onTick = {}, onComplete = {})
        advanceTimeBy((WorkoutViewModel.PREVIEW_DURATION_SECONDS + 1) * 1000L)
        assertEquals(2, model.remainingExercises)
        assertTrue(model.shouldCompleteOnExit, "second-to-last exercise should count on exit")

        model.stop()
    }

    @Test
    fun `exiting during the very first preview is never a completion`() = runTest(dispatcher) {
        val model = model(exerciseCount = 1)
        model.start(onTick = {}, onComplete = {})
        advanceTimeBy(1_000)

        // Only one exercise, so remaining is within the threshold — but nothing has been done yet.
        assertEquals(1, model.remainingExercises)
        assertFalse(model.shouldCompleteOnExit, "an untouched workout was counted as complete")

        model.stop()
    }

    /**
     * Reported from a device: tapping Start mid "5-4-3-2-1" left the countdown clip playing
     * underneath the exercise narration. The clip is ~5s, so skipping the preview must actively
     * silence it, not merely stop scheduling new cues.
     */
    @Test
    fun `skipping the preview silences the countdown clip`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})

        // Run far enough for the countdown cue to have been emitted.
        advanceTimeBy(6_000)
        assertTrue(model.audioCueEvent?.cue != null, "countdown cue should have fired")

        model.finishPreview(onTick = {}, onComplete = {})
        assertEquals(null, model.audioCueEvent?.cue, "countdown clip was not silenced on Start")
        assertFalse(model.isShowingPreview, "preview still showing after Start")

        model.stop()
    }

    @Test
    fun `a stalled video silences cues so nothing narrates over a frozen frame`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})
        advanceTimeBy(6_000)
        assertTrue(model.audioCueEvent?.cue != null)

        model.onVideoStalledChanged(true)
        assertEquals(null, model.audioCueEvent?.cue, "cue kept playing while the video was frozen")

        model.stop()
    }

    @Test
    fun `moving to the next exercise silences any lingering cue`() = runTest(dispatcher) {
        val model = model()
        model.start(onTick = {}, onComplete = {})
        advanceTimeBy(6_000)

        model.nextExercise(onTick = {}, onComplete = {})
        assertEquals(null, model.audioCueEvent?.cue)

        model.stop()
    }
}
