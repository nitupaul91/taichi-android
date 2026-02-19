package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.runtime.Composable
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
    NumberPickerScreen(
        progress = progressFor(OnboardingRoutes.Height),
        title = "What's your height?",
        subtitle = "This helps us calculate your health metrics.",
        metricValues = metricValues,
        selectedMetric = state.heightCm,
        metricUnit = "cm",
        imperialUnit = "ft/in",
        metricLabel = { "$it cm" },
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
