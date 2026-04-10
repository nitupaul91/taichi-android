package com.mobteq.billing.service

import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.events.firebase.AnalyticsEvent
import com.mobteq.billing.service.PlayStoreBillingService.Companion.MONTHLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.PlayStoreBillingService.Companion.YEARLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.SubscriptionStartEvent.Companion.EVENT_NAME_MONTHLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.SubscriptionStartEvent.Companion.EVENT_NAME_YEARLY_SUBSCRIPTION_V1
import timber.log.Timber
import javax.inject.Inject

class BillingAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) {

    fun trackPurchaseComplete(productId: String) {
        Timber.tag(TAG).i("Tracking purchase_complete. productId: $productId")

        firebaseAnalytics.logEvent(
            EVENT_NAME_PURCHASE_COMPLETE,
            bundleOf(PROPERTY_PRODUCT_ID to productId)
        )
    }

    fun trackPurchase(productId: String) {
        Timber.tag(TAG).i("Tracking purchase. productId: $productId")

        when (productId) {
            MONTHLY_SUBSCRIPTION_V1 -> firebaseAnalytics.logEvent(
                SubscriptionStartEvent(EVENT_NAME_MONTHLY_SUBSCRIPTION_V1)
            )
            YEARLY_SUBSCRIPTION_V1 -> firebaseAnalytics.logEvent(
                SubscriptionStartEvent(EVENT_NAME_YEARLY_SUBSCRIPTION_V1)
            )
        }
    }

    companion object {
        private const val TAG = "BillingAnalytics"
        private const val EVENT_NAME_PURCHASE_COMPLETE = "purchase_complete"
        private const val PROPERTY_PRODUCT_ID = "product_id"
    }
}

data class SubscriptionStartEvent(
    override val name: String
) : AnalyticsEvent() {

    companion object {
        const val EVENT_NAME_MONTHLY_SUBSCRIPTION_V1 = "monthly_subscription"
        const val EVENT_NAME_YEARLY_SUBSCRIPTION_V1 = "yearly_subscription"
    }
}
