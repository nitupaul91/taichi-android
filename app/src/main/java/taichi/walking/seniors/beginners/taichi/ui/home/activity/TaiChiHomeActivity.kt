package taichi.walking.seniors.beginners.taichi.ui.home.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.screen.TaiChiHomeRoot
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaiChiHomeActivity : ComponentActivity() {
    private var pendingPaywallRequest by mutableStateOf<PaywallLaunchRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
