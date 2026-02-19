package com.mobteq.analytics.events.firebase.common

import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.params.EventParamsName

class ErrorEvent(
    val screen: String?,
    val message: String?,
    override val name: String = ERROR_EVENT
) : AnalyticsEvent() {

    init {
        params.apply {
            putString(EventParamsName.TITLE, message)
            putString(EventParamsName.SCREEN, screen)
        }
    }

    companion object {
        const val ERROR_EVENT = "error_event"
    }
}
