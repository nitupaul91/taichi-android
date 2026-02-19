package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun TaiChiLevelScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    SingleSelectScreen(
        progress = progressFor(OnboardingRoutes.TaiChiLevel),
        title = "Your Tai Chi level",
        subtitle = "We’ll adjust the plan to your level.",
        options = OnboardingOptions.taiChiLevel,
        selectedId = state.taiChiLevelId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.TAI_CHI_LEVEL, it.id)) },
        onContinue = onContinue
    )
}
