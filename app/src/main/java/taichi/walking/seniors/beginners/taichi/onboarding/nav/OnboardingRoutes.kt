package taichi.walking.seniors.beginners.taichi.onboarding.nav

/**
 * One onboarding screen: its NavHost route and the step name iOS reports for it
 * (`String(describing: OnboardingStep)`), which analytics and the profile upload's `completedStep`
 * share so both platforms land in one funnel.
 *
 * The ORDER of the screens and which of them a user sees live in [OnboardingSteps], built per user
 * like iOS's `orderedSteps.filter(shouldIncludeStepInNavigation)`; a screen's position is never a
 * fixed number.
 */
sealed class OnboardingRoutes(
    val route: String,
    val analyticsStepName: String
) {

    object Welcome : OnboardingRoutes("welcome", "welcome")
    object Gender : OnboardingRoutes("gender", "gender")
    object CurrentBodyType : OnboardingRoutes("current_body_type", "currentBodyType")
    object Goals : OnboardingRoutes("goals", "goals")
    object TargetBodyType : OnboardingRoutes("target_body_type", "targetBodyType")
    object TargetZones : OnboardingRoutes("target_zones", "targetZones")
    object HeardAboutUs : OnboardingRoutes("heard_about_us", "heardAboutUs")
    object ValuePropOnWay : OnboardingRoutes("value_prop_on_way", "valueOnYourWay")
    object Familiarity : OnboardingRoutes("familiarity", "taiChiFamiliarity")
    object ValuePropMeditation : OnboardingRoutes("value_prop_meditation", "valueMeditationInMotion")
    object ActivityLevel : OnboardingRoutes("activity_level", "activityLevel")
    object WalkDaily : OnboardingRoutes("walk_daily", "dailyWalking")
    object StairsFeeling : OnboardingRoutes("stairs_feeling", "stairsFeeling")
    object SquatAbility : OnboardingRoutes("squat_ability", "squatsAbility")
    object RotateHead : OnboardingRoutes("rotate_head", "canRotateHead")
    object ArmsForward : OnboardingRoutes("arms_forward", "canHoldArms")
    object TaiChiLevel : OnboardingRoutes("tai_chi_level", "taiChiLevel")
    object BetweenMeals : OnboardingRoutes("between_meals", "betweenMeals")
    object SleepAmount : OnboardingRoutes("sleep_amount", "sleepAmount")
    object WaterIntake : OnboardingRoutes("water_intake", "waterIntake")
    object DietType : OnboardingRoutes("diet_type", "dietType")
    object Height : OnboardingRoutes("height", "height")
    object Weight : OnboardingRoutes("weight", "weight")
    object Bmi : OnboardingRoutes("bmi", "bmiFeedback")
    object TargetWeight : OnboardingRoutes("target_weight", "targetWeight")
    object Age : OnboardingRoutes("age", "age")
    object ShapeUpEvent : OnboardingRoutes("shape_up_event", "shapeUpEvent")
    object ShapeUpEventDate : OnboardingRoutes("shape_up_event_date", "shapeUpEventDate")
    object Motivation : OnboardingRoutes("motivation", "motivationLevel")
    object ExerciseBlockers : OnboardingRoutes("exercise_blockers", "exerciseBlockers")
    object Summary : OnboardingRoutes("summary", "personalizedSummary")
    object NotificationPermission : OnboardingRoutes("notification_permission", "notificationPermission")
    object Loading : OnboardingRoutes("loading", "loadingPlan")
    object SocialProof : OnboardingRoutes("social_proof", "socialProof1")
    object PlanReady : OnboardingRoutes("plan_ready", "planReady")
    object Paywall : OnboardingRoutes("paywall", "paywall")

    override fun toString(): String = analyticsStepName

    companion object {
        /** Every destination, in iOS `allCases` order; for looking one up by its NavHost route. */
        val all: List<OnboardingRoutes> by lazy { OnboardingSteps.allCases }

        fun fromRoute(route: String?): OnboardingRoutes? = all.firstOrNull { it.route == route }
    }
}
