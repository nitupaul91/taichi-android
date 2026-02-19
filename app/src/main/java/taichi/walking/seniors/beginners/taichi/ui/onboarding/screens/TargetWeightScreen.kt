package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun TargetWeightScreen(
    state: OnboardingState,
    onChange: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val metricValues = (40..140).toList()
    NumberPickerScreen(
        progress = progressFor(OnboardingRoutes.TargetWeight),
        title = "What's your goal weight?",
        subtitle = "We'll help you work towards it.",
        metricValues = metricValues,
        selectedMetric = state.targetWeightKg,
        metricUnit = "kg",
        imperialUnit = "lbs",
        metricLabel = { "$it kg" },
        imperialLabel = { "$it lbs" },
        metricToImperial = { kg -> (kg * 2.20462).toInt() },
        imperialToMetric = { lbs -> (lbs / 2.20462).toInt() },
        onBack = onBack,
        onChange = { onChange(OnboardingAction.TargetWeightChange(it)) },
        onContinue = onContinue
    )
}
