package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun WeightScreen(
    state: OnboardingState,
    onChange: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val metricValues = (40..140).toList()
    val kgUnit = stringResource(R.string.unit_kg)
    val lbsUnit = stringResource(R.string.unit_lbs)
    NumberPickerScreen(
        progress = progressFor(OnboardingRoutes.Weight),
        title = stringResource(R.string.onboarding_weight_title),
        subtitle = stringResource(R.string.onboarding_weight_subtitle),
        metricValues = metricValues,
        selectedMetric = state.weightKg,
        metricUnit = kgUnit,
        imperialUnit = lbsUnit,
        metricLabel = { "$it $kgUnit" },
        imperialLabel = { "$it $lbsUnit" },
        metricToImperial = { kg -> (kg * 2.20462).toInt() },
        imperialToMetric = { lbs -> (lbs / 2.20462).toInt() },
        useMetric = state.useMetric,
        onUnitChange = { onChange(OnboardingAction.MeasurementSystemChange(it)) },
        onBack = onBack,
        onChange = { onChange(OnboardingAction.WeightChange(it)) },
        onContinue = onContinue
    )
}
