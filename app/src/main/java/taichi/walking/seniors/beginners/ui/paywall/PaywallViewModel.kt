package taichi.walking.seniors.beginners.ui.paywall

import android.app.Activity
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mobteq.billing.datastore.DataStorePrefs
import com.mobteq.billing.domain.Product
import com.mobteq.billing.domain.PurchaseStatus
import com.mobteq.billing.domain.repository.PurchasesRepository
import com.mobteq.billing.service.PlayStoreBillingService.Companion.MONTHLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.PlayStoreBillingService.Companion.PREMIUM_LIFETIME_PRODUCT
import com.mobteq.billing.service.PlayStoreBillingService.Companion.YEARLY_SUBSCRIPTION_V1
import com.sageai.id.IDService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.service.UserService
import taichi.walking.seniors.beginners.taichi.notifications.reminder.TrialStartReminders
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingPrefs
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import taichi.walking.seniors.beginners.util.Strings
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val purchasesRepository: PurchasesRepository,
    private val analytics: PaywallAnalyticsTracker,
    private val strings: Strings,
    private val dataStorePrefs: DataStorePrefs,
    private val userService: UserService,
    private val onboardingPrefs: OnboardingPrefs,
    private val gson: Gson,
    private val idService: IDService,
    private val remotePaywallCache: RemotePaywallCache,
    private val trialStartReminders: TrialStartReminders,
    private val remoteConfig: AiAssistantRemoteConfig,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val events = MutableSharedFlow<PaywallEvents>()
    val eventsFlow = events.asSharedFlow()

    val products = MutableStateFlow<List<UIProduct>>(emptyList())

    val selectedProduct = MutableStateFlow<Product?>(null)

    private val _isLoadingProducts = MutableStateFlow(true)
    val isLoadingProducts: StateFlow<Boolean> = _isLoadingProducts

    private val _isPurchaseInProgress = MutableStateFlow(false)
    val isPurchaseInProgress: StateFlow<Boolean> = _isPurchaseInProgress

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _onboardingState = MutableStateFlow<OnboardingState?>(null)
    val onboardingState: StateFlow<OnboardingState?> = _onboardingState

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    /**
     * The product of the purchase sheet this paywall opened, until Play reports back. Only this
     * ViewModel's own purchase starts a trial here: the purchase status is shared by every paywall
     * instance, and a restore is not a trial start.
     */
    @Volatile
    private var purchaseAttemptProduct: Product? = null

    private val _purchaseRecovery = MutableStateFlow<PurchaseRecovery?>(null)

    /** Shown over the paywall after the user dismisses the Play sheet (`show_purchase_cancel_recovery`). */
    val purchaseRecovery: StateFlow<PurchaseRecovery?> = _purchaseRecovery.asStateFlow()

    init {
        getProducts()
        subscribeToPurchaseStatus()
        subscribeToPurchaseCancellations()
        subscribeToOnboardingState()
        fetchUserId()
    }

    /**
     * One call per presentation. The view model can outlive a paywall (home keeps one per
     * activity), so the view is tracked here rather than at creation.
     */
    fun onPaywallShown(source: String, sourceDetail: String?, renderer: String, paywallVariant: String) {
        analytics.setSource(source, sourceDetail)
        _purchaseRecovery.value = null
        viewModelScope.launch {
            analytics.trackPaywallView(
                bundleOf(
                    "paywall_variant" to paywallVariant,
                    "renderer" to renderer,
                    "was_premium" to dataStorePrefs.isSubscribed().first()
                )
            )
        }
    }

    private fun subscribeToPurchaseStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            purchasesRepository.getPurchaseStatus()
                .collect {
                    when (it) {
                        PurchaseStatus.InProgress -> {
                            _isPurchaseInProgress.value = true
                            _errorMessage.value = null
                            events.emit(PaywallEvents.PurchaseInProgress)
                        }
                        is PurchaseStatus.Acknowledged -> {
                            purchasesRepository.clearPurchaseStatus()
                            _isPurchaseInProgress.value = false
                            _isRestoring.value = false
                            _errorMessage.value = null

                            val purchasedProduct = takePurchaseAttempt(it.productId)
                            val trialDetails = purchasedProduct?.let { product ->
                                trialStartReminders.onPurchaseAcknowledged(product)
                                TrialTimelineDetails.make(product, System.currentTimeMillis())
                            }
                            purchasedProduct?.let { product ->
                                analytics.trackPurchaseOutcome(
                                    outcome = "success",
                                    product = product,
                                    extraParams = bundleOf("is_trial" to (trialDetails != null))
                                )
                            }
                            trialDetails?.let { details ->
                                events.emit(PaywallEvents.TrialPurchased(details))
                            }
                            events.emit(PaywallEvents.PurchaseIsAcknowledged)

//                            garageRepository.trainModel()

                            closeScreen()
                        }

                        PurchaseStatus.SubNotValid -> {
                            purchaseAttemptProduct = null
                            _isPurchaseInProgress.value = false
                            _errorMessage.value = if (_isRestoring.value) {
                                "No previous purchases found."
                            } else {
                                "Purchase failed. Please try again."
                            }
                            _isRestoring.value = false
                            events.emit(PaywallEvents.PurchaseNotValid)
                        }
                        else -> Unit
                    }
                }
        }
    }

    /**
     * A purchase this paywall launched was acknowledged: schedules the trial reminders and returns
     * the trial it started, if the product's Play offer is a free trial (iOS
     * PaywallView.isConfirmedFreeTrial). Play only offers trials the user is eligible for, and the
     * purchase used that offer's token. The end date is projected from the offer: Play Billing
     * gives the client no expiry time.
     */
    /**
     * The product this paywall opened the Play sheet for, when [acknowledgedProductId] is it, and
     * forgets it: only this ViewModel's own purchase counts (the purchase status is shared by every
     * paywall instance, and a restore is not a trial start).
     */
    private fun takePurchaseAttempt(acknowledgedProductId: String?): Product? {
        val product = purchaseAttemptProduct
            ?.takeIf { acknowledgedProductId == null || it.productId == acknowledgedProductId }
            ?: return null
        purchaseAttemptProduct = null
        return product
    }

    private fun productById(productId: String): Product? =
        products.value.firstOrNull { it.product.productId == productId }?.product

    private fun subscribeToPurchaseCancellations() {
        viewModelScope.launch {
            purchasesRepository.getPurchaseCancellations().collect { onPurchaseSheetCanceled() }
        }
    }

    /**
     * iOS PaywallView `.userCancelled`: if the dismissed product carries a free trial, reassure the
     * user that nothing is charged today and offer the trial again.
     */
    private suspend fun onPurchaseSheetCanceled() {
        val product = purchaseAttemptProduct ?: return
        purchaseAttemptProduct = null
        analytics.trackPurchaseOutcome(outcome = "user_cancelled", product = product)

        if (!remoteConfig.showPurchaseCancelRecovery()) return
        if (dataStorePrefs.isSubscribed().first()) return
        val details = TrialTimelineDetails.make(product, System.currentTimeMillis()) ?: return
        val reminderMessaging = TrialReminderMessaging.current(context, remoteConfig.showTrialEndingReminder())
        _purchaseRecovery.value = PurchaseRecovery(details, reminderMessaging)
    }

    fun onPurchaseRecoveryViewed(recovery: PurchaseRecovery) {
        analytics.trackPurchaseRecoveryViewed(recovery.details)
    }

    /** "Start Free Trial": opens the Play sheet again for the same product. */
    fun onPurchaseRecoveryRetry(activity: Activity) {
        val recovery = _purchaseRecovery.value ?: return
        analytics.trackPurchaseRecoveryRetry(recovery.details)
        _purchaseRecovery.value = null
        makePurchase(activity, recovery.details.productId)
    }

    /** "Not now", or back: returns to the paywall. */
    fun onPurchaseRecoveryDismissed() {
        val recovery = _purchaseRecovery.value ?: return
        analytics.trackPurchaseRecoveryDismissed(recovery.details)
        _purchaseRecovery.value = null
    }

    private fun closeScreen() {
        viewModelScope.launch {
            val isUserPremium = dataStorePrefs.isSubscribed().first()
            if (isUserPremium) {
                events.emit(PaywallEvents.CloseScreen)
            }
        }

        analytics.trackPaywallClose()
    }

    private fun getProducts() {
        viewModelScope.launch {
            _isLoadingProducts.value = true

            purchasesRepository.getProducts()
                .collect { purchaseProducts ->
                    _isLoadingProducts.value = false

                    setPreselectedProduct(purchaseProducts)

                    val uiProducts = purchaseProducts
                        .mapToUiProducts()
                        .sortProducts()

                    products.emit(uiProducts)
                }
        }
    }

    private fun subscribeToOnboardingState() {
        viewModelScope.launch {
            onboardingPrefs.observeAnswersJson().collect { json ->
                _onboardingState.value = json
                    ?.takeIf { it.isNotBlank() }
                    ?.let { runCatching { gson.fromJson(it, OnboardingState::class.java) }.getOrNull() }
            }
        }
    }

    private fun fetchUserId() {
        viewModelScope.launch {
            _userId.value = runCatching { idService.getUserID() }.getOrNull()
        }
    }

    private fun List<UIProduct>.sortProducts(): List<UIProduct> {
        return sortedWith(compareBy<UIProduct> { obj ->
            when (obj.product.productId) {
                PREMIUM_LIFETIME_PRODUCT -> 4
                YEARLY_SUBSCRIPTION_V1 -> 2
                MONTHLY_SUBSCRIPTION_V1 -> 1
                else -> 0
            }
        })
    }

    private fun List<Product>.mapToUiProducts(): List<UIProduct> {
        return map { product ->
            UIProduct(
                product = product,
                isChecked = product.productId == selectedProduct.value?.productId,
                showDiscountPercent = null,
                discountText = "",
                title = getProductTitle(product),
                description = getProductDescription(product),
                benefits = emptyList(),
                billingPeriod = getBillingPeriodForProduct(product)
            )
        }
    }

    private fun getBillingPeriodForProduct(product: Product): String {
        return when {
            product.productId == YEARLY_SUBSCRIPTION_V1 -> strings.getString(R.string.year)
            else -> strings.getString(R.string.month)
        }

    }

    private fun setPreselectedProduct(products: List<Product>) {
        selectedProduct.value = products.find { it.productId == YEARLY_SUBSCRIPTION_V1 }
            ?: products.find { it.productId == MONTHLY_SUBSCRIPTION_V1 }
            ?: products.firstOrNull()
    }

    private fun getProductTitle(product: Product): String {
        return when {
            product.productId == MONTHLY_SUBSCRIPTION_V1 -> {
                strings.getString(R.string.monthly_access)
            }

            product.productId == YEARLY_SUBSCRIPTION_V1 -> {
                strings.getString(R.string.yearly_access)
            }

            else -> ""
        }
    }

    private fun Product.getPeriodString() =
        when (productId) {
            MONTHLY_SUBSCRIPTION_V1 -> strings.getString(R.string.month)
            YEARLY_SUBSCRIPTION_V1 -> strings.getString(R.string.year)
            else -> throw IllegalArgumentException("unexpected productId $productId")
        }.capitalize()

    private fun getProductDescription(product: Product): String {
        if (product.productId == PREMIUM_LIFETIME_PRODUCT) {
            return product.displayPrice
        }
        return if (!product.freeTrial.isNullOrBlank()) {
            getTrialDescription(product)
        } else {
            getNoTrialDescription(product)
        }
    }

    private fun getNoTrialDescription(product: Product) = strings.getString(
        R.string.no_trial_product_name,
        product.displayPrice,
        product.getPeriodString()
    )

    private fun getTrialDescription(product: Product) = strings.getString(
        R.string.trial_product_name,
        product.displayPrice,
        product.getPeriodString()
    )

    fun makePurchase(activity: Activity, productId: String? = null) {
        viewModelScope.launch {
            val purchaseProduct = productId
                ?.let { requestedProductId ->
                    products.value.firstOrNull { it.product.productId == requestedProductId }?.product
                }
                ?: selectedProduct.value

            if (purchaseProduct != null && this@PaywallViewModel.selectedProduct.value != purchaseProduct) {
                selectedProduct(purchaseProduct, trackAnalytics = false)
            }

            analytics.trackMakePurchase(purchaseProduct)

            if (purchaseProduct == null) {
                events.emit(PaywallEvents.SelectProduct)
                return@launch
            }

            _errorMessage.value = null
            purchaseAttemptProduct = purchaseProduct
            purchasesRepository.makePurchase(activity, purchaseProduct)
        }
    }


    fun selectProduct(newlySelectedProduct: Product) {
        selectedProduct(newlySelectedProduct, trackAnalytics = true)
    }

    fun selectProductById(productId: String) {
        val product = products.value.firstOrNull { it.product.productId == productId }?.product ?: return
        selectedProduct(product, trackAnalytics = true)
    }

    private fun selectedProduct(newlySelectedProduct: Product, trackAnalytics: Boolean) {
        viewModelScope.launch {
            selectedProduct.value = newlySelectedProduct
            products.emit(products.value.map { uiProduct ->
                uiProduct.copy(isChecked = uiProduct.product == selectedProduct.value)
            })
        }

        if (trackAnalytics) {
            analytics.trackSelectProduct(newlySelectedProduct.productId)
        }
    }

    fun navigateBack() {
        closeScreen()
        analytics.trackPaywallClose()
    }

    fun onCloseRequested() {
        analytics.trackPaywallClose()
    }

    fun trackPersonalization(variants: Map<String, Any?>) {
        analytics.trackPaywallPersonalization(variants)
    }

    fun observeCachedPaywall(variant: RemotePaywallVariant): StateFlow<CachedRemotePaywall?> {
        return remotePaywallCache.observeCachedPaywall(variant)
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isRestoring.value = true
            _errorMessage.value = null
            purchaseAttemptProduct = null
            purchasesRepository.clearPurchaseStatus()
            purchasesRepository.queryPurchases()
            delay(1_500)
            val isPremium = dataStorePrefs.isSubscribed().first()
            if (!isPremium) {
                _isRestoring.value = false
                _errorMessage.value = "No previous purchases found."
            }
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            analytics.trackPaywallClose()
            events.emit(PaywallEvents.DismissPurchaseStatusDialog)
        }
    }

    private companion object {
        private const val TIER_ONE_MAX_CARS = "1"
        private const val TIER_TWO_MAX_CARS = "3"
        private const val TIER_ONE_MAX_IMAGES = "80"
        private const val TIER_TWO_MAX_IMAGES = "300"
    }

}

/** The free trial the user just backed out of, and what the reminder row may promise. */
data class PurchaseRecovery(
    val details: TrialTimelineDetails,
    val reminderMessaging: TrialReminderMessaging
)

sealed class PaywallEvents {
    object DismissPurchaseStatusDialog : PaywallEvents()
    object PurchaseInProgress : PaywallEvents()
    object CloseScreen : PaywallEvents()
    object PurchaseNotValid : PaywallEvents()
    object SelectProduct : PaywallEvents()
    object PurchaseIsAcknowledged : PaywallEvents()

    /** A purchase from this paywall started a free trial; sent just before [PurchaseIsAcknowledged]. */
    data class TrialPurchased(val details: TrialTimelineDetails) : PaywallEvents()
}
