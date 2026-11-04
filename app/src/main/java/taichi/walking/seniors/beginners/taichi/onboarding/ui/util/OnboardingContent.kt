package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingOption

object OnboardingQuestions {
    const val HEARD_ABOUT_US = "heard_about_us"
    const val AGE = "age"
    const val GENDER = "gender"
    const val GOALS = "goals"
    const val FAMILIARITY = "familiarity"
    const val CURRENT_BODY = "current_body"
    const val TARGET_BODY = "target_body"
    const val ZONES = "zones"
    const val ACTIVITY = "activity"
    const val WALK = "walk"
    const val STAIRS = "stairs"
    const val SQUAT = "squat"
    const val ROTATE_HEAD = "rotate_head"
    const val ARMS_FORWARD = "arms_forward"
    const val TAI_CHI_LEVEL = "tai_chi_level"
    const val BETWEEN_MEALS = "between_meals"
    const val SLEEP = "sleep"
    const val WATER = "water"
    const val DIET = "diet"
    const val SHAPE_UP_EVENT = "shape_up_event"
    const val MOTIVATION = "motivation"
    const val BLOCKERS = "blockers"
}

/**
 * Answer lists. The icons mirror iOS's `iconSystemName` per answer and are drawn only with
 * `onboarding_option_icons_enabled`; answers iOS gives no icon (walking time, squats, sleep, water)
 * have none here either.
 */
object OnboardingOptions {
    val gender = listOf(
        OnboardingOption("female", R.string.onb_opt_gender_female, icon = Icons.Default.Woman),
        OnboardingOption("male", R.string.onb_opt_gender_male, icon = Icons.Default.Man),
        OnboardingOption("prefer_not_to_say", R.string.onb_opt_gender_prefer_not_to_say, icon = Icons.Default.Person)
    )

    val goals = listOf(
        OnboardingOption("weight_loss", R.string.onb_opt_goals_weight_loss, icon = Icons.Default.MonitorWeight),
        OnboardingOption("improve_balance", R.string.onb_opt_goals_improve_balance, icon = Icons.Default.SelfImprovement),
        OnboardingOption("reduce_stress", R.string.onb_opt_goals_reduce_stress, icon = Icons.Default.Psychology),
        OnboardingOption("increase_flexibility", R.string.onb_opt_goals_increase_flexibility, icon = Icons.Default.AccessibilityNew),
        OnboardingOption("more_energy", R.string.onb_opt_goals_more_energy, icon = Icons.Default.Bolt),
        OnboardingOption("pain_relief", R.string.onb_opt_goals_pain_relief, icon = Icons.Default.MedicalServices)
    )

