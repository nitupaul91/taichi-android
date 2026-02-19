package taichi.walking.seniors.beginners.taichi.ui.progress.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProgressTabScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF5FAF8),
                        Color(0xFFFAFDFC),
                        Color.White
                    )
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        StatCard(
            title = "Days Completed",
            value = state.totalDaysCompleted.toString(),
            subtitle = "out of 28 days",
            icon = Icons.Default.CheckCircle,
            iconBackground = Color(0xFFE4F2EA),
            iconColor = Color(0xFF41A872)
        )
        StatCard(
            title = "Current Streak",
            value = state.streak.toString(),
            subtitle = if (state.streak == 1) "day in a row" else "days in a row",
            icon = Icons.Default.LocalFireDepartment,
            iconBackground = Color(0xFFF6EBDD),
            iconColor = Color(0xFFEE9A44)
        )
        StatCard(
            title = "Total Practice",
            value = state.totalMinutes.toString(),
            subtitle = "minutes",
            icon = Icons.Default.Schedule,
            iconBackground = Color(0xFFE4ECF7),
            iconColor = Color(0xFF6A9AD8)
        )

        Text(
            "This Week",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            state.weeklyProgress.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFFF9FBFA), CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (day.isCompleted) Color(0xFF45A977) else Color(0xFFD1D4D4),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF45A977), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        color = Color(0xFF6E7B82)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE4F0EA), RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFCFE6DB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFF43A773),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = encouragement(state.totalDaysCompleted),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                color = Color(0xFF2D3C45)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.size(14.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFF6D7B82)
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF6A7780)
                )
            }
        }
    }
}

private fun encouragement(daysCompleted: Int): String = when {
    daysCompleted == 0 -> "Start your journey today! Every step counts."
    daysCompleted < 7 -> "Great start! Keep up the good work."
    daysCompleted < 14 -> "You're doing amazing! One week down."
    daysCompleted < 21 -> "Halfway there! Your dedication shows."
    else -> "Almost done! You're an inspiration."
}
