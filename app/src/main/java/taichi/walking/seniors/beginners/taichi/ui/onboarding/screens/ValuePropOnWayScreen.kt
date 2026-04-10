package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold

@Composable
fun ValuePropOnWayScreen(
    state: OnboardingState,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val videoRes = when (state.genderId) {
        "male" -> R.raw.woman_movement_one
        "female" -> R.raw.man_movement_one
        else -> R.raw.woman_movement_one
    }

    QuestionScaffold(
        progress = 0f,
        title = "",
        subtitle = null,
        showProgress = false,
        applyTopInset = false,
        overlayBackButton = true,
        applyOverlayBackButtonInset = true,
        contentTopSpacing = 106.dp,
        onBack = onBack,
        continueEnabled = true,
        onContinue = onContinue
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LoopingVideoCard(
                videoResId = videoRes,
                modifier = Modifier.fillMaxWidth(0.82f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_value_on_way_title),
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_value_on_way_description),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
