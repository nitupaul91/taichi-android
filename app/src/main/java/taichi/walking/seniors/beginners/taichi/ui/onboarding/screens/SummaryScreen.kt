package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.graph.ValuePropositionGraph
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import taichi.walking.seniors.beginners.R

@Composable
fun SummaryScreen(
    state: OnboardingState,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val projectedLoss = (state.weightKg - state.targetWeightKg).coerceAtLeast(0)
    var showLevel by remember { mutableStateOf(false) }
    var showMotivation by remember { mutableStateOf(false) }
    var showBmi by remember { mutableStateOf(false) }
    var showActivity by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(180)
        showLevel = true
        delay(130)
        showMotivation = true
        delay(130)
        showBmi = true
        delay(130)
        showActivity = true
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.Summary),
        title = stringResource(R.string.onboarding_summary_title),
        subtitle = "",
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GraphCard(
                current = state.weightKg,
                target = state.targetWeightKg,
                projectedLoss = projectedLoss
            )

            AnimatedVisibility(
                visible = showLevel,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SummaryInfoRow(
                    icon = Icons.Default.SelfImprovement,
                    label = stringResource(R.string.onboarding_summary_taichi_level),
                    value = state.taiChiLevelId?.replaceFirstChar { it.uppercase() } ?: stringResource(R.string.onboarding_summary_taichi_level_default)
                )
            }
            AnimatedVisibility(
                visible = showMotivation,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SummaryInfoRow(
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(R.string.onboarding_summary_motivation),
                    value = mapMotivation(state.motivationLevelId)
                )
            }
            AnimatedVisibility(
                visible = showBmi,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SummaryInfoRow(
                    icon = Icons.Default.FitnessCenter,
                    label = stringResource(R.string.onboarding_summary_bmi),
                    value = String.format("%.1f (%s)", state.bmi, bmiTag(state.bmi))
                )
            }
            AnimatedVisibility(
                visible = showActivity,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                SummaryInfoRow(
                    icon = Icons.Default.DirectionsRun,
                    label = stringResource(R.string.onboarding_summary_activity),
                    value = mapActivity(state.activityLevelId)
                )
            }
        }
    }
}

@Composable
private fun GraphCard(current: Int, target: Int, projectedLoss: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.onboarding_summary_weight_journey), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.onboarding_summary_projected_loss), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${projectedLoss.toFloat()} ${stringResource(R.string.unit_kg)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.onboarding_summary_to_lose), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        ValuePropositionGraph(data = listOf(current.toFloat(), current - 0.6f, current - 1.1f, current - 1.6f, target.toFloat()))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.onboarding_summary_now), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("${current}.0 ${stringResource(R.string.unit_kg)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.onboarding_summary_in_4_weeks), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                Text("${target}.0 ${stringResource(R.string.unit_kg)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun mapMotivation(id: String?): String = when (id) {
    "very_motivated" -> "Very motivated"
    "motivated" -> "Motivated"
    "need_help" -> "Need some help"
    "struggling" -> "Struggling"
    else -> "Motivated"
}

private fun mapActivity(id: String?): String = when (id) {
    "very_active" -> "Very active"
    "moderately_active" -> "Moderately active"
    "lightly_active" -> "Light activity"
    "sedentary" -> "Mostly sitting"
    else -> "Moderately active"
}

private fun bmiTag(bmi: Double): String = when {
    bmi < 18.5 -> "Low"
    bmi < 25.0 -> "Healthy"
    bmi < 30.0 -> "Balanced"
    else -> "Higher"
}
