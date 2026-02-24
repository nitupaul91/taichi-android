package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes

@Composable
fun EmailCaptureScreen(
    state: OnboardingState,
    onEmailChange: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.EmailCapture),
        title = stringResource(R.string.onboarding_email_title),
        subtitle = stringResource(R.string.onboarding_email_subtitle),
        onBack = onBack,
        continueText = stringResource(R.string.continue_button),
        onContinue = onContinue
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEmailChange(OnboardingAction.EmailChange(it)) },
            label = { Text(stringResource(R.string.onboarding_email_label)) },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(top = 8.dp)
        )
    }
}
