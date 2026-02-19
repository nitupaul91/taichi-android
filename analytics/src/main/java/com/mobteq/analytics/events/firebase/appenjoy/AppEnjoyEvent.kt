package com.mobteq.analytics.events.firebase.appenjoy

import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.params.EventParamsName

data class AppEnjoyEvent(
    val result: String?,
    override val name: String = APP_ENJOY_EVENT
) : AnalyticsEvent() {

    init {
        params.apply {
            putString(EventParamsName.TITLE, result)
        }
    }

    companion object {
        const val APP_ENJOY_EVENT = "c_app_enjoy"
    }
}
