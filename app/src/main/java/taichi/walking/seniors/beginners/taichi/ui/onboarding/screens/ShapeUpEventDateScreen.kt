package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Calendar
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun ShapeUpEventDateScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val minCalendar = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } }
    val maxCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, get(Calendar.YEAR) + 3)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val selectedDate = remember(state.shapeUpEventDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = state.shapeUpEventDateMillis.coerceIn(minCalendar.timeInMillis, maxCalendar.timeInMillis)
        }
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.ShapeUpEventDate),
        title = stringResource(R.string.onboarding_shape_up_event_date_title),
        subtitle = stringResource(R.string.onboarding_shape_up_event_date_subtitle),
        onBack = onBack,
        onContinue = {
            onAction(OnboardingAction.ShapeUpEventDateSkipped(false))
            onContinue()
        }
    ) {
        Column {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 4.dp, vertical = 14.dp),
                factory = {
                    DatePicker(context).apply {
                        this.minDate = minCalendar.timeInMillis
                        this.maxDate = maxCalendar.timeInMillis
                        calendarViewShown = false
                        spinnersShown = true
                        init(
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                        ) { _, year, monthOfYear, dayOfMonth ->
                            selectedDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0)
                            selectedDate.set(Calendar.MILLISECOND, 0)
                            onAction(OnboardingAction.ShapeUpEventDateChange(selectedDate.timeInMillis))
                        }
                    }
                },
                update = { picker ->
                    picker.minDate = minCalendar.timeInMillis
                    picker.maxDate = maxCalendar.timeInMillis
                    picker.updateDate(
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_shape_up_event_date_skip),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAction(OnboardingAction.ShapeUpEventDateSkipped(true))
                        onContinue()
                    },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}
