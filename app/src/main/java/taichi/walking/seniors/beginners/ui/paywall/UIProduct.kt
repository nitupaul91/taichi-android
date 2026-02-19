package taichi.walking.seniors.beginners.ui.paywall

import com.mobteq.billing.domain.Product

data class UIProduct(
    val product: Product,
    val isChecked: Boolean,
    val showDiscountPercent: Boolean?,
    val discountText: String?,
    val title: String,
    val description: String,
    val benefits: List<String>,
    val billingPeriod: String,
)