package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PlanReadyScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var showOne by remember { mutableStateOf(false) }
    var showTwo by remember { mutableStateOf(false) }
    var showThree by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(160)
        showOne = true
        delay(120)
        showTwo = true
        delay(120)
        showThree = true
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.PlanReady),
        title = "",
        subtitle = null,
        onBack = onBack,
        continueText = stringResource(R.string.onboarding_plan_ready_button),
        onContinue = onContinue
    ) {
        ReadyBadge()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_ready_title),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.onboarding_plan_ready_subtitle),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        AnimatedVisibility(visible = showOne, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })) {
            ReadyFeature(icon = Icons.Default.QueryBuilder, text = stringResource(R.string.onboarding_plan_ready_feature_1))
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = showTwo, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })) {
            ReadyFeature(icon = Icons.Default.CalendarMonth, text = stringResource(R.string.onboarding_plan_ready_feature_2))
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = showThree, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })) {
            ReadyFeature(icon = Icons.Default.ShowChart, text = stringResource(R.string.onboarding_plan_ready_feature_3))
        }
    }
}

@Composable
private fun ReadyBadge() {
    val ringOne = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val ringTwo = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val ringThree = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(230.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val c = size.minDimension / 2
                drawCircle(color = ringOne, radius = c)
                drawCircle(color = ringTwo, radius = c * 0.75f)
                drawCircle(color = ringThree, radius = c * 0.5f)
            }
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

@Composable
private fun ReadyFeature(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
