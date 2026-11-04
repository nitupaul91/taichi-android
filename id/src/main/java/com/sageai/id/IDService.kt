package com.sageai.id

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class IDService @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userIdDataStore: UserIdDataStore
) {

    suspend fun getUserID(): String {
        val storedUserId = userIdDataStore.getUserId().first()
        if (storedUserId == null) {
            val deviceId = getDeviceId()
            userIdDataStore.setUserId(getDeviceId())

            Timber.tag(TAG).d("#getUserId userId=$deviceId")

            return deviceId
        }

        Timber.tag(TAG).d("#getUserId userId=$storedUserId")

        return storedUserId
    }

    suspend fun getUserIdFlow(): Flow<String> {
        return userIdDataStore.getUserId().filterNotNull()
    }

    suspend fun updateUserId(newUserId: String) {
        Timber.tag(TAG).d("updateUserId newUserId=$newUserId")

        if (newUserId.isBlank()) {
            return
        }
        if (isRetiredUserId(newUserId)) {
            Timber.tag(TAG).d("updateUserId ignored: the id belongs to a deleted account")
            return
        }
        userIdDataStore.setUserId(newUserId)
    }

    /**
     * Replaces the stored id with a brand-new random UUID (never ANDROID_ID, which would hand back
     * the identity the user just deleted) and retires [retiredUserIds], so [updateUserId] can never
     * restore one of them. A Play purchase keeps the id it was made under as its obfuscated account
     * id and would otherwise bring the deleted id straight back on the next purchase query.
     */
    suspend fun resetUserId(retiredUserIds: Collection<String>): String {
        val newUserId = UUID.randomUUID().toString().lowercase()
        userIdDataStore.replaceUserId(newUserId, retiredUserIds)

        Timber.tag(TAG).d("#resetUserId userId=$newUserId")

        return newUserId
    }

    suspend fun isRetiredUserId(userId: String): Boolean =
        userId in userIdDataStore.getRetiredUserIds().first()

    suspend fun getDeviceID(): String = getDeviceId()

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String =
        Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

    companion object {
        const val TAG = "Identity"
    }
}
