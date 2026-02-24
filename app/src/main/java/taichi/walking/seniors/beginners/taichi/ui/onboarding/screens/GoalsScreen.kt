package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun GoalsScreen(
    state: OnboardingState,
    onToggle: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    MultiSelectScreen(
        progress = progressFor(OnboardingRoutes.Goals),
        title = stringResource(R.string.onboarding_goals_title),
        subtitle = stringResource(R.string.select_all_that_apply),
        options = OnboardingOptions.goals,
        selectedIds = state.goals,
        onBack = onBack,
        onToggle = { onToggle(OnboardingAction.MultiToggle(OnboardingQuestions.GOALS, it.id)) },
        onContinue = onContinue
    )
}
