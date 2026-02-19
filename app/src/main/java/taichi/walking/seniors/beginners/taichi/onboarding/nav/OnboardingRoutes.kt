package taichi.walking.seniors.beginners.taichi.onboarding.nav

sealed class OnboardingRoutes(val route: String, val index: Int, val total: Int = TOTAL_STEPS) {

    object Welcome : OnboardingRoutes("welcome", 1)
    object Age : OnboardingRoutes("age", 2)
    object Gender : OnboardingRoutes("gender", 3)
    object SocialProof : OnboardingRoutes("social_proof", 4)
    object CommunityValue : OnboardingRoutes("community_value", 5)
    object Goals : OnboardingRoutes("goals", 6)
    object ValuePropOnWay : OnboardingRoutes("value_prop_on_way", 7)
    object Familiarity : OnboardingRoutes("familiarity", 8)
    object ValuePropMeditation : OnboardingRoutes("value_prop_meditation", 9)
    object CurrentBodyType : OnboardingRoutes("current_body_type", 10)
    object TargetBodyType : OnboardingRoutes("target_body_type", 11)
    object TargetZones : OnboardingRoutes("target_zones", 12)
    object ActivityLevel : OnboardingRoutes("activity_level", 13)
    object WalkDaily : OnboardingRoutes("walk_daily", 14)
    object StairsFeeling : OnboardingRoutes("stairs_feeling", 15)
    object SquatAbility : OnboardingRoutes("squat_ability", 16)
    object RotateHead : OnboardingRoutes("rotate_head", 17)
    object ArmsForward : OnboardingRoutes("arms_forward", 18)
    object TaiChiLevel : OnboardingRoutes("tai_chi_level", 19)
    object ValuePropPlanAdjusted : OnboardingRoutes("value_prop_plan_adjusted", 20)
    object BetweenMeals : OnboardingRoutes("between_meals", 21)
    object SleepAmount : OnboardingRoutes("sleep_amount", 22)
    object WaterIntake : OnboardingRoutes("water_intake", 23)
    object DietType : OnboardingRoutes("diet_type", 24)
    object ValuePropAlmostDone : OnboardingRoutes("value_prop_almost_done", 25)
    object Height : OnboardingRoutes("height", 26)
    object Weight : OnboardingRoutes("weight", 27)
    object Bmi : OnboardingRoutes("bmi", 28)
    object TargetWeight : OnboardingRoutes("target_weight", 29)
    object Motivation : OnboardingRoutes("motivation", 30)
    object ExerciseBlockers : OnboardingRoutes("exercise_blockers", 31)
    object Summary : OnboardingRoutes("summary", 32)
    object Loading : OnboardingRoutes("loading", 33)
    object PlanReady : OnboardingRoutes("plan_ready", 34)
    object EmailCapture : OnboardingRoutes("email_capture", 0)
    object ImprovementGraph : OnboardingRoutes("improvement_graph", 35)
    object Paywall : OnboardingRoutes("paywall", 36)

    companion object {
        const val TOTAL_STEPS = 35
        val all: List<OnboardingRoutes> by lazy {
            listOf(
                Welcome,
                Age,
                Gender,
                SocialProof,
                Goals,
                ValuePropOnWay,
                Familiarity,
                ValuePropMeditation,
                CurrentBodyType,
                TargetBodyType,
                TargetZones,
                ActivityLevel,
                WalkDaily,
                StairsFeeling,
                SquatAbility,
                RotateHead,
                ArmsForward,
                TaiChiLevel,
                ValuePropPlanAdjusted,
                BetweenMeals,
                SleepAmount,
                WaterIntake,
                DietType,
                ValuePropAlmostDone,
                Height,
                Weight,
                Bmi,
                TargetWeight,
                Motivation,
                ExerciseBlockers,
                Summary,
                Loading,
                PlanReady,
                ImprovementGraph,
                Paywall
            )
        }

        fun nextOf(route: String): OnboardingRoutes? {
            val index = all.indexOfFirst { it.route == route }
            return if (index >= 0 && index < all.size - 1) all[index + 1] else null
        }
    }
}
