package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.SeniorNumberPicker
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun TargetWeightScreen(
    state: OnboardingState,
    onChange: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val metricValues = (40..140).toList()
    val kgUnit = stringResource(R.string.unit_kg)
    val lbsUnit = stringResource(R.string.unit_lbs)
    val insight = targetWeightInsight(
        currentWeightKg = state.weightKg.toDouble(),
        targetWeightKg = state.targetWeightKg.toDouble()
    )
    val visibleValues = if (state.useMetric) metricValues else metricValues.map { (it * 2.20462).toInt() }.distinct()
    val selectedVisible = if (state.useMetric) state.targetWeightKg else (state.targetWeightKg * 2.20462).toInt()
    val selectedText = if (state.useMetric) {
        "${state.targetWeightKg} $kgUnit"
    } else {
        "$selectedVisible $lbsUnit"
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.TargetWeight),
        title = stringResource(R.string.onboarding_target_weight_title),
        subtitle = stringResource(R.string.onboarding_target_weight_subtitle),
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp))
                    .padding(horizontal = 14.dp, vertical = 18.dp)
            ) {
                Text(
                    text = selectedText,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    SeniorNumberPicker(
                        values = visibleValues,
                        selected = selectedVisible,
                        unit = "",
                        onValueChange = { visible ->
                            val metricValue = if (state.useMetric) visible else (visible / 2.20462).toInt()
                            onChange(OnboardingAction.TargetWeightChange(metricValue))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = insight.emoji,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(insight.title, insight.percentText),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(insight.message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
        }
    }
}

private data class TargetWeightInsight(
    val emoji: String,
    /** Format resource taking the percentage, e.g. "Easy win: lose %1$s of your weight". */
    @StringRes val title: Int,
    @StringRes val message: Int,
    val percentText: String
)

private fun targetWeightInsight(currentWeightKg: Double, targetWeightKg: Double): TargetWeightInsight {
    val goalChangePercent = if (currentWeightKg <= 0.0) 0.0 else kotlin.math.abs((targetWeightKg - currentWeightKg) / currentWeightKg) * 100.0
    val percentText = String.format("%.1f%%", goalChangePercent)
    val isWeightLossGoal = targetWeightKg < currentWeightKg

    return if (isWeightLossGoal) {
        when {
            goalChangePercent <= 5 -> TargetWeightInsight(
                emoji = "\uD83C\uDFC6",
                title = R.string.target_weight_lose_easy_title,
                message = R.string.target_weight_lose_easy_message,
                percentText = percentText
            )
            goalChangePercent <= 10 -> TargetWeightInsight(
                emoji = "\uD83D\uDC4C",
                title = R.string.target_weight_lose_realistic_title,
                message = R.string.target_weight_lose_realistic_message,
                percentText = percentText
            )
            goalChangePercent <= 20 -> TargetWeightInsight(
                emoji = "\uD83D\uDC9A",
                title = R.string.target_weight_lose_health_title,
                message = R.string.target_weight_lose_health_message,
                percentText = percentText
            )
            else -> TargetWeightInsight(
                emoji = "\uD83D\uDCAA",
                title = R.string.target_weight_lose_challenging_title,
                message = R.string.target_weight_lose_challenging_message,
                percentText = percentText
            )
        }
    } else {
        when {
            goalChangePercent <= 5 -> TargetWeightInsight(
                emoji = "\uD83C\uDFC6",
                title = R.string.target_weight_build_easy_title,
                message = R.string.target_weight_build_easy_message,
                percentText = percentText
            )
            goalChangePercent <= 10 -> TargetWeightInsight(
                emoji = "\uD83D\uDC4C",
                title = R.string.target_weight_build_realistic_title,
                message = R.string.target_weight_build_realistic_message,
                percentText = percentText
            )
            goalChangePercent <= 20 -> TargetWeightInsight(
                emoji = "\uD83D\uDC9A",
                title = R.string.target_weight_build_health_title,
                message = R.string.target_weight_build_health_message,
                percentText = percentText
            )
            else -> TargetWeightInsight(
                emoji = "\uD83D\uDD25",
                title = R.string.target_weight_build_challenging_title,
                message = R.string.target_weight_build_challenging_message,
                percentText = percentText
            )
        }
    }
}
