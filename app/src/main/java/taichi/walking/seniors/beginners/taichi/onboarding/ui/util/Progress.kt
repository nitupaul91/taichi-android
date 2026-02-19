package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

fun progressFor(route: OnboardingRoutes): Float = route.index.toFloat() / route.total.toFloat()
