package taichi.walking.seniors.beginners.taichi.onboarding.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "taichi_onboarding_prefs"

val Context.taichiOnboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class OnboardingPrefs(private val context: Context) {
    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ONBOARDING_ANSWERS_JSON = stringPreferencesKey("onboarding_answers_json")
    }

    fun observeCompleted(): Flow<Boolean> = context.taichiOnboardingDataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    fun observeAnswersJson(): Flow<String?> = context.taichiOnboardingDataStore.data.map { prefs ->
        prefs[ONBOARDING_ANSWERS_JSON]
    }

    suspend fun getAnswersJson(): String? = observeAnswersJson().first()

    suspend fun setCompleted(completed: Boolean) {
        context.taichiOnboardingDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAnswersJson(json: String) {
        context.taichiOnboardingDataStore.edit { prefs ->
            prefs[ONBOARDING_ANSWERS_JSON] = json
        }
    }
}
