package taichi.walking.seniors.beginners.taichi.ui.home.activity

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import taichi.walking.seniors.beginners.util.AppFontScale
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import taichi.walking.seniors.beginners.taichi.notifications.reminder.ReminderNotificationTapTracker
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.screen.TaiChiHomeRoot
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import taichi.walking.seniors.beginners.util.ReviewPromptService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaiChiHomeActivity : ComponentActivity() {

    // dynamic_type_enabled: fixed text sizes, or the phone's font size capped (see AppFontScale).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppFontScale.wrap(newBase))
    }

    private var pendingPaywallRequest by mutableStateOf<PaywallLaunchRequest?>(null)

    @Inject
    lateinit var reminderNotificationTapTracker: ReminderNotificationTapTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            reminderNotificationTapTracker.trackIfFromReminder(intent)
        }
        pendingPaywallRequest = intent.toPaywallLaunchRequest()
        setContent {
            TaiChiTheme(useDarkTheme = false) {
                TaiChiHomeRoot(
                    pendingPaywallRequest = pendingPaywallRequest,
                    onPaywallRequestConsumed = {
                        pendingPaywallRequest = null
                        intent.removeExtra(EXTRA_PAYWALL_SOURCE)
                        intent.removeExtra(EXTRA_PAYWALL_VARIANT)
                    }
                )
            }
        }
        if (savedInstanceState == null) onHomeVisit()
    }

    override fun onRestart() {
        super.onRestart()
        // Back from the background: iOS counts this as another Home visit too.
        onHomeVisit()
    }

    /**
     * iOS JourneyHomeView.requestReviewIfAppropriate: a rating is asked from the second visit to
     * Home onwards (the first lands straight out of onboarding), 2.5 s in, and never on top of a
     * paywall.
     */
    private fun onHomeVisit() {
        if (ReviewPromptService.recordHomeVisit(this) < 2 || pendingPaywallRequest != null) return
        lifecycleScope.launch {
            delay(2_500)
            if (pendingPaywallRequest == null) {
                ReviewPromptService.requestIfNeeded(this@TaiChiHomeActivity, surface = "home")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        reminderNotificationTapTracker.trackIfFromReminder(intent)
        pendingPaywallRequest = intent.toPaywallLaunchRequest()
    }

    companion object {
        private const val EXTRA_PAYWALL_SOURCE = "extra_paywall_source"
        private const val EXTRA_PAYWALL_VARIANT = "extra_paywall_variant"

        fun createIntent(
            context: Context,
            paywallRequest: PaywallLaunchRequest
        ): Intent = Intent(context, TaiChiHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PAYWALL_SOURCE, paywallRequest.source)
            putExtra(EXTRA_PAYWALL_VARIANT, paywallRequest.variant.name)
        }
    }

    private fun Intent?.toPaywallLaunchRequest(): PaywallLaunchRequest? {
        val variant = this?.getStringExtra(EXTRA_PAYWALL_VARIANT)
            ?.let { runCatching { RemotePaywallVariant.valueOf(it) }.getOrNull() }
            ?: return null

        return PaywallLaunchRequest(
            source = this.getStringExtra(EXTRA_PAYWALL_SOURCE),
            variant = variant
        )
    }
}
