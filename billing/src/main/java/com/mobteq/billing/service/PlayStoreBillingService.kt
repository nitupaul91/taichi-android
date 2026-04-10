package com.mobteq.billing.service

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.mobteq.billing.domain.PurchaseStatus
import com.mobteq.billing.domain.Subscription
import com.mobteq.billing.mapper.SubscriptionMapper
import com.mobteq.billing.model.purchases.local.PurchaseManager
import com.sageai.util.requireMainThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PlayStoreBillingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionsService: SubscriptionsService,
    private val billingUserIdUpdater: BillingUserIdUpdater,
    private val purchaseManager: PurchaseManager,
    private val subscriptionMapper: SubscriptionMapper,
    private val analyticsTracker: BillingAnalyticsTracker,
) : PurchasesUpdatedListener, BillingClientStateListener {

    var connectRetryDelay = 1

    val purchaseStatus = MutableStateFlow<PurchaseStatus?>(null)

    private val subscriptionProducts = MutableStateFlow<List<ProductDetails>>(listOf())
    private val oneTimeProducts = MutableStateFlow<List<ProductDetails>>(listOf())
    val products =
        combine(subscriptionProducts, oneTimeProducts) { subscriptions, oneTimeProducts ->
            subscriptions.plus(oneTimeProducts)
        }

    val purchaseFlow = MutableStateFlow<List<Subscription>>(listOf())

    @Volatile
    private var billingClient = createBillingClient()

    fun queryPurchases() {
        if (!billingClient.isReady) {
            Timber.tag(TAG).d("queryPurchases: BillingClient is not ready")

            billingClient.startConnection(this)
            return
        }
        queryOneTimeProductPurchases()
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        requireMainThread()

        if (!billingClient.isReady) {
            Timber.tag(TAG).d("launchBillingFlow: BillingClient is not ready")

            billingClient.startConnection(this)

            return 0
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode

        Timber.tag(TAG).d("launchBillingFlow: BillingResponse $responseCode")

        return responseCode
    }

    fun endConnection() {
        billingClient.endConnection()

        Timber.tag(TAG).d("PlayBillingClient -ending connection")
    }

    private fun queryOneTimeProductPurchases() {
        val subs = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(subs) { result, purchases ->
            Timber.tag(TAG)
                .d("OnQueryPurchasesResponse one time product purchase list size: ${purchases.size}")

            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.tag(TAG)
                    .e("Query one time product purchases error. ${result.responseCode} ${result.debugMessage}")
                return@queryPurchasesAsync
            }

            if (purchases.isEmpty()) {
                querySubscriptionPurchases()
                return@queryPurchasesAsync
            }

            Timber.d("User has active one time product purchases: ${purchases.size}")

            processPurchases(purchases)
        }
    }

    private fun querySubscriptionPurchases() {
        val subs = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subs) { result, purchases ->
            Timber.tag(TAG)
                .d("OnQueryPurchasesResponse subscription purchase list size: ${purchases.size}")

            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.tag(TAG)
                    .e("Query subscription purchases error. ${result.responseCode} ${result.debugMessage}")
                return@queryPurchasesAsync
            }

            processPurchases(purchases)
        }
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        Timber.tag(TAG).d("onBillingSetupFinished: ${result.responseCode}")

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Timber.tag(TAG).i("onBillingSetupFinished: BillingClient is ready")

                connectRetryDelay = 0

                queryOneTimeProductPurchases()

                querySubscriptionProducts()
