package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        title = "Your 4‑week improvement",
        subtitle = "A gentle upward trend with consistent practice.",
        onBack = onBack,
        continueText = "Continue",
        onContinue = onContinue
    ) {
        ValuePropositionGraph(data = listOf(20f, 32f, 44f, 55f, 65f, 72f, 78f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Week 4: steadier balance and calmer sleep", style = MaterialTheme.typography.bodyLarge)
    }
}
