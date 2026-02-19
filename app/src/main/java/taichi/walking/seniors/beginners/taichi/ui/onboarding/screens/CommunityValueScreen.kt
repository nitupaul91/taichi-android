package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.OptionCard
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingOptions

@Composable
fun CommunityValueScreen(
    progress: Float,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = "A calm, supportive community",
        subtitle = "We keep it simple and gentle from day one.",
        onBack = onBack,
        onContinue = onContinue
    ) {
        OnboardingOptions.communityPoints.forEach { item ->
            OptionCard(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                selected = false,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
