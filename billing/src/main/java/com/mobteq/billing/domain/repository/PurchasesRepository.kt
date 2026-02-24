package com.mobteq.billing.domain.repository

import android.app.Activity
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.mobteq.billing.domain.Product
import com.mobteq.billing.domain.PurchaseStatus
import com.mobteq.billing.domain.SubscriptionProduct
import com.mobteq.billing.mapper.mapToPurchaseProduct
import com.mobteq.billing.service.PlayStoreBillingService
import com.sageai.id.IDService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchasesRepository @Inject constructor(
    private val billingClient: PlayStoreBillingService,
    private val idService: IDService
) {

    private val products = MutableStateFlow<List<Product>>(emptyList())

    val purchaseFlow = billingClient.purchaseFlow

    private val playBillingProductMap = mutableMapOf<String, ProductDetails>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preloadSubsProducts()
        }
    }

    suspend fun preloadSubsProducts() {
        billingClient.products
            .onCompletion { error ->
                error?.also {
                    Timber.tag(PlayStoreBillingService.TAG).e(error, "Product flow error.")
                }
            }
            .collect { subs ->
                // Clear old cached products to ensure we use fresh offer tokens
                playBillingProductMap.clear()

                val products = subs.mapNotNull { billingProduct ->
                    playBillingProductMap[billingProduct.productId] = billingProduct

                    billingProduct.mapToPurchaseProduct()
                }
                this.products.emit(products)
            }
    }

    suspend fun makePurchase(activity: Activity, product: Product) {
        Timber.d("makePurchase SubscriptionProduct")

        val playBillingProduct = playBillingProductMap[product.productId]
        playBillingProduct?.let {
            val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(it)
            if (product is SubscriptionProduct) {
                builder.setOfferToken(product.offerIdToken)
            }
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(builder.build()))
                .setObfuscatedAccountId(idService.getUserID())
                .build()
            billingClient.launchBillingFlow(activity, params)
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    fun queryPurchases() {
        billingClient.queryPurchases()
    }

    fun getPurchaseStatus(): StateFlow<PurchaseStatus?> {
        return billingClient.purchaseStatus
    }

    fun getProducts(): SharedFlow<List<Product>> {
        return products
    }

    fun clearPurchaseStatus() {
        billingClient.purchaseStatus.value = null
    }
}