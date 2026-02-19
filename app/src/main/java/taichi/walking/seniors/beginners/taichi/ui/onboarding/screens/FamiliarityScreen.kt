package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun FamiliarityScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    SingleSelectScreen(
        progress = progressFor(OnboardingRoutes.Familiarity),
        title = "Are you familiar with Tai Chi?",
        subtitle = "No experience needed.",
        options = OnboardingOptions.familiarity,
        selectedId = state.taiChiFamiliarityId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.FAMILIARITY, it.id)) },
        onContinue = onContinue
    )
}
