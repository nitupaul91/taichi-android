package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun ExerciseBlockersScreen(
    state: OnboardingState,
    onToggle: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    MultiSelectScreen(
        progress = progressFor(OnboardingRoutes.ExerciseBlockers),
        title = "What challenges do you face?",
        subtitle = "Select all that apply.",
        options = OnboardingOptions.blockers,
        selectedIds = state.exerciseBlockers,
        onBack = onBack,
        onToggle = { onToggle(OnboardingAction.MultiToggle(OnboardingQuestions.BLOCKERS, it.id)) },
        onContinue = onContinue
    )
}
