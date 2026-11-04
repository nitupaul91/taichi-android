package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R

private val TaiChiGreen = Color(0xFF0BA56B)

/** The five levels the backend accepts, iOS JourneyHomeView.intensityOptions. */
internal enum class WorkoutIntensityLevel(val value: String, @StringRes val title: Int) {
    VERY_GENTLE("1", R.string.intensity_very_gentle),
    GENTLE("2", R.string.intensity_gentle),
    MODERATE("3", R.string.intensity_moderate),
    CHALLENGING("4", R.string.intensity_challenging),
    INTENSE("5", R.string.intensity_intense);

    companion object {
        /** Anything unknown is the recommended middle level, like iOS normalizedIntensity. */
        fun normalized(value: String?): WorkoutIntensityLevel =
            entries.firstOrNull { it.value == value?.trim() } ?: MODERATE
    }
}

/** Home card behind `show_workout_intensity_widget` (iOS workoutIntensityWidget). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkoutIntensityWidget(intensity: String?, onClick: () -> Unit) {
    val level = WorkoutIntensityLevel.normalized(intensity)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Speed, contentDescription = null, tint = TaiChiGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.intensity_widget_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // The badge wraps under the value on a narrow screen instead of splitting the word.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.intensity_value, level.value.toInt(), stringResource(level.title)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                    if (level == WorkoutIntensityLevel.MODERATE) {
                        Text(
                            text = stringResource(R.string.intensity_recommended),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF159A68),
                            maxLines = 1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDDF7EB))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA3A3A3))
        }
    }
}

/** iOS WorkoutIntensityPickerSheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutIntensitySheet(
    currentIntensity: String?,
    isApplying: Boolean,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(WorkoutIntensityLevel.normalized(currentIntensity)) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 14.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.intensity_sheet_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101010),
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = stringResource(R.string.intensity_sheet_subtitle),
                fontSize = 18.sp,
                color = Color(0xFF6C6C6C),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                WorkoutIntensityLevel.entries.forEach { level ->
                    val isSelected = level == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                BorderStroke(if (isSelected) 4.dp else 2.dp, if (isSelected) TaiChiGreen else Color(0xFFD7D7D7)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selected = level }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(level.title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101010),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TaiChiGreen,
                            modifier = Modifier
                                .size(30.dp)
                                .alpha(if (isSelected) 1f else 0f)
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TaiChiGreen,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = !isApplying) { onApply(selected.value) }
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.intensity_apply, selected.value.toInt()),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
