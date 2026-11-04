package taichi.walking.seniors.beginners.taichi.onboarding.analytics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import java.text.SimpleDateFormat
import java.util.Locale

/** iOS `OnboardingFlowModel.answerSummary`, on the Android answer ids. */
class OnboardingAnswerSummaryTest {

    private val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("2026-11-04 12:00")!!.time

    @Test
    fun `single choice reports the id, or none before an answer`() {
        assertEquals(
            OnboardingAnswerSummary("female", 1, false),
            OnboardingAnswerSummary.of(OnboardingRoutes.Gender, OnboardingState(genderId = "female"), now)
        )
        assertEquals(
            OnboardingAnswerSummary("none", 0, false),
            OnboardingAnswerSummary.of(OnboardingRoutes.HeardAboutUs, OnboardingState(), now)
        )
        assertEquals(
            OnboardingAnswerSummary("let_us_decide", 1, false),
            OnboardingAnswerSummary.of(OnboardingRoutes.ProgramSelection, OnboardingState(programSelection = "let_us_decide"), now)
        )
    }

    @Test
    fun `multi choice is sorted and comma joined`() {
        assertEquals(
            OnboardingAnswerSummary("improve_balance,weight_loss", 2, false),
            OnboardingAnswerSummary.of(
                OnboardingRoutes.Goals,
                OnboardingState(goals = setOf("weight_loss", "improve_balance")),
                now
            )
        )
        assertEquals(
            OnboardingAnswerSummary("none", 0, false),
            OnboardingAnswerSummary.of(OnboardingRoutes.ExerciseBlockers, OnboardingState(), now)
        )
    }

    @Test
    fun `pickers report whether they were left on their opening value`() {
        val female = OnboardingState(genderId = "female", heightCm = 163, weightKg = 75, targetWeightKg = 71)
        assertEquals(OnboardingAnswerSummary("163", 1, true), OnboardingAnswerSummary.of(OnboardingRoutes.Height, female, now))
        assertEquals(OnboardingAnswerSummary("75.0", 1, true), OnboardingAnswerSummary.of(OnboardingRoutes.Weight, female, now))
        assertEquals(OnboardingAnswerSummary("71.0", 1, true), OnboardingAnswerSummary.of(OnboardingRoutes.TargetWeight, female, now))

        val moved = female.copy(heightCm = 170, weightKg = 80, targetWeightKg = 70)
        assertEquals(false, OnboardingAnswerSummary.of(OnboardingRoutes.Height, moved, now)!!.isDefault)
        assertEquals(false, OnboardingAnswerSummary.of(OnboardingRoutes.Weight, moved, now)!!.isDefault)
        assertEquals(false, OnboardingAnswerSummary.of(OnboardingRoutes.TargetWeight, moved, now)!!.isDefault)

        assertEquals(OnboardingAnswerSummary("55", 1, true), OnboardingAnswerSummary.of(OnboardingRoutes.Age, OnboardingState(), now))
        assertEquals(false, OnboardingAnswerSummary.of(OnboardingRoutes.Age, OnboardingState(actualAge = 67), now)!!.isDefault)
    }

    @Test
    fun `event date is a day, or skipped`() {
        val eightWeeksOut = now + 56L * 24 * 60 * 60 * 1000
        val summary = OnboardingAnswerSummary.of(
            OnboardingRoutes.ShapeUpEventDate,
            OnboardingState(shapeUpEventDateMillis = eightWeeksOut),
            now
        )!!
        assertEquals("2026-12-30", summary.answer)
        assertEquals(true, summary.isDefault)

        assertEquals(
            OnboardingAnswerSummary("skipped", 0, false),
            OnboardingAnswerSummary.of(OnboardingRoutes.ShapeUpEventDate, OnboardingState(shapeUpEventDateSkipped = true), now)
        )
    }

    @Test
    fun `screens that collect nothing report no answer`() {
        listOf(
            OnboardingRoutes.Welcome, OnboardingRoutes.ValuePropOnWay, OnboardingRoutes.Bmi,
            OnboardingRoutes.Summary, OnboardingRoutes.Loading, OnboardingRoutes.SocialProof,
            OnboardingRoutes.NotificationPermission, OnboardingRoutes.Paywall
        ).forEach { assertNull(OnboardingAnswerSummary.of(it, OnboardingState(), now), "$it") }
    }
}
