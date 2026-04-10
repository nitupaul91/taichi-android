package taichi.walking.seniors.beginners.taichi.ui.progress.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
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
    private val gson: Gson
) {
    private object Keys {
        val legacyCurrentDay = intPreferencesKey("current_day")
        val legacyTotalMinutes = intPreferencesKey("total_minutes_practiced")
        val legacyCompletionDates = stringPreferencesKey("completion_dates")
        val isPremium = intPreferencesKey("is_premium")
        val summaries = stringPreferencesKey("practice_day_summaries")
        val taiChiCurrentDay = intPreferencesKey("tai_chi_current_day")
        val walkingCurrentDay = intPreferencesKey("walking_current_day")
        val workoutIntensity = stringPreferencesKey("workout_intensity")
    }

    private data class StoredPracticeDaySummary(
        val date: String,
        val taiChiMinutes: Int,
        val walkingMinutes: Int
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
                val existing = summaries[existingIndex]
                summaries[existingIndex] = when (kind) {
                    PracticeKind.TAI_CHI -> existing.copy(
                        taiChiMinutes = existing.taiChiMinutes + durationMinutes
                    )
                    PracticeKind.WALKING -> existing.copy(
                        walkingMinutes = existing.walkingMinutes + durationMinutes
                    )
                }
            } else {
                summaries += PracticeDaySummary(
                    date = date,
                    taiChiMinutes = if (kind == PracticeKind.TAI_CHI) durationMinutes else 0,
                    walkingMinutes = if (kind == PracticeKind.WALKING) durationMinutes else 0
                )
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
            dailyPractice = snapshot.summaries.map { summary ->
                PracticeSyncDay(
                    date = syncDateFormatter.format(summary.date),
                    taiChiMinutes = summary.taiChiMinutes,
                    walkingMinutes = summary.walkingMinutes
                )
            }
        )
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
            programVariant = PracticeProgramVariant.TAI_CHI_WALKING
        )
    }

    private fun legacyCurrentDay(preferences: Preferences): Int =
        max(preferences[Keys.legacyCurrentDay] ?: 1, 1)

    private fun currentDayKey(kind: PracticeKind) = when (kind) {
        PracticeKind.TAI_CHI -> Keys.taiChiCurrentDay
        PracticeKind.WALKING -> Keys.walkingCurrentDay
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
                    walkingMinutes = stored.walkingMinutes
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
                    walkingMinutes = summary.walkingMinutes
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
