package com.mobteq.analytics.events.firebase

import android.os.Bundle
import androidx.core.os.bundleOf

abstract class AnalyticsEvent {
    abstract val name: String
    val params: Bundle = bundleOf()
}
