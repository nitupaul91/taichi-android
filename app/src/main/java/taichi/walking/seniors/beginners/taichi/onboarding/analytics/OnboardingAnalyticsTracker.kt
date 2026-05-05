package taichi.walking.seniors.beginners.taichi.onboarding.analytics

import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.TikTokAnalyticsTracker
import javax.inject.Inject

class OnboardingAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val tikTokAnalyticsTracker: TikTokAnalyticsTracker,
) {
    fun trackScreenView(screenIndex: Int, stepName: String) {
        firebaseAnalytics.logEvent(
            "onboarding_view_${screenIndex + 1}",
            bundleOf("step_name" to stepName)
        )
    }

    fun trackOnboardingComplete() {
        firebaseAnalytics.logEvent("onboarding_completed")
        tikTokAnalyticsTracker.trackCustomEvent("onboarding_complete")
    }
}
