package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import coil.compose.AsyncImage
import com.mobteq.analytics.logAppEvent
import kotlinx.coroutines.delay
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.video.WorkoutMediaCache

private val TaiChiGreen = Color(0xFF0BA56B)

/**
 * Sits between "Begin Workout" and the workout (iOS WorkoutPreparationView). Nothing plays until
 * every video and spoken track of the day is on disk, so a poor connection shows honest progress
 * here instead of a session running its timer and narration over a video that never arrives.
 * Speech files the CDN does not have are skipped rather than failing the day.
 */
@Composable
fun WorkoutPreparationScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    onReady: () -> Unit,
    onClose: () -> Unit,
    analyticsSource: String = "home_day_card"
) {
    val context = LocalContext.current
    val urls = remember(dayData) { WorkoutMediaCache.mediaUrls(dayData.exercises) }
    var fraction by remember { mutableFloatStateOf(0f) }
    var failed by remember { mutableStateOf(false) }
    // 1 on the first download, +1 for every "Try Again"; restarts the download and the slow hint.
    var attempt by remember { mutableIntStateOf(1) }
    var showsSlowHint by remember { mutableStateOf(false) }
    var startedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    val animatedFraction by animateFloatAsState(fraction, label = "preparation")

    fun track(result: String, failedCount: Int = 0, skippedCount: Int = 0) {
        logAppEvent(
            EVENT_WORKOUT_PREPARATION,
            bundleOf(
                "day_number" to dayNumber,
                "result" to result,
                "seconds" to ((System.currentTimeMillis() - startedAt) / 1000).toInt(),
                "file_count" to urls.size,
                "failed_count" to failedCount,
                "skipped_count" to skippedCount,
                "was_failed" to failed,
                "source" to analyticsSource,
                "attempt" to attempt,
                "slow_hint_shown" to showsSlowHint
            )
        )
    }

    val close = {
        track(result = "cancelled")
        onClose()
    }
    BackHandler(onBack = close)

    // Keep the screen on while downloading, like iOS's isIdleTimerDisabled.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(attempt) {
        failed = false
        showsSlowHint = false
        startedAt = System.currentTimeMillis()
        fraction = 0f
        val outcome = WorkoutMediaCache.downloadAll(context, urls) { fraction = it }
        track(
            result = if (outcome.succeeded) "ready" else "failed",
            failedCount = outcome.failed,
            skippedCount = outcome.skipped
        )
        if (outcome.succeeded) {
            fraction = 1f
            onReady()
        } else {
            failed = true
        }
    }
    LaunchedEffect(attempt) {
        delay(SLOW_HINT_DELAY_MILLIS)
        showsSlowHint = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = dayData.coverUrl.ifBlank { dayData.exercises.firstOrNull()?.coverUrl },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, start = 20.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = close),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.workout_preparation_go_back),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (failed) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = stringResource(R.string.workout_preparation_failed),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(TaiChiGreen)
                            .clickable { attempt += 1 }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.workout_preparation_try_again),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.workout_preparation_go_back),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = close)
                            .padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.workout_preparation_day, dayNumber),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.workout_preparation_title),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.workout_preparation_subtitle),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(TaiChiGreen)
                        )
                    }
                    Text(
                        text = "${(fraction * 100).toInt().coerceIn(0, 100)}%",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = showsSlowHint, enter = fadeIn()) {
                        Text(
                            text = stringResource(R.string.workout_preparation_slow_hint),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/** An average day is ~55-70 MB; still downloading after this long means a slow connection. */
private const val SLOW_HINT_DELAY_MILLIS = 20_000L

private const val EVENT_WORKOUT_PREPARATION = "workout_preparation"
