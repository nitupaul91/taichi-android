package taichi.walking.seniors.beginners.ui.paywall

import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.billing.model.purchases.local.PurchaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import taichi.walking.seniors.beginners.taichi.ui.home.activity.PaywallLaunchRequest
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * The two paywalls home opens on its own (iOS JourneyHomeView):
 * - the limited-offer paywall on the very first home visit (`show_discount_paywall`), once ever,
 *   and only when a dedicated limited-offer HTML paywall is cached;
 * - the re-entry paywall on later visits, the next launch or a return from the background
 *   (`show_reentry_paywall`), at most once per app process.
 * Premium users get neither.
 */
@HiltViewModel
class HomeEntryPaywallViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val purchaseManager: PurchaseManager,
    private val remotePaywallCache: RemotePaywallCache,
    private val firebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    /** What home is showing when an entry is evaluated. */
    class HomeContext(val practiceKind: String?, val canPresentPaywall: Boolean)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _paywallRequests = MutableSharedFlow<PaywallLaunchRequest>(extraBufferCapacity = 1)
    val paywallRequests: SharedFlow<PaywallLaunchRequest> = _paywallRequests.asSharedFlow()

    /** Read when an entry is evaluated; home updates it as tabs and screens change. */
    var homeContext: HomeContext = HomeContext(practiceKind = null, canPresentPaywall = true)

    private var hasCountedInitialEntry = false
    private var wentToBackground = false

    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            wentToBackground = true
        }

        override fun onStart(owner: LifecycleOwner) {
            if (!wentToBackground) return
            wentToBackground = false
            onHomeEntered(ENTRY_FOREGROUND)
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
    }

    /** Home appeared; counted once per view model, so a rotation is not a new visit. */
    fun onHomeShown() {
        if (hasCountedInitialEntry) return
        hasCountedInitialEntry = true
        onHomeEntered(ENTRY_INITIAL)
    }

    private fun onHomeEntered(entryPoint: String) {
        val viewCount = prefs.getInt(KEY_HOME_VIEW_COUNT, 0) + 1
        prefs.edit().putInt(KEY_HOME_VIEW_COUNT, viewCount).apply()

        viewModelScope.launch {
            if (viewCount == 1) {
                presentLimitedOfferIfNeeded()
            } else {
                presentReentryIfNeeded(entryPoint, viewCount)
            }
        }
    }

    private suspend fun presentLimitedOfferIfNeeded() {
        if (isPremium()) return
        if (!remoteConfig.showDiscountPaywall()) return
        if (prefs.getBoolean(KEY_HAS_SHOWN_DISCOUNT_PAYWALL, false)) return
        if (reentryPresentedThisProcess) return
        if (!homeContext.canPresentPaywall) return
        // iOS only shows it from the limited-offer cache. Android's cache falls back to the main
        // paywall URL when the limited-offer one is unset, which must not count as an offer.
        if (remoteConfig.getLimitedOfferPaywallHtmlUrl().isBlank()) return
        if (remotePaywallCache.observeCachedPaywall(RemotePaywallVariant.ONBOARDING_LIMITED_OFFER).value == null) return

        prefs.edit().putBoolean(KEY_HAS_SHOWN_DISCOUNT_PAYWALL, true).apply()
        _paywallRequests.tryEmit(
            PaywallLaunchRequest(
                variant = RemotePaywallVariant.ONBOARDING_LIMITED_OFFER,
                paywallSource = SOURCE_HOME_LIMITED_OFFER
            )
        )
    }

    private suspend fun presentReentryIfNeeded(entryPoint: String, viewCount: Int) {
        if (isPremium()) return
        // iOS fetches first so a flag flipped since launch applies to this entry.
        fetchRemoteConfig()
        if (!remoteConfig.showReentryPaywall()) return
        if (isPremium()) return
        if (reentryPresentedThisProcess) return
        val context = homeContext
        if (!context.canPresentPaywall) return

        reentryPresentedThisProcess = true
        _paywallRequests.tryEmit(PaywallLaunchRequest(paywallSource = SOURCE_REENTRY))
        firebaseAnalytics.logEvent(
            EVENT_REENTRY_PAYWALL_SHOWN,
            bundleOf(
                "entry_point" to entryPoint,
                "view_count" to viewCount.toLong(),
                "practice_kind" to context.practiceKind,
                "is_premium" to false
            )
        )
    }

    private suspend fun isPremium(): Boolean = purchaseManager.isUserPremiumSubscribed().first()

    private suspend fun fetchRemoteConfig() {
        withTimeoutOrNull(REMOTE_CONFIG_FETCH_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                remoteConfig.fetchAndActivate { if (continuation.isActive) continuation.resume(Unit) }
            }
        }
    }

    companion object {
        const val SOURCE_REENTRY = "reentry"
        const val SOURCE_HOME_LIMITED_OFFER = "home_limited_offer"

        private const val PREFS_NAME = "home_entry_paywalls"
        private const val KEY_HOME_VIEW_COUNT = "journey_home_view_count"
        private const val KEY_HAS_SHOWN_DISCOUNT_PAYWALL = "has_shown_discount_paywall"
        private const val ENTRY_INITIAL = "initial"
        private const val ENTRY_FOREGROUND = "foreground"
        private const val EVENT_REENTRY_PAYWALL_SHOWN = "reentry_paywall_shown"

        /** A late fetch must not drop a paywall on a user who has moved on. */
        private const val REMOTE_CONFIG_FETCH_TIMEOUT_MILLIS = 5_000L

        /** iOS ReentryPaywallCoordinator.hasPresentedThisSession: once per app process. */
        @Volatile
        private var reentryPresentedThisProcess = false
    }
}
