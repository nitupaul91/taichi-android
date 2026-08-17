package com.mobteq.analytics

import android.app.Application
import android.os.Bundle
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import timber.log.Timber
import java.math.BigDecimal
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Meta (Facebook) app events, used to optimise and attribute Facebook ads campaigns.
 *
 * The SDK auto-initialises from the `com.facebook.sdk.*` meta-data in the app manifest via
 * `FacebookInitProvider`, which runs before `Application.onCreate`. [initialize] only has to start
 * automatic event logging, mirroring the `ApplicationDelegate` call the iOS app makes in
 * `didFinishLaunchingWithOptions`.
 */
@Singleton
class FacebookAnalyticsTracker @Inject constructor() {

    @Volatile
    private var logger: AppEventsLogger? = null

    fun initialize(application: Application) {
        if (logger != null) {
            return
        }

        if (!FacebookSdk.isInitialized()) {
            Timber.tag(TAG).w(
                "Facebook SDK is not initialized. Meta event delivery is disabled."
            )
            return
        }

        // Logs installs/activations and starts the session tracking Meta uses for attribution.
        AppEventsLogger.activateApp(application)
        logger = AppEventsLogger.newLogger(application)

        Timber.tag(TAG).i(
            "Facebook SDK initialized. appId=%s advertiserIdCollection=%s",
            FacebookSdk.getApplicationId(),
            FacebookSdk.getAdvertiserIDCollectionEnabled()
        )
    }

    /**
     * Logs the standard `Purchase` event Facebook ads campaigns optimise against.
     *
     * @param priceAmountMicros price in micro-units of [currencyCode], as reported by Play Billing.
     */
    fun trackPurchase(
        productId: String,
        productName: String?,
        priceAmountMicros: Long?,
        currencyCode: String?
    ) {
        val logger = logger
        if (logger == null) {
            Timber.tag(TAG).w("Logger unavailable. Dropping purchase for %s", productId)
            return
        }

        if (priceAmountMicros == null || currencyCode.isNullOrBlank()) {
            Timber.tag(TAG).w(
                "Missing price for %s (micros=%s currency=%s). Dropping purchase.",
                productId,
                priceAmountMicros,
                currencyCode
            )
            return
        }

        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        if (currency == null) {
            Timber.tag(TAG).w("Unknown currency %s. Dropping purchase for %s", currencyCode, productId)
            return
        }

        val amount = BigDecimal.valueOf(priceAmountMicros, MICROS_SCALE)
        val parameters = Bundle().apply {
            putString(PARAM_PRODUCT_ID, productId)
            putString(PARAM_CONTENT_TYPE, VALUE_CONTENT_TYPE_SUBSCRIPTION)
            putString(PARAM_CONTENT_NAME, productName?.ifBlank { null } ?: productId)
        }

        logger.logPurchase(amount, currency, parameters)

        Timber.tag(TAG).i("Tracked Facebook purchase %s %s for %s", amount, currency, productId)
    }

    companion object {
        private const val TAG = "FacebookAnalytics"

        /** Play Billing reports prices in micro-units, i.e. 1_990_000 == 1.99. */
        private const val MICROS_SCALE = 6

        private const val PARAM_PRODUCT_ID = "product_id"
        private const val PARAM_CONTENT_TYPE = "content_type"
        private const val PARAM_CONTENT_NAME = "content_name"

        private const val VALUE_CONTENT_TYPE_SUBSCRIPTION = "subscription"
    }
}
