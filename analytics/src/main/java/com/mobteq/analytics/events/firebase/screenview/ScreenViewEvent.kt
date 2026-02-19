package com.mobteq.analytics.events.firebase.screenview

import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.params.EventParamsName

data class ScreenViewEvent(
    val screenName: String,
    override val name: String = SCREEN_VIEW
) : AnalyticsEvent() {

    init {
        params.apply {
            putString(EventParamsName.SCREEN, screenName)
        }
    }

    companion object {
        const val SCREEN_VIEW = "screen_view"
    }
}