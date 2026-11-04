package taichi.walking.seniors.beginners.taichi.onboarding.nav

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The ordering rules ported from iOS `OnboardingRootView.navigationSteps` / `adjacentStep`. */
class OnboardingStepsTest {

    private val defaults = OnboardingFlowFlags()

    @Test
    fun `default flow follows iOS allCases with loading before social proof and no plan-ready`() {
        val steps = OnboardingSteps.navigationSteps(goalsFirst = false, flags = defaults, shapeUpEventId = null)

        assertEquals(OnboardingRoutes.Welcome, steps.first())
        assertEquals(OnboardingRoutes.Paywall, steps.last())
        assertEquals(
            listOf(OnboardingRoutes.Summary, OnboardingRoutes.Loading, OnboardingRoutes.SocialProof, OnboardingRoutes.Paywall),
            steps.takeLast(4)
        )
        assertFalse(OnboardingRoutes.PlanReady in steps)
        // iOS default: show_notifications_permission is off.
        assertFalse(OnboardingRoutes.NotificationPermission in steps)
        // No event answered yet, so there is no date to ask for.
        assertFalse(OnboardingRoutes.ShapeUpEventDate in steps)
    }

    @Test
    fun `event date is asked only for an actual event`() {
        val noEvent = OnboardingSteps.navigationSteps(false, defaults, shapeUpEventId = "none")
        val vacation = OnboardingSteps.navigationSteps(false, defaults, shapeUpEventId = "vacation")

        assertFalse(OnboardingRoutes.ShapeUpEventDate in noEvent)
        assertEquals(
            OnboardingRoutes.ShapeUpEventDate,
            vacation[vacation.indexOf(OnboardingRoutes.ShapeUpEvent) + 1]
        )
        assertEquals(
            OnboardingRoutes.Motivation,
            noEvent[noEvent.indexOf(OnboardingRoutes.ShapeUpEvent) + 1]
        )
    }

    @Test
    fun `flags remove body type, lifestyle and add notification screens`() {
        val steps = OnboardingSteps.navigationSteps(
            goalsFirst = false,
            flags = OnboardingFlowFlags(
                bodyTypeScreensEnabled = false,
                skipLifestyleQuestions = true,
                showNotificationsPermission = true
            ),
            shapeUpEventId = null
        )

        assertFalse(OnboardingRoutes.CurrentBodyType in steps)
        assertFalse(OnboardingRoutes.TargetBodyType in steps)
        OnboardingSteps.lifestyleSteps.forEach { assertFalse(it in steps, "$it should be skipped") }
        assertEquals(
            OnboardingRoutes.NotificationPermission,
            steps[steps.indexOf(OnboardingRoutes.Summary) + 1]
        )
    }

    @Test
    fun `goals-first order moves gender and body types in front of height`() {
        val steps = OnboardingSteps.navigationSteps(goalsFirst = true, flags = defaults, shapeUpEventId = null)

        assertEquals(OnboardingRoutes.Goals, steps[1])
        val heightIndex = steps.indexOf(OnboardingRoutes.Height)
        assertEquals(
            listOf(OnboardingRoutes.Gender, OnboardingRoutes.CurrentBodyType, OnboardingRoutes.TargetBodyType),
            steps.subList(heightIndex - 3, heightIndex)
        )
        assertEquals(OnboardingSteps.allCases.toSet(), OnboardingSteps.goalsFirstOrder.toSet())
    }

    @Test
    fun `adjacent step walks the navigation list`() {
        val ordered = OnboardingSteps.allCases
        val steps = OnboardingSteps.navigationSteps(false, defaults, null)

        assertEquals(OnboardingRoutes.Gender, OnboardingSteps.adjacentStep(OnboardingRoutes.Welcome, true, ordered, steps))
        assertEquals(OnboardingRoutes.Welcome, OnboardingSteps.adjacentStep(OnboardingRoutes.Gender, false, ordered, steps))
        assertNull(OnboardingSteps.adjacentStep(OnboardingRoutes.Welcome, false, ordered, steps))
        assertNull(OnboardingSteps.adjacentStep(OnboardingRoutes.Paywall, true, ordered, steps))
        assertEquals(OnboardingRoutes.Paywall, OnboardingSteps.adjacentStep(OnboardingRoutes.SocialProof, true, ordered, steps))
    }

    @Test
    fun `a screen a fetched flag removed continues from the nearest step still in the flow`() {
        val ordered = OnboardingSteps.allCases
        val withoutBodyTypes = OnboardingSteps.navigationSteps(
            false,
            OnboardingFlowFlags(bodyTypeScreensEnabled = false),
            null
        )

        assertEquals(
            OnboardingRoutes.Goals,
            OnboardingSteps.adjacentStep(OnboardingRoutes.CurrentBodyType, true, ordered, withoutBodyTypes)
        )
        assertEquals(
            OnboardingRoutes.Gender,
            OnboardingSteps.adjacentStep(OnboardingRoutes.CurrentBodyType, false, ordered, withoutBodyTypes)
        )
    }

    @Test
    fun `progress runs from zero on the welcome to one on the paywall`() {
        val steps = OnboardingSteps.navigationSteps(false, defaults, null)

        assertEquals(0f, OnboardingSteps.progress(OnboardingRoutes.Welcome, steps))
        assertEquals(1f, OnboardingSteps.progress(OnboardingRoutes.Paywall, steps))
        assertEquals(1f / (steps.size - 1), OnboardingSteps.progress(OnboardingRoutes.Gender, steps))
        assertEquals(0f, OnboardingSteps.progress(OnboardingRoutes.PlanReady, steps))
    }

    @Test
    fun `every route resolves from its NavHost route`() {
        OnboardingSteps.allCases.forEach { assertEquals(it, OnboardingRoutes.fromRoute(it.route)) }
        assertTrue(OnboardingRoutes.all.map { it.route }.distinct().size == OnboardingRoutes.all.size)
    }
}
