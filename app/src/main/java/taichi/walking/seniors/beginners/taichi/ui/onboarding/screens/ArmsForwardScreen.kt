package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun ArmsForwardScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    SingleSelectScreen(
        progress = progressFor(OnboardingRoutes.ArmsForward),
        title = "Can you hold your arms forward for 5 seconds?",
        subtitle = "This helps set a safe starting point.",
        options = OnboardingOptions.yesNo,
        selectedId = state.armsForwardId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.ARMS_FORWARD, it.id)) },
        onContinue = onContinue
    )
}
