package com.mobteq.billing.model.purchases.remote

import com.google.gson.annotations.SerializedName

data class FindUserIdByPurchaseResponse(
    @SerializedName("userId") val userId: String?,
)