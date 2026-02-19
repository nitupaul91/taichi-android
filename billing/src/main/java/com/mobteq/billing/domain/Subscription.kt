package com.mobteq.billing.domain

data class Subscription(
    val purchaseToken: String,
    val orderNumber: String,
    val purchaseTime: Long,
)