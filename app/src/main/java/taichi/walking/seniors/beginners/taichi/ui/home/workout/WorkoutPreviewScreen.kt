package taichi.walking.seniors.beginners.taichi.ui.home.workout

import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import taichi.walking.seniors.beginners.R
import androidx.activity.compose.BackHandler
import taichi.walking.seniors.beginners.taichi.ui.video.WorkoutMediaCache
import com.mobteq.analytics.logAppEvent
import androidx.core.os.bundleOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val TaiChiGreen = Color(0xFF0BA56B)

@Composable
fun WorkoutPreviewScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    currentDay: Int,
    isPremium: Boolean = false,
    /** Days 1..N are free (Remote Config `free_days_count`, iOS WorkoutPreviewView). */
    freeDaysCount: Int,
    onClose: () -> Unit,
    onBegin: () -> Unit,
    onOpenPaywall: (() -> Unit)? = null
) {
    val requiresPremium = dayNumber > freeDaysCount && !isPremium
    val accessible = dayNumber <= currentDay
    val context = LocalContext.current

    // iOS workout_preview_view: once per preview presentation.
    LaunchedEffect(Unit) {
        val cached = WorkoutMediaCache.isDayReady(context, WorkoutMediaCache.mediaUrls(dayData.exercises))
        logAppEvent(
            "workout_preview_view",
            bundleOf(
                "day_number" to dayNumber,
                "is_accessible" to accessible,
                "requires_premium" to requiresPremium,
                "exercise_count" to dayData.exercises.size,
                "is_media_cached" to cached
            )
        )
    }
    val close = {
        logAppEvent("workout_preview_close", bundleOf("day_number" to dayNumber))
        onClose()
    }
    // System back returns to the journey like the close button, instead of leaving the app.
    BackHandler(onBack = close)
    var showCompleteFirstDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5F3))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                    AsyncImage(
                        model = dayData.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.White
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircleIcon(icon = Icons.Default.Close, onClick = close)
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                stringResource(R.string.day_number, dayNumber),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (dayData.description.isBlank()) stringResource(R.string.preview_todays_workout) else dayData.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBadge("${dayData.durationInMinutes}", "minutes")
                                StatBadge("${dayData.exercises.sumOf { it.calories }}", "calories")
                                StatBadge("${dayData.exercises.size}", "exercises")
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.preview_exercises),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.workout_item_count, dayData.exercises.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = TaiChiGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            itemsIndexed(dayData.exercises) { _, ex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ex.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ex.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                formatSeconds(ex.duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.preview_calories, ex.calories),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(140.dp)) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFFF2F5F3))
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(TaiChiGreen)
                    .clickable {
                        when {
                            requiresPremium -> {
                                logBeginOutcome(dayNumber, "paywall")
                                onOpenPaywall?.invoke()
                            }
                            !accessible -> {
                                logBeginOutcome(dayNumber, "locked")
                                showCompleteFirstDialog = true
                            }
                            else -> {
                                logBeginOutcome(dayNumber, "started")
                                onBegin()
                            }
                        }
                    }
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.start_day, dayNumber),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }

    if (showCompleteFirstDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteFirstDialog = false },
            title = { Text(stringResource(R.string.preview_complete_previous_title)) },
            text = { Text(stringResource(R.string.preview_complete_previous_message, currentDay, dayNumber)) },
            confirmButton = {
                TextButton(onClick = { showCompleteFirstDialog = false }) {
                    Text(stringResource(android.R.string.ok), color = TaiChiGreen)
                }
            }
        )
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
private fun CircleIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.95f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TaiChiGreen.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TaiChiGreen
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

/** iOS workout_preview_begin: what the bottom button did. */
private fun logBeginOutcome(dayNumber: Int, outcome: String) {
    logAppEvent("workout_preview_begin", bundleOf("day_number" to dayNumber, "outcome" to outcome))
}
