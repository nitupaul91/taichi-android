package com.mobteq.billing.service

import com.mobteq.billing.api.PurchaseApi
import com.mobteq.billing.model.purchases.remote.FindUserIdByPurchaseBody
import com.mobteq.billing.model.purchases.remote.ValidatePurchaseBody
import com.sageai.id.IDService
import com.sageai.util.retryIO
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class SubscriptionsService @Inject constructor(
    private val purchaseApi: PurchaseApi,
    private val idService: IDService,
) {

    suspend fun validatePurchase(
        subscriptionId: String,
        purchaseToken: String
    ): Result<Unit> {
        val requestBody =
            ValidatePurchaseBody(subscriptionId, purchaseToken, idService.getUserID())
        val result = retryIO { purchaseApi.validatePurchase(requestBody) }
        return when (result.code()) {
            in 200..299 -> Result.success(Unit)
            404 -> Result.failure(Throwable(SUB_NOT_VALID))
            else -> Result.failure(HttpException(result))
        }
    }

    suspend fun getUserIdFromRemote(
        orderId: String,
        purchaseToken: String
    ): Result<String?> {
        Timber.tag(TAG).d("getUserIdFromRemote")

        val requestBody = FindUserIdByPurchaseBody(orderId, purchaseToken)
        try {
            val result = retryIO { purchaseApi.findUserIdByPurchase(requestBody) }
            return when (result.code()) {
                in 200..299 -> Result.success(result.body()?.userId)
                else -> Result.failure(HttpException(result))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    companion object {
        const val TAG = "SubscriptionsService"

        const val SUB_NOT_VALID = "Subscription not valid."
        const val SUB_NOT_EXTENDED = "Subscription not extended."
    }
}