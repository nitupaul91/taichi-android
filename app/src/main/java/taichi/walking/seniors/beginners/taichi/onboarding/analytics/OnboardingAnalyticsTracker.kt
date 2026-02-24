package taichi.walking.seniors.beginners.taichi.onboarding.analytics

import com.mobteq.analytics.FirebaseAnalytics
import javax.inject.Inject

class OnboardingAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun trackScreenView(screenIndex: Int) {
        firebaseAnalytics.logEvent("onboarding_view_$screenIndex")
    }

    fun trackOnboardingComplete() {
        firebaseAnalytics.logEvent("onboarding_complete")
    }
}
