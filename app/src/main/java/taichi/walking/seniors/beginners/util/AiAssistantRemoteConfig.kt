package taichi.walking.seniors.beginners.util

import taichi.walking.seniors.beginners.R
import com.mobteq.remoteconfig.RemoteConfig
import javax.inject.Inject

class AiAssistantRemoteConfig @Inject constructor(
    private val remoteConfig: RemoteConfig
) {
    fun shouldShowOnboardingVariant(): Boolean {
        return remoteConfig.getBoolean(SHOW_ONBOARDING_VARIANT)
    }

    fun getChatPromptCategories(): String {
        return remoteConfig.getString(CHAT_PROMPT_CATEGORIES)
    }

    fun shouldShowPaywallWithRewardedAds(): Boolean {
        return false
    }

    fun getDailyMessageQuota(): Int {
        return remoteConfig.getLong(DAILY_MESSAGE_QUOTA).toInt()
    }

    fun getRewardedMessagesPerAdView(): Int {
        return remoteConfig.getLong(MESSAGES_PER_AD).toInt()
    }

    fun shouldShowMonthlyTrial(): Boolean {
        return remoteConfig.getBoolean(SHOW_MONTHLY_TRIAL)
    }

    fun isTTSActive(): Boolean {
        return remoteConfig.getBoolean(IS_TTS_ACTIVE)
    }

    fun fetchAndActivate() {
        remoteConfig.fetchAndActivate(R.xml.remote_config_values)
    }

    fun getPromotedAppsJson(): String {
        return remoteConfig.getString(PROMOTED_APPS)
    }

    fun getVisibleProducts(): String {
        return remoteConfig.getString(VISIBLE_PRODUCTS)
    }

    fun shouldAllowImageQueries(): Boolean {
        return remoteConfig.getBoolean(ALLOW_IMAGE_QUERIES)
    }

    companion object {
        const val SHOW_ONBOARDING_VARIANT = "showOnboardingVariant"
        const val SHOW_PAYWALL_WITH_REWARDED_ADS = "showPaywallWithRewardedAds"
        const val CHAT_PROMPT_CATEGORIES = "chatPromptCategories"
        const val DAILY_MESSAGE_QUOTA = "dailyMessageQuota"
        const val MESSAGES_PER_AD = "messagesPerAd"
        const val SHOW_MONTHLY_TRIAL = "showMonthlyTrial"
        const val IS_TTS_ACTIVE = "isTTSActive"
        const val PROMOTED_APPS = "promotedApps"
        const val VISIBLE_PRODUCTS = "visibleProducts"
        const val ALLOW_IMAGE_QUERIES = "allowImageQueries"
    }
}