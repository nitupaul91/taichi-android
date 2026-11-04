package taichi.walking.seniors.beginners.taichi.printable

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDifficulty
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseStage
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPhaseDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import java.io.File

@RunWith(AndroidJUnit4::class)
class PrintablePlanPdfGeneratorTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val output = File(context.cacheDir, "printable_plan_generator_test.pdf")

    /** Solid thumbnails, so the bitmap path runs without the network. */
    private val grayImages = PrintablePlanImageSource { _, width, height ->
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GRAY) }
    }

    @After
    fun tearDown() {
        output.delete()
    }

    @Test
    fun rendersFixedPagesDayPagesAchievementsAndTracker() = runBlocking {
        val plan = samplePlan(days = 30, exercisesPerDay = 4)

        PrintablePlanKinds.forEach { kind ->
            output.outputStream().use { PrintablePlanPdfGenerator(context, grayImages).writePdf(plan, kind, it) }

            // Cover, about, how-to; seven days of four exercises at three per page; achievement
            // pages after days 1, 3 and 7; the tracker.
            assertEquals("pages for $kind", 3 + 7 * 2 + 3 + 1, pageCount(output))
        }
    }

    @Test
    fun missingImagesFallBackToPlaceholders() = runBlocking {
        val plan = samplePlan(days = 2, exercisesPerDay = 3)

        output.outputStream().use {
            PrintablePlanPdfGenerator(context) { _, _, _ -> null }.writePdf(plan, PracticeKind.WALKING, it)
        }

        // Cover, about, how-to; two single-page days; achievement after day 1; the tracker.
        assertEquals(3 + 2 + 1 + 1, pageCount(output))
        assertTrue(output.length() > 0)
    }

    @Test
    fun planWithoutDaysIsRejected() {
        runBlocking {
            try {
                output.outputStream().use {
                    PrintablePlanPdfGenerator(context, grayImages)
                        .writePdf(WorkoutPlanDto(phases = emptyList()), PracticeKind.TAI_CHI, it)
                }
                fail("A plan without days must not produce a PDF")
            } catch (expected: IllegalStateException) {
                // Expected.
            }
        }
    }

    private fun pageCount(file: File): Int {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        return try {
            renderer.pageCount
        } finally {
            renderer.close()
        }
    }

    private fun samplePlan(days: Int, exercisesPerDay: Int) = WorkoutPlanDto(
        phases = listOf(
            WorkoutPhaseDto(
                phase = 1,
                name = "Foundation",
                description = "",
                days = (1..days).map { day ->
                    WorkoutDayDto(
                        day = day,
                        lengthSeconds = 540,
                        description = "",
                        heroImageUrl = "",
                        exercises = (1..exercisesPerDay).map { index ->
                            ExerciseDto(
                                exerciseId = "$day-$index",
                                name = "Gentle Exercise With A Fairly Long Name $index",
                                limitations = emptyList(),
                                steps = List(6) { step -> "Step ${step + 1}: move slowly and breathe calmly through the motion." },
                                energyUse = 2,
                                lengthSeconds = 45 + index * 30,
                                intensity = ExerciseDifficulty.LIGHT,
                                goals = emptyList(),
                                stage = ExerciseStage.PRIMARY,
                                heroImageUrl = "https://example.invalid/$day-$index.webp",
                                hintSpeechUrl = "",
                                stepsSpeechUrl = "",
                                nameSpeechUrl = "",
                                videoUrl = ""
                            )
                        }
                    )
                }
            )
        )
    )

    private companion object {
        val PrintablePlanKinds = PracticeKind.entries
    }
}
