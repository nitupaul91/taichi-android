package com.mobteq.billing.domain

sealed class PurchaseStatus {
    object Acknowledged : PurchaseStatus()
    object InProgress : PurchaseStatus()
    object SubNotValid : PurchaseStatus()
}