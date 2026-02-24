package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.R
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
        title = stringResource(R.string.onboarding_community_title),
        subtitle = stringResource(R.string.onboarding_community_subtitle),
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
