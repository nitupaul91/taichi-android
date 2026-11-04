package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import taichi.walking.seniors.beginners.util.findActivity
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

/**
 * @param onPermissionResult analytics (iOS `notification_permission_result`): whether the system
 *   prompt was shown, whether notifications are allowed afterwards, and the status before, in iOS's
 *   vocabulary ("authorized", "denied", "notDetermined").
 */
@Composable
fun NotificationPermissionScreen(
    state: OnboardingState,
    onBack: () -> Unit,
    onPermissionResult: (promptShown: Boolean, authorized: Boolean, previousStatus: String) -> Unit = { _, _, _ -> },
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var previousStatus by remember { mutableStateOf("notDetermined") }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(true, granted, previousStatus)
        onContinue()
    }

    val illustration = if (state.genderId == "female") {
        R.drawable.female_notifications
    } else {
        R.drawable.male_notifications
    }

    val requestNotifications = remember(context, launcher) {
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // No runtime permission below Android 13: notifications are on unless switched off.
                val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                onPermissionResult(false, enabled, if (enabled) "authorized" else "denied")
                onContinue()
            } else {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    onPermissionResult(false, true, "authorized")
                    onContinue()
                } else {
                    // A rationale is offered only after an earlier refusal.
                    val deniedBefore = activity != null &&
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                    previousStatus = if (deniedBefore) "denied" else "notDetermined"
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.NotificationPermission),
        title = stringResource(R.string.onboarding_notification_title),
        subtitle = stringResource(R.string.onboarding_notification_subtitle),
        onBack = onBack,
        onContinue = requestNotifications
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = illustration),
                contentDescription = null,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.onboarding_notification_prompt_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
