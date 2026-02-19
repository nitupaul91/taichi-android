package com.mobteq.analytics

import android.os.Bundle
import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.appenjoy.AppEnjoyEvent
import com.mobteq.analytics.events.firebase.common.ClickEvent
import com.mobteq.analytics.events.firebase.common.ErrorEvent
import com.mobteq.analytics.events.firebase.screenview.ScreenViewEvent
import javax.inject.Inject

class MainTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val mixpanelAnalytics: MixpanelAnalytics
) {

    fun trackScreenViewEvent(screenName: String) {
        val event = ScreenViewEvent(screenName)
        firebaseAnalytics.logEvent(event)

        val params = mapOf(SCREEN_NAME to screenName)

        mixpanelAnalytics.logEvent(
            event.name, params
        )
    }

    fun trackAppEnjoyEvent(result: String) {
        val event = AppEnjoyEvent(result)
        firebaseAnalytics.logEvent(event)

        val params = mapOf(RESULT to result)

        mixpanelAnalytics.logEvent(
            event.name, params
        )
    }

    fun trackClickEvent(title: String, screenName: String) {
        val event = ClickEvent(title, screenName)
        firebaseAnalytics.logEvent(event)

        val params = mapOf(
            TITLE to title,
            SCREEN_NAME to screenName,
        )

        mixpanelAnalytics.logEvent(
            event.name, params
        )
    }

    fun trackErrorEvent(screenName: String, message: String) {
        val event = ErrorEvent(screenName, message)
        firebaseAnalytics.logEvent(event)

        val params = mapOf(
            SCREEN_NAME to screenName,
            MESSAGE to message
        )

        mixpanelAnalytics.logEvent(
            event.name, params
        )
    }

    fun trackEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event)
        mixpanelAnalytics.logEvent(event.name, bundleToMap(event.params))
    }

    private fun bundleToMap(bundle: Bundle): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        for (key in bundle.keySet()) {
            bundle.get(key)?.let { value ->
                map[key] = value
            }
        }
        return map
    }

    companion object {
        const val SCREEN_NAME = "screen_name"
        const val MESSAGE = "message"
        const val TITLE = "title"
        const val RESULT = "result"
    }
}
