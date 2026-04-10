package taichi.walking.seniors.beginners.taichi.ui.progress.data

import androidx.annotation.DrawableRes
import taichi.walking.seniors.beginners.R
import java.time.LocalDate

enum class PracticeKind(
    val storageValue: String,
    val displayName: String
) {
    TAI_CHI("tai_chi", "Tai Chi"),
    WALKING("walking", "Tai Chi Walking")
}

enum class PracticeProgramVariant(val storageValue: String) {
    TAI_CHI("tai_chi"),
    TAI_CHI_WALKING("tai_chi_walking")
}

data class PracticeDaySummary(
    val date: LocalDate,
    val taiChiMinutes: Int,
    val walkingMinutes: Int
) {
    val totalMinutes: Int
        get() = taiChiMinutes + walkingMinutes

    fun hasPractice(): Boolean = totalMinutes > 0

    fun minutesFor(kind: PracticeKind): Int = when (kind) {
        PracticeKind.TAI_CHI -> taiChiMinutes
        PracticeKind.WALKING -> walkingMinutes
    }
}

data class PracticeSyncDay(
    val date: String,
    val taiChiMinutes: Int,
    val walkingMinutes: Int
)

data class PracticeProgressSyncPayload(
    val version: Int,
    val programVariant: PracticeProgramVariant,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val dailyPractice: List<PracticeSyncDay>
)

data class PracticeSnapshot(
    val summaries: List<PracticeDaySummary>,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val programVariant: PracticeProgramVariant
) {
    fun currentDay(kind: PracticeKind): Int = when (kind) {
        PracticeKind.TAI_CHI -> taiChiCurrentDay
        PracticeKind.WALKING -> walkingCurrentDay
    }

    val totalMinutesPracticed: Int
        get() = summaries.sumOf { it.totalMinutes }

    val totalDaysPracticed: Int
        get() = summaries.count { it.hasPractice() }
}

data class PracticeTitleModel(
    val title: String,
    val icon: String,
    val badgeId: String,
    @DrawableRes val badgeDrawableRes: Int,
    val encouragement: String
)

data class PracticeCompletionResult(
    val totalDaysPracticed: Int,
    val currentStreak: Int,
    val title: PracticeTitleModel?,
    val badgeUnlocked: Boolean
)

object PracticeTitleService {
    private val milestones: List<Pair<Int, PracticeTitleModel>> = listOf(
        1 to PracticeTitleModel(
            title = "Initiate of Tai Chi",
            icon = "\uD83C\uDF31",
            badgeId = "initiate",
            badgeDrawableRes = R.drawable.badge_one,
            encouragement = "Your journey has begun.\nKeep showing up for yourself."
        ),
        3 to PracticeTitleModel(
            title = "Student of Tai Chi",
            icon = "\uD83C\uDF43",
            badgeId = "student",
            badgeDrawableRes = R.drawable.badge_two,
            encouragement = "You're building a real habit.\nConsistency is your superpower."
        ),
        7 to PracticeTitleModel(
            title = "Practitioner",
            icon = "\uD83C\uDF3F",
            badgeId = "practitioner",
            badgeDrawableRes = R.drawable.badge_three,
            encouragement = "One full week complete.\nThe flow is becoming part of you."
        ),
        14 to PracticeTitleModel(
            title = "Disciplined Practitioner",
            icon = "\uD83C\uDF33",
            badgeId = "disciplined",
            badgeDrawableRes = R.drawable.badge_four,
            encouragement = "True discipline in action.\nYou're inspiring others around you."
        ),
        30 to PracticeTitleModel(
            title = "Master of Flow",
            icon = "\uD83E\uDDD8",
            badgeId = "master",
            badgeDrawableRes = R.drawable.badge_five,
            encouragement = "You've mastered the flow.\nYour mind and body are transformed."
        )
    )

    fun getTitle(totalDays: Int): PracticeTitleModel? =
        milestones.lastOrNull { totalDays >= it.first }?.second

    fun shouldUnlockBadge(days: Int): Boolean =
        milestones.any { it.first == days }
}
