package com.mobteq.billing.domain

sealed interface Product {
    val productId: String
    val productName: String
    val displayPrice: String
    val price: Long
    val freeTrial: String?
    val subscriptionPeriodUnit: String?
    val subscriptionPeriodValue: Int?
    val introOfferPaymentMode: String?
    val introOfferPeriodUnit: String?
    val introOfferPeriodValue: Int?
}

data class SubscriptionProduct(
    override val productId: String,
    override val productName: String,
    override val displayPrice: String,
    override val price: Long,
    override val freeTrial: String?,
    override val subscriptionPeriodUnit: String?,
    override val subscriptionPeriodValue: Int?,
    override val introOfferPaymentMode: String?,
    override val introOfferPeriodUnit: String?,
    override val introOfferPeriodValue: Int?,
    val offerIdToken: String,
) : Product

data class OneTimePurchaseProduct(
    override val productId: String,
    override val productName: String,
    override val displayPrice: String,
    override val price: Long,
    override val freeTrial: String?,
    override val subscriptionPeriodUnit: String? = null,
    override val subscriptionPeriodValue: Int? = null,
    override val introOfferPaymentMode: String? = null,
    override val introOfferPeriodUnit: String? = null,
    override val introOfferPeriodValue: Int? = null,
) : Product
