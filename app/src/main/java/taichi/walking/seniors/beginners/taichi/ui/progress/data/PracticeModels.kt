package taichi.walking.seniors.beginners.taichi.ui.progress.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import taichi.walking.seniors.beginners.R
import java.time.LocalDate

enum class PracticeKind(
    val storageValue: String,
    @StringRes val displayNameRes: Int
) {
    TAI_CHI("tai_chi", R.string.program_tai_chi),
    WALKING("walking", R.string.program_tai_chi_walking),
    CHAIR("chair", R.string.chair_tai_chi_title)
}

enum class PracticeProgramVariant(val storageValue: String) {
    TAI_CHI("tai_chi"),
    TAI_CHI_WALKING("tai_chi_walking"),
    CHAIR_TAI_CHI("chair_tai_chi")
}

data class PracticeDaySummary(
    val date: LocalDate,
    val taiChiMinutes: Int,
    val walkingMinutes: Int,
    // Defaulted so summaries persisted before the seated program existed still decode.
    val chairMinutes: Int = 0
) {
    val totalMinutes: Int
        get() = taiChiMinutes + walkingMinutes + chairMinutes

    fun hasPractice(): Boolean = totalMinutes > 0

    fun minutesFor(kind: PracticeKind): Int = when (kind) {
        PracticeKind.TAI_CHI -> taiChiMinutes
        PracticeKind.WALKING -> walkingMinutes
        PracticeKind.CHAIR -> chairMinutes
    }

    fun plusMinutes(kind: PracticeKind, minutes: Int): PracticeDaySummary = when (kind) {
        PracticeKind.TAI_CHI -> copy(taiChiMinutes = taiChiMinutes + minutes)
        PracticeKind.WALKING -> copy(walkingMinutes = walkingMinutes + minutes)
        PracticeKind.CHAIR -> copy(chairMinutes = chairMinutes + minutes)
    }
}

// Gson-serialized inside ExerciseProgressSyncUploadBody; @SerializedName keeps the wire field
// names stable through release R8 renaming.
data class PracticeSyncDay(
    @SerializedName("date") val date: String,
    @SerializedName("taiChiMinutes") val taiChiMinutes: Int,
    @SerializedName("walkingMinutes") val walkingMinutes: Int,
    @SerializedName("chairMinutes") val chairMinutes: Int = 0
)

data class PracticeProgressSyncPayload(
    val version: Int,
    val programVariant: PracticeProgramVariant,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val chairCurrentDay: Int,
    val dailyPractice: List<PracticeSyncDay>
)

data class PracticeSnapshot(
    val summaries: List<PracticeDaySummary>,
    val taiChiCurrentDay: Int,
    val walkingCurrentDay: Int,
    val chairCurrentDay: Int,
    val programVariant: PracticeProgramVariant
) {
    fun currentDay(kind: PracticeKind): Int = when (kind) {
        PracticeKind.TAI_CHI -> taiChiCurrentDay
        PracticeKind.WALKING -> walkingCurrentDay
        PracticeKind.CHAIR -> chairCurrentDay
    }

    val totalMinutesPracticed: Int
        get() = summaries.sumOf { it.totalMinutes }

    val totalDaysPracticed: Int
        get() = summaries.count { it.hasPractice() }
}

/**
 * Folds server-side days into local days, taking `max` per field — never a replace.
 *
 * Restore runs on a device that may hold practice the server has never seen (an offline session),
 * so overwriting would destroy exactly the data restore exists to protect. Unparseable remote dates
 * are dropped rather than failing the whole merge. Mirrors the backend's own upload merge and iOS's
 * `PracticeProgressStore.mergeAndSave`.
 */
fun mergePracticeSummaries(
    local: List<PracticeDaySummary>,
    remote: List<ExerciseProgressRestoreDay>
): List<PracticeDaySummary> {
    val byDate = local.associateBy { it.date }.toMutableMap()

    remote.forEach { remoteDay ->
        val date = runCatching { LocalDate.parse(remoteDay.date) }.getOrNull() ?: return@forEach
        val existing = byDate[date]
        byDate[date] = PracticeDaySummary(
            date = date,
            taiChiMinutes = maxOf(existing?.taiChiMinutes ?: 0, remoteDay.taiChiMinutes),
            walkingMinutes = maxOf(existing?.walkingMinutes ?: 0, remoteDay.walkingMinutes),
            chairMinutes = maxOf(existing?.chairMinutes ?: 0, remoteDay.chairMinutes)
        )
    }

    return byDate.values.sortedBy { it.date }
}

data class PracticeTitleModel(
    @StringRes val titleRes: Int,
    val icon: String,
    val badgeId: String,
    @DrawableRes val badgeDrawableRes: Int,
    @StringRes val encouragementRes: Int
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
            titleRes = R.string.badge_initiate_title,
            icon = "\uD83C\uDF31",
            badgeId = "initiate",
            badgeDrawableRes = R.drawable.badge_one,
            encouragementRes = R.string.badge_initiate_encouragement
        ),
        3 to PracticeTitleModel(
            titleRes = R.string.badge_student_title,
            icon = "\uD83C\uDF43",
            badgeId = "student",
            badgeDrawableRes = R.drawable.badge_two,
            encouragementRes = R.string.badge_student_encouragement
        ),
        7 to PracticeTitleModel(
            titleRes = R.string.badge_practitioner_title,
            icon = "\uD83C\uDF3F",
            badgeId = "practitioner",
            badgeDrawableRes = R.drawable.badge_three,
            encouragementRes = R.string.badge_practitioner_encouragement
        ),
        14 to PracticeTitleModel(
            titleRes = R.string.badge_disciplined_title,
            icon = "\uD83C\uDF33",
            badgeId = "disciplined",
            badgeDrawableRes = R.drawable.badge_four,
            encouragementRes = R.string.badge_disciplined_encouragement
        ),
        30 to PracticeTitleModel(
            titleRes = R.string.badge_master_title,
            icon = "\uD83E\uDDD8",
            badgeId = "master",
            badgeDrawableRes = R.drawable.badge_five,
            encouragementRes = R.string.badge_master_encouragement
        )
    )

    fun getTitle(totalDays: Int): PracticeTitleModel? =
        milestones.lastOrNull { totalDays >= it.first }?.second

    fun shouldUnlockBadge(days: Int): Boolean =
        milestones.any { it.first == days }
}
