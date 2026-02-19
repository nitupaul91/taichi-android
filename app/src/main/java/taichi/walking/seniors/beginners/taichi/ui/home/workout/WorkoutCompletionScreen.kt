package taichi.walking.seniors.beginners.taichi.ui.home.workout

import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutCompletionScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    onReturnHome: () -> Unit
) {
    val calories = dayData.exercises.sumOf { it.calories }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE5F6EE),
                        Color(0xFFF1FAF6),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape))
                Box(modifier = Modifier.size(90.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape))
                Box(modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            Text("Day $dayNumber Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                "Great work! You're building a healthy habit.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.size(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Default.Schedule,
                    value = "${dayData.durationInMinutes}",
                    label = "Minutes",
                    iconTint = Color(0xFF6A9AD8),
                    iconBackground = Color(0xFFE6EFFA)
                )
                StatCard(
                    icon = Icons.Default.FlashOn,
                    value = "$calories",
                    label = "Energy",
                    iconTint = Color(0xFFF0A140),
                    iconBackground = Color(0xFFFCEFD9)
                )
                StatCard(
                    icon = Icons.Default.DirectionsWalk,
                    value = "${dayData.exercises.size}",
                    label = "Exercises",
                    iconTint = Color(0xFF4BAA78),
                    iconBackground = Color(0xFFE5F4EB)
                )
            }
            Spacer(modifier = Modifier.size(28.dp))
            Button(onClick = onReturnHome, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconTint: Color,
    iconBackground: Color
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
    }
}
