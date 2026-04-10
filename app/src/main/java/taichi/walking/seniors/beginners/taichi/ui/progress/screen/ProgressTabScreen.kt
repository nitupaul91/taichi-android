package taichi.walking.seniors.beginners.taichi.ui.progress.screen

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun ProgressTabScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5EE),
                        Color(0xFFF0FAF4),
                        Color.White
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(top = statusBarPadding + 16.dp, start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.weight(1f))
            state.currentTitle?.let { title ->
                Row(
                    modifier = Modifier
                        .background(Color(0x1A0BA56B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title.icon)
                    Text(
                        text = title.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0BA56B)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF0BA56B),
                title = "Days Practiced",
                value = state.totalDaysPracticed.toString(),
                subtitle = if (state.totalDaysPracticed == 1) "day" else "days"
            )
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                iconColor = Color(0xFFFF9800),
                title = "Current Streak",
                value = state.currentStreak.toString(),
                subtitle = if (state.currentStreak == 1) "day in a row" else "days in a row"
            )
            StatCard(
                icon = Icons.Default.Schedule,
                iconColor = Color(0xFF5A9BD5),
                title = "Total Practice Time",
                value = state.totalMinutesPracticed.toString(),
                subtitle = "minutes"
            )
        }

        SectionTitle("This Week")
        WeeklyProgressCard(days = state.weeklyProgress)

        SectionTitle("Last 30 Days")
        MonthlyPracticeCalendar(days = state.monthlyProgress)

        EncouragementCard(daysCompleted = state.totalDaysPracticed)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1A1A)
    )
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.size(20.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6A7A80)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6A7A80)
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(days: List<ProgressViewModel.DayProgress>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (day.isCompleted) Color(0xFF0BA56B) else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(if (day.isCompleted) 32.dp else 40.dp)
                            .background(
                                color = if (day.isCompleted) Color(0xFF0BA56B) else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = if (day.isCompleted) Color(0xFF0BA56B) else Color(0xFFE0E0E0),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                    if (day.isCompleted) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = day.dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6A7A80)
                )
            }
        }
    }
}

@Composable
private fun MonthlyPracticeCalendar(days: List<ProgressViewModel.CalendarDayProgress>) {
    val locale = Locale.getDefault()
    val weekFields = WeekFields.of(locale)
    val firstDayOfWeek = weekFields.firstDayOfWeek
    val weekdayHeaders = buildList {
        var current = firstDayOfWeek
        repeat(7) {
            add(current.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, locale))
            current = current.plus(1)
        }
    }
    val leadingEmptyDays = days.firstOrNull()?.let { first ->
        val firstValue = first.date.dayOfWeek.value
        val startValue = firstDayOfWeek.value
        (firstValue - startValue + 7) % 7
    } ?: 0
    val cells = List(leadingEmptyDays) { null } + days

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CalendarRow(items = weekdayHeaders) { header ->
            Text(
                text = header,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9AA8AD),
                textAlign = TextAlign.Center
            )
        }

        cells.chunked(7).forEach { week ->
            CalendarRow(items = week) { day ->
                if (day == null) {
                    Spacer(modifier = Modifier.height(42.dp))
                } else {
                    val isToday = day.date == java.time.LocalDate.now()
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .fillMaxWidth()
                            .background(
                                color = if (day.isCompleted) Color(0xFF0BA56B) else Color(0xFFF7F9F8),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent, RoundedCornerShape(12.dp))
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRoundRect(
                                        color = Color(0xFF0BA56B),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                    )
                                }
                            }
                        }
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (day.isCompleted) Color.White else Color(0xFF2A3A40)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> CalendarRow(
    items: List<T>,
    content: @Composable (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                content(item)
            }
        }
    }
}

@Composable
private fun EncouragementCard(daysCompleted: Int) {
    val message = when {
        daysCompleted == 0 -> "Start your journey today! Every step counts."
        daysCompleted < 7 -> "Great start! Keep up the good work."
        daysCompleted < 14 -> "You're doing amazing! One week down."
        daysCompleted < 21 -> "Halfway there! Your dedication shows."
        else -> "Almost done! You're an inspiration."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6F5EE), RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFF0BA56B),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2A3A40)
        )
    }
}
