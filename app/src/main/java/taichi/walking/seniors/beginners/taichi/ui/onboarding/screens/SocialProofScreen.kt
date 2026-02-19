package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun SocialProofScreen(
    progress: Float,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = "Trusted by Thousands",
        subtitle = "Over 10M members have started a gentle Tai Chi routine.",
        onBack = onBack,
        onContinue = onContinue
    ) {
        TestimonialCard("At 72, I finally found an exercise I can do every day.", "Margaret S., 72")
        Spacer(modifier = Modifier.height(12.dp))
        TestimonialCard("The gentle movements have helped with my balance.", "Robert M., 68")
        Spacer(modifier = Modifier.height(12.dp))
        TestimonialCard("I sleep better and have more energy throughout the day.", "Patricia L., 75")
    }
}

@Composable
private fun TestimonialCard(quote: String, name: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "★★★★★", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "\"$quote\"", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
