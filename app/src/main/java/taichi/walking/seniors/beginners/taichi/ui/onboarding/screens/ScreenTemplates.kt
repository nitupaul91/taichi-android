package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingOption
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.ImageGridQuestion
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.MultiSelectQuestion
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.SeniorNumberPicker
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.SingleSelectQuestion
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SingleSelectScreen(
    progress: Float,
    title: String,
    subtitle: String,
    options: List<OnboardingOption>,
    selectedId: String?,
    onBack: () -> Unit,
    onSelect: (OnboardingOption) -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        continueEnabled = selectedId != null,
        onContinue = onContinue
    ) {
        SingleSelectQuestion(options = options, selectedId = selectedId, onSelect = onSelect)
    }
}

@Composable
fun MultiSelectScreen(
    progress: Float,
    title: String,
    subtitle: String,
    options: List<OnboardingOption>,
    selectedIds: Set<String>,
    onBack: () -> Unit,
    onToggle: (OnboardingOption) -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        continueEnabled = selectedIds.isNotEmpty(),
        onContinue = onContinue
    ) {
        MultiSelectQuestion(options = options, selectedIds = selectedIds, onToggle = onToggle)
    }
}

@Composable
fun ImageGridScreen(
    progress: Float,
    title: String,
    subtitle: String,
    options: List<OnboardingOption>,
    selectedId: String?,
    onBack: () -> Unit,
    onSelect: (OnboardingOption) -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        continueEnabled = selectedId != null,
        onContinue = onContinue
    ) {
        ImageGridQuestion(options = options, selectedId = selectedId, onSelect = onSelect)
    }
}

@Composable
fun NumberPickerScreen(
    progress: Float,
    title: String,
    subtitle: String,
    metricValues: List<Int>,
    selectedMetric: Int,
    metricUnit: String,
    imperialUnit: String,
    metricLabel: (Int) -> String,
    imperialLabel: (Int) -> String,
    metricToImperial: (Int) -> Int,
    imperialToMetric: (Int) -> Int,
    useMetric: Boolean,
    onUnitChange: (Boolean) -> Unit,
    showUnitSwitcher: Boolean = true,
    onBack: () -> Unit,
    onChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val visibleValues = if (useMetric) metricValues else metricValues.map(metricToImperial).distinct()
    val selectedVisible = if (useMetric) selectedMetric else metricToImperial(selectedMetric)
    val selectedText = if (useMetric) metricLabel(selectedMetric) else imperialLabel(selectedVisible)

    QuestionScaffold(
        progress = progress,
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp))
                .padding(horizontal = 14.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showUnitSwitcher) {
                UnitSwitcher(
                    metricUnit = metricUnit,
                    imperialUnit = imperialUnit,
                    useMetric = useMetric,
                    onMetricClick = { onUnitChange(true) },
                    onImperialClick = { onUnitChange(false) }
                )
                Spacer(modifier = Modifier.height(22.dp))
            }
            Text(
                text = selectedText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 66.sp),
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
                        val metric = if (useMetric) visible else imperialToMetric(visible)
                        onChange(metric)
                    }
                )
            }
        }
    }
}

@Composable
private fun UnitSwitcher(
    metricUnit: String,
    imperialUnit: String,
    useMetric: Boolean,
    onMetricClick: () -> Unit,
    onImperialClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = metricUnit,
            modifier = Modifier
                .background(
                    if (useMetric) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onMetricClick() }
                .padding(horizontal = 32.dp, vertical = 14.dp),
            color = if (useMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = imperialUnit,
            modifier = Modifier
                .background(
                    if (!useMetric) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onImperialClick() }
                .padding(horizontal = 30.dp, vertical = 14.dp),
            color = if (!useMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ValuePropScreen(
    progress: Float,
    title: String,
    subtitle: String,
    body: String,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = title,
        subtitle = subtitle,
        showProgress = false,
        onBack = onBack,
        continueEnabled = true,
        onContinue = onContinue
    ) {
        Text(text = body, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
    }
}
