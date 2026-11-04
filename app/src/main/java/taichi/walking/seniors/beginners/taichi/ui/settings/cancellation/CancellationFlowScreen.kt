package taichi.walking.seniors.beginners.taichi.ui.settings.cancellation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PaywallScreen
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant

private val ScreenBackground = Color(0xFFF6FBF7)
private val TextPrimary = Color(0xFF2F3C42)
private val TextSecondary = Color(0xFF7D8A8F)
private val TextMuted = Color(0xFFBBC5C9)
private val Primary = Color(0xFF2FA36B)
private val PrimaryDark = Color(0xFF1E7F51)
private val Border = Color(0xFFE4E8EA)

private const val SUPPORT_EMAIL = "hello@easytaichi.app"

/**
 * Full-screen cancellation flow. The host shows it after [CancellationFlowViewModel.start] and
 * removes it once [CancellationFlowUiState.isFinished].
 */
@Composable
fun CancellationFlowScreen(viewModel: CancellationFlowViewModel) {
    val state by viewModel.state.collectAsState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BackHandler { viewModel.leave(trigger = "back_button") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Drawn over Settings: without a pointer handler, taps between the controls would reach
            // the Settings rows underneath (Delete Account among them).
            .pointerInput(Unit) {}
            .background(ScreenBackground)
            .padding(top = statusBarPadding, bottom = navBarPadding)
    ) {
        TopBar(onClose = { viewModel.leave(trigger = "close_button") })

        when (val step = state.step) {
            CancellationStep.Reason -> ReasonStep(
                selectedReason = state.selectedReason,
                onSelect = viewModel::selectReason,
                onContinue = viewModel::continueFromReason,
                onKeep = { viewModel.leave(trigger = "keep_subscription") }
            )
            is CancellationStep.Retention -> RetentionStep(
                reason = step.reason,
                isOpeningPlayManagement = state.isOpeningPlayManagement,
                onAccept = { viewModel.acceptAdjustment(step.reason) },
                onCancelAnyway = { viewModel.continueCancellation(step.reason) },
                onKeep = { viewModel.leave(trigger = "keep_subscription") }
            )
            CancellationStep.Feedback -> FeedbackStep(
                feedbackText = state.feedbackText,
                onFeedbackChange = viewModel::updateFeedback,
                onContinue = viewModel::submitFeedback,
                onKeep = { viewModel.leave(trigger = "keep_subscription") }
            )
            CancellationStep.Applying -> ApplyingStep()
            is CancellationStep.Applied -> AppliedStep(reason = step.reason, onOk = viewModel::finish)
        }
    }

    if (state.showDiscountPaywall) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackHandler { viewModel.onDiscountPaywallFinished(purchased = false) }
            // Same variant iOS shows here: the limited offer (limited_offer_paywall_html_url).
            PaywallScreen(
                paywallVariant = RemotePaywallVariant.ONBOARDING_LIMITED_OFFER,
                onClose = { viewModel.onDiscountPaywallFinished(purchased = false) },
                onComplete = { viewModel.onDiscountPaywallFinished(purchased = true) }
            )
        }
    }

    if (state.showPlayFallback) {
        PlayFallbackDialog(onDismiss = viewModel::dismissPlayFallback)
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = TextSecondary
            )
        }
        Text(
            text = stringResource(R.string.cancel_flow_title),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ReasonStep(
    selectedReason: CancellationReason?,
    onSelect: (CancellationReason) -> Unit,
    onContinue: () -> Unit,
    onKeep: () -> Unit
) {
    StepLayout(
        title = stringResource(R.string.cancel_flow_reason_title),
        subtitle = stringResource(R.string.cancel_flow_reason_subtitle),
        primaryTitle = stringResource(R.string.continue_button),
        isPrimaryEnabled = selectedReason != null,
        onPrimary = onContinue,
        secondaryTitle = stringResource(R.string.cancel_flow_keep_subscription),
        onSecondary = onKeep
    ) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CancellationReason.entries.forEach { reason ->
                ReasonOptionRow(
                    reason = reason,
                    isSelected = selectedReason == reason,
                    onSelect = { onSelect(reason) }
                )
            }
        }
    }
}

