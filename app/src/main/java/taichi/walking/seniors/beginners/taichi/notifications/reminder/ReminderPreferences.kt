package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderDataStore by preferencesDataStore(name = "taichi_reminder_prefs")

/** Local state behind the practice and trial reminders. Never synced: it describes this device's schedule. */
@Singleton
class ReminderPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val preferredHour = intPreferencesKey("practice_reminder_preferred_hour")
        val preferredMinute = intPreferencesKey("practice_reminder_preferred_minute")
        val lastPromptDay = stringPreferencesKey("practice_reminder_last_prompt_day")
        val recentPracticeHours = stringPreferencesKey("recent_practice_hours")
        val trialEndEpochMillis = longPreferencesKey("trial_reminder_trial_end")
    }

    /** The time the user last accepted in the reminder prompt, if any. */
    suspend fun preferredTime(): LocalTime? {
        val prefs = context.reminderDataStore.data.first()
        val hour = prefs[Keys.preferredHour] ?: return null
        val minute = prefs[Keys.preferredMinute] ?: return null
        return runCatching { LocalTime.of(hour, minute) }.getOrNull()
    }

    suspend fun setPreferredTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.preferredHour] = hour
            prefs[Keys.preferredMinute] = minute
        }
    }

    suspend fun lastPromptDay(): LocalDate? =
        context.reminderDataStore.data.first()[Keys.lastPromptDay]
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    suspend fun setLastPromptDay(day: LocalDate) {
        context.reminderDataStore.edit { it[Keys.lastPromptDay] = day.toString() }
    }

    /** Hours of day of the latest sessions, oldest first. */
    suspend fun recentPracticeHours(): List<Int> =
        decodeHours(context.reminderDataStore.data.first()[Keys.recentPracticeHours])

    suspend fun recordPracticeHour(hour: Int) {
        context.reminderDataStore.edit { prefs ->
            val hours = TrialReminderTimes.appendPracticeHour(decodeHours(prefs[Keys.recentPracticeHours]), hour)
            prefs[Keys.recentPracticeHours] = hours.joinToString(separator = ",")
        }
    }

    suspend fun trialEnd(): Instant? =
        context.reminderDataStore.data.first()[Keys.trialEndEpochMillis]?.let(Instant::ofEpochMilli)

    suspend fun setTrialEnd(trialEnd: Instant) {
        context.reminderDataStore.edit { it[Keys.trialEndEpochMillis] = trialEnd.toEpochMilli() }
    }

    suspend fun clearTrialEnd() {
        context.reminderDataStore.edit { it.remove(Keys.trialEndEpochMillis) }
    }

    private fun decodeHours(raw: String?): List<Int> =
        raw.orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it in 0..23 }
}
