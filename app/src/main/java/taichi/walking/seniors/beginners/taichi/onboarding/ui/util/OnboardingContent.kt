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
        OnboardingOption("female", "Female", icon = Icons.Default.Woman),
        OnboardingOption("male", "Male", icon = Icons.Default.Man),
        OnboardingOption("prefer_not_to_say", "Prefer not to say", icon = Icons.Default.Person)
    )

    val goals = listOf(
        OnboardingOption("weight_loss", "Weight Loss", icon = Icons.Default.MonitorWeight),
        OnboardingOption("improve_balance", "Improve Balance", icon = Icons.Default.SelfImprovement),
        OnboardingOption("reduce_stress", "Reduce Stress", icon = Icons.Default.Psychology),
        OnboardingOption("increase_flexibility", "Increase Flexibility", icon = Icons.Default.AccessibilityNew),
        OnboardingOption("more_energy", "More Energy", icon = Icons.Default.Bolt),
        OnboardingOption("pain_relief", "Pain Relief", icon = Icons.Default.MedicalServices)
    )

    val familiarity = listOf(
        OnboardingOption("never_tried", "Never tried it", icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("tried_once", "Tried once or twice", icon = Icons.Default.LooksOne),
        OnboardingOption("practice_occasionally", "Practice occasionally", icon = Icons.Default.LooksTwo),
        OnboardingOption("regular_practice", "Regular practice", icon = Icons.Default.Looks3)
    )

    private val femaleCurrentBodyTypes = listOf(
        OnboardingOption("regular", "Regular", imageRes = R.drawable.current_female_regular),
        OnboardingOption("rounded", "Rounded", imageRes = R.drawable.current_female_rounded),
        OnboardingOption("full", "Full", imageRes = R.drawable.current_female_full)
    )

    private val maleCurrentBodyTypes = listOf(
        OnboardingOption("skinny", "Skinny", imageRes = R.drawable.current_male_skinny),
        OnboardingOption("average", "Average", imageRes = R.drawable.current_male_average),
        OnboardingOption("heavyset", "Heavyset", imageRes = R.drawable.current_male_heavyset)
    )

    private val femaleTargetBodyTypes = listOf(
        OnboardingOption("regular", "Regular", imageRes = R.drawable.target_female_regular),
        OnboardingOption("fit", "Fit", imageRes = R.drawable.target_female_fit),
        OnboardingOption("athletic", "Athletic", imageRes = R.drawable.target_female_athletic)
    )

    private val maleTargetBodyTypes = listOf(
        OnboardingOption("fit", "Fit", imageRes = R.drawable.target_male_fit),
        OnboardingOption("bulk", "Bulk", imageRes = R.drawable.target_male_bulk),
        OnboardingOption("extrabulk", "Extrabulk", imageRes = R.drawable.target_male_extrabulk)
    )

    val zones = listOf(
        OnboardingOption("arms", "Arms", icon = Icons.Default.FitnessCenter),
        OnboardingOption("legs", "Legs", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("core", "Core", icon = Icons.Default.AccessibilityNew),
        OnboardingOption("back", "Back", icon = Icons.Default.Person),
        OnboardingOption("shoulders", "Shoulders", icon = Icons.Default.SelfImprovement),
        OnboardingOption("hips", "Hips", icon = Icons.Default.Spa)
    )

    val activity = listOf(
        OnboardingOption("sedentary", "Mostly sitting", icon = Icons.Default.Chair),
        OnboardingOption("lightly_active", "Light activity", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("moderately_active", "Moderately active", icon = Icons.Default.SelfImprovement),
        OnboardingOption("very_active", "Very active", icon = Icons.Default.DirectionsRun)
    )

    val walkDaily = listOf(
        OnboardingOption("less_than_10", "Less than 10 min"),
        OnboardingOption("ten_to_30", "10 - 30 minutes"),
        OnboardingOption("thirty_to_60", "30 - 60 minutes"),
        OnboardingOption("more_than_60", "More than 1 hour")
    )

    val stairs = listOf(
        OnboardingOption("easy", "Easy, no problem", icon = Icons.Default.CheckCircleOutline),
        OnboardingOption("slightly_tired", "Slightly tired", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("need_to_rest", "Need to rest", icon = Icons.Default.PauseCircleOutline),
        OnboardingOption("avoid_stairs", "I avoid stairs", icon = Icons.Default.HighlightOff)
    )

    val squat = listOf(
        OnboardingOption("none", "0 squats"),
        OnboardingOption("one_to_five", "1 - 5 squats"),
        OnboardingOption("six_to_ten", "6 - 10 squats"),
        OnboardingOption("more_than_ten", "More than 10")
    )

    val yesNo = listOf(
        OnboardingOption("yes", "Yes"),
        OnboardingOption("no", "No")
    )

    val taiChiLevel = listOf(
        OnboardingOption("complete", "Complete beginner", "Never practiced before", icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("beginner", "Beginner", "Know a few basic moves", icon = Icons.Default.LooksOne),
        OnboardingOption("intermediate", "Intermediate", "Comfortable with forms", icon = Icons.Default.LooksTwo),
        OnboardingOption("advanced", "Advanced", "Years of experience", icon = Icons.Default.Looks3)
    )

    val betweenMeals = listOf(
        OnboardingOption("energetic", "Energetic", icon = Icons.Default.Bolt),
        OnboardingOption("normal", "Normal", icon = Icons.Default.Person),
        OnboardingOption("tired", "Tired", icon = Icons.Default.Bedtime),
        OnboardingOption("very_tired", "Very tired", icon = Icons.Default.Bedtime)
    )

    val sleep = listOf(
        OnboardingOption("less_than_5", "Less than 5 hours"),
        OnboardingOption("five_to_six", "5 - 6 hours"),
        OnboardingOption("seven_to_eight", "7 - 8 hours"),
        OnboardingOption("more_than_8", "More than 8 hours")
    )

    val water = listOf(
        OnboardingOption("less_than_2", "Less than 2 glasses"),
        OnboardingOption("two_to_four", "2 - 4 glasses"),
        OnboardingOption("five_to_seven", "5 - 7 glasses"),
        OnboardingOption("eight_plus", "8+ glasses")
    )

    val diet = listOf(
        OnboardingOption("regular", "Regular", icon = Icons.Default.Restaurant),
        OnboardingOption("vegetarian", "Vegetarian", icon = Icons.Default.Eco),
        OnboardingOption("vegan", "Vegan", icon = Icons.Default.Grass),
        OnboardingOption("keto", "Keto", icon = Icons.Default.LocalFireDepartment),
        OnboardingOption("mediterranean", "Mediterranean", icon = Icons.Default.SetMeal),
        OnboardingOption("gluten_free", "Gluten-free", icon = Icons.Default.Verified)
    )

    val shapeUpEvents = listOf(
        OnboardingOption("none", "No", icon = Icons.Default.Block),
        OnboardingOption("vacation", "Vacation", icon = Icons.Default.Flight),
        OnboardingOption("wedding", "Wedding", icon = Icons.Default.FavoriteBorder),
        OnboardingOption("reunion", "Reunion", icon = Icons.Default.People),
        OnboardingOption("family_reunion", "Family Reunion", icon = Icons.Default.Groups),
        OnboardingOption("birthday", "Birthday", icon = Icons.Default.Cake),
        OnboardingOption("beach_trip", "Beach trip", icon = Icons.Default.WbSunny),
        OnboardingOption("adventure_trip", "Adventure trip", icon = Icons.Default.Map),
        OnboardingOption("sporting_event", "Sporting event", icon = Icons.Default.DirectionsRun),
        OnboardingOption("important_date", "Important date", icon = Icons.Default.Event),
        OnboardingOption("other", "Other", icon = Icons.Default.MoreHoriz)
    )

    val motivation = listOf(
        OnboardingOption("very_motivated", "Very motivated", "Ready to start today!", icon = Icons.Default.LocalFireDepartment),
        OnboardingOption("motivated", "Motivated", "Looking forward to it", icon = Icons.Default.Bolt),
        OnboardingOption("need_help", "Need some help", "Could use encouragement", icon = Icons.Default.PanTool),
        OnboardingOption("struggling", "Struggling", "Finding it hard to begin", icon = Icons.Default.Cloud)
    )

    val blockers = listOf(
        OnboardingOption("no_time", "Not enough time", icon = Icons.Default.Schedule),
        OnboardingOption("too_tired", "Too tired", icon = Icons.Default.Bedtime),
        OnboardingOption("pain_discomfort", "Pain or discomfort", icon = Icons.Default.MedicalServices),
        OnboardingOption("lack_motivation", "Lack of motivation", icon = Icons.Default.BatteryAlert),
        OnboardingOption("dont_know_how", "Don't know how", icon = Icons.AutoMirrored.Filled.HelpOutline),
        OnboardingOption("no_equipment", "No equipment", icon = Icons.Default.Inventory2)
    )

    fun currentBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleCurrentBodyTypes else femaleCurrentBodyTypes
    }

    fun targetBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleTargetBodyTypes else femaleTargetBodyTypes
    }
}
