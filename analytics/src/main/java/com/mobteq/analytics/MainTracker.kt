package com.mobteq.analytics

import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.appenjoy.AppEnjoyEvent
import com.mobteq.analytics.events.firebase.common.ClickEvent
import com.mobteq.analytics.events.firebase.common.ErrorEvent
import com.mobteq.analytics.events.firebase.screenview.ScreenViewEvent
import javax.inject.Inject

class MainTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun trackScreenViewEvent(screenName: String) {
        val event = ScreenViewEvent(screenName)
        firebaseAnalytics.logEvent(event)
    }

    fun trackAppEnjoyEvent(result: String) {
        val event = AppEnjoyEvent(result)
        firebaseAnalytics.logEvent(event)
    }

    fun trackClickEvent(title: String, screenName: String) {
        val event = ClickEvent(title, screenName)
        firebaseAnalytics.logEvent(event)
    }

    fun trackErrorEvent(screenName: String, message: String) {
        val event = ErrorEvent(screenName, message)
        firebaseAnalytics.logEvent(event)
    }

    fun trackEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event)
    }

    companion object {
        const val SCREEN_NAME = "screen_name"
        const val MESSAGE = "message"
        const val TITLE = "title"
        const val RESULT = "result"
    }
}
