package taichi.walking.seniors.beginners.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.PrimaryButton

/** Which moment the timeline explains (iOS TrialTimelineMode). */
enum class TrialTimelineMode {
    /** Before the onboarding paywall: what happens if the user starts the trial today. */
    PRE_PURCHASE,

    /** Over the paywall after the user dismissed the Play purchase sheet. */
    CANCELLATION_RECOVERY,

    /** After a trial purchase: the trial has started and when it ends. */
    POST_PURCHASE_CONFIRMATION
}

// iOS SeniorDesign colours, so the screen reads the same on both platforms.
private val TimelinePrimaryDark = Color(0xFF0BA56B)
private val TimelineTextPrimary = Color(0xFF2F3C42)
private val TimelineTextSecondary = Color(0xFF6A7B82)
private val TimelineTextMuted = Color(0xFF9AA8AD)
private val TimelineBorder = Color(0xFFE8EEF0)
private val TimelineBackground = Color(0xFFF8FAFA)

/**
 * The free-trial timeline (iOS TrialTimelineView): plan ready, today, the reminder two days before
 * the end, and the renewal date, plus how to cancel.
 */
@Composable
fun TrialTimelineScreen(
    details: TrialTimelineDetails,
    mode: TrialTimelineMode,
    reminderMessaging: TrialReminderMessaging,
    onPrimaryAction: () -> Unit,
    progress: Float? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Shown over the paywall after a cancelled purchase: taps must not reach the paywall.
            .pointerInput(Unit) { detectTapGestures { } }
            .background(TimelineBackground)
            .padding(top = statusBarPadding, bottom = navBarPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimelineHeader(details = details, mode = mode, reminderMessaging = reminderMessaging, progress = progress)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimelineCard(details = details, mode = mode, reminderMessaging = reminderMessaging)
                CancellationCard()
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PrimaryButton(text = mode.primaryButtonTitle(), onClick = onPrimaryAction)
            if (onSecondaryAction != null) {
                TextButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.trial_timeline_not_now),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TimelineTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHeader(
    details: TrialTimelineDetails,
    mode: TrialTimelineMode,
    reminderMessaging: TrialReminderMessaging,
    progress: Float?
) {
    val progressDescription = stringResource(R.string.trial_timeline_progress_accessibility)
    Column(
        modifier = Modifier
            .widthIn(max = 620.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (progress != null) {
            val shape = RoundedCornerShape(percent = 50)
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
                    .semantics { contentDescription = progressDescription }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Text(
            text = mode.title(),
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = TimelineTextPrimary
        )
        Text(
            text = mode.subtitle(details, reminderMessaging),
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium,
            color = TimelineTextSecondary
        )
    }
}

private enum class TimelineItemState { DONE, CURRENT, UPCOMING }

private data class TimelineItem(
    val icon: ImageVector,
    val state: TimelineItemState,
    val title: String,
    val subtitle: String
)

@Composable
private fun TimelineCard(
    details: TrialTimelineDetails,
    mode: TrialTimelineMode,
    reminderMessaging: TrialReminderMessaging
) {
    val items = listOf(
        TimelineItem(
            icon = Icons.Default.Check,
            state = TimelineItemState.DONE,
            title = stringResource(R.string.trial_timeline_plan_ready_title),
            subtitle = stringResource(R.string.trial_timeline_plan_ready_subtitle)
        ),
        TimelineItem(
            icon = Icons.Default.CardGiftcard,
            state = TimelineItemState.CURRENT,
            title = stringResource(R.string.trial_timeline_today_title),
            subtitle = mode.todaySubtitle()
        ),
        TimelineItem(
            icon = Icons.Default.Notifications,
            state = TimelineItemState.UPCOMING,
            title = stringResource(
                R.string.trial_timeline_reminder_title,
                formatShortTrialDate(details.reminderMillis)
            ),
            subtitle = reminderMessaging.timelineSubtitle()
        ),
        TimelineItem(
            icon = Icons.Default.Autorenew,
            state = TimelineItemState.UPCOMING,
            title = stringResource(
                R.string.trial_timeline_end_title,
                formatShortTrialDate(details.trialEndMillis)
            ),
            subtitle = stringResource(R.string.trial_timeline_end_subtitle)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        items.forEachIndexed { index, item ->
            TimelineRow(item = item, isLast = index == items.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(item: TimelineItem, isLast: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val markerSize = 38.dp
    val markerFill = when (item.state) {
        TimelineItemState.DONE -> TimelinePrimaryDark
        TimelineItemState.CURRENT -> primary
        TimelineItemState.UPCOMING -> TimelineBackground
    }
    val iconColor = if (item.state == TimelineItemState.UPCOMING) TimelineTextMuted else Color.White
    // The rail is tinted down to the current step and neutral after it, so the eye lands on "Today".
    val connectorColor = if (item.state == TimelineItemState.UPCOMING) TimelineBorder else primary.copy(alpha = 0.35f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.width(markerSize + 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(markerSize + 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.state == TimelineItemState.CURRENT) {
                    // Soft halo marks the step the user is on right now.
                    Box(
                        modifier = Modifier
                            .size(markerSize + 6.dp)
                            .border(6.dp, primary.copy(alpha = 0.22f), CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(markerSize)
                        .clip(CircleShape)
                        .background(markerFill)
                        .then(
                            if (item.state == TimelineItemState.UPCOMING) {
                                Modifier.border(2.dp, TimelineBorder, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .width(3.dp)
                        .weight(1f)
                        .heightIn(min = 30.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(connectorColor)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 6.dp, bottom = if (isLast) 0.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TimelineTextPrimary
            )
            Text(
                text = item.subtitle,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = TimelineTextSecondary
            )
        }
    }
}

@Composable
private fun CancellationCard() {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(primary.copy(alpha = 0.08f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Help,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.trial_timeline_cancel_question),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TimelineTextPrimary
            )
        }
        Text(
            text = stringResource(R.string.trial_timeline_cancel_answer),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = TimelineTextSecondary
        )
    }
}

@Composable
private fun TrialTimelineMode.title(): String = when (this) {
    TrialTimelineMode.PRE_PURCHASE -> stringResource(R.string.trial_timeline_title_pre_purchase)
    TrialTimelineMode.CANCELLATION_RECOVERY -> stringResource(R.string.trial_timeline_title_cancellation)
    TrialTimelineMode.POST_PURCHASE_CONFIRMATION -> stringResource(R.string.trial_timeline_title_confirmation)
}

@Composable
private fun TrialTimelineMode.subtitle(
    details: TrialTimelineDetails,
    reminderMessaging: TrialReminderMessaging
): String = when (this) {
    TrialTimelineMode.PRE_PURCHASE -> stringResource(R.string.trial_timeline_subtitle_pre_purchase)
    TrialTimelineMode.CANCELLATION_RECOVERY -> {
        val endDate = formatShortTrialDate(details.trialEndMillis)
        when (reminderMessaging) {
            TrialReminderMessaging.AUTHORIZED ->
                stringResource(R.string.trial_timeline_subtitle_cancellation_authorized, endDate)
            TrialReminderMessaging.PERMISSION_NEEDED ->
                stringResource(R.string.trial_timeline_subtitle_cancellation_permission_needed, endDate)
            TrialReminderMessaging.UNAVAILABLE ->
                stringResource(R.string.trial_timeline_subtitle_cancellation_unavailable, endDate)
        }
    }
    TrialTimelineMode.POST_PURCHASE_CONFIRMATION -> stringResource(
        R.string.trial_timeline_subtitle_confirmation,
        formatShortTrialDate(details.trialEndMillis)
    )
}

@Composable
private fun TrialTimelineMode.todaySubtitle(): String = when (this) {
    TrialTimelineMode.POST_PURCHASE_CONFIRMATION -> stringResource(R.string.trial_timeline_today_active)
    else -> stringResource(R.string.trial_timeline_today_start)
}

@Composable
private fun TrialTimelineMode.primaryButtonTitle(): String = when (this) {
    TrialTimelineMode.PRE_PURCHASE -> stringResource(R.string.continue_button)
    TrialTimelineMode.CANCELLATION_RECOVERY -> stringResource(R.string.trial_timeline_button_cancellation)
    TrialTimelineMode.POST_PURCHASE_CONFIRMATION -> stringResource(R.string.trial_timeline_button_confirmation)
}

@Composable
private fun TrialReminderMessaging.timelineSubtitle(): String = when (this) {
    TrialReminderMessaging.AUTHORIZED -> stringResource(R.string.trial_timeline_reminder_authorized)
    TrialReminderMessaging.PERMISSION_NEEDED -> stringResource(R.string.trial_timeline_reminder_permission_needed)
    TrialReminderMessaging.UNAVAILABLE -> stringResource(R.string.trial_timeline_reminder_unavailable)
}