    val familiarity = listOf(
        OnboardingOption("never_tried", R.string.onb_opt_familiarity_never_tried, icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("tried_once", R.string.onb_opt_familiarity_tried_once, icon = Icons.Default.LooksOne),
        OnboardingOption("practice_occasionally", R.string.onb_opt_familiarity_practice_occasionally, icon = Icons.Default.LooksTwo),
        OnboardingOption("regular_practice", R.string.onb_opt_familiarity_regular_practice, icon = Icons.Default.Looks3)
    )

    private val femaleCurrentBodyTypes = listOf(
        OnboardingOption("regular", R.string.onb_opt_female_current_body_types_regular, imageRes = R.drawable.current_female_regular),
        OnboardingOption("rounded", R.string.onb_opt_female_current_body_types_rounded, imageRes = R.drawable.current_female_rounded),
        OnboardingOption("full", R.string.onb_opt_female_current_body_types_full, imageRes = R.drawable.current_female_full)
    )

    private val maleCurrentBodyTypes = listOf(
        OnboardingOption("skinny", R.string.onb_opt_male_current_body_types_skinny, imageRes = R.drawable.current_male_skinny),
        OnboardingOption("average", R.string.onb_opt_male_current_body_types_average, imageRes = R.drawable.current_male_average),
        OnboardingOption("heavyset", R.string.onb_opt_male_current_body_types_heavyset, imageRes = R.drawable.current_male_heavyset)
    )

    private val femaleTargetBodyTypes = listOf(
        OnboardingOption("regular", R.string.onb_opt_female_target_body_types_regular, imageRes = R.drawable.target_female_regular),
        OnboardingOption("fit", R.string.onb_opt_female_target_body_types_fit, imageRes = R.drawable.target_female_fit),
        OnboardingOption("athletic", R.string.onb_opt_female_target_body_types_athletic, imageRes = R.drawable.target_female_athletic)
    )

    private val maleTargetBodyTypes = listOf(
        OnboardingOption("fit", R.string.onb_opt_male_target_body_types_fit, imageRes = R.drawable.target_male_fit),
        OnboardingOption("bulk", R.string.onb_opt_male_target_body_types_bulk, imageRes = R.drawable.target_male_bulk),
        OnboardingOption("extrabulk", R.string.onb_opt_male_target_body_types_extrabulk, imageRes = R.drawable.target_male_extrabulk)
    )

    val zones = listOf(
        OnboardingOption("arms", R.string.onb_opt_zones_arms, icon = Icons.Default.FitnessCenter),
        OnboardingOption("legs", R.string.onb_opt_zones_legs, icon = Icons.Default.DirectionsWalk),
        OnboardingOption("core", R.string.onb_opt_zones_core, icon = Icons.Default.AccessibilityNew),
        OnboardingOption("back", R.string.onb_opt_zones_back, icon = Icons.Default.Person),
        OnboardingOption("shoulders", R.string.onb_opt_zones_shoulders, icon = Icons.Default.SelfImprovement),
        OnboardingOption("hips", R.string.onb_opt_zones_hips, icon = Icons.Default.Spa)
    )

    val activity = listOf(
        OnboardingOption("sedentary", R.string.onb_opt_activity_sedentary, icon = Icons.Default.Chair),
        OnboardingOption("lightly_active", R.string.onb_opt_activity_lightly_active, icon = Icons.Default.DirectionsWalk),
        OnboardingOption("moderately_active", R.string.onb_opt_activity_moderately_active, icon = Icons.Default.SelfImprovement),
        OnboardingOption("very_active", R.string.onb_opt_activity_very_active, icon = Icons.Default.DirectionsRun)
    )

    val walkDaily = listOf(
        OnboardingOption("less_than_10", R.string.onb_opt_walk_daily_less_than_10),
        OnboardingOption("ten_to_30", R.string.onb_opt_walk_daily_ten_to_30),
        OnboardingOption("thirty_to_60", R.string.onb_opt_walk_daily_thirty_to_60),
        OnboardingOption("more_than_60", R.string.onb_opt_walk_daily_more_than_60)
    )

    val stairs = listOf(
        OnboardingOption("easy", R.string.onb_opt_stairs_easy, icon = Icons.Default.CheckCircleOutline),
        OnboardingOption("slightly_tired", R.string.onb_opt_stairs_slightly_tired, icon = Icons.Default.DirectionsWalk),
        OnboardingOption("need_to_rest", R.string.onb_opt_stairs_need_to_rest, icon = Icons.Default.PauseCircleOutline),
        OnboardingOption("avoid_stairs", R.string.onb_opt_stairs_avoid_stairs, icon = Icons.Default.HighlightOff)
    )

    val squat = listOf(
        OnboardingOption("none", R.string.onb_opt_squat_none),
        OnboardingOption("one_to_five", R.string.onb_opt_squat_one_to_five),
        OnboardingOption("six_to_ten", R.string.onb_opt_squat_six_to_ten),
        OnboardingOption("more_than_ten", R.string.onb_opt_squat_more_than_ten)
    )

    val yesNo = listOf(
        OnboardingOption("yes", R.string.onb_opt_yes_no_yes),
        OnboardingOption("no", R.string.onb_opt_yes_no_no)
    )

    val taiChiLevel = listOf(
        OnboardingOption("complete", R.string.onb_opt_tai_chi_level_complete, R.string.onb_opt_tai_chi_level_complete_subtitle, icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("beginner", R.string.onb_opt_tai_chi_level_beginner, R.string.onb_opt_tai_chi_level_beginner_subtitle, icon = Icons.Default.LooksOne),
        OnboardingOption("intermediate", R.string.onb_opt_tai_chi_level_intermediate, R.string.onb_opt_tai_chi_level_intermediate_subtitle, icon = Icons.Default.LooksTwo),
        OnboardingOption("advanced", R.string.onb_opt_tai_chi_level_advanced, R.string.onb_opt_tai_chi_level_advanced_subtitle, icon = Icons.Default.Looks3)
    )

    val betweenMeals = listOf(
        OnboardingOption("energetic", R.string.onb_opt_between_meals_energetic, icon = Icons.Default.Bolt),
        OnboardingOption("normal", R.string.onb_opt_between_meals_normal, icon = Icons.Default.Person),
        OnboardingOption("tired", R.string.onb_opt_between_meals_tired, icon = Icons.Default.Bedtime),
        OnboardingOption("very_tired", R.string.onb_opt_between_meals_very_tired, icon = Icons.Default.Bedtime)
    )

    val sleep = listOf(
        OnboardingOption("less_than_5", R.string.onb_opt_sleep_less_than_5),
        OnboardingOption("five_to_six", R.string.onb_opt_sleep_five_to_six),
        OnboardingOption("seven_to_eight", R.string.onb_opt_sleep_seven_to_eight),
        OnboardingOption("more_than_8", R.string.onb_opt_sleep_more_than_8)
    )

    val water = listOf(
        OnboardingOption("less_than_2", R.string.onb_opt_water_less_than_2),
        OnboardingOption("two_to_four", R.string.onb_opt_water_two_to_four),
        OnboardingOption("five_to_seven", R.string.onb_opt_water_five_to_seven),
        OnboardingOption("eight_plus", R.string.onb_opt_water_eight_plus)
    )

    val diet = listOf(
        OnboardingOption("regular", R.string.onb_opt_diet_regular, icon = Icons.Default.Restaurant),
        OnboardingOption("vegetarian", R.string.onb_opt_diet_vegetarian, icon = Icons.Default.Eco),
        OnboardingOption("vegan", R.string.onb_opt_diet_vegan, icon = Icons.Default.Grass),
        OnboardingOption("keto", R.string.onb_opt_diet_keto, icon = Icons.Default.LocalFireDepartment),
        OnboardingOption("mediterranean", R.string.onb_opt_diet_mediterranean, icon = Icons.Default.SetMeal),
        OnboardingOption("gluten_free", R.string.onb_opt_diet_gluten_free, icon = Icons.Default.Verified)
    )

    val shapeUpEvents = listOf(
        OnboardingOption("none", R.string.onb_opt_shape_up_events_none, icon = Icons.Default.Block),
        OnboardingOption("vacation", R.string.onb_opt_shape_up_events_vacation, icon = Icons.Default.Flight),
        OnboardingOption("wedding", R.string.onb_opt_shape_up_events_wedding, icon = Icons.Default.FavoriteBorder),
        OnboardingOption("reunion", R.string.onb_opt_shape_up_events_reunion, icon = Icons.Default.People),
        OnboardingOption("family_reunion", R.string.onb_opt_shape_up_events_family_reunion, icon = Icons.Default.Groups),
        OnboardingOption("birthday", R.string.onb_opt_shape_up_events_birthday, icon = Icons.Default.Cake),
        OnboardingOption("beach_trip", R.string.onb_opt_shape_up_events_beach_trip, icon = Icons.Default.WbSunny),
        OnboardingOption("adventure_trip", R.string.onb_opt_shape_up_events_adventure_trip, icon = Icons.Default.Map),
        OnboardingOption("sporting_event", R.string.onb_opt_shape_up_events_sporting_event, icon = Icons.Default.DirectionsRun),
        OnboardingOption("important_date", R.string.onb_opt_shape_up_events_important_date, icon = Icons.Default.Event),
        OnboardingOption("other", R.string.onb_opt_shape_up_events_other, icon = Icons.Default.MoreHoriz)
    )

    val motivation = listOf(
        OnboardingOption("very_motivated", R.string.onb_opt_motivation_very_motivated, R.string.onb_opt_motivation_very_motivated_subtitle, icon = Icons.Default.LocalFireDepartment),
        OnboardingOption("motivated", R.string.onb_opt_motivation_motivated, R.string.onb_opt_motivation_motivated_subtitle, icon = Icons.Default.Bolt),
        OnboardingOption("need_help", R.string.onb_opt_motivation_need_help, R.string.onb_opt_motivation_need_help_subtitle, icon = Icons.Default.PanTool),
        OnboardingOption("struggling", R.string.onb_opt_motivation_struggling, R.string.onb_opt_motivation_struggling_subtitle, icon = Icons.Default.Cloud)
    )

    val blockers = listOf(
        OnboardingOption("no_time", R.string.onb_opt_blockers_no_time, icon = Icons.Default.Schedule),
        OnboardingOption("too_tired", R.string.onb_opt_blockers_too_tired, icon = Icons.Default.Bedtime),
        OnboardingOption("pain_discomfort", R.string.onb_opt_blockers_pain_discomfort, icon = Icons.Default.MedicalServices),
        OnboardingOption("lack_motivation", R.string.onb_opt_blockers_lack_motivation, icon = Icons.Default.BatteryAlert),
        OnboardingOption("dont_know_how", R.string.onb_opt_blockers_dont_know_how, icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("no_equipment", R.string.onb_opt_blockers_no_equipment, icon = Icons.Default.Inventory2)
    )

    fun currentBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleCurrentBodyTypes else femaleCurrentBodyTypes
    }

    fun targetBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleTargetBodyTypes else femaleTargetBodyTypes
    }
}
