package com.sageai.id

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    /** Ids of accounts the user deleted on this device. */
    fun getRetiredUserIds(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[RETIRED_USER_IDS] ?: emptySet()
        }
    }

    /**
     * Stores [newUserId] and retires [retiredUserIds] in one edit, so nothing can read the new id
     * while an old one is still eligible to come back.
     */
    suspend fun replaceUserId(newUserId: String, retiredUserIds: Collection<String>) {
        context.dataStore.edit { preferences ->
            val retired = preferences[RETIRED_USER_IDS] ?: emptySet()
            preferences[RETIRED_USER_IDS] = retired + retiredUserIds.filter { it.isNotBlank() }
            preferences[USER_ID] = newUserId
        }
    }

    companion object {
        val USER_ID = stringPreferencesKey("userId")
        val RETIRED_USER_IDS = stringSetPreferencesKey("retiredUserIds")
    }
}
