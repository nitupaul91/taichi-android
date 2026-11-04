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

    private fun trackEvent(name: String, params: Bundle = bundleOf()) {
        firebaseAnalytics.logEvent(name, params)
    }

    private companion object {
        const val ACCOUNT_DELETE_TAPPED = "account_delete_tapped"
        const val ACCOUNT_DELETE_CONFIRMED = "account_delete_confirmed"
        const val ACCOUNT_DELETE_OUTCOME = "account_delete_outcome"

        const val PARAM_OUTCOME = "outcome"
    }
}
