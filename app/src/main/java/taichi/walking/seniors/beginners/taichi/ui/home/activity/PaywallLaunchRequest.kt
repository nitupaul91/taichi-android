package taichi.walking.seniors.beginners.taichi.ui.home.activity

import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant

data class PaywallLaunchRequest(
    val source: String? = null,
    val variant: RemotePaywallVariant = RemotePaywallVariant.PRIMARY
)
