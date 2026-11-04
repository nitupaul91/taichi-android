package taichi.walking.seniors.beginners.ui.paywall

import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.billing.domain.Product
import com.mobteq.billing.domain.repository.PurchasesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import javax.inject.Inject

/** Where the onboarding paywall step is (iOS OnboardingRootView.startPaywallStep and its covers). */
sealed interface OnboardingPaywallPhase {
    /** Deciding whether the trial timeline goes first; nothing is drawn yet. */
    object Starting : OnboardingPaywallPhase

    /** The informational timeline shown before the paywall (`show_pre_paywall_trial_timeline`). */
    data class TrialTimeline(
        val details: TrialTimelineDetails,
        val reminderMessaging: TrialReminderMessaging
    ) : OnboardingPaywallPhase

    object Paywall : OnboardingPaywallPhase

    /** After a trial purchase (`show_post_purchase_trial_confirmation`): the trial has started. */
    data class TrialConfirmation(
        val details: TrialTimelineDetails,
        val reminderMessaging: TrialReminderMessaging
    ) : OnboardingPaywallPhase

    /** The step is over; the caller moves on to home. */
    object Finished : OnboardingPaywallPhase
}

@HiltViewModel
class OnboardingPaywallStepViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val purchasesRepository: PurchasesRepository,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val firebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    private val _phase = MutableStateFlow<OnboardingPaywallPhase>(OnboardingPaywallPhase.Starting)
    val phase: StateFlow<OnboardingPaywallPhase> = _phase.asStateFlow()

    private var reminderMessaging = TrialReminderMessaging.UNAVAILABLE

    /** Set by a trial purchase; shown once the paywall has finished (iOS pendingTrialConfirmation). */
    private var pendingTrialConfirmation: TrialTimelineDetails? = null

    init {
        viewModelScope.launch { startPaywallStep() }
    }

    /**
     * Shows the trial timeline first when the flag is on and Play has a free trial to describe;
     * otherwise goes straight to the paywall. Products normally arrived long before the end of
     * onboarding; the short wait only covers a cold billing connection.
     */
    private suspend fun startPaywallStep() {
        if (!remoteConfig.showPrePaywallTrialTimeline()) {
            _phase.value = OnboardingPaywallPhase.Paywall
            return
        }

        val details = TrialTimelineDetails.preferredFor(currentProducts(), System.currentTimeMillis())
        if (details == null) {
            _phase.value = OnboardingPaywallPhase.Paywall
            return
        }

        reminderMessaging = TrialReminderMessaging.current(context, remoteConfig.showTrialEndingReminder())
        _phase.value = OnboardingPaywallPhase.TrialTimeline(details, reminderMessaging)
    }

    private suspend fun currentProducts(): List<Product> {
        val products = purchasesRepository.getProducts()
        return withTimeoutOrNull(PRODUCTS_WAIT_MILLIS) { products.first { it.isNotEmpty() } }
            ?: products.replayCache.lastOrNull().orEmpty()
    }

    fun onTrialTimelineViewed(details: TrialTimelineDetails) {
        trackTrialEvent(EVENT_TRIAL_TIMELINE_VIEWED, details)
    }

    fun onTrialTimelineContinued(details: TrialTimelineDetails) {
        trackTrialEvent(EVENT_TRIAL_TIMELINE_CONTINUED, details)
        _phase.value = OnboardingPaywallPhase.Paywall
    }

    fun onTrialPurchased(details: TrialTimelineDetails) {
        if (!remoteConfig.showPostPurchaseTrialConfirmation()) return
        pendingTrialConfirmation = details
    }

    /**
     * Every way the paywall ends (closed, purchased, restored) comes through here; only a trial
     * purchase inserts its confirmation before home (iOS continueAfterPaywallDismissal).
     */
    fun onPaywallFinished() {
        if (_phase.value != OnboardingPaywallPhase.Paywall) return
        val details = pendingTrialConfirmation
        pendingTrialConfirmation = null
        if (details == null) {
            _phase.value = OnboardingPaywallPhase.Finished
            return
        }
        // Resolved again: the timeline may not have been shown, and the permission can have changed.
        reminderMessaging = TrialReminderMessaging.current(context, remoteConfig.showTrialEndingReminder())
        _phase.value = OnboardingPaywallPhase.TrialConfirmation(details, reminderMessaging)
    }

    fun onTrialConfirmationViewed(details: TrialTimelineDetails) {
        trackTrialEvent(EVENT_TRIAL_CONFIRMATION_VIEWED, details)
    }

    fun onTrialConfirmationContinued(details: TrialTimelineDetails) {
        if (_phase.value !is OnboardingPaywallPhase.TrialConfirmation) return
        trackTrialEvent(EVENT_TRIAL_CONFIRMATION_CONTINUED, details)
        _phase.value = OnboardingPaywallPhase.Finished
    }

    /** iOS OnboardingRootView.trialAnalyticsParams. */
    private fun trackTrialEvent(name: String, details: TrialTimelineDetails) {
        firebaseAnalytics.logEvent(
            name,
            bundleOf(
                "product_id" to details.productId,
                "trial_days" to details.trialLengthDays.toLong(),
                "reminders_enabled" to remoteConfig.showTrialEndingReminder(),
                "reminder_messaging" to reminderMessaging.analyticsValue
            )
        )
    }

    private companion object {
        const val PRODUCTS_WAIT_MILLIS = 1_500L
        const val EVENT_TRIAL_TIMELINE_VIEWED = "trial_timeline_viewed"
        const val EVENT_TRIAL_TIMELINE_CONTINUED = "trial_timeline_continued"
        const val EVENT_TRIAL_CONFIRMATION_VIEWED = "trial_confirmation_viewed"
        const val EVENT_TRIAL_CONFIRMATION_CONTINUED = "trial_confirmation_continued"
    }
}
