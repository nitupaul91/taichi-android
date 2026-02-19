package com.mobteq.analytics.events.firebase.common

import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.analytics.events.firebase.params.EventParamsName

data class ClickEvent(
    val screen: String?,
    val title: String?,
    override val name: String = CLICK_EVENT
) : AnalyticsEvent() {

    init {
        params.apply {
            putString(EventParamsName.TITLE, title)
            putString(EventParamsName.SCREEN, screen)
        }
    }

    companion object {
        const val CLICK_EVENT = "click_event"
    }
}
