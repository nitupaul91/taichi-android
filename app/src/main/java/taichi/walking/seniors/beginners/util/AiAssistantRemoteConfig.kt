package taichi.walking.seniors.beginners.util

import com.mobteq.remoteconfig.RemoteConfig
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import javax.inject.Inject

enum class LocalNotificationCampaign(
    val rawValue: String,
    val paywallVariant: RemotePaywallVariant?,
    val paywallSource: String?
) {
    OFF(
        rawValue = "off",
        paywallVariant = null,
        paywallSource = null
    ),
    NORMAL_PAYWALL(
        rawValue = "normal_paywall",
        paywallVariant = RemotePaywallVariant.PRIMARY,
        paywallSource = "local_1h_normal"
    ),
    DISCOUNT_PAYWALL(
        rawValue = "discount_paywall",
        paywallVariant = RemotePaywallVariant.ONBOARDING_LIMITED_OFFER,
        paywallSource = "local_1h_discount"
    );

    companion object {
        fun fromRemoteValue(value: String?): LocalNotificationCampaign =
            entries.firstOrNull { it.rawValue == value?.trim()?.lowercase() } ?: OFF
    }
}

class AiAssistantRemoteConfig @Inject constructor(
    private val remoteConfig: RemoteConfig
) {

    fun fetchAndActivate(onComplete: (() -> Unit)? = null) {
        remoteConfig.fetchAndActivate(R.xml.remote_config_values)
            .addOnCompleteListener { onComplete?.invoke() }
    }

    fun getPromotedAppsJson(): String {
        return remoteConfig.getString(PROMOTED_APPS)
    }

    fun getPaywallHtmlUrl(): String {
        return remoteConfig.getString(PAYWALL_HTML_URL).trim()
    }

    fun getAndroidPaywallHtmlUrl(): String {
        return remoteConfig.getString(PAYWALL_HTML_ANDROID_URL).trim()
    }

    fun getLimitedOfferPaywallHtmlUrl(): String {
        return remoteConfig.getString(LIMITED_OFFER_PAYWALL_HTML_URL).trim()
    }

    fun localNotificationCampaign(): LocalNotificationCampaign {
        return LocalNotificationCampaign.fromRemoteValue(
            remoteConfig.getString(LOCAL_NOTIFICATION_CAMPAIGN)
        )
    }

    companion object {
        const val PROMOTED_APPS = "promotedApps"
        const val PAYWALL_HTML_URL = "paywall_html_url"
        const val PAYWALL_HTML_ANDROID_URL = "paywall_html_android"
        const val LIMITED_OFFER_PAYWALL_HTML_URL = "limited_offer_paywall_html_url"
        const val LOCAL_NOTIFICATION_CAMPAIGN = "local_notification_campaign"
    }
}