//                queryOneTimePurchaseProducts()
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Timber.tag(TAG)
                    .e("Billing service disconnected. ${result.responseCode} ${result.debugMessage}")

                billingClient = createBillingClient()
                billingClient.startConnection(this)
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Timber.tag(TAG)
                    .e("Billing service error. ${result.responseCode} ${result.debugMessage}")

                retryConnecting()
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.tag(TAG).d("onBillingServiceDisconnected")

        retryConnecting()
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Timber.tag(TAG)
            .d("onPurchasesUpdated: ${billingResult.responseCode} ${billingResult.debugMessage}")

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    Timber.tag(TAG)
                        .d("Purchases ${it.firstOrNull()?.products?.joinToString { it }}")

                    processPurchases(purchases)
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.tag(TAG).d("Purchase canceled by user")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.tag(TAG).d("User already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Timber.tag(TAG).d(billingResult.debugMessage)
            }
        }
    }

    private fun processPurchases(purchases: MutableList<Purchase>) {
        Timber.tag(TAG).d("Processing purchases")

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            if (purchases.isEmpty()) {
                Timber.tag(TAG).d("Purchases are empty")

                purchaseManager.removeUserPremium()
                purchaseFlow.emit(emptyList())

                return@launch
            }

            Timber.tag(TAG).d("User has active purchases: ${purchases.size}")

            purchaseFlow.emit(purchases.map { subscriptionMapper.playBillingToDomain(it) })

            purchases.forEach { purchase ->
                billingUserIdUpdater.updateUserIdFromPurchase(purchase)

                if (purchase.isAcknowledged) {
                    Timber.tag(TAG).d("Purchase is already acknowledged.")

                    if (!purchaseManager.isUserPremiumSubscribed().first()) {
                        Timber.tag(TAG).d("setting user subscribed")

                        purchaseManager.setUserPremiumSubscribed()
                    }
                } else {
                    Timber.tag(TAG)
                        .d("Purchase is not acknowledged with state ${purchase.purchaseState}")

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        validatePurchase(purchase)
                    }
                }
            }
        }
    }

    private suspend fun validatePurchase(purchase: Purchase) {
        Timber.tag(TAG).d("Validating purchase")

        purchaseStatus.emit(PurchaseStatus.InProgress)

        Timber.tag(TAG).d("Making new api call")

        try {
            val result = subscriptionsService.validatePurchase(
                subscriptionId = purchase.products.first(),
                purchaseToken = purchase.purchaseToken
            )

            when {
                result.isSuccess && !purchase.isAcknowledged -> {
                    Timber.tag(TAG).d("Validate subscription successful")

                    acknowledgePurchase(purchase)
                }

                result.exceptionOrNull()?.message == SubscriptionsService.SUB_NOT_VALID -> {
                    purchaseStatus.emit(PurchaseStatus.SubNotValid)

                    Timber.tag(TAG).e(result.exceptionOrNull(), "Subscription not valid.")
                }

                result.exceptionOrNull() != null -> {
                    Timber.tag(TAG).e(result.exceptionOrNull(), "Validate subscription error.")
                }
            }
        } catch (e: Exception) {
            purchaseStatus.emit(PurchaseStatus.InProgress)

            Timber.tag(TAG).e(e, "Validate subscription error. Retrying.")

            queryPurchases()
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        Timber.tag(TAG).i("Acknowledging new purchase after server validation.")

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val result = billingClient.acknowledgePurchase(params)

        Timber.tag(TAG).d("Acknowledging new purchase - server response ${result.responseCode}")

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                handleAcknowledgeSuccess(purchase)
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                Timber.tag(TAG)
                    .e("Subscription acknowledgement failed. ${result.responseCode} ${result.debugMessage}")

                retryConnecting()
            }

            else -> {
                Timber.tag(TAG)
                    .e("Subscription acknowledgement failed. ${result.responseCode} ${result.debugMessage}")
            }
        }
    }

    private suspend fun handleAcknowledgeSuccess(purchase: Purchase) {
        purchaseManager.setUserPremiumSubscribed()

        val productId = purchase.products.firstOrNull()

        Timber.tag(TAG).d("Emitting PurchaseStatus.Acknowledged for productId=$productId")

        productId?.let(analyticsTracker::trackPurchaseComplete)
        purchaseStatus.emit(PurchaseStatus.Acknowledged(productId))

        trackPurchase(purchase)
    }

    private fun trackPurchase(purchase: Purchase) {
        val productId = purchase.products.first()
        analyticsTracker.trackPurchase(productId)
    }

    private fun retryConnecting() {
        Timber.tag(TAG).d("retrying to connect")

        Handler(Looper.getMainLooper()).postDelayed({
            if (!billingClient.isReady) {
                billingClient.startConnection(this@PlayStoreBillingService)
            }
        }, getNextConnectRetryDelay())
    }

    private fun querySubscriptionProducts() {
        Timber.tag(TAG).d("querying subscription products")

        val subscriptionProducts = SUBSCRIPTION_PRODUCTS_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subscriptionProducts)
            .build()

        billingClient.queryProductDetailsAsync(
            queryProductDetailsParams,
            handleSubscriptionProductsResponse()
        )
    }

    private fun handleSubscriptionProductsResponse(): (BillingResult, MutableList<ProductDetails>) -> Unit =
        { result, productList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.tag(TAG).d("Product list size ${productList.size}")

                subscriptionProducts.value = productList
            } else {
                Timber.tag(TAG)
                    .e("Query subscription products error. ${result.responseCode} ${result.debugMessage}")

                Handler(Looper.getMainLooper()).postDelayed({
                    querySubscriptionProducts()
                }, 5 * 1000L)
            }
        }

//    private fun queryOneTimePurchaseProducts() {
//        Timber.d("querying one time purchase products")
//
//        val productList = ONE_TIME_PRODUCTS.map {
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(it)
//                .setProductType(BillingClient.ProductType.INAPP)
//                .build()
//        }
//
//        val inAppProductDetailsParams = QueryProductDetailsParams.newBuilder()
//            .setProductList(productList)
//            .build()
//
//        billingClient.queryProductDetailsAsync(
//            inAppProductDetailsParams, handleOneTimeProductsResponse()
//        )
//    }

//    private fun handleOneTimeProductsResponse(): (BillingResult, List<ProductDetails>) -> Unit {
//        return { result: BillingResult, productList: List<ProductDetails> ->
//            Timber.i("Product list size ${productList.size}")
//
//            if (result.responseCode == BillingClient.BillingResponseCode.OK && productList.isNotEmpty()) {
//                Timber.i("Product list size ${productList.size} product name ${productList.get(0).name}")
//
//                oneTimeProducts.value = productList
//            } else {
//                Timber.tag(TAG)
//                    .e("Query one time purchase products error. ${result.responseCode} ${result.debugMessage}")
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    queryOneTimePurchaseProducts()
//                }, 5 * 1000L)
//            }
//        }
//    }

    private fun getNextConnectRetryDelay(): Long {
        connectRetryDelay = max(30, connectRetryDelay + 5)
        return connectRetryDelay * 1000L
    }

    private fun createBillingClient() =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    companion object {
        const val TAG = "Billing"

        const val MONTHLY_SUBSCRIPTION_V1 = "taichi_monthly_v1"
        const val YEARLY_SUBSCRIPTION_V1 = "taichi_yearly_v1"

        const val PREMIUM_LIFETIME_PRODUCT = "ai_premium_lifetime"

        private val ONE_TIME_PRODUCTS = listOf(
            PREMIUM_LIFETIME_PRODUCT
        )

        private val SUBSCRIPTION_PRODUCTS_IDS = listOf(
            MONTHLY_SUBSCRIPTION_V1,
            YEARLY_SUBSCRIPTION_V1,
        )
    }
}
