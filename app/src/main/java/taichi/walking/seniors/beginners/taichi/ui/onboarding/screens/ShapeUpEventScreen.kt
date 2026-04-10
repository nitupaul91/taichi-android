package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun ShapeUpEventScreen(
    state: OnboardingState,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    SingleSelectScreen(
        progress = progressFor(OnboardingRoutes.ShapeUpEvent),
        title = stringResource(R.string.onboarding_shape_up_event_title),
        subtitle = stringResource(R.string.onboarding_shape_up_event_subtitle),
        options = OnboardingOptions.shapeUpEvents,
        selectedId = state.shapeUpEventId,
        onBack = onBack,
        onSelect = { onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.SHAPE_UP_EVENT, it.id)) },
        onContinue = onContinue
    )
}
