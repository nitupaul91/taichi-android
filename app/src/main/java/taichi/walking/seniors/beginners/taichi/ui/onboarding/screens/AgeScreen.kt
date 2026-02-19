package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun AgeScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    SingleSelectScreen(
        progress = progressFor(OnboardingRoutes.Age),
        title = "What is your age range?",
        subtitle = "This helps us tailor your practice.",
        options = OnboardingOptions.ageRanges,
        selectedId = state.ageRangeId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.AGE, it.id)) },
        onContinue = onContinue
    )
}
