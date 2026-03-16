package com.mobteq.billing.service

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
