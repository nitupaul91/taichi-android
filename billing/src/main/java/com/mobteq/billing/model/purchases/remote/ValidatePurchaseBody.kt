package com.mobteq.billing.model.purchases.remote

import com.google.gson.annotations.SerializedName

data class ValidatePurchaseBody(
    @SerializedName("subscriptionId") val subscriptionId: String,
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: String,
)