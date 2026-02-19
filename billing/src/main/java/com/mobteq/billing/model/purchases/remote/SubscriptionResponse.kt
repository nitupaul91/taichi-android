package com.mobteq.billing.model.purchases.remote

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionResponse(
    @SerializedName("expiryTimeMillis") val expiryTimeMillis: Long
)