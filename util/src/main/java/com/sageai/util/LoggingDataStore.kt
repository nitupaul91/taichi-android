package com.sageai.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "logging_data_store")

@Singleton
class LoggingDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val MIN_LOG_LEVEL = stringPreferencesKey("minLogLevel")
    }

    fun getMinLogLevel() = context.dataStore.data.map { preferences ->
        preferences[MIN_LOG_LEVEL] ?: LogLevel.ERROR
    }

    suspend fun setMinLogLevel(token: String) {
        context.dataStore.edit { preferences ->
            preferences[MIN_LOG_LEVEL] = token
        }
    }
}