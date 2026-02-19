package com.mobteq.billing.mapper

import com.android.billingclient.api.ProductDetails
import com.mobteq.billing.domain.OneTimePurchaseProduct
import com.mobteq.billing.domain.Product
import com.mobteq.billing.domain.SubscriptionProduct
import timber.log.Timber

fun ProductDetails.mapToPurchaseProduct(): Product? {
    val oneTimePurchaseOfferDetails = oneTimePurchaseOfferDetails
    if (oneTimePurchaseOfferDetails != null) {
        val formattedPrice = oneTimePurchaseOfferDetails.formattedPrice
        return OneTimePurchaseProduct(
            productId,
            name,
            formattedPrice,
            oneTimePurchaseOfferDetails.priceAmountMicros,
            null
        )
    }

    val subscriptionOfferDetails = subscriptionOfferDetails
    if (!subscriptionOfferDetails.isNullOrEmpty()) {
        val selectedOffer = selectOfferForPurchase(subscriptionOfferDetails)
        val pricingPhases = selectedOffer.pricingPhases.pricingPhaseList
        val price: Pair<String, Long> = getPrice(pricingPhases, productId)
        val freeTrial = getFreeTrialIfEligible(pricingPhases)

        return SubscriptionProduct(
            productId,
            name,
            price.first,
            price.second,
            freeTrial,
            selectedOffer.offerToken,
        )
    }

    Timber.e("OneTimePurchaseOfferDetails + SubscriptionOfferDetails null for product $productId.")

    return null
}

private fun selectOfferForPurchase(
    offers: List<ProductDetails.SubscriptionOfferDetails>
): ProductDetails.SubscriptionOfferDetails {
    val purchasableOffers = offers.filter { offer ->
        offer.offerToken.isNotBlank() &&
            offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros > 0L }
    }
    if (purchasableOffers.isEmpty()) return offers.first()

    // Prefer a stable paid base-plan style offer to avoid ineligible trial-offer tokens.
    return purchasableOffers.firstOrNull { offer ->
        offer.pricingPhases.pricingPhaseList.none { it.priceAmountMicros == 0L }
    } ?: purchasableOffers.first()
}

private fun getFreeTrialIfEligible(
    pricingPhases: List<ProductDetails.PricingPhase>
): String? {
    val freeTrialPhase = pricingPhases.firstOrNull {
        it.priceAmountMicros == 0L && it.billingPeriod.isNotBlank()
    } ?: return null

    return freeTrialPhase.billingPeriod.filter { it.isDigit() }.ifBlank { null }
}

private fun getPrice(
    pricingPhases: List<ProductDetails.PricingPhase>,
    productId: String
): Pair<String, Long> {
    val paidPhase = pricingPhases.lastOrNull { it.priceAmountMicros > 0L }
        ?: throw IllegalArgumentException("invalid subscription product $productId")

    return Pair(paidPhase.formattedPrice, paidPhase.priceAmountMicros)
}
