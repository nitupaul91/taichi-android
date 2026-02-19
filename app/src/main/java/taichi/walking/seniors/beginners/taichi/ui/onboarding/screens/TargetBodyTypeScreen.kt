package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun TargetBodyTypeScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    ImageGridScreen(
        progress = progressFor(OnboardingRoutes.TargetBodyType),
        title = "Your TARGET body type",
        subtitle = "Pick the shape you’re aiming for.",
        options = OnboardingOptions.bodyTypesForGender(state.genderId),
        selectedId = state.targetBodyTypeId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.TARGET_BODY, it.id)) },
        onContinue = onContinue
    )
}
