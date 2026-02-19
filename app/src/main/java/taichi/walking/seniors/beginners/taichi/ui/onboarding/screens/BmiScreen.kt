package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip

@Composable
fun BmiScreen(
    state: OnboardingState,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val bmi = state.bmi
    val bmiText = String.format("%.1f", bmi)
    val (label, message) = bmiFeedback(bmi)

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.Bmi),
        title = "Your BMI",
        subtitle = "Here's where you are right now.",
        onBack = onBack,
        onContinue = onContinue
    ) {
        BmiRing(value = bmiText)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        BmiZoneLegend()
    }
}

@Composable
private fun BmiRing(value: String) {
    val progress = remember { Animatable(0f) }
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val ringStart = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val ringEnd = MaterialTheme.colorScheme.primary
    LaunchedEffect(Unit) {
        progress.animateTo(0.82f, tween(1100, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(250.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = outlineColor,
                startAngle = 130f,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ringStart,
                        ringEnd
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                ),
                startAngle = 130f,
                sweepAngle = 280f * progress.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 72.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BMI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun BmiZoneLegend() {
    val lowColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
    val healthyColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val highColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            lowColor,
                            healthyColor,
                            highColor
                        )
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Low           Healthy           Higher",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}

private fun bmiFeedback(bmi: Double): Pair<String, String> {
    return when {
        bmi < 18.5 -> "Getting Started" to "You're on the lighter side. We'll focus on strength and steady progress."
        bmi < 25.0 -> "Healthy" to "Great! You're in a healthy range. Let's keep building balance and mobility."
        bmi < 30.0 -> "Balanced Progress" to "You're close to a healthy range. Consistent practice can make a real difference."
        else -> "Step by Step" to "We'll move gently and safely, with routines designed for your comfort."
    }
}
