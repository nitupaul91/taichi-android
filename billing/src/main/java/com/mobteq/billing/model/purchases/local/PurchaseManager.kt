package com.mobteq.billing.model.purchases.local

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.mobteq.billing.datastore.DataStorePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

private const val WIDGET_COMPONENT = "taichi.walking.seniors.beginners.NewAppWidget"

class PurchaseManager @Inject constructor(
    private val dataStore: DataStorePrefs,
    @ApplicationContext private val context: Context,
) {

    fun isUserPremiumSubscribed(): Flow<Boolean> = dataStore.isSubscribed()

    suspend fun setUserPremiumSubscribed() {
        dataStore.setSubscribed(true)
    }

    fun enableWidget() {
        val pm = context.packageManager

        pm.setComponentEnabledSetting(
            ComponentName(context, WIDGET_COMPONENT),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun disableWidget() {
        val pm = context.packageManager

        pm.setComponentEnabledSetting(
            ComponentName(context, WIDGET_COMPONENT),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    suspend fun removeUserPremium() {
        dataStore.setSubscribed(false)

        Timber.d("User is no longer premium.")
    }
}