package com.mobteq.billing.model.purchases.remote

import com.google.gson.annotations.SerializedName

data class FindUserIdByPurchaseBody(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("purchaseToken") val purchaseToken: String,
)