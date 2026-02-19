package taichi.walking.seniors.beginners.ui.paywall

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.FirebaseAnalytics.Companion.PROPERTY_SCREEN_NAME
import com.mobteq.analytics.MixpanelAnalytics
import javax.inject.Inject

class PaywallAnalyticsTracker @Inject constructor(
    private val firebaseAnalyticsTracker: FirebaseAnalytics,
    private val mixpanelAnalytics: MixpanelAnalytics
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

    private fun trackEvent(name: String, params: Bundle = bundleOf()) {
        params.putString(PROPERTY_SCREEN_NAME, VALUE_PAYWALL)

        // Firebase
        firebaseAnalyticsTracker.logEvent(name, params)

        // Mixpanel
        val mapParams = bundleToMap(params)
        mixpanelAnalytics.logEvent(name, mapParams)
    }

    private fun bundleToMap(bundle: Bundle): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        for (key in bundle.keySet()) {
            bundle.get(key)?.let { value ->
                map[key] = value
            }
        }
        return map
    }

    companion object {
        private const val NAME_VIEWED_PAYWALL = "viewed_paywall"
        private const val NAME_CLOSED_PAYWALL = "closed_paywall"
        private const val NAME_CLICKED_PRODUCT = "selected_product"
        private const val NAME_CLICKED_MAKE_PURCHASE = "clicked_make_purchase"

        private const val PROPERTY_PRODUCT_ID = "product_id"

        private const val VALUE_PAYWALL = "paywall_screen"
    }
}
