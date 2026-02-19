package com.mobteq.billing.domain

sealed interface Product {
    val productId: String
    val productName: String
    val displayPrice: String
    val price: Long
    val freeTrial: String?
}

data class SubscriptionProduct(
    override val productId: String,
    override val productName: String,
    override val displayPrice: String,
    override val price: Long,
    override val freeTrial: String?,
    val offerIdToken: String,
) : Product

data class OneTimePurchaseProduct(
    override val productId: String,
    override val productName: String,
    override val displayPrice: String,
    override val price: Long,
    override val freeTrial: String?,
) : Product