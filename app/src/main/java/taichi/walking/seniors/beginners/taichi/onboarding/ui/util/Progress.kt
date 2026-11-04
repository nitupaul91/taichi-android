package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingSteps

/** The progress bar position of [route] in this user's step list (iOS `topHeaderProgress`). */
@Composable
fun progressFor(route: OnboardingRoutes): Float =
    OnboardingSteps.progress(route, LocalOnboardingChrome.current.navigationSteps)
