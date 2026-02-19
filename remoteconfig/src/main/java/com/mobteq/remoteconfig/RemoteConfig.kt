package com.mobteq.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfig @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    fun fetchAndActivate(defaults: Int) {
        firebaseRemoteConfig.setDefaultsAsync(defaults)
            .continueWith { firebaseRemoteConfig.fetchAndActivate() }
    }

    fun getBoolean(key: String) = firebaseRemoteConfig.getBoolean(key)
    fun getString(key: String) = firebaseRemoteConfig.getString(key)
    fun getLong(key: String) = firebaseRemoteConfig.getLong(key)
}