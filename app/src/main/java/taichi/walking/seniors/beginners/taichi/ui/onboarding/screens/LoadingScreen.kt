package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingSocialProof
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.RatingStars
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingScreen(
    onContinue: () -> Unit
) {
    var secondsRemaining by rememberSaveable { mutableIntStateOf(10) }
    var showCountdown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4_000)
        showCountdown = true
        for (second in 10 downTo 1) {
            secondsRemaining = second
            kotlinx.coroutines.delay(1_000)
        }
        onContinue()
    }

    // No rating request here: iOS took it out of onboarding (874528f). It is asked after the first
    // completed session and on a later visit to Home instead (ReviewPromptService).

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.Loading),
        title = "",
        subtitle = null,
        continueText = stringResource(R.string.please_wait),
        continueEnabled = false,
        onContinue = onContinue
    ) {
        LoadingProgress()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_loading_title),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.onboarding_loading_subtitle),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (showCountdown) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.loading_finalizing_plan, secondsRemaining),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        // iOS LoadingPlanScreen badges with the store rating (4e26ec1 on iOS), here both stores combined.
        SocialCard(
            icon = {
                RatingStars(
                    filled = OnboardingSocialProof.filledStars,
                    starSize = 22.dp,
                    color = Color(0xFFF5A623),
                    spacing = 4.dp
                )
            },
            text = stringResource(
                R.string.onboarding_loading_rating_count,
                OnboardingSocialProof.formattedRating(),
                OnboardingSocialProof.formattedRatingsCount() + "+"
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialCard(
            icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            text = stringResource(R.string.onboarding_loading_members_thousands)
        )
    }
}

@Composable
private fun LoadingProgress() {
    val progress = remember { Animatable(0f) }
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val primary = MaterialTheme.colorScheme.primary
    val primarySoft = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val ringRotation = rememberInfiniteTransition(label = "rot")
    val rotate by ringRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart),
        label = "ring"
    )

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(14_000, easing = FastOutSlowInEasing))
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val stroke = 16.dp.toPx()
                drawArc(
                    color = outline,
                    startAngle = 140f,
                    sweepAngle = 280f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = primary,
                    startAngle = 140f,
                    sweepAngle = 280f * progress.value,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Canvas(
                modifier = Modifier
                    .size(246.dp)
                .rotate(rotate)
            ) {
                drawArc(
                    color = primarySoft,
                    startAngle = 8f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 60.sp),
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun SocialCard(icon: @Composable () -> Unit, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
