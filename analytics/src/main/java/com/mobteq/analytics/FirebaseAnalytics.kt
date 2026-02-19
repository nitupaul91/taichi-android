package com.mobteq.analytics

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.mobteq.analytics.events.firebase.AnalyticsEvent
import timber.log.Timber
import javax.inject.Inject

class FirebaseAnalytics @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun logEvent(event: AnalyticsEvent) {
        event.params.keySet().forEach {
            val param = event.params.get(it)
            Timber.d("AnalyticsEvent logged to Firebase: ${event.name} param: $it - value $param")
        }
        firebaseAnalytics.logEvent(event.name, event.params)
    }

    fun logEvent(eventName: String, params: Bundle = bundleOf()) {
        Timber.d("AnalyticsEvent logged to Firebase.\n${eventName}\n${params}")

        firebaseAnalytics.logEvent(eventName, params)
    }

    companion object {
        const val PROPERTY_SCREEN_NAME = "screen_name"
    }
}