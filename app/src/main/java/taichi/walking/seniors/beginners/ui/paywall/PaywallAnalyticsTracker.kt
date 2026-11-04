package taichi.walking.seniors.beginners.ui.paywall

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.FirebaseAnalytics.Companion.PROPERTY_SCREEN_NAME
import com.mobteq.analytics.TikTokAnalyticsTracker
import com.mobteq.billing.domain.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/** Which surface opened the paywall, sent as `source` on every paywall event (iOS PaywallTracker). */
object PaywallSource {
    const val ONBOARDING = "onboarding"
    const val WORKOUT_PREVIEW = "workout_preview"
    const val SETTINGS = "settings"
    const val POST_WORKOUT = "post_workout"
    const val DEEP_LINK = "deep_link"
    const val UNKNOWN = "unknown"
}

class PaywallAnalyticsTracker @Inject constructor(
    private val firebaseAnalyticsTracker: FirebaseAnalytics,
    private val tikTokAnalyticsTracker: TikTokAnalyticsTracker,
) {

    /** Set when a paywall is shown; the view model can outlive one presentation. */
    private var source: String = PaywallSource.UNKNOWN
    private var sourceDetail: String? = null

    fun setSource(source: String, detail: String?) {
        this.source = source
        this.sourceDetail = detail
    }

    fun trackPaywallView(extraParams: Bundle = bundleOf()) {
        trackEvent(NAME_VIEWED_PAYWALL, extraParams)
        tikTokAnalyticsTracker.trackCustomEvent(
            eventName = TIKTOK_EVENT_PAYWALL_VIEW,
            properties = mapOf(PROPERTY_SCREEN_NAME to VALUE_PAYWALL)
        )
    }

    fun trackSelectProduct(productId: String) {
        trackEvent(
            name = NAME_CLICKED_PRODUCT,
            params = bundleOf(
                PROPERTY_PRODUCT_ID to productId
            )
        )
    }

    fun trackMakePurchase(product: Product?) {
        val productId = product?.productId ?: "NULL"
        trackEvent(
            name = NAME_CLICKED_MAKE_PURCHASE,
            params = bundleOf(
                PROPERTY_PRODUCT_ID to productId
            )
        )
        tikTokAnalyticsTracker.trackCustomEvent(
            eventName = NAME_CLICKED_MAKE_PURCHASE,
            properties = mapOf(
                PROPERTY_SCREEN_NAME to VALUE_PAYWALL,
                PROPERTY_PRODUCT_ID to productId,
                PROPERTY_PRICE to product?.displayPrice,
                PROPERTY_HAS_FREE_TRIAL to (product?.freeTrial != null),
                PROPERTY_FREE_TRIAL_DAYS to product?.freeTrial
            )
        )
    }

    fun trackPaywallClose() {
        trackEvent(NAME_CLOSED_PAYWALL)
    }

    /** iOS PaywallTracker.trackPurchaseOutcome: "success", "user_cancelled", … */
    fun trackPurchaseOutcome(outcome: String, product: Product, extraParams: Bundle = bundleOf()) {
        val params = productParams(product).apply {
            putString(PROPERTY_PRODUCT_ID, product.productId)
            putString("outcome", outcome)
            putAll(extraParams)
        }
        trackEvent(NAME_PURCHASE_OUTCOME, params)
    }

    fun trackPurchaseRecoveryViewed(details: TrialTimelineDetails) {
        trackTrialEvent(NAME_PURCHASE_RECOVERY_VIEWED, details)
    }

    fun trackPurchaseRecoveryRetry(details: TrialTimelineDetails) {
        trackTrialEvent(NAME_PURCHASE_RECOVERY_RETRY, details)
    }

    fun trackPurchaseRecoveryDismissed(details: TrialTimelineDetails) {
        trackTrialEvent(NAME_PURCHASE_RECOVERY_DISMISSED, details)
    }

    fun trackPaywallPersonalization(variants: Map<String, Any?>) {
        val params = bundleOf()
        (variants["titleVariant"] as? String)?.let { params.putString("title_variant", it) }
        (variants["heroVariant"] as? String)?.let { params.putString("hero_variant", it) }
        (variants["urgencyVariant"] as? String)?.let { params.putString("urgency_variant", it) }
        ((variants["segment"] as? Map<*, *>)?.get("primaryGoal") as? String)?.let {
            params.putString("primary_goal", it)
        }
        trackEvent(NAME_PAYWALL_PERSONALIZATION, params)
    }

    private fun trackTrialEvent(name: String, details: TrialTimelineDetails) {
        trackEvent(
            name = name,
            params = bundleOf(
                PROPERTY_PRODUCT_ID to details.productId,
                "trial_days" to details.trialLengthDays.toString(),
                "trial_end" to iso8601(details.trialEndMillis)
            )
        )
    }

    private fun trackEvent(name: String, params: Bundle = bundleOf()) {
        params.putString(PROPERTY_SCREEN_NAME, VALUE_PAYWALL)
        params.putString(PROPERTY_SOURCE, source)
        sourceDetail?.takeIf { it.isNotBlank() }?.let { params.putString(PROPERTY_SOURCE_DETAIL, it) }
        firebaseAnalyticsTracker.logEvent(name, params)
    }

    /** Scalar pricing and offer facts (iOS PaywallTracker.productParams; Play gives no currency here). */
    private fun productParams(product: Product): Bundle = bundleOf(
        PROPERTY_PRICE to product.price / 1_000_000.0,
        "period_unit" to (product.subscriptionPeriodUnit ?: "none"),
        "period_value" to (product.subscriptionPeriodValue ?: 0).toLong(),
        "offer_type" to if (product.introOfferPaymentMode == "freeTrial") "free_trial" else "none",
        "trial_days" to (product.freeTrial?.toLongOrNull() ?: 0L)
    )

    private fun iso8601(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    companion object {
        private const val NAME_VIEWED_PAYWALL = "viewed_paywall"
        private const val NAME_CLOSED_PAYWALL = "closed_paywall"
        private const val NAME_CLICKED_PRODUCT = "selected_product"
        private const val NAME_CLICKED_MAKE_PURCHASE = "clicked_make_purchase"
        private const val NAME_PAYWALL_PERSONALIZATION = "paywall_personalization"
        private const val NAME_PURCHASE_OUTCOME = "purchase_outcome"
        private const val NAME_PURCHASE_RECOVERY_VIEWED = "purchase_recovery_viewed"
        private const val NAME_PURCHASE_RECOVERY_RETRY = "purchase_recovery_retry_tapped"
        private const val NAME_PURCHASE_RECOVERY_DISMISSED = "purchase_recovery_dismissed"

        private const val PROPERTY_PRODUCT_ID = "product_id"
        private const val PROPERTY_PRICE = "price"
        private const val PROPERTY_HAS_FREE_TRIAL = "has_free_trial"
        private const val PROPERTY_FREE_TRIAL_DAYS = "free_trial_days"
        private const val PROPERTY_SOURCE = "source"
        private const val PROPERTY_SOURCE_DETAIL = "source_detail"

        private const val VALUE_PAYWALL = "paywall_screen"
        private const val TIKTOK_EVENT_PAYWALL_VIEW = "paywall_view"
    }
}
