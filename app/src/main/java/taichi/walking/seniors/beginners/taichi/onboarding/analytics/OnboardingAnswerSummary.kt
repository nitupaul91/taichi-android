package taichi.walking.seniors.beginners.taichi.onboarding.analytics

import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Analytics-only snapshot of the answer stored for a step (iOS `OnboardingFlowModel.answerSummary`):
 * the answer ids ("none" when unanswered, multi-select sorted and comma-joined), how many were
 * picked, and whether a picker was left on the value it opened with. Never free text or PII.
 *
 * Answers are the Android option ids, the same values the profile upload sends.
 */
data class OnboardingAnswerSummary(
    val answer: String,
    val count: Int,
    val isDefault: Boolean
) {
    companion object {

        /** Null for informational screens that collect nothing. */
        fun of(
            step: OnboardingRoutes,
            state: OnboardingState,
            nowMillis: Long = System.currentTimeMillis()
        ): OnboardingAnswerSummary? = when (step) {
            OnboardingRoutes.ProgramSelection -> single(state.programSelection)
            OnboardingRoutes.HeardAboutUs -> single(state.discoverySourceId)
            // The Android age picker always holds a value; it opens on the state default.
            OnboardingRoutes.Age -> OnboardingAnswerSummary(
                state.actualAge.toString(),
                1,
                state.actualAge == DEFAULT_STATE.actualAge
            )
            OnboardingRoutes.Gender -> single(state.genderId)
            OnboardingRoutes.Goals -> multi(state.goals)
            OnboardingRoutes.Familiarity -> single(state.taiChiFamiliarityId)
            OnboardingRoutes.CurrentBodyType -> single(state.currentBodyTypeId)
            OnboardingRoutes.TargetBodyType -> single(state.targetBodyTypeId)
            OnboardingRoutes.TargetZones -> multi(state.targetZones)
            OnboardingRoutes.ActivityLevel -> single(state.activityLevelId)
            OnboardingRoutes.WalkDaily -> single(state.walkDailyId)
            OnboardingRoutes.StairsFeeling -> single(state.stairsFeelingId)
            OnboardingRoutes.SquatAbility -> single(state.squatAbilityId)
            OnboardingRoutes.RotateHead -> single(state.rotateHeadId)
            OnboardingRoutes.ArmsForward -> single(state.armsForwardId)
            OnboardingRoutes.TaiChiLevel -> single(state.taiChiLevelId)
            OnboardingRoutes.BetweenMeals -> single(state.betweenMealsId)
            OnboardingRoutes.SleepAmount -> single(state.sleepAmountId)
            OnboardingRoutes.WaterIntake -> single(state.waterIntakeId)
            OnboardingRoutes.DietType -> multi(state.dietTypes)
            OnboardingRoutes.Height -> OnboardingAnswerSummary(
                state.heightCm.toString(),
                1,
                state.heightCm == defaultHeightCm(state.genderId)
            )
            // Weights are reported as decimals, as iOS (and the upload) carry them.
            OnboardingRoutes.Weight -> OnboardingAnswerSummary(
                state.weightKg.toDouble().toString(),
                1,
                state.weightKg == defaultWeightKg(state.genderId)
            )
            OnboardingRoutes.TargetWeight -> OnboardingAnswerSummary(
                state.targetWeightKg.toDouble().toString(),
                1,
                state.targetWeightKg == autoTargetWeight(state.weightKg)
            )
            OnboardingRoutes.ShapeUpEvent -> single(state.shapeUpEventId)
            OnboardingRoutes.ShapeUpEventDate -> if (state.shapeUpEventDateSkipped) {
                OnboardingAnswerSummary("skipped", 0, false)
            } else {
                OnboardingAnswerSummary(
                    dayFormat().format(Date(state.shapeUpEventDateMillis)),
                    1,
                    isSameDay(state.shapeUpEventDateMillis, nowMillis + DEFAULT_EVENT_OFFSET_MILLIS)
                )
            }
            OnboardingRoutes.Motivation -> single(state.motivationLevelId)
            OnboardingRoutes.ExerciseBlockers -> multi(state.exerciseBlockers)
            OnboardingRoutes.Welcome,
            OnboardingRoutes.ValuePropOnWay,
            OnboardingRoutes.ValuePropMeditation,
            OnboardingRoutes.Bmi,
            OnboardingRoutes.Summary,
            OnboardingRoutes.NotificationPermission,
            OnboardingRoutes.Loading,
            OnboardingRoutes.SocialProof,
            OnboardingRoutes.PlanReady,
            OnboardingRoutes.Paywall -> null
        }

        private val DEFAULT_STATE = OnboardingState()

        /** The event date picker opens 8 weeks out (OnboardingState default). */
        private const val DEFAULT_EVENT_OFFSET_MILLIS = 56L * 24L * 60L * 60L * 1000L

        private fun single(id: String?) = OnboardingAnswerSummary(id ?: "none", if (id == null) 0 else 1, false)

        private fun multi(ids: Set<String>): OnboardingAnswerSummary {
            val sorted = ids.sorted()
            return OnboardingAnswerSummary(
                if (sorted.isEmpty()) "none" else sorted.joinToString(","),
                sorted.size,
                false
            )
        }

        /** The value the height picker was pre-populated with (gender defaults, else the state default). */
        private fun defaultHeightCm(genderId: String?): Int = when (genderId) {
            "female" -> 163
            "male" -> 172
            else -> DEFAULT_STATE.heightCm
        }

        /** The value the weight picker was pre-populated with (gender defaults, else the state default). */
        private fun defaultWeightKg(genderId: String?): Int = when (genderId) {
            "female" -> 75
            "male" -> 85
            else -> DEFAULT_STATE.weightKg
        }

        /** OnboardingViewModel.autoTargetWeight. */
        private fun autoTargetWeight(weightKg: Int): Int = (weightKg * 0.95f).toInt()

        private fun dayFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        private fun isSameDay(a: Long, b: Long): Boolean {
            val first = Calendar.getInstance().apply { timeInMillis = a }
            val second = Calendar.getInstance().apply { timeInMillis = b }
            return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
                first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
        }
    }
}
