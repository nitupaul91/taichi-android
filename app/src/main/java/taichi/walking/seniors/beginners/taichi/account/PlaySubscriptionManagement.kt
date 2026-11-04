package taichi.walking.seniors.beginners.taichi.account

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mobteq.billing.domain.repository.PurchasesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

/** Google Play's subscription management page, the Android counterpart of iOS `showManageSubscriptions`. */
class PlaySubscriptionManagement @Inject constructor(
    @ApplicationContext private val context: Context,
    private val purchasesRepository: PurchasesRepository,
) {

    /** Returns false when nothing on the device can open the page. */
    fun open(): Boolean {
        val url = subscriptionsUrl(context.packageName, activeProductId())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "Could not open Google Play subscription management")
            false
        }
    }

    /**
     * The product of the active Play purchase. With several active products the deep link would
     * pick one arbitrarily, so the generic page (which lists all of them) is used instead.
     */
    private fun activeProductId(): String? =
        purchasesRepository.purchaseFlow.value
            .mapNotNull { it.productId }
            .distinct()
            .singleOrNull()

    companion object {
        private const val SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions"

        fun subscriptionsUrl(packageName: String, productId: String?): String {
            if (productId.isNullOrBlank()) return SUBSCRIPTIONS_URL

            return "$SUBSCRIPTIONS_URL?sku=${productId.urlEncoded()}&package=${packageName.urlEncoded()}"
        }

        private fun String.urlEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
    }
}
