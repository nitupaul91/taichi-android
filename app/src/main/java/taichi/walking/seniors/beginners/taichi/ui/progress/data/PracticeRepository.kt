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
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.practiceDataStore by preferencesDataStore(name = "taichi_practice_prefs")

@Singleton
class PracticeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private object Keys {
        val CURRENT_DAY = intPreferencesKey("current_day")
        val TOTAL_MINUTES = intPreferencesKey("total_minutes_practiced")
        val COMPLETION_DATES = stringPreferencesKey("completion_dates")
        val IS_PREMIUM = intPreferencesKey("is_premium")
    }

    val currentDay: Flow<Int> = context.practiceDataStore.data.map { it[Keys.CURRENT_DAY] ?: 1 }
    val totalMinutes: Flow<Int> = context.practiceDataStore.data.map { it[Keys.TOTAL_MINUTES] ?: 0 }
    val completionDates: Flow<List<LocalDate>> = context.practiceDataStore.data.map {
        parseDates(it[Keys.COMPLETION_DATES])
    }
    val isPremium: Flow<Boolean> = context.practiceDataStore.data.map { (it[Keys.IS_PREMIUM] ?: 0) == 1 }

    suspend fun setPremium(value: Boolean) {
        context.practiceDataStore.edit { it[Keys.IS_PREMIUM] = if (value) 1 else 0 }
    }

    suspend fun setCurrentDay(value: Int) {
        context.practiceDataStore.edit { it[Keys.CURRENT_DAY] = value.coerceAtLeast(1) }
    }

    suspend fun recordCompletion(durationMinutes: Int) {
        context.practiceDataStore.edit { prefs ->
            val dates = parseDates(prefs[Keys.COMPLETION_DATES]).toMutableSet()
            val today = LocalDate.now()
            dates.add(today)

            val currentDay = prefs[Keys.CURRENT_DAY] ?: 1
            prefs[Keys.CURRENT_DAY] = currentDay + 1
            prefs[Keys.TOTAL_MINUTES] = (prefs[Keys.TOTAL_MINUTES] ?: 0) + durationMinutes
            prefs[Keys.COMPLETION_DATES] = gson.toJson(dates.toList().sorted().map { it.toString() })
        }
    }

    private fun parseDates(raw: String?): List<LocalDate> {
        if (raw.isNullOrBlank()) return emptyList()
        val token = object : TypeToken<List<String>>() {}.type
        val list: List<String> = runCatching { gson.fromJson<List<String>>(raw, token) }.getOrDefault(emptyList<String>())
        return list.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
    }
}
