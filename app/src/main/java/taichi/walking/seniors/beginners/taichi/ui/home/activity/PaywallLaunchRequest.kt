package taichi.walking.seniors.beginners.taichi.ui.home.activity

import taichi.walking.seniors.beginners.ui.paywall.PaywallSource
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant

data class PaywallLaunchRequest(
    /** The notification campaign that opened the app, sent as the paywall's `source_detail`. */
    val source: String? = null,
    val variant: RemotePaywallVariant = RemotePaywallVariant.PRIMARY,
    /** The paywall's analytics `source`: a notification deep link unless home opened it itself. */
    val paywallSource: String = PaywallSource.DEEP_LINK
)
