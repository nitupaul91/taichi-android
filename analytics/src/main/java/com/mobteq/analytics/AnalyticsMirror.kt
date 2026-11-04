package com.mobteq.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Every analytics event the app logs to Firebase is also handed to [sink] — the app's own
 * uploader for the backend user-journey dashboard (iOS AnalyticsService.track mirrors each call to
 * AppEventUploader the same way). Set once at startup; null drops the copy.
 */
object AnalyticsMirror {
    @Volatile
    var sink: ((name: String, params: Bundle) -> Unit)? = null

    internal fun mirror(name: String, params: Bundle) {
        runCatching { sink?.invoke(name, params) }
    }
}

/** Logs [name] to Firebase and to the journey mirror. For call sites without injection (Composables). */
fun logAppEvent(name: String, params: Bundle = Bundle()) {
    Firebase.analytics.logEvent(name, params.forFirebase())
    AnalyticsMirror.mirror(name, params)
}

/**
 * Firebase Analytics on Android accepts only String, Long and Double parameters and silently drops
 * Booleans; the iOS SDK logs a Bool as 1 / 0. Booleans become 1L / 0L for Firebase, so both
 * platforms report the same numbers; the journey mirror keeps true / false, as iOS uploads them.
 */
@Suppress("DEPRECATION")
internal fun Bundle.forFirebase(): Bundle {
    if (keySet().none { get(it) is Boolean }) return this
    return Bundle(this).also { copy ->
        keySet().forEach { key ->
            val value = get(key)
            if (value is Boolean) copy.putLong(key, if (value) 1L else 0L)
        }
    }
}
