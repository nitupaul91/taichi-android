package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalDrink
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

object OnboardingOptions {
    val discoverySources = listOf(
        OnboardingOption("google", "Google"),
        OnboardingOption("tv_commercial", "TV Commercial", icon = Icons.Default.LiveTv),
        OnboardingOption("trainer_therapist_recommendation", "Recommended by Trainer / Therapist", icon = Icons.Default.MedicalServices),
        OnboardingOption("tiktok", "TikTok"),
        OnboardingOption("instagram", "Instagram"),
        OnboardingOption("facebook", "Facebook"),
        OnboardingOption("friends_family", "Friends / Family"),
        OnboardingOption("other", "Other")
    )

    val gender = listOf(
        OnboardingOption("female", "Female"),
        OnboardingOption("male", "Male"),
        OnboardingOption("prefer_not_to_say", "Prefer not to say")
    )

    val goals = listOf(
        OnboardingOption("weight_loss", "Weight Loss", icon = Icons.Default.FitnessCenter),
        OnboardingOption("improve_balance", "Improve Balance", icon = Icons.Default.AccessibilityNew),
        OnboardingOption("reduce_stress", "Reduce Stress", icon = Icons.Default.Psychology),
        OnboardingOption("increase_flexibility", "Increase Flexibility", icon = Icons.Default.SelfImprovement),
        OnboardingOption("more_energy", "More Energy", icon = Icons.Default.Bolt),
        OnboardingOption("pain_relief", "Pain Relief", icon = Icons.Default.Healing)
    )

    val familiarity = listOf(
        OnboardingOption("never_tried", "Never tried it"),
        OnboardingOption("tried_once", "Tried once or twice"),
        OnboardingOption("practice_occasionally", "Practice occasionally"),
        OnboardingOption("regular_practice", "Regular practice")
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
        OnboardingOption("sedentary", "Mostly sitting", "Desk work, minimal movement", icon = Icons.Default.Chair),
        OnboardingOption("lightly_active", "Light activity", "Some walking, light tasks", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("moderately_active", "Moderately active", "Regular walks, active hobbies", icon = Icons.Default.SelfImprovement),
        OnboardingOption("very_active", "Very active", "Exercise most days", icon = Icons.Default.DirectionsRun)
    )

    val walkDaily = listOf(
        OnboardingOption("less_than_10", "Less than 10 min", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("ten_to_30", "10 - 30 minutes", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("thirty_to_60", "30 - 60 minutes", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("more_than_60", "More than 1 hour", icon = Icons.Default.DirectionsWalk)
    )

    val stairs = listOf(
        OnboardingOption("easy", "Easy, no problem"),
        OnboardingOption("slightly_tired", "Slightly tired"),
        OnboardingOption("need_to_rest", "Need to rest"),
        OnboardingOption("avoid_stairs", "I avoid stairs")
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
        OnboardingOption("complete", "Complete beginner", "Never practiced before"),
        OnboardingOption("beginner", "Beginner", "Know a few basic moves"),
        OnboardingOption("intermediate", "Intermediate", "Comfortable with forms"),
        OnboardingOption("advanced", "Advanced", "Years of experience")
    )

    val betweenMeals = listOf(
        OnboardingOption("energetic", "Energetic", icon = Icons.Default.Bolt),
        OnboardingOption("normal", "Normal", icon = Icons.Default.Person),
        OnboardingOption("tired", "Tired", icon = Icons.Default.Bedtime),
        OnboardingOption("very_tired", "Very tired", icon = Icons.Default.Bedtime)
    )

    val sleep = listOf(
        OnboardingOption("less_than_5", "Less than 5 hours", icon = Icons.Default.Bedtime),
        OnboardingOption("five_to_six", "5 - 6 hours", icon = Icons.Default.Bedtime),
        OnboardingOption("seven_to_eight", "7 - 8 hours", icon = Icons.Default.Bedtime),
        OnboardingOption("more_than_8", "More than 8 hours", icon = Icons.Default.Bedtime)
    )

    val water = listOf(
        OnboardingOption("less_than_2", "Less than 2 glasses", icon = Icons.Default.LocalDrink),
        OnboardingOption("two_to_four", "2 - 4 glasses", icon = Icons.Default.LocalDrink),
        OnboardingOption("five_to_seven", "5 - 7 glasses", icon = Icons.Default.LocalDrink),
        OnboardingOption("eight_plus", "8+ glasses", icon = Icons.Default.LocalDrink)
    )

    val diet = listOf(
        OnboardingOption("regular", "Regular"),
        OnboardingOption("vegetarian", "Vegetarian"),
        OnboardingOption("vegan", "Vegan"),
        OnboardingOption("keto", "Keto"),
        OnboardingOption("mediterranean", "Mediterranean"),
        OnboardingOption("gluten_free", "Gluten-free")
    )

    val shapeUpEvents = listOf(
        OnboardingOption("none", "No"),
        OnboardingOption("vacation", "Vacation"),
        OnboardingOption("wedding", "Wedding"),
        OnboardingOption("reunion", "Reunion"),
        OnboardingOption("family_reunion", "Family Reunion"),
        OnboardingOption("birthday", "Birthday"),
        OnboardingOption("beach_trip", "Beach trip"),
        OnboardingOption("adventure_trip", "Adventure trip"),
        OnboardingOption("sporting_event", "Sporting event"),
        OnboardingOption("important_date", "Important date"),
        OnboardingOption("other", "Other")
    )

    val motivation = listOf(
        OnboardingOption("very_motivated", "Very motivated", "Ready to start today!", icon = Icons.Default.Bolt),
        OnboardingOption("motivated", "Motivated", "Looking forward to it", icon = Icons.Default.SelfImprovement),
        OnboardingOption("need_help", "Need some help", "Could use encouragement", icon = Icons.Default.Psychology),
        OnboardingOption("struggling", "Struggling", "Finding it hard to begin", icon = Icons.Default.Spa)
    )

    val blockers = listOf(
        OnboardingOption("no_time", "Not enough time"),
        OnboardingOption("too_tired", "Too tired"),
        OnboardingOption("pain_discomfort", "Pain or discomfort"),
        OnboardingOption("lack_motivation", "Lack of motivation"),
        OnboardingOption("dont_know_how", "Don't know how"),
        OnboardingOption("no_equipment", "No equipment")
    )

    fun currentBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleCurrentBodyTypes else femaleCurrentBodyTypes
    }

    fun targetBodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleTargetBodyTypes else femaleTargetBodyTypes
    }
}
