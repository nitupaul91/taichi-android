package taichi.walking.seniors.beginners.taichi.ui.progress.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import taichi.walking.seniors.beginners.taichi.program.ProgramPreference
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

private val Context.practiceDataStore by preferencesDataStore(name = "taichi_practice_prefs")

@Singleton
class PracticeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val programPreference: ProgramPreference
) {
    private object Keys {
        val legacyCurrentDay = intPreferencesKey("current_day")
        val legacyTotalMinutes = intPreferencesKey("total_minutes_practiced")
        val legacyCompletionDates = stringPreferencesKey("completion_dates")
        val isPremium = intPreferencesKey("is_premium")
        val summaries = stringPreferencesKey("practice_day_summaries")
        val taiChiCurrentDay = intPreferencesKey("tai_chi_current_day")
        val walkingCurrentDay = intPreferencesKey("walking_current_day")
        val chairCurrentDay = intPreferencesKey("chair_current_day")
        val workoutIntensity = stringPreferencesKey("workout_intensity")
    }

    // Persisted practice history via Gson; @SerializedName keeps the stored keys stable across
    // releases. Release builds up to 1.0.9 had no annotations, so R8 renamed the fields and the
    // stored JSON uses a/b/c/d (1.0.9 mapping.txt). The alternates read that data, and the next
    // save rewrites it under the real names, so updating users keep their practice history.
    @VisibleForTesting
    internal data class StoredPracticeDaySummary(
        @SerializedName(value = "date", alternate = ["a"]) val date: String,
        @SerializedName(value = "taiChiMinutes", alternate = ["b"]) val taiChiMinutes: Int,
        @SerializedName(value = "walkingMinutes", alternate = ["c"]) val walkingMinutes: Int,
        // Gson leaves this at 0 for summaries written before the seated program shipped.
        @SerializedName(value = "chairMinutes", alternate = ["d"]) val chairMinutes: Int = 0
    )

    val practiceSnapshot: Flow<PracticeSnapshot> = context.practiceDataStore.data.map(::buildSnapshot)

    val isPremium: Flow<Boolean> = context.practiceDataStore.data.map { (it[Keys.isPremium] ?: 0) == 1 }

    fun currentDay(kind: PracticeKind): Flow<Int> = practiceSnapshot.map { it.currentDay(kind) }

    fun workoutIntensity(): Flow<String?> = context.practiceDataStore.data.map { prefs ->
        normalizeWorkoutIntensity(prefs[Keys.workoutIntensity])
    }

    suspend fun setPremium(value: Boolean) {
        context.practiceDataStore.edit { it[Keys.isPremium] = if (value) 1 else 0 }
    }

    suspend fun setCurrentDay(kind: PracticeKind, value: Int) {
        migrateIfNeeded()
        context.practiceDataStore.edit { prefs ->
            prefs[currentDayKey(kind)] = value.coerceAtLeast(1)
        }
    }

    suspend fun currentWorkoutIntensity(): String? {
        migrateIfNeeded()
        return workoutIntensity().first()
    }

    suspend fun setWorkoutIntensity(value: String?) {
        migrateIfNeeded()
        context.practiceDataStore.edit { prefs ->
            val normalizedValue = normalizeWorkoutIntensity(value)
            if (normalizedValue == null) {
                prefs.remove(Keys.workoutIntensity)
            } else {
                prefs[Keys.workoutIntensity] = normalizedValue
            }
        }
    }

    suspend fun completeWorkout(
        kind: PracticeKind,
        dayNumber: Int,
        durationMinutes: Int,
        date: LocalDate = LocalDate.now()
    ): PracticeCompletionResult {
        migrateIfNeeded()
        context.practiceDataStore.edit { prefs ->
            val summaries = decodeSummaries(prefs).toMutableList()
            val existingIndex = summaries.indexOfFirst { it.date == date }

            if (existingIndex >= 0) {
                summaries[existingIndex] = summaries[existingIndex].plusMinutes(kind, durationMinutes)
            } else {
                summaries += PracticeDaySummary(date, 0, 0).plusMinutes(kind, durationMinutes)
            }

            prefs[Keys.summaries] = encodeSummaries(summaries)

            val currentDayKey = currentDayKey(kind)
            val currentDay = max(prefs[currentDayKey] ?: legacyCurrentDay(prefs), 1)
            if (dayNumber >= currentDay) {
                prefs[currentDayKey] = dayNumber + 1
            }
        }

        val snapshot = currentSnapshot()
        val currentStreak = computeCurrentStreak(snapshot.summaries)
        val totalDays = snapshot.totalDaysPracticed
        return PracticeCompletionResult(
            totalDaysPracticed = totalDays,
            currentStreak = currentStreak,
            title = PracticeTitleService.getTitle(totalDays),
            badgeUnlocked = PracticeTitleService.shouldUnlockBadge(totalDays)
        )
    }

    /**
     * Folds server-side progress into local progress, taking `max` on every value — never a
     * replace. Restore runs on a device that may already hold practice the server has not seen
     * (offline sessions), so overwriting would destroy exactly the data this feature exists to
     * protect. Mirrors the backend's own merge and iOS's `PracticeProgressStore.mergeAndSave`.
     *
     * @return true if anything changed locally, so the caller can decide whether to re-upload.
     */
    suspend fun mergeRemoteProgress(remote: ExerciseProgressRestoreResponse): Boolean {
        migrateIfNeeded()

        var changed = false
        context.practiceDataStore.edit { prefs ->
            val localTaiChiDay = max(prefs[Keys.taiChiCurrentDay] ?: legacyCurrentDay(prefs), 1)
            val localWalkingDay = max(prefs[Keys.walkingCurrentDay] ?: legacyCurrentDay(prefs), 1)
            val localChairDay = max(prefs[Keys.chairCurrentDay] ?: 1, 1)
            val mergedTaiChiDay = max(localTaiChiDay, remote.taiChiCurrentDay.coerceAtLeast(1))
            val mergedWalkingDay = max(localWalkingDay, remote.walkingCurrentDay.coerceAtLeast(1))
            val mergedChairDay = max(localChairDay, remote.chairCurrentDay.coerceAtLeast(1))

            if (mergedTaiChiDay != localTaiChiDay) {
                prefs[Keys.taiChiCurrentDay] = mergedTaiChiDay
                changed = true
            }
            if (mergedWalkingDay != localWalkingDay) {
                prefs[Keys.walkingCurrentDay] = mergedWalkingDay
                changed = true
            }
            if (mergedChairDay != localChairDay) {
                prefs[Keys.chairCurrentDay] = mergedChairDay
                changed = true
            }

            val localSummaries = decodeSummaries(prefs)
            val mergedSummaries = mergePracticeSummaries(localSummaries, remote.dailyPractice)
            if (mergedSummaries != localSummaries.sortedBy { it.date }) {
                prefs[Keys.summaries] = encodeSummaries(mergedSummaries)
                changed = true
            }
        }
        return changed
    }

    suspend fun currentSnapshot(): PracticeSnapshot {
        migrateIfNeeded()
        return practiceSnapshot.first()
    }

    suspend fun syncPayload(): PracticeProgressSyncPayload {
        val snapshot = currentSnapshot()
        return PracticeProgressSyncPayload(
            version = 1,
            programVariant = snapshot.programVariant,
            taiChiCurrentDay = snapshot.taiChiCurrentDay,
            walkingCurrentDay = snapshot.walkingCurrentDay,
            chairCurrentDay = snapshot.chairCurrentDay,
            dailyPractice = snapshot.summaries.map { summary ->
                PracticeSyncDay(
                    date = syncDateFormatter.format(summary.date),
                    taiChiMinutes = summary.taiChiMinutes,
                    walkingMinutes = summary.walkingMinutes,
                    chairMinutes = summary.chairMinutes
                )
            }
        )
    }

    /**
     * Removes all practice history and every program's current day, including the legacy keys the
     * migration would otherwise rebuild history from, so nothing survives an account deletion.
     * Like iOS `PracticeProgressStore.eraseAll`, the premium flag and workout intensity are kept:
     * they are device settings, not account data.
     */
    suspend fun eraseAll() {
        context.practiceDataStore.edit { prefs ->
            listOf(
                Keys.legacyCurrentDay,
                Keys.legacyTotalMinutes,
                Keys.legacyCompletionDates,
                Keys.summaries,
                Keys.taiChiCurrentDay,
                Keys.walkingCurrentDay,
                Keys.chairCurrentDay
            ).forEach { key -> prefs -= key }
        }
    }

    fun computeCurrentStreak(summaries: List<PracticeDaySummary>): Int {
        val practicedDates = summaries.filter { it.hasPractice() }.map { it.date }.toSet()
        if (practicedDates.isEmpty()) return 0

        var streak = 0
        var day = LocalDate.now()
        if (day !in practicedDates) {
            day = day.minusDays(1)
        }

        while (day in practicedDates) {
            streak += 1
            day = day.minusDays(1)
        }

        return streak
    }

    private suspend fun migrateIfNeeded() {
        context.practiceDataStore.edit { prefs ->
            val legacyDay = legacyCurrentDay(prefs)

            if (prefs[Keys.taiChiCurrentDay] == null) {
                prefs[Keys.taiChiCurrentDay] = legacyDay
            }
            if (prefs[Keys.walkingCurrentDay] == null) {
                prefs[Keys.walkingCurrentDay] = legacyDay
            }
            // Seated practice is its own 30-day program, so it starts fresh rather than inheriting
            // the legacy day a user reached in the original single-program build.
            if (prefs[Keys.chairCurrentDay] == null) {
                prefs[Keys.chairCurrentDay] = 1
            }
            if (prefs[Keys.summaries].isNullOrBlank()) {
                val migrated = migrateLegacySummaries(prefs)
                if (migrated.isNotEmpty()) {
                    prefs[Keys.summaries] = encodeSummaries(migrated)
                }
            }
        }
    }

    private fun buildSnapshot(preferences: Preferences): PracticeSnapshot {
        val summaries = decodeSummaries(preferences).ifEmpty { migrateLegacySummaries(preferences) }
            .sortedBy { it.date }

        return PracticeSnapshot(
            summaries = summaries,
            taiChiCurrentDay = max(preferences[Keys.taiChiCurrentDay] ?: legacyCurrentDay(preferences), 1),
            walkingCurrentDay = max(preferences[Keys.walkingCurrentDay] ?: legacyCurrentDay(preferences), 1),
            chairCurrentDay = max(preferences[Keys.chairCurrentDay] ?: 1, 1),
            // The program the user picked in onboarding, as iOS reports it.
            programVariant = programPreference.programVariant
        )
    }

    private fun legacyCurrentDay(preferences: Preferences): Int =
        max(preferences[Keys.legacyCurrentDay] ?: 1, 1)

    private fun currentDayKey(kind: PracticeKind) = when (kind) {
        PracticeKind.TAI_CHI -> Keys.taiChiCurrentDay
        PracticeKind.WALKING -> Keys.walkingCurrentDay
        PracticeKind.CHAIR -> Keys.chairCurrentDay
    }

    private fun migrateLegacySummaries(preferences: Preferences): List<PracticeDaySummary> {
        val completionDates = parseLegacyDates(preferences[Keys.legacyCompletionDates]).distinct().sorted()
        if (completionDates.isEmpty()) return emptyList()

        val totalMinutes = preferences[Keys.legacyTotalMinutes] ?: 0
        val fallbackMinutes = if (totalMinutes > 0) totalMinutes else completionDates.size * 10
        var remainingMinutes = fallbackMinutes

        return completionDates.mapIndexed { index, date ->
            val remainingDays = completionDates.size - index
            val allocatedMinutes = if (remainingDays > 0) remainingMinutes / remainingDays else 0
            remainingMinutes -= allocatedMinutes
            PracticeDaySummary(
                date = date,
                taiChiMinutes = allocatedMinutes,
                walkingMinutes = 0
            )
        }
    }

    private fun decodeSummaries(preferences: Preferences): List<PracticeDaySummary> {
        val raw = preferences[Keys.summaries].orEmpty()
        if (raw.isBlank()) return emptyList()

        val storedType = object : TypeToken<List<StoredPracticeDaySummary>>() {}.type
        val storedSummaries: List<StoredPracticeDaySummary> =
            runCatching { gson.fromJson<List<StoredPracticeDaySummary>>(raw, storedType) }
                .getOrDefault(emptyList())

        return storedSummaries.mapNotNull { stored ->
            runCatching {
                PracticeDaySummary(
                    date = LocalDate.parse(stored.date),
                    taiChiMinutes = stored.taiChiMinutes,
                    walkingMinutes = stored.walkingMinutes,
                    chairMinutes = stored.chairMinutes
                )
            }.getOrNull()
        }
    }

    private fun encodeSummaries(summaries: List<PracticeDaySummary>): String {
        val stored = summaries
            .sortedBy { it.date }
            .map { summary ->
                StoredPracticeDaySummary(
                    date = summary.date.toString(),
                    taiChiMinutes = summary.taiChiMinutes,
                    walkingMinutes = summary.walkingMinutes,
                    chairMinutes = summary.chairMinutes
                )
            }
        return gson.toJson(stored)
    }

    private fun parseLegacyDates(raw: String?): List<LocalDate> {
        if (raw.isNullOrBlank()) return emptyList()

        val token = object : TypeToken<List<String>>() {}.type
        val dates = runCatching { gson.fromJson<List<String>>(raw, token) }.getOrDefault(emptyList<String>())
        return dates.mapNotNull { value ->
            runCatching { LocalDate.parse(value, legacyDateFormatter) }
                .recoverCatching {
                    LocalDate.ofInstant(java.time.Instant.parse(value), ZoneOffset.UTC)
                }
                .getOrNull()
        }
    }

    private fun normalizeWorkoutIntensity(value: String?): String? =
        value
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it in 1..5 }
            ?.toString()

    private companion object {
        val legacyDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val syncDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
