package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingOption
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa

object OnboardingQuestions {
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
    const val MOTIVATION = "motivation"
    const val BLOCKERS = "blockers"
}

object OnboardingOptions {
    val ageRanges = listOf(
        OnboardingOption("under_45", "Under 45"),
        OnboardingOption("45_54", "45–54"),
        OnboardingOption("55_64", "55–64"),
        OnboardingOption("65_74", "65–74"),
        OnboardingOption("75_plus", "75+")
    )

    val gender = listOf(
        OnboardingOption("female", "Female"),
        OnboardingOption("male", "Male"),
        OnboardingOption("no_say", "Prefer not to say")
    )

    val goals = listOf(
        OnboardingOption("balance", "Improve balance", icon = Icons.Default.AccessibilityNew),
        OnboardingOption("stress", "Reduce stress", icon = Icons.Default.Psychology),
        OnboardingOption("flexibility", "Increase flexibility", icon = Icons.Default.SelfImprovement),
        OnboardingOption("energy", "More energy", icon = Icons.Default.Bolt),
        OnboardingOption("pain_relief", "Pain relief", icon = Icons.Default.Healing)
    )

    val familiarity = listOf(
        OnboardingOption("beginner", "Beginner"),
        OnboardingOption("some", "Some experience"),
        OnboardingOption("regular", "Regular practice")
    )

    val femaleBodyTypes = listOf(
        OnboardingOption("female_skinny", "Skinny", imageRes = R.drawable.femaleskinny),
        OnboardingOption("female_normal", "Normal", imageRes = R.drawable.femalenormal),
        OnboardingOption("female_toned", "Toned", imageRes = R.drawable.femaletoned),
        OnboardingOption("female_fat", "Curvy", imageRes = R.drawable.femalefat)
    )

    val maleBodyTypes = listOf(
        OnboardingOption("male_skinny", "Skinny", imageRes = R.drawable.maleskinny),
        OnboardingOption("male_normal", "Normal", imageRes = R.drawable.malenormal),
        OnboardingOption("male_bulk", "Bulk", imageRes = R.drawable.malebulk),
        OnboardingOption("male_fat", "Broad", imageRes = R.drawable.malefat)
    )

    val zones = listOf(
        OnboardingOption("neck", "Neck"),
        OnboardingOption("shoulders", "Shoulders"),
        OnboardingOption("back", "Back"),
        OnboardingOption("hips", "Hips"),
        OnboardingOption("knees", "Knees"),
        OnboardingOption("ankles", "Ankles")
    )

    val activity = listOf(
        OnboardingOption("seated", "Mostly seated"),
        OnboardingOption("light", "Lightly active"),
        OnboardingOption("active", "Active most days")
    )

    val walkDaily = listOf(
        OnboardingOption("lt_10", "Less than 10 min", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("10_30", "10–30 min", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("30_60", "30–60 min", icon = Icons.Default.DirectionsWalk),
        OnboardingOption("60_plus", "60+ min", icon = Icons.Default.DirectionsWalk)
    )

    val stairs = listOf(
        OnboardingOption("easy", "Very easy"),
        OnboardingOption("winded", "A bit winded"),
        OnboardingOption("break", "Need a break")
    )

    val squat = listOf(
        OnboardingOption("cannot", "Cannot"),
        OnboardingOption("partial", "Partial"),
        OnboardingOption("full", "Full")
    )

    val yesNo = listOf(
        OnboardingOption("yes", "Yes"),
        OnboardingOption("no", "No")
    )

    val taiChiLevel = listOf(
        OnboardingOption("new", "New"),
        OnboardingOption("intermediate", "Intermediate"),
        OnboardingOption("advanced", "Advanced")
    )

    val betweenMeals = listOf(
        OnboardingOption("often", "Often hungry"),
        OnboardingOption("sometimes", "Sometimes hungry"),
        OnboardingOption("rarely", "Rarely hungry")
    )

    val sleep = listOf(
        OnboardingOption("2_4", "2–4 hours", "Light sleeper", icon = Icons.Default.Bedtime),
        OnboardingOption("4_6", "4–6 hours", "Below average", icon = Icons.Default.Bedtime),
        OnboardingOption("6_8", "6–8 hours", "Recommended range", icon = Icons.Default.Bedtime),
        OnboardingOption("8_plus", "8+ hours", "Well rested", icon = Icons.Default.Bedtime)
    )

    val water = listOf(
        OnboardingOption("1_3", "1–3 cups", icon = Icons.Default.LocalDrink),
        OnboardingOption("4_6", "4–6 cups", icon = Icons.Default.LocalDrink),
        OnboardingOption("7_9", "7–9 cups", icon = Icons.Default.LocalDrink),
        OnboardingOption("10_plus", "10+ cups", icon = Icons.Default.LocalDrink)
    )

    val diet = listOf(
        OnboardingOption("balanced", "Balanced"),
        OnboardingOption("low_carb", "Low-carb"),
        OnboardingOption("plant_forward", "Plant-forward"),
        OnboardingOption("no_pref", "No preference")
    )

    val motivation = listOf(
        OnboardingOption("not_ready", "Not ready", icon = Icons.Default.Psychology),
        OnboardingOption("somewhat", "Somewhat ready", icon = Icons.Default.Psychology),
        OnboardingOption("ready", "Ready", icon = Icons.Default.Psychology),
        OnboardingOption("very_ready", "Very ready", icon = Icons.Default.Psychology)
    )

    val blockers = listOf(
        OnboardingOption("time", "Time"),
        OnboardingOption("pain", "Pain"),
        OnboardingOption("low_energy", "Low energy"),
        OnboardingOption("no_routine", "No routine"),
        OnboardingOption("not_sure", "Not sure where to start")
    )

    val communityPoints = listOf(
        OnboardingOption("guided", "Guided practice", "Calm voice guidance and gentle pacing", icon = Icons.Default.SelfImprovement),
        OnboardingOption("community", "Community support", "A welcoming space for beginners", icon = Icons.Default.Person),
        OnboardingOption("routine", "Daily routine", "Simple steps to build consistency", icon = Icons.Default.Spa)
    )

    fun bodyTypesForGender(genderId: String?): List<OnboardingOption> {
        return if (genderId == "male") maleBodyTypes else femaleBodyTypes
    }
}
