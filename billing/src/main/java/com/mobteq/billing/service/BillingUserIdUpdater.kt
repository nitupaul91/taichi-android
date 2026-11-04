package com.mobteq.billing.service

import com.android.billingclient.api.Purchase
import com.mobteq.billing.datastore.DataStorePrefs
import com.sageai.id.IDService
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class BillingUserIdUpdater @Inject constructor(
    private val idService: IDService,
    private val subscriptionsService: SubscriptionsService,
    private val dataStorePrefs: DataStorePrefs,
) {

    suspend fun updateUserIdFromPurchase(purchase: Purchase) {
        if (dataStorePrefs.needsSubscriptionRelink().first()) {
            relinkToCurrentUserId(purchase)
            return
        }

        val userId = purchase.accountIdentifiers?.obfuscatedAccountId
        if (!userId.isNullOrBlank()) {
            Timber.tag(IDService.TAG).d("UserId found on purchase")

            idService.updateUserId(userId)
        } else {
            Timber.tag(IDService.TAG).e("UserId not found on purchase. orderId=${purchase.orderId}")

            if (!dataStorePrefs.hasSyncedUserIdWithRemote().first()) {
                Timber.tag(IDService.TAG).d("Retrieving userId from remote")

                val remoteUserId = subscriptionsService.getUserIdFromRemote(
                    purchase.orderId!!,
                    purchase.purchaseToken
                )
                    .getOrNull()

                if (!remoteUserId.isNullOrBlank()) {
                    idService.updateUserId(remoteUserId)
                    dataStorePrefs.setHasSyncedUserIdWithRemote(true)
                } else {
                    Timber.tag(IDService.TAG).d("Remote returned null user id")
                }
            }
        }
    }

    /**
     * After an account deletion the device has a brand-new user id, while a still-active Play
     * purchase keeps the deleted id as its obfuscated account id. Validating the purchase again
     * under the current id attaches the subscription to the new account on the server, like the iOS
     * app's subscription relink; account deletion never touches the Play subscription itself.
     * Stays pending (retried on the next purchase query) until the server answers.
     */
    private suspend fun relinkToCurrentUserId(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return

        Timber.tag(IDService.TAG).d("Relinking purchase to the current userId")

        val result = runCatching {
            subscriptionsService.validatePurchase(productId, purchase.purchaseToken)
        }.getOrElse { Result.failure(it) }

        val nothingLeftToLink = result.exceptionOrNull()?.message == SubscriptionsService.SUB_NOT_VALID
        if (result.isSuccess || nothingLeftToLink) {
            dataStorePrefs.setNeedsSubscriptionRelink(false)
            dataStorePrefs.setHasSyncedUserIdWithRemote(true)
        } else {
            Timber.tag(IDService.TAG).w(result.exceptionOrNull(), "Subscription relink failed; will retry")
        }
    }
}
