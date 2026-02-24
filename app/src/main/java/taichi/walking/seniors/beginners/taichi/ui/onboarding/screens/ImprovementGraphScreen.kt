package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.graph.ValuePropositionGraph
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun ImprovementGraphScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.ImprovementGraph),
        title = stringResource(R.string.onboarding_improvement_title),
        subtitle = stringResource(R.string.onboarding_improvement_subtitle),
        onBack = onBack,
        continueText = stringResource(R.string.continue_button),
        onContinue = onContinue
    ) {
        ValuePropositionGraph(data = listOf(20f, 32f, 44f, 55f, 65f, 72f, 78f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.onboarding_improvement_description), style = MaterialTheme.typography.bodyLarge)
    }
}
