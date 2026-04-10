package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun BmiScreen(
    state: OnboardingState,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val bmi = state.bmi
    val category = bmiCategory(bmi)
    val insight = bmiInsight(category)

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.Bmi),
        title = "",
        subtitle = null,
        onBack = onBack,
        topInsetExtra = 0.dp,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Health Status",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 38.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Based on your BMI and activity level",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            BmiResultRing(bmi = bmi, category = category)
            Spacer(modifier = Modifier.height(24.dp))
            BmiInsightCard(
                title = insight.title,
                message = insight.message,
                statusColor = bmiStatusColor(category)
            )
        }
    }
}

@Composable
private fun BmiResultRing(
    bmi: Double,
    category: String
) {
    val progress = remember { Animatable(0f) }
    val statusColor = bmiStatusColor(category)
    val backgroundColor = Color(0xFFEBEFF4)
    val ringGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFFFA51B),
            Color(0xFFFF7B1F),
            Color(0xFFFF8F17),
            Color(0xFFE8D813),
            Color(0xFF31D29A),
            Color(0xFFE8D813)
        )
    )

    LaunchedEffect(bmi) {
        progress.animateTo(bmiRingProgress(bmi), tween(1100, easing = FastOutSlowInEasing))
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(250.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = backgroundColor,
                startAngle = -61.2f,
                sweepAngle = 302.4f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = ringGradient,
                startAngle = -61.2f,
                sweepAngle = 302.4f * progress.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", bmi),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 58.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BMI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp),
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BmiInsightCard(
    title: String,
    message: String,
    statusColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.96f), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = statusColor
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
        )
    }
}

private data class BmiInsight(
    val title: String,
    val message: String
)

private fun bmiCategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Healthy"
        bmi < 30.0 -> "Overweight"
        else -> "Elevated"
    }
}

private fun bmiInsight(category: String): BmiInsight {
    return when (category) {
        "Healthy" -> BmiInsight(
            title = "Your BMI is healthy.",
            message = "Maintaining muscle mass and staying active can help preserve your energy, balance, and long-term health."
        )
        "Underweight" -> BmiInsight(
            title = "Your BMI is low.",
            message = "Building strength and healthy body mass can improve resilience, mobility, and day-to-day energy."
        )
        "Overweight" -> BmiInsight(
            title = "Your BMI is above average.",
            message = "Increasing daily movement and improving body composition can help lower strain on your joints and metabolism."
        )
        else -> BmiInsight(
            title = "Your BMI is elevated.",
            message = "Improving muscle mass and daily activity can significantly reduce health risks."
        )
    }
}

private fun bmiStatusColor(category: String): Color {
    return when (category) {
        "Healthy" -> Color(0xFF31D29A)
        "Underweight" -> Color(0xFF8CB4FF)
        "Overweight" -> Color(0xFFFF8A1E)
        else -> Color(0xFFE15D44)
    }
}

private fun bmiRingProgress(bmi: Double): Float {
    return when {
        bmi < 18.5 -> maxOf(0.18f, ((bmi / 18.5) * 0.42).toFloat())
        bmi < 25.0 -> 0.42f + (((bmi - 18.5) / 6.5) * 0.20).toFloat()
        bmi < 30.0 -> 0.62f + (((bmi - 25.0) / 5.0) * 0.16).toFloat()
        else -> minOf(0.90f, 0.78f + (((bmi - 30.0) / 10.0) * 0.12).toFloat())
    }
}
