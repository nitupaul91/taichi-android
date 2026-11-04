package taichi.walking.seniors.beginners.taichi.ui.reminder

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.notifications.reminder.PracticeReminderScheduler.ScheduleResult
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.PrimaryButton
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.Dimens
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiGreenDark
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTextPrimary
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTextSecondary
import java.time.ZonedDateTime
import java.util.Date

private val ContentMaxWidth = 560.dp

/**
 * Post-session opt-in for tomorrow's practice reminder (iOS `PracticeReminderPromptView`). Kept
 * deliberately small: one question, a suggested time, and a way out.
 *
 * Tapping Remind Me is the in-context moment to ask for the notification permission, so the
 * system dialog is launched from here when it has not been granted yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeReminderPromptScreen(
    initialTime: ZonedDateTime,
    onFinish: () -> Unit,
    viewModel: PracticeReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )
    var isEditingTime by rememberSaveable { mutableStateOf(false) }
    var isScheduling by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Always tomorrow: the picker only changes the time of day, as on iOS.
    val reminderTime = initialTime.withHour(timePickerState.hour).withMinute(timePickerState.minute)
    val formattedTime = remember(reminderTime, context) {
        DateFormat.getTimeFormat(context).format(Date(reminderTime.toInstant().toEpochMilli()))
    }

    LaunchedEffect(Unit) {
        viewModel.trackPromptShown()
    }

    fun finish() {
        viewModel.markPromptAnswered()
        onFinish()
    }

    fun showPermissionDenied(wasPrompt: Boolean) {
        isScheduling = false
        viewModel.onPermissionDenied(wasPrompt)
        showPermissionDeniedDialog = true
    }

    fun scheduleReminder() {
        viewModel.schedule(reminderTime, didChangeTime = isEditingTime) { result ->
            isScheduling = false
            when (result) {
                ScheduleResult.SCHEDULED, ScheduleResult.FAILED -> finish()
                ScheduleResult.PERMISSION_DENIED -> showPermissionDenied(wasPrompt = false)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.trackSystemPermissionResult(granted)
        if (granted) scheduleReminder() else showPermissionDenied(wasPrompt = true)
    }

    fun remindMe() {
        if (isScheduling) return
        isScheduling = true

        when {
            NotificationManagerCompat.from(context).areNotificationsEnabled() -> scheduleReminder()
            needsNotificationPermission(context) ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            // Notifications switched off for the app (or a pre-13 phone): only Settings can fix it.
            else -> showPermissionDenied(wasPrompt = false)
        }
    }

    fun decline() {
        viewModel.decline(reminderHour = reminderTime.hour)
        finish()
    }

    BackHandler(enabled = !showPermissionDeniedDialog) { decline() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = Dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = stringResource(R.string.practice_reminder_prompt_title),
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaiChiTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.practice_reminder_prompt_body, formattedTime),
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = TaiChiTextSecondary,
                    textAlign = TextAlign.Center
                )

                if (isEditingTime) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TimePicker(
                                state = timePickerState,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.practice_reminder_prompt_remind_me),
                    enabled = !isScheduling,
                    onClick = { remindMe() }
                )

                if (!isEditingTime) {
                    PromptTextButton(
                        text = stringResource(R.string.practice_reminder_prompt_change_time),
                        color = TaiChiGreenDark,
                        onClick = { isEditingTime = true }
                    )
                }

                PromptTextButton(
                    text = stringResource(R.string.practice_reminder_prompt_not_now),
                    color = TaiChiTextSecondary,
                    onClick = { decline() }
                )
            }
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDeniedDialog = false
                finish()
            },
            title = { Text(stringResource(R.string.practice_reminder_permission_denied_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.practice_reminder_permission_denied_message,
                        stringResource(R.string.app_name)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    openNotificationSettings(context)
                    finish()
                }) {
                    Text(stringResource(R.string.practice_reminder_permission_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    finish()
                }) {
                    Text(stringResource(R.string.practice_reminder_prompt_not_now))
                }
            }
        )
    }
}

@Composable
private fun PromptTextButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/** Android 13+ asks at runtime; earlier versions grant notifications at install. */
private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED

/** The app's notification settings page, or its app-info page on phones that predate it. */
private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(intent)
    } catch (notFound: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
