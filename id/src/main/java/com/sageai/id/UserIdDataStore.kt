package com.sageai.id

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "identity")

@Singleton
class UserIdDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID]
        }
    }

    suspend fun setUserId(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = token
        }
    }

    companion object {
        val USER_ID = stringPreferencesKey("userId")
    }
}