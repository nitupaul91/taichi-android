package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.R
import androidx.compose.ui.res.stringResource
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeTitleService
import com.mobteq.analytics.logAppEvent
import androidx.core.os.bundleOf
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeCompletionResult
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.reminder.PracticeReminderPromptScreen
import taichi.walking.seniors.beginners.taichi.ui.reminder.PracticeReminderViewModel
import taichi.walking.seniors.beginners.util.sharePlainText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import taichi.walking.seniors.beginners.util.ReviewPromptService

@Composable
fun WorkoutCompletionScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    practiceKind: PracticeKind,
    completionResult: PracticeCompletionResult?,
    onReturnHome: () -> Unit,
    /**
     * Runs on Done before the reminder prompt, like iOS handleDoneTap: the host may show the
     * post-workout paywall first and call [continueFlow] when it closes.
     */
    beforeReminderPrompt: (continueFlow: () -> Unit) -> Unit = { it() },
    isPremium: Boolean = false,
    reminderViewModel: PracticeReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Non-null while the "same time tomorrow?" reminder prompt is shown over this screen.
    var reminderPromptTime by remember { mutableStateOf<ZonedDateTime?>(null) }
    val activity = context as? ComponentActivity
    val title = completionResult?.title
    val calories = dayData.exercises.sumOf { it.calories }
    val shareTitle = stringResource(R.string.completion_share_title)
    val shareText = buildString {
        append(context.getString(R.string.completion_share_line, dayNumber, context.getString(practiceKind.displayNameRes)))
        append("\n")
        title?.let { append("${it.icon} ${context.getString(it.titleRes)}\n") }
        append("\u23F1 ")
        append(context.resources.getQuantityString(R.plurals.completion_share_minutes, dayData.durationInMinutes, dayData.durationInMinutes))
        append("\n\uD83D\uDD25 ")
        val streak = completionResult?.currentStreak ?: 0
        append(context.resources.getQuantityString(R.plurals.completion_share_streak, streak, streak))
        append("\n\n")
        append(context.getString(R.string.completion_share_start))
        append("\n")
        append(context.getString(R.string.app_link))
    }

    // iOS WorkoutCompletionView: finishing the first session is the engagement worth a rating ask;
    // the once-per-version gate in ReviewPromptService keeps this and the Home ask from both firing.
    LaunchedEffect(Unit) {
        if (dayNumber == 1 && activity != null) {
            ReviewPromptService.requestIfNeeded(activity, surface = "completion", delayMillis = 1_500)
        }
    }

    LaunchedEffect(Unit) {
        reminderViewModel.onWorkoutCompletionShown()
    }
    // iOS workout_completion_view; progress is recorded before this screen, so no projection.
    LaunchedEffect(Unit) {
        logAppEvent(
            "workout_completion_view",
            bundleOf(
                "day_number" to dayNumber,
                "variant" to "classic",
                "is_premium" to isPremium,
                "total_days_practiced" to (completionResult?.totalDaysPracticed ?: 0),
                "current_streak" to (completionResult?.currentStreak ?: 0),
                "source" to "home_day_card"
            )
        )
    }

    // Offers tomorrow's reminder before leaving, unless the flag is off or the user already
    // answered the prompt today (iOS WorkoutCompletionView.finishOrPromptForReminder).
    val finishOrPromptForReminder: () -> Unit = {
        scope.launch {
            val promptTime = reminderViewModel.promptTimeIfOffered()
            if (promptTime != null) reminderPromptTime = promptTime else onReturnHome()
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
                    text = "${it.icon} ${stringResource(it.titleRes)}",
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
                    text = stringResource(R.string.completion_day_complete, dayNumber),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A5A60)
                )
            }

            title?.let {
                Image(
                    painter = painterResource(id = it.badgeDrawableRes),
                    contentDescription = stringResource(it.titleRes),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(130.dp)
                )
                Text(
                    text = stringResource(it.encouragementRes),
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
                    label = stringResource(R.string.completion_stat_minutes)
                )
                VerticalDivider()
                StatColumn(
                    icon = Icons.Default.FlashOn,
                    iconColor = Color(0xFFEF5350),
                    iconBackground = Color(0xFFFDECEA),
                    value = calories.toString(),
                    label = stringResource(R.string.completion_stat_calories)
                )
                VerticalDivider()
                StatColumn(
                    icon = Icons.Default.DirectionsWalk,
                    iconColor = Color(0xFF0BA56B),
                    iconBackground = Color(0xFFE6F5EE),
                    value = dayData.exercises.size.toString(),
                    label = stringResource(R.string.completion_stat_moves)
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
                    onClick = { sharePlainText(context, shareText, shareTitle) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x1F0BA56B),
                        contentColor = Color(0xFF0BA56B)
                    )
                ) {
                    Text(stringResource(R.string.completion_share), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        logAppEvent(
                            "workout_completion_done_tap",
                            bundleOf(
                                "day_number" to dayNumber,
                                "minutes" to dayData.lengthSeconds / 60,
                                "calories" to calories,
                                "exercises_count" to dayData.exercises.size,
                                "source" to "home_day_card"
                            )
                        )
                        val totalDays = completionResult?.totalDaysPracticed ?: 0
                        if (PracticeTitleService.shouldUnlockBadge(totalDays)) {
                            logAppEvent(
                                "badge_unlocked",
                                bundleOf(
                                    "badge_id" to PracticeTitleService.getTitle(totalDays)?.badgeId.orEmpty(),
                                    "total_days" to totalDays
                                )
                            )
                        }
                        beforeReminderPrompt(finishOrPromptForReminder)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0BA56B),
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.completion_done), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    reminderPromptTime?.let { promptTime ->
        PracticeReminderPromptScreen(
            initialTime = promptTime,
            onFinish = onReturnHome,
            viewModel = reminderViewModel
        )
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
