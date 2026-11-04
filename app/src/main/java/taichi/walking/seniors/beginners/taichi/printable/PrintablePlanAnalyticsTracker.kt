package taichi.walking.seniors.beginners.taichi.printable

import androidx.core.os.bundleOf
import com.mobteq.analytics.FirebaseAnalytics
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import javax.inject.Inject

/**
 * Printable-plan funnel, with iOS's event names (`AnalyticsTracker.swift`) so one dashboard covers
 * both platforms. iOS's `printable_plan_purchase_*` events belong to its standalone App Store product,
 * which Android does not sell; the paywall reports its own purchases.
 */
class PrintablePlanAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    /** A Premium user tapped "Download PDF". */
    fun trackCardTap(kind: PracticeKind) = log(EVENT_CARD_TAP, kind)

    /** The PDF is ready (freshly generated or from cache). */
    fun trackGenerated(kind: PracticeKind) = log(EVENT_GENERATED, kind)

    /** A user without Premium tapped the card, which opens the paywall. */
    fun trackUpsellTap(kind: PracticeKind) = log(EVENT_UPSELL_TAP, kind)

    /** The user sent the ready PDF to the print dialog or the share sheet. */
    fun trackShared(kind: PracticeKind, method: ShareMethod) = log(EVENT_SHARED, kind, PARAM_METHOD to method.value)

    enum class ShareMethod(val value: String) { PRINT("print"), SHARE("share") }

    private fun log(name: String, kind: PracticeKind, vararg extra: Pair<String, String>) {
        firebaseAnalytics.logEvent(
            name,
            bundleOf(
                PARAM_SURFACE to SURFACE_HOME_CARD,
                PARAM_PRACTICE_KIND to kind.storageValue,
                *extra
            )
        )
    }

    private companion object {
        const val EVENT_CARD_TAP = "printable_plan_card_tap"
        const val EVENT_GENERATED = "printable_plan_generated"
        const val EVENT_SHARED = "printable_plan_shared"
        const val EVENT_UPSELL_TAP = "printable_plan_upsell_tap"

        const val PARAM_SURFACE = "surface"
        const val PARAM_PRACTICE_KIND = "practice_kind"
        const val PARAM_METHOD = "method"
        const val SURFACE_HOME_CARD = "home_card"
    }
}
