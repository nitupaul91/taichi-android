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
}