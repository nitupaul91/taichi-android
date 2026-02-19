package com.mobteq.billing.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@Singleton
class DataStorePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SUBSCRIBED = booleanPreferencesKey("isSubscribed")
        val ENJOY_APP = booleanPreferencesKey("shouldEnjoyAppDialog")
        val ENJOY_APP_COUNT = intPreferencesKey("enjoyAppDialogCount")
        val APP_CHECK_TOKEN = stringPreferencesKey("appCheckToken")
        val APP_CHECK_TOKEN_EXPIRY_IN_MILLIS = longPreferencesKey("appCheckTokenExpiryInMillis")
        val LAST_SHOWN_AD_TIME_IN_MILLIS = longPreferencesKey("lastShownAdTimeInMillis")
        val SHOW_PAYWALL_COUNT = intPreferencesKey("showPaywallCount")
        val SYNCED_USED_ID_WITH_REMOTE = booleanPreferencesKey("syncedUserIdWithRemote")
        val LAST_ONBOARDING_SHOW_VERSION = intPreferencesKey("lastOnboardingShowVersion")
        val REMAINING_MESSAGES = intPreferencesKey("remainingMessages")
        val CURRENT_THEME = stringPreferencesKey("currentTheme")
        val SELECTED_CAR_ID = stringPreferencesKey("selectedCarId")
        val SHOW_ONBOARDING = booleanPreferencesKey("showOnboarding")
        val TRAINING_COMPLETED_CARS = stringSetPreferencesKey("training_completed_cars")
    }

    fun isSubscribed() = context.dataStore.data.map { preferences ->
        preferences[SUBSCRIBED] ?: false
    }

    suspend fun setSubscribed(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SUBSCRIBED] = value
        }
    }

    fun shouldShowEnjoyAppDialog() = context.dataStore.data.map { preferences ->
        preferences[ENJOY_APP] ?: true
    }

    suspend fun changeEnjoyAppDialogStatus(status: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENJOY_APP] = status
        }
    }

    fun getEnjoyAppDialogCount() = context.dataStore.data.map { preferences ->
        preferences[ENJOY_APP_COUNT]
    }

    suspend fun increaseEnjoyAppDialogCount() {
        context.dataStore.edit { preferences ->
            preferences[ENJOY_APP_COUNT] = preferences[ENJOY_APP_COUNT]?.plus(1) ?: 1
        }
    }

    fun getFirebaseAppCheckToken() = context.dataStore.data.map { preferences ->
        preferences[APP_CHECK_TOKEN] ?: ""
    }

    suspend fun setFirebaseAppCheckToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_CHECK_TOKEN] = token
        }
    }

    suspend fun setFirebaseAppCheckTokenExpiration(expiryInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[APP_CHECK_TOKEN_EXPIRY_IN_MILLIS] = expiryInMillis
        }
    }

    suspend fun getFirebaseAppCheckTokenExpiryInMillis(): Long =
        context.dataStore.data.map { preferences ->
            preferences[APP_CHECK_TOKEN_EXPIRY_IN_MILLIS] ?: 0L
        }.first()

    suspend fun setAdLastShownInMillis(expiryInMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SHOWN_AD_TIME_IN_MILLIS] = expiryInMillis
        }
    }

    suspend fun getAdLastShownInMillis(): Long =
        context.dataStore.data.map { preferences ->
            preferences[LAST_SHOWN_AD_TIME_IN_MILLIS] ?: 0L
        }.first()

    fun getPaywallCount() = context.dataStore.data.map { preferences ->
        preferences[SHOW_PAYWALL_COUNT]
    }

    suspend fun increasePaywallCount() {
        context.dataStore.edit { preferences ->
            preferences[SHOW_PAYWALL_COUNT] = preferences[SHOW_PAYWALL_COUNT]?.plus(1) ?: 0
        }
    }

    fun getLastOnboardingShowVersion() = context.dataStore.data.map { preferences ->
        preferences[LAST_ONBOARDING_SHOW_VERSION] ?: 0
    }

    suspend fun setLastOnboardingShowVersion(versionCode: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ONBOARDING_SHOW_VERSION] = versionCode
        }
    }

    fun hasSyncedUserIdWithRemote() = context.dataStore.data.map { preferences ->
        preferences[SYNCED_USED_ID_WITH_REMOTE] ?: false
    }

    suspend fun setHasSyncedUserIdWithRemote(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNCED_USED_ID_WITH_REMOTE] = value
        }
    }

    fun getRemainingMessages() = context.dataStore.data.map { preferences ->
        preferences[REMAINING_MESSAGES]
    }

    suspend fun setRemainingMessages(remainingMessages: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMAINING_MESSAGES] = remainingMessages
        }
    }

    suspend fun incRemainingMessages(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMAINING_MESSAGES] = preferences[REMAINING_MESSAGES]?.plus(amount) ?: 0
        }
    }

    fun getCurrentTheme() = context.dataStore.data.map { preferences ->
        preferences[CURRENT_THEME]
    }

    suspend fun setCurrentTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_THEME] = theme
        }
    }

    suspend fun setSelectedCarId(carId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CAR_ID] = carId
        }
    }

    fun getSelectedCarId() = context.dataStore.data.map { preferences ->
        preferences[SELECTED_CAR_ID]
    }


    fun shouldShowOnboarding() = context.dataStore.data.map { preferences ->
        preferences[SHOW_ONBOARDING] ?: true
    }

    suspend fun setShowOnboarding(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_ONBOARDING] = show
        }
    }

    fun getCompletedCarTrainingIds() = context.dataStore.data.map { preferences ->
        preferences[TRAINING_COMPLETED_CARS] ?: emptySet()
    }

    suspend fun setCarTrainingCompleted(carId: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[TRAINING_COMPLETED_CARS] ?: emptySet()
            preferences[TRAINING_COMPLETED_CARS] = currentSet + carId
        }
    }

    suspend fun isCarTrainingCompleted(carId: String): Boolean {
        val completedSet = getCompletedCarTrainingIds().first()
        return completedSet.contains(carId)
    }

}
