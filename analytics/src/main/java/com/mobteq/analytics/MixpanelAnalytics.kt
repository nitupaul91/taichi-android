package com.mobteq.analytics

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class MixpanelAnalytics @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mixpanel = MixpanelAPI.getInstance(context, PROJECT_TOKEN, true)

    /**
     * Log a Mixpanel event with optional properties.
     *
     * @param eventName The name of the event to track.
     * @param properties Optional key-value properties to include.
     */
    fun logEvent(eventName: String, properties: Map<String, Any>? = null) {
        Timber.d("Mixpanel Event: $eventName, Properties: $properties")

        val jsonProps = JSONObject()

        properties?.forEach { (key, value) ->
            jsonProps.put(key, value)
        }

        mixpanel.track(eventName, jsonProps)
    }

    companion object {
        const val PROJECT_TOKEN = "d265f9f8cd8ab692d3c1f8070c942333"
    }
}
