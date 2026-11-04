package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import androidx.compose.runtime.compositionLocalOf
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingSteps

/**
 * What every onboarding screen reads from the flow rather than hard-coding: the user's step list,
 * which the progress bars are drawn against (iOS `OnboardingRootView.navigationSteps`).
 */
data class OnboardingChrome(
    val navigationSteps: List<OnboardingRoutes> = OnboardingSteps.allCases
)

val LocalOnboardingChrome = compositionLocalOf { OnboardingChrome() }
