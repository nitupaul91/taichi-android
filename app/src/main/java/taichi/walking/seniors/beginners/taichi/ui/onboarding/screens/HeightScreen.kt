package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun HeightScreen(
    state: OnboardingState,
    onChange: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val metricValues = (140..200).toList()
    val cmUnit = stringResource(R.string.unit_cm)
    NumberPickerScreen(
        progress = progressFor(OnboardingRoutes.Height),
        title = stringResource(R.string.onboarding_height_title),
        subtitle = stringResource(R.string.onboarding_height_subtitle),
        metricValues = metricValues,
        selectedMetric = state.heightCm,
        metricUnit = cmUnit,
        imperialUnit = stringResource(R.string.unit_ft_in),
        metricLabel = { "$it $cmUnit" },
        imperialLabel = {
            val feet = it / 12
            val inches = it % 12
            "$feet'${inches}\""
        },
        metricToImperial = { cm -> (cm / 2.54).toInt() },
        imperialToMetric = { inches -> (inches * 2.54).toInt() },
        onBack = onBack,
        onChange = { onChange(OnboardingAction.HeightChange(it)) },
        onContinue = onContinue
    )
}
