package com.mobteq.billing.api

import com.mobteq.billing.model.purchases.remote.FindUserIdByPurchaseBody
import com.mobteq.billing.model.purchases.remote.FindUserIdByPurchaseResponse
import com.mobteq.billing.model.purchases.remote.SubscriptionResponse
import com.mobteq.billing.model.purchases.remote.ValidatePurchaseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchaseApi {

    @POST("purchases/subscriptions/validate")
    suspend fun validatePurchase(
        @Body body: ValidatePurchaseBody
    ): Response<SubscriptionResponse>

    @POST("purchases/findUserId")
    suspend fun findUserIdByPurchase(
        @Body body: FindUserIdByPurchaseBody
    ): Response<FindUserIdByPurchaseResponse>

    @GET("purchases/trial/validate/{userId}")
    suspend fun hasTrialRenewed(
        @Path("userId") userId: String
    ): Response<Boolean>

}