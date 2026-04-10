package taichi.walking.seniors.beginners.ui.paywall

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.FirebaseAnalytics.Companion.PROPERTY_SCREEN_NAME
import javax.inject.Inject

class PaywallAnalyticsTracker @Inject constructor(
    private val firebaseAnalyticsTracker: FirebaseAnalytics
) {

    fun trackPaywallView() {
        trackEvent(NAME_VIEWED_PAYWALL)
    }

    fun trackSelectProduct(productId: String) {
        trackEvent(
            name = NAME_CLICKED_PRODUCT,
            params = bundleOf(
                PROPERTY_PRODUCT_ID to productId
            )
        )
    }

    fun trackMakePurchase(productId: String?) {
        trackEvent(
            name = NAME_CLICKED_MAKE_PURCHASE,
            params = bundleOf(
                PROPERTY_PRODUCT_ID to (productId ?: "NULL")
            )
        )
    }

    fun trackPaywallClose() {
        trackEvent(NAME_CLOSED_PAYWALL)
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

    private fun trackEvent(name: String, params: Bundle = bundleOf()) {
        params.putString(PROPERTY_SCREEN_NAME, VALUE_PAYWALL)
        firebaseAnalyticsTracker.logEvent(name, params)
    }

    companion object {
        private const val NAME_VIEWED_PAYWALL = "viewed_paywall"
        private const val NAME_CLOSED_PAYWALL = "closed_paywall"
        private const val NAME_CLICKED_PRODUCT = "selected_product"
        private const val NAME_CLICKED_MAKE_PURCHASE = "clicked_make_purchase"
        private const val NAME_PAYWALL_PERSONALIZATION = "paywall_personalization"

        private const val PROPERTY_PRODUCT_ID = "product_id"

        private const val VALUE_PAYWALL = "paywall_screen"
    }
}
