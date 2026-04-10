package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeCompletionResult
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.util.InAppReviewHelper
import taichi.walking.seniors.beginners.util.sharePlainText
import kotlinx.coroutines.delay

@Composable
fun WorkoutCompletionScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    practiceKind: PracticeKind,
    completionResult: PracticeCompletionResult?,
    onReturnHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val title = completionResult?.title
    val calories = dayData.exercises.sumOf { it.calories }
    val shareText = buildString {
        append("I just completed Day $dayNumber of ${practiceKind.displayName} \uD83E\uDDD8\n")
        title?.let { append("${it.icon} ${it.title}\n") }
        append("\u23F1 ${dayData.durationInMinutes} minutes\n")
        append("\uD83D\uDD25 ${completionResult?.currentStreak ?: 0} day streak\n\n")
        append("Start your journey:\n")
        append(context.getString(R.string.app_link))
    }

    LaunchedEffect(Unit) {
        delay(1_500)
        if (activity != null) {
            InAppReviewHelper.launchIfAvailable(activity)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5EE),
                        Color(0xFFF6FBF7),
                        Color.White
                    )
                )
            )
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            title?.let {
                Text(
                    text = "${it.icon} ${it.title}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3C42),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2728",
                    fontSize = 16.sp
                )
                Text(
                    text = "Day $dayNumber Complete",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A5A60)
                )
            }

            title?.let {
                Image(
                    painter = painterResource(id = it.badgeDrawableRes),
                    contentDescription = it.title,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(130.dp)
                )
                Text(
                    text = it.encouragement,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
                    color = Color(0xFF6A7A80),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFF5C6BC0),
                    iconBackground = Color(0xFFE8EAF6),
                    value = dayData.durationInMinutes.toString(),
                    label = "min"
                )
                VerticalDivider()
                StatColumn(
                    icon = Icons.Default.FlashOn,
                    iconColor = Color(0xFFEF5350),
                    iconBackground = Color(0xFFFDECEA),
                    value = calories.toString(),
                    label = "cal"
                )
                VerticalDivider()
                StatColumn(
                    icon = Icons.Default.DirectionsWalk,
                    iconColor = Color(0xFF0BA56B),
                    iconBackground = Color(0xFFE6F5EE),
                    value = dayData.exercises.size.toString(),
                    label = "moves"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { sharePlainText(context, shareText, "Share your progress") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x1F0BA56B),
                        contentColor = Color(0xFF0BA56B)
                    )
                ) {
                    Text("Share", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReturnHome,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0BA56B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3C42)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7D8A8F)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 44.dp)
            .background(Color(0xFFE1E8E4))
    )
}
