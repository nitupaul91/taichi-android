package taichi.walking.seniors.beginners.ui.paywall

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.service.UserService
import taichi.walking.seniors.beginners.util.Strings
import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.billing.datastore.DataStorePrefs
import com.mobteq.billing.domain.Product
import com.mobteq.billing.domain.PurchaseStatus
import com.mobteq.billing.domain.repository.PurchasesRepository
import com.mobteq.billing.service.PlayStoreBillingService.Companion.MONTHLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.PlayStoreBillingService.Companion.PREMIUM_LIFETIME_PRODUCT
import com.mobteq.billing.service.PlayStoreBillingService.Companion.YEARLY_SUBSCRIPTION_V1
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val purchasesRepository: PurchasesRepository,
    private val analytics: PaywallAnalyticsTracker,
    private val strings: Strings,
    private val dataStorePrefs: DataStorePrefs,
    private val userService: UserService,
) : ViewModel() {

    private val events = MutableSharedFlow<PaywallEvents>()
    val eventsFlow = events.asSharedFlow()

    val products = MutableStateFlow<List<UIProduct>>(emptyList())

    val selectedProduct = MutableStateFlow<Product?>(null)

    private val _isLoadingProducts = MutableStateFlow(true)
    val isLoadingProducts: StateFlow<Boolean> = _isLoadingProducts

    init {
        getProducts()
        subscribeToPurchaseStatus()

        analytics.trackPaywallView()
    }

    private fun subscribeToPurchaseStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            purchasesRepository.getPurchaseStatus()
                .collect {
                    when (it) {
                        PurchaseStatus.InProgress -> events.emit(PaywallEvents.PurchaseInProgress)
                        is PurchaseStatus.Acknowledged -> {
                            purchasesRepository.clearPurchaseStatus()

                            events.emit(PaywallEvents.PurchaseIsAcknowledged)

//                            garageRepository.trainModel()

                            closeScreen()
                        }

                        PurchaseStatus.SubNotValid -> events.emit(PaywallEvents.PurchaseNotValid)
                        else -> Unit
                    }
                }
        }
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
                benefits = getProductBenefits(product.productId),
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

    private fun getProductBenefits(productId: String): List<String> {
        return when (productId) {
            MONTHLY_SUBSCRIPTION_V1 -> listOf(
//                strings.getString(R.string.monthly_benefit_one, TIER_ONE_MAX_CARS),
                strings.getString(R.string.monthly_benefit_two, TIER_ONE_MAX_IMAGES),
                strings.getString(R.string.no_watermark),
                strings.getString(R.string.high_res_images),
                strings.getString(R.string.free_premium_templates),
            )

            YEARLY_SUBSCRIPTION_V1 -> listOf(
//                strings.getString(R.string.monthly_benefit_one, TIER_TWO_MAX_CARS),
                strings.getString(R.string.monthly_benefit_two, TIER_TWO_MAX_IMAGES),
                strings.getString(R.string.no_watermark),
                strings.getString(R.string.high_res_images),
                strings.getString(R.string.free_premium_templates),
            )

            else -> emptyList()
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

    fun makePurchase(activity: Activity) {
        viewModelScope.launch {
            val selectedProduct = selectedProduct.value

            analytics.trackMakePurchase(selectedProduct?.productId)

            if (selectedProduct == null) {
                events.emit(PaywallEvents.SelectProduct)
                return@launch
            }

            purchasesRepository.makePurchase(activity, selectedProduct)
        }
    }


    fun selectProduct(newlySelectedProduct: Product) {
        viewModelScope.launch {
            selectedProduct.value = newlySelectedProduct
            products.emit(products.value.map { uiProduct ->
                uiProduct.copy(isChecked = uiProduct.product == selectedProduct.value)
            })
        }

        analytics.trackSelectProduct(newlySelectedProduct.productId)
    }

    fun navigateBack() {
        closeScreen()
        analytics.trackPaywallClose()
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

sealed class PaywallEvents {
    object DismissPurchaseStatusDialog : PaywallEvents()
    object PurchaseInProgress : PaywallEvents()
    object CloseScreen : PaywallEvents()
    object PurchaseNotValid : PaywallEvents()
    object SelectProduct : PaywallEvents()
    object PurchaseIsAcknowledged : PaywallEvents()
}
