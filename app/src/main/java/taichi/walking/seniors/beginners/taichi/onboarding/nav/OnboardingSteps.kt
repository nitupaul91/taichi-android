package taichi.walking.seniors.beginners.taichi.onboarding.nav

import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ActivityLevel
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Age
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ArmsForward
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.BetweenMeals
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Bmi
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.CurrentBodyType
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.DietType
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ExerciseBlockers
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Familiarity
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Gender
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Goals
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.HeardAboutUs
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Height
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Loading
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Motivation
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.NotificationPermission
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Paywall
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.PlanReady
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.RotateHead
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ShapeUpEvent
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ShapeUpEventDate
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.SleepAmount
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.SocialProof
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.SquatAbility
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.StairsFeeling
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Summary
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.TaiChiLevel
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.TargetBodyType
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.TargetWeight
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.TargetZones
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ValuePropMeditation
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.ValuePropOnWay
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.WalkDaily
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.WaterIntake
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Weight
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes.Welcome
import kotlin.math.max

/**
 * The Remote Config switches that decide which screens are part of the flow (iOS
 * `shouldIncludeStepInNavigation`). Read live at every navigation, as iOS does, so a config that
 * activates mid-onboarding applies to the screens still ahead.
 */
data class OnboardingFlowFlags(
    val bodyTypeScreensEnabled: Boolean = true,
    val skipLifestyleQuestions: Boolean = false,
    val showNotificationsPermission: Boolean = false
)

/**
 * The onboarding step order, ported from iOS `OnboardingStep.allCases` / `goalsFirstOrder` and
 * `OnboardingRootView.navigationSteps`. Pure, so the ordering rules are unit-tested.
 */
object OnboardingSteps {

    /** iOS `OnboardingStep.allCases`: loading comes before social proof, plan-ready is excluded below. */
    val allCases: List<OnboardingRoutes> by lazy {
        listOf(
            Welcome, Gender, CurrentBodyType, Goals,
            TargetBodyType, TargetZones, HeardAboutUs,
            ValuePropOnWay, Familiarity, ValuePropMeditation,
            ActivityLevel, WalkDaily, StairsFeeling, SquatAbility,
            RotateHead, ArmsForward, TaiChiLevel, BetweenMeals,
            SleepAmount, WaterIntake, DietType, Height, Weight,
            Bmi, TargetWeight, Age, ShapeUpEvent,
            ShapeUpEventDate, Motivation, ExerciseBlockers,
            Summary, NotificationPermission, Loading,
            SocialProof, PlanReady, Paywall
        )
    }

    /**
     * A/B test `onboarding_goals_first_enabled`: the same screens, opening on what the user wants.
     * Gender and the body-type pair move next to height and weight; the body-type pictures need the
     * gender answer, so the three stay in this order.
     */
    val goalsFirstOrder: List<OnboardingRoutes> by lazy {
        val bodyQuestions = listOf(Gender, CurrentBodyType, TargetBodyType)
        val steps = allCases.filterNot { it in bodyQuestions }.toMutableList()
        val heightIndex = steps.indexOf(Height).takeIf { it >= 0 } ?: steps.size
        steps.addAll(heightIndex, bodyQuestions)
        steps
    }

    /** Skipped together by `onboarding_skip_lifestyle_questions_enabled`. */
    val lifestyleSteps: Set<OnboardingRoutes> by lazy { setOf(BetweenMeals, SleepAmount, WaterIntake, DietType) }

    fun orderedSteps(goalsFirst: Boolean): List<OnboardingRoutes> =
        if (goalsFirst) goalsFirstOrder else allCases

    /** iOS `orderedSteps.filter(shouldIncludeStepInNavigation)`. */
    fun navigationSteps(
        goalsFirst: Boolean,
        flags: OnboardingFlowFlags,
        shapeUpEventId: String?
    ): List<OnboardingRoutes> = orderedSteps(goalsFirst).filter { isIncluded(it, flags, shapeUpEventId) }

    fun isIncluded(step: OnboardingRoutes, flags: OnboardingFlowFlags, shapeUpEventId: String?): Boolean {
        if (flags.skipLifestyleQuestions && step in lifestyleSteps) return false
        return when (step) {
            CurrentBodyType, TargetBodyType -> flags.bodyTypeScreensEnabled
            // Only an actual event has a date to ask for; no answer yet counts as no event.
            ShapeUpEventDate -> shapeUpEventRequiresDate(shapeUpEventId)
            NotificationPermission -> flags.showNotificationsPermission
            // Plan creation finishes inline; social proof is the last screen before the paywall.
            PlanReady -> false
            else -> true
        }
    }

    /** iOS `ShapeUpEvent.requiresDate`. */
    fun shapeUpEventRequiresDate(shapeUpEventId: String?): Boolean =
        shapeUpEventId != null && shapeUpEventId != SHAPE_UP_EVENT_NONE

    /**
     * iOS `adjacentStep(from:direction:)`. A fetched flag can exclude the screen that is already
     * being displayed: then continue from the nearest step, in the requested direction, that is still
     * part of the flow.
     */
    fun adjacentStep(
        from: OnboardingRoutes,
        forward: Boolean,
        orderedSteps: List<OnboardingRoutes>,
        navigationSteps: List<OnboardingRoutes>
    ): OnboardingRoutes? {
        val startIndex = navigationSteps.indexOf(from)
        if (startIndex >= 0) {
            return navigationSteps.getOrNull(startIndex + if (forward) 1 else -1)
        }

        val orderedIndex = orderedSteps.indexOf(from)
        if (orderedIndex < 0) return null
        val candidates = if (forward) {
            orderedSteps.drop(orderedIndex + 1)
        } else {
            orderedSteps.take(orderedIndex).asReversed()
        }
        return candidates.firstOrNull { it in navigationSteps }
    }

    /** iOS `topHeaderProgress`: index over (count - 1), so the welcome is 0 and the paywall is 1. */
    fun progress(step: OnboardingRoutes, navigationSteps: List<OnboardingRoutes>): Float {
        val index = navigationSteps.indexOf(step)
        if (index < 0) return 0f
        val total = max(navigationSteps.size - 1, 1).toFloat()
        return (index / total).coerceIn(0f, 1f)
    }

    const val SHAPE_UP_EVENT_NONE = "none"
}
