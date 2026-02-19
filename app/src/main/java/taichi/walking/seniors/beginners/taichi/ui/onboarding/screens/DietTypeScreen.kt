package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun DietTypeScreen(
    state: OnboardingState,
    onToggle: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    MultiSelectScreen(
        progress = progressFor(OnboardingRoutes.DietType),
        title = "What best describes your diet?",
        subtitle = "Select all that apply.",
        options = OnboardingOptions.diet,
        selectedIds = state.dietTypes,
        onBack = onBack,
        onToggle = { onToggle(OnboardingAction.MultiToggle(OnboardingQuestions.DIET, it.id)) },
        onContinue = onContinue
    )
}
