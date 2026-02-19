package taichi.walking.seniors.beginners.taichi.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.Dimens

@Composable
fun QuestionScaffold(
    progress: Float,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    showProgress: Boolean = true,
    continueEnabled: Boolean = true,
    continueText: String = "Continue",
    onContinue: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenPadding)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.buttonHeight + 24.dp)
        ) {
            item {
                if (showProgress) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.progressHeight),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
                Spacer(modifier = Modifier.height(Dimens.sectionSpacing))
            }
            item {
                content()
            }
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
            PrimaryButton(text = continueText, enabled = continueEnabled, onClick = onContinue)
        }
    }
}
