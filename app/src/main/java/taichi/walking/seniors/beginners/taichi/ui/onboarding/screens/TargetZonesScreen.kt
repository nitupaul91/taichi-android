package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun TargetZonesScreen(
    state: OnboardingState,
    onToggle: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    MultiSelectScreen(
        progress = progressFor(OnboardingRoutes.TargetZones),
        title = "Which areas need the most care?",
        subtitle = "Select all that apply.",
        options = OnboardingOptions.zones,
        selectedIds = state.targetZones,
        onBack = onBack,
        onToggle = { onToggle(OnboardingAction.MultiToggle(OnboardingQuestions.ZONES, it.id)) },
        onContinue = onContinue
    )
}
