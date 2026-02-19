package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold

@Composable
fun ValuePropAlmostDoneScreen(
    progress: Float,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progress,
        title = "",
        subtitle = null,
        showProgress = true,
        onBack = onBack,
        continueEnabled = true,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(110.dp))
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Finish flag",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(86.dp)
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "You're almost done",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Just a few more details to personalize your plan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "A calm routine, built around your comfort and daily life.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