@Composable
private fun RetentionStep(
    reason: CancellationReason,
    isOpeningPlayManagement: Boolean,
    onAccept: () -> Unit,
    onCancelAnyway: () -> Unit,
    onKeep: () -> Unit
) {
    val adjustment = reason.retentionAdjustment
    if (adjustment != null) {
        StepLayout(
            title = stringResource(adjustment.offerTitle),
            subtitle = stringResource(adjustment.offerSubtitle),
            primaryTitle = stringResource(adjustment.acceptTitle),
            isPrimaryEnabled = true,
            onPrimary = onAccept,
            secondaryTitle = stringResource(R.string.cancel_flow_cancel_anyway),
            onSecondary = onCancelAnyway
        )
    } else {
        // Reached when the discount paywall was closed without a purchase.
        StepLayout(
            title = stringResource(R.string.cancel_flow_ready_title),
            subtitle = stringResource(R.string.cancel_flow_ready_subtitle),
            primaryTitle = stringResource(R.string.cancel_flow_cancel_anyway),
            isPrimaryEnabled = !isOpeningPlayManagement,
            onPrimary = onCancelAnyway,
            secondaryTitle = stringResource(R.string.cancel_flow_keep_subscription),
            onSecondary = onKeep
        )
    }
}

@Composable
private fun FeedbackStep(
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    onContinue: () -> Unit,
    onKeep: () -> Unit
) {
    StepLayout(
        title = stringResource(R.string.cancel_flow_feedback_title),
        subtitle = stringResource(R.string.cancel_flow_feedback_subtitle),
        primaryTitle = stringResource(R.string.continue_button),
        isPrimaryEnabled = true,
        onPrimary = onContinue,
        secondaryTitle = stringResource(R.string.cancel_flow_keep_subscription),
        onSecondary = onKeep
    ) {
        OutlinedTextField(
            value = feedbackText,
            onValueChange = onFeedbackChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                cursorColor = Primary
            )
        )
    }
}

@Composable
private fun ApplyingStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Primary)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.cancel_flow_applying),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun AppliedStep(reason: CancellationReason, onOk: () -> Unit) {
    val adjustment = reason.retentionAdjustment ?: return
    StepLayout(
        title = stringResource(adjustment.confirmationTitle),
        subtitle = stringResource(adjustment.confirmationBody),
        primaryTitle = stringResource(R.string.settings_account_ok),
        isPrimaryEnabled = true,
        onPrimary = onOk
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(64.dp)
        )
    }
}

@Composable
private fun PlayFallbackDialog(onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(R.string.cancel_flow_play_fallback_title), color = TextPrimary) },
        text = {
            Text(
                stringResource(R.string.cancel_flow_play_fallback_message, SUPPORT_EMAIL),
                color = TextSecondary
            )
        },
        dismissButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(SUPPORT_EMAIL))
                onDismiss()
            }) {
                Text(
                    stringResource(R.string.cancel_flow_copy_support_email),
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_account_ok), color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun StepLayout(
    title: String,
    subtitle: String?,
    primaryTitle: String,
    isPrimaryEnabled: Boolean,
    onPrimary: () -> Unit,
    secondaryTitle: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onPrimary,
                enabled = isPrimaryEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White,
                    disabledContainerColor = Primary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = primaryTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            if (secondaryTitle != null && onSecondary != null) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = secondaryTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ReasonOptionRow(
    reason: CancellationReason,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Primary.copy(alpha = 0.1f) else Color.White, shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryDark else Border,
                shape = shape
            )
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = reason.icon,
            contentDescription = null,
            tint = if (isSelected) PrimaryDark else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(reason.titleRes),
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) PrimaryDark else TextMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

private val CancellationReason.icon: ImageVector
    get() = when (this) {
        CancellationReason.TOO_EXPENSIVE -> Icons.Default.CreditCard
        CancellationReason.EXERCISES_TOO_HARD -> Icons.Default.FitnessCenter
        CancellationReason.EXERCISES_TOO_EASY -> Icons.Default.Eco
        CancellationReason.OTHER -> Icons.Default.ChatBubble
    }
