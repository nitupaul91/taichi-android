package com.mobteq.analytics

import android.app.Application
import com.tiktok.TikTokBusinessSdk
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TikTokAnalyticsTracker @Inject constructor() {

    fun initialize(
        application: Application,
        sdkAppId: String?,
        tikTokAppId: String?
    ) {
        if (TikTokBusinessSdk.isInitialized()) {
            return
        }

        val resolvedSdkAppId = sdkAppId?.trim().orEmpty()
        val resolvedTikTokAppId = tikTokAppId?.trim().orEmpty()

        if (resolvedSdkAppId.isBlank()) {
            Timber.tag(TAG).w(
                "TikTok SDK appId is missing. TikTok event delivery is disabled."
            )
            return
        }

        val config = TikTokBusinessSdk.TTConfig(application)
            .setAppId(resolvedSdkAppId)
            .disableAutoIapTrack()

        if (resolvedTikTokAppId.isNotBlank()) {
            config.setTTAppId(resolvedTikTokAppId)
        }

        TikTokBusinessSdk.initializeSdk(config, object : TikTokBusinessSdk.TTInitCallback {
            override fun success() {
                Timber.tag(TAG).i(
                    "TikTok SDK initialized. appId=%s ttAppId=%s",
                    resolvedSdkAppId,
                    resolvedTikTokAppId
                )
            }

            override fun fail(code: Int, msg: String?) {
                Timber.tag(TAG).e(
                    "TikTok SDK init failed. code=%s message=%s appId=%s ttAppId=%s",
                    code,
                    msg,
                    resolvedSdkAppId,
                    resolvedTikTokAppId
                )
            }
        })
    }

    fun trackCustomEvent(
        eventName: String,
        properties: Map<String, Any?> = emptyMap()
    ) {
        val payload = JSONObject()
        properties.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is Boolean -> payload.put(key, value)
                is Number -> payload.put(key, value)
                is String -> if (value.isNotBlank()) payload.put(key, value)
                else -> payload.put(key, value.toString())
            }
        }

        TikTokBusinessSdk.trackEvent(eventName, payload)
        Timber.tag(TAG).d("Tracked TikTok event %s with %s", eventName, payload)
    }

    companion object {
        private const val TAG = "TikTokAnalytics"
    }
}
