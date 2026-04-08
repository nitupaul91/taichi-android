package taichi.walking.seniors.beginners.taichi.onboarding.nav

sealed class OnboardingRoutes(
    val route: String,
    val index: Int,
    val analyticsStepName: String,
    val total: Int = TOTAL_STEPS
) {

    object Welcome : OnboardingRoutes("welcome", 0, "welcome")
    object Gender : OnboardingRoutes("gender", 1, "gender")
    object CurrentBodyType : OnboardingRoutes("current_body_type", 2, "currentBodyType")
    object Goals : OnboardingRoutes("goals", 3, "goals")
    object TargetBodyType : OnboardingRoutes("target_body_type", 4, "targetBodyType")
    object TargetZones : OnboardingRoutes("target_zones", 5, "targetZones")
    object HeardAboutUs : OnboardingRoutes("heard_about_us", 6, "heardAboutUs")
    object ValuePropOnWay : OnboardingRoutes("value_prop_on_way", 7, "valueOnYourWay")
    object Familiarity : OnboardingRoutes("familiarity", 8, "taiChiFamiliarity")
    object ValuePropMeditation : OnboardingRoutes("value_prop_meditation", 9, "valueMeditationInMotion")
    object ActivityLevel : OnboardingRoutes("activity_level", 10, "activityLevel")
    object WalkDaily : OnboardingRoutes("walk_daily", 11, "dailyWalking")
    object StairsFeeling : OnboardingRoutes("stairs_feeling", 12, "stairsFeeling")
    object SquatAbility : OnboardingRoutes("squat_ability", 13, "squatsAbility")
    object RotateHead : OnboardingRoutes("rotate_head", 14, "canRotateHead")
    object ArmsForward : OnboardingRoutes("arms_forward", 15, "canHoldArms")
    object TaiChiLevel : OnboardingRoutes("tai_chi_level", 16, "taiChiLevel")
    object BetweenMeals : OnboardingRoutes("between_meals", 17, "betweenMeals")
    object SleepAmount : OnboardingRoutes("sleep_amount", 18, "sleepAmount")
    object WaterIntake : OnboardingRoutes("water_intake", 19, "waterIntake")
    object DietType : OnboardingRoutes("diet_type", 20, "dietType")
    object Height : OnboardingRoutes("height", 21, "height")
    object Weight : OnboardingRoutes("weight", 22, "weight")
    object Bmi : OnboardingRoutes("bmi", 23, "bmiFeedback")
    object TargetWeight : OnboardingRoutes("target_weight", 24, "targetWeight")
    object Age : OnboardingRoutes("age", 25, "age")
    object ShapeUpEvent : OnboardingRoutes("shape_up_event", 26, "shapeUpEvent")
    object ShapeUpEventDate : OnboardingRoutes("shape_up_event_date", 27, "shapeUpEventDate")
    object Motivation : OnboardingRoutes("motivation", 28, "motivationLevel")
    object ExerciseBlockers : OnboardingRoutes("exercise_blockers", 29, "exerciseBlockers")
    object Summary : OnboardingRoutes("summary", 30, "personalizedSummary")
    object NotificationPermission : OnboardingRoutes("notification_permission", 31, "notificationPermission")
    object SocialProof : OnboardingRoutes("social_proof", 32, "socialProof1")
    object Loading : OnboardingRoutes("loading", 33, "loadingPlan")
    object PlanReady : OnboardingRoutes("plan_ready", 34, "planReady")
    object Paywall : OnboardingRoutes("paywall", 35, "paywall")

    companion object {
        const val TOTAL_STEPS = 35
        val all: List<OnboardingRoutes> by lazy {
            listOf(
                Welcome,
                Gender,
                CurrentBodyType,
                Goals,
                TargetBodyType,
                TargetZones,
                HeardAboutUs,
                ValuePropOnWay,
                Familiarity,
                ValuePropMeditation,
                ActivityLevel,
                WalkDaily,
                StairsFeeling,
                SquatAbility,
                RotateHead,
                ArmsForward,
                TaiChiLevel,
                BetweenMeals,
                SleepAmount,
                WaterIntake,
                DietType,
                Height,
                Weight,
                Bmi,
                TargetWeight,
                Age,
                ShapeUpEvent,
                ShapeUpEventDate,
                Motivation,
                ExerciseBlockers,
                Summary,
                NotificationPermission,
                SocialProof,
                Loading,
                PlanReady,
                Paywall
            )
        }

        fun nextOf(route: String, shapeUpEventId: String?): OnboardingRoutes? {
            val index = all.indexOfFirst { it.route == route }
            if (index < 0 || index >= all.size - 1) return null

            val next = all[index + 1]
            if (route == ShapeUpEvent.route && shapeUpEventId == "none") {
                return Motivation
            }
            return next
        }
    }
}
