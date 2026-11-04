package taichi.walking.seniors.beginners.taichi.ui.settings.screen

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import javax.inject.Inject

/** Settings events, named exactly as the iOS app names them so both platforms share dashboards. */
class SettingsAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) {

    fun trackAccountDeleteTapped() = trackEvent(ACCOUNT_DELETE_TAPPED)

    fun trackAccountDeleteConfirmed() = trackEvent(ACCOUNT_DELETE_CONFIRMED)

    /** [outcome] is `deleted` or `failed:<reason>`. */
    fun trackAccountDeleteOutcome(outcome: String) =
        trackEvent(ACCOUNT_DELETE_OUTCOME, bundleOf(PARAM_OUTCOME to outcome))

    fun trackSubscriptionCancelTapped() = trackEvent(SUBSCRIPTION_CANCEL_TAPPED)

    /**
     * One event for the whole cancellation funnel, told apart by its `event` parameter. Firebase
     * takes no booleans, so they are sent as 1/0, which is what the iOS SDK records for a Bool.
     */
    fun trackCancellationFlow(parameters: Map<String, Any>) {
        val params = bundleOf()
        parameters.forEach { (key, value) ->
            when (value) {
                is Boolean -> params.putLong(key, if (value) 1L else 0L)
                is Int -> params.putLong(key, value.toLong())
                is Long -> params.putLong(key, value)
                else -> params.putString(key, value.toString())
            }
        }
        trackEvent(SUBSCRIPTION_CANCELLATION_FLOW, params)
    }

    private fun trackEvent(name: String, params: Bundle = bundleOf()) {
        firebaseAnalytics.logEvent(name, params)
    }

    private companion object {
        const val ACCOUNT_DELETE_TAPPED = "account_delete_tapped"
        const val ACCOUNT_DELETE_CONFIRMED = "account_delete_confirmed"
        const val ACCOUNT_DELETE_OUTCOME = "account_delete_outcome"
        const val SUBSCRIPTION_CANCEL_TAPPED = "subscription_cancel_tapped"
        const val SUBSCRIPTION_CANCELLATION_FLOW = "subscription_cancellation_flow"

        const val PARAM_OUTCOME = "outcome"
    }
}
