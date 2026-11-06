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

    /**
     * The values experiments read, for the per-session `remote_config_snapshot` journey event
     * (iOS RemoteConfigService.snapshotValues). Leftover AI-assistant keys from the app template
     * are left out; they carry no experiment and some are kilobytes long.
     */
    fun snapshotValues(): Map<String, String> =
        remoteConfig.allValues().filterKeys { it !in LEGACY_TEMPLATE_KEYS }

    /** A/B test: whether a workout session starts with background music playing (iOS parity). */
    fun musicOnByDefault(): Boolean = remoteConfig.getBoolean(MUSIC_ON_BY_DEFAULT)

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

    // iOS parity flags. Same keys and in-app defaults as iOS's RemoteConfigService, so one Firebase
    // experiment design can run on both platforms (the Android app has its own Firebase values).

    /** Onboarding: tai chi / chair / walking picker after the goals screens. */
    fun onboardingProgramSelectionEnabled(): Boolean = remoteConfig.getBoolean("onboarding_program_selection_enabled")
    fun onboardingSkipLifestyleQuestionsEnabled(): Boolean = remoteConfig.getBoolean("onboarding_skip_lifestyle_questions_enabled")
    fun onboardingGoalsFirstEnabled(): Boolean = remoteConfig.getBoolean("onboarding_goals_first_enabled")
    fun onboardingBackButtonEnabled(): Boolean = remoteConfig.getBoolean("onboarding_back_button_enabled")
    fun onboardingHideContinueOnSingleChoiceEnabled(): Boolean = remoteConfig.getBoolean("onboarding_hide_continue_on_single_choice_enabled")
    fun onboardingOptionIconsEnabled(): Boolean = remoteConfig.getBoolean("onboarding_option_icons_enabled")
    fun onboardingBodyTypeScreensEnabled(): Boolean = remoteConfig.getBoolean("onboarding_body_type_screens_enabled")
    fun onboardingWalkingExperimentEnabled(): Boolean = remoteConfig.getBoolean("onboarding_walking_experiment_enabled")
    fun onboardingResumeSkipsPaywallEnabled(): Boolean = remoteConfig.getBoolean("onboarding_resume_skips_paywall_enabled")
    fun showNotificationsPermission(): Boolean = remoteConfig.getBoolean("show_notifications_permission")
    fun showExtendedDiscoverySources(): Boolean = remoteConfig.getBoolean("show_extended_discovery_sources")

    /** Days 1..N of each program are free; later days need a subscription. */
    fun freeDaysCount(): Int = remoteConfig.getLong("free_days_count").toInt().coerceAtLeast(0)

    fun showReentryPaywall(): Boolean = remoteConfig.getBoolean("show_reentry_paywall")
    fun showDiscountPaywall(): Boolean = remoteConfig.getBoolean("show_discount_paywall")
    fun showPrePaywallTrialTimeline(): Boolean = remoteConfig.getBoolean("show_pre_paywall_trial_timeline")
    fun showPurchaseCancelRecovery(): Boolean = remoteConfig.getBoolean("show_purchase_cancel_recovery")
    fun showPostPurchaseTrialConfirmation(): Boolean = remoteConfig.getBoolean("show_post_purchase_trial_confirmation")
    fun shouldShowPostWorkoutPaywall(): Boolean = remoteConfig.getBoolean("should_show_post_workout_paywall")
    fun showPrintablePlanPaidUpsell(): Boolean = remoteConfig.getBoolean("show_printable_plan_paid_upsell")
    fun showSettingsCancelSubscriptionFlow(): Boolean = remoteConfig.getBoolean("show_settings_cancel_subscription_flow")
    fun showTrialEndingReminder(): Boolean = remoteConfig.getBoolean("show_trial_ending_reminder")
    fun trialPracticeReminderEnabled(): Boolean = remoteConfig.getBoolean("trial_practice_reminder_enabled")
    fun showPostWorkoutReminderPrompt(): Boolean = remoteConfig.getBoolean("show_post_workout_reminder_prompt")
    fun showWorkoutIntensityWidget(): Boolean = remoteConfig.getBoolean("show_workout_intensity_widget")

    companion object {
        private val LEGACY_TEMPLATE_KEYS = setOf(
            "showOnboardingVariant", "adsCapTimeInMillis", "chatPromptCategories", "showPaywallWithRewardedAds",
            "dailyMessageQuota", "messagesPerAd", "showMonthlyTrial", "isTTSActive", "promotedApps",
            "visibleProducts", "allowImageQueries"
        )
        const val PROMOTED_APPS = "promotedApps"
        const val PAYWALL_HTML_URL = "paywall_html_url"
        const val PAYWALL_HTML_ANDROID_URL = "paywall_html_android"
        const val LIMITED_OFFER_PAYWALL_HTML_URL = "limited_offer_paywall_html_url"
        const val LOCAL_NOTIFICATION_CAMPAIGN = "local_notification_campaign"
        const val MUSIC_ON_BY_DEFAULT = "music_on_by_default"
    }
}
