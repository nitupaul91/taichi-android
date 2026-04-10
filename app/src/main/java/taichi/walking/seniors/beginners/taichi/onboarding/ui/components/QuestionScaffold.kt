package taichi.walking.seniors.beginners.taichi.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.Dimens

@Composable
fun QuestionScaffold(
    progress: Float,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    showProgress: Boolean = true,
    applyTopInset: Boolean = true,
    topInsetExtra: Dp = 16.dp,
    overlayBackButton: Boolean = false,
    applyOverlayBackButtonInset: Boolean = false,
    contentTopSpacing: Dp = 0.dp,
    showContinueButton: Boolean = true,
    continueEnabled: Boolean = true,
    continueText: String = "Continue",
    onContinue: () -> Unit,
    content: @Composable () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val shouldRenderBackInHeader = onBack != null && !(overlayBackButton && !showProgress)
    val hasHeaderText = title.isNotBlank() || !subtitle.isNullOrBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.screenPadding)
            .padding(
                top = if (applyTopInset) statusBarPadding + topInsetExtra else 0.dp,
                bottom = navBarPadding + 24.dp
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (showContinueButton) Dimens.buttonHeight + 24.dp else 8.dp
            )
        ) {
            if (showProgress || shouldRenderBackInHeader || hasHeaderText) {
                item {
                    if (showProgress && shouldRenderBackInHeader) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onBack?.invoke() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OnboardingProgressBar(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    } else if (showProgress) {
                        OnboardingProgressBar(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    } else if (shouldRenderBackInHeader) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { onBack?.invoke() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (hasHeaderText) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (title.isNotBlank()) {
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                    }
                    subtitle?.takeIf { it.isNotBlank() }?.let { subtitleText ->
                        if (title.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                    if (hasHeaderText) {
                        Spacer(modifier = Modifier.height(Dimens.sectionSpacing))
                    }
                }
            }
            if (contentTopSpacing > 0.dp) {
                item {
                    Spacer(modifier = Modifier.height(contentTopSpacing))
                }
            }
            item {
                content()
            }
        }

        if (overlayBackButton && !showProgress && onBack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        top = if (applyOverlayBackButtonInset) statusBarPadding + 8.dp else 8.dp
                    )
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showContinueButton) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                PrimaryButton(text = continueText, enabled = continueEnabled, onClick = onContinue)
            }
        }
    }
}

@Composable
private fun OnboardingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .height(Dimens.progressHeight)
            .clip(shape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        val fillFraction = if (clampedProgress <= 0f) 0f else clampedProgress
        Box(
            modifier = Modifier
                .fillMaxWidth(fillFraction)
                .fillMaxHeight()
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
