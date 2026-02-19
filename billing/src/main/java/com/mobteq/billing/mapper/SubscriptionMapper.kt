package com.mobteq.billing.mapper

import com.android.billingclient.api.Purchase
import com.mobteq.billing.domain.Subscription
import javax.inject.Inject

class SubscriptionMapper @Inject constructor() {

    fun playBillingToDomain(purchase: Purchase): Subscription =
        Subscription(
            purchaseToken = purchase.purchaseToken,
            orderNumber = purchase.orderId!!,
            purchaseTime = purchase.purchaseTime,
        )
}