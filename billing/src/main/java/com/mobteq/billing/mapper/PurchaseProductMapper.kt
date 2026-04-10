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
        val paidPhase = pricingPhases.lastOrNull { it.priceAmountMicros > 0L }
        val introOfferPhase = pricingPhases.firstOrNull {
            it.priceAmountMicros == 0L && it.billingPeriod.isNotBlank()
        }
        val paidPeriod = paidPhase?.billingPeriod?.let(::parseIsoDurationToPeriod)
        val introOfferPeriod = introOfferPhase?.billingPeriod?.let(::parseIsoDurationToPeriod)

        return SubscriptionProduct(
            productId,
            name,
            price.first,
            price.second,
            freeTrial,
            paidPeriod?.unit,
            paidPeriod?.value,
            introOfferPhase?.let(::mapPaymentMode),
            introOfferPeriod?.unit,
            introOfferPeriod?.value,
            selectedOffer.offerToken,
        )
    }

    Timber.e("OneTimePurchaseOfferDetails + SubscriptionOfferDetails null for product $productId.")

    return null
}

private fun selectOfferForPurchase(
    offers: List<ProductDetails.SubscriptionOfferDetails>
): ProductDetails.SubscriptionOfferDetails {

    // 1. Prefer trial offer
    val trialOffer = offers.firstOrNull { offer ->
        offer.pricingPhases.pricingPhaseList.any {
            it.priceAmountMicros == 0L
        }
    }

    if (trialOffer != null) {
        Timber.d("Selected TRIAL offer: ${trialOffer.offerToken}")
        return trialOffer
    }

    // 2. Fallback to regular paid offer
    val paidOffer = offers.firstOrNull { offer ->
        offer.pricingPhases.pricingPhaseList.any {
            it.priceAmountMicros > 0L
        }
    }

    if (paidOffer != null) {
        Timber.d("Selected PAID offer: ${paidOffer.offerToken}")
        return paidOffer
    }

    // 3. Final fallback
    Timber.w("Fallback to first offer")
    return offers.first()
}

private fun getFreeTrialIfEligible(
    pricingPhases: List<ProductDetails.PricingPhase>
): String? {
    val freeTrialPhase = pricingPhases.firstOrNull {
        it.priceAmountMicros == 0L && it.billingPeriod.isNotBlank()
    } ?: return null

    return parseIsoDurationToDays(freeTrialPhase.billingPeriod)
}

private data class ParsedPeriod(
    val unit: String,
    val value: Int
)

private fun parseIsoDurationToPeriod(isoDuration: String): ParsedPeriod? {
    val period = isoDuration.uppercase().removePrefix("P")

    return when {
        period.endsWith("D") -> period.removeSuffix("D").toIntOrNull()?.let { ParsedPeriod("day", it) }
        period.endsWith("W") -> period.removeSuffix("W").toIntOrNull()?.let { ParsedPeriod("week", it) }
        period.endsWith("M") -> period.removeSuffix("M").toIntOrNull()?.let { ParsedPeriod("month", it) }
        period.endsWith("Y") -> period.removeSuffix("Y").toIntOrNull()?.let { ParsedPeriod("year", it) }
        else -> null
    }
}

private fun mapPaymentMode(phase: ProductDetails.PricingPhase): String {
    return when {
        phase.priceAmountMicros == 0L -> "freeTrial"
        else -> "payAsYouGo"
    }
}

/**
 * Parses ISO 8601 duration format to days.
 * Examples: P7D -> 7, P1W -> 7, P2W -> 14, P1M -> 30, P3M -> 90
 */
private fun parseIsoDurationToDays(isoDuration: String): String? {
    val period = isoDuration.uppercase().removePrefix("P")

    return when {
        period.endsWith("D") -> period.removeSuffix("D").toIntOrNull()?.toString()
        period.endsWith("W") -> period.removeSuffix("W").toIntOrNull()?.let { it * 7 }?.toString()
        period.endsWith("M") -> period.removeSuffix("M").toIntOrNull()?.let { it * 30 }?.toString()
        period.endsWith("Y") -> period.removeSuffix("Y").toIntOrNull()?.let { it * 365 }?.toString()
        else -> period.filter { it.isDigit() }.ifBlank { null }
    }
}

private fun getPrice(
    pricingPhases: List<ProductDetails.PricingPhase>,
    productId: String
): Pair<String, Long> {
    val paidPhase = pricingPhases.lastOrNull { it.priceAmountMicros > 0L }
        ?: throw IllegalArgumentException("invalid subscription product $productId")

    return Pair(paidPhase.formattedPrice, paidPhase.priceAmountMicros)
}
