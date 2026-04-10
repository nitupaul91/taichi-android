package com.mobteq.billing.domain

sealed class PurchaseStatus {
    data class Acknowledged(val productId: String?) : PurchaseStatus()
    object InProgress : PurchaseStatus()
    object SubNotValid : PurchaseStatus()
}
