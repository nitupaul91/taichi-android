package com.mobteq.billing.domain

data class Subscription(
    val purchaseToken: String,
    val orderNumber: String,
    val purchaseTime: Long,
    /** The Play product this purchase is for; null only if Play returned no product. */
    val productId: String? = null,
)
