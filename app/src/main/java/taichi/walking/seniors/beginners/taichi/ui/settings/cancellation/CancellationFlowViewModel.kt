package taichi.walking.seniors.beginners.taichi.ui.settings.cancellation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.billing.domain.repository.PurchasesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.account.PlaySubscriptionManagement
import taichi.walking.seniors.beginners.taichi.ui.home.data.WorkoutPlanService
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import timber.log.Timber
import javax.inject.Inject

sealed interface CancellationStep {
    object Reason : CancellationStep

    /** A retention offer, or, for a reason without one, the last stop before Google Play. */
    data class Retention(val reason: CancellationReason) : CancellationStep
    object Feedback : CancellationStep
    object Applying : CancellationStep
    data class Applied(val reason: CancellationReason) : CancellationStep
}

data class CancellationFlowUiState(
    val step: CancellationStep = CancellationStep.Reason,
    val selectedReason: CancellationReason? = null,
    val feedbackText: String = "",
    val showDiscountPaywall: Boolean = false,
    val isOpeningPlayManagement: Boolean = false,
    val showPlayFallback: Boolean = false,
    val isFinished: Boolean = false,
    /**
     * Set once a new intensity is saved and the plans regenerated; the home tabs keep their plan in
     * memory, so the host reloads them and calls [CancellationFlowViewModel.onPlansRefreshed].
     */
    val plansNeedRefresh: Boolean = false
)

/**
 * The retention flow behind Settings > Cancel Subscription, ported from iOS `CancellationFlowView`:
 * reason, then a matching offer (discount paywall, gentler or harder exercises, or a feedback box),
 * then Google Play's subscription page for those who still want to cancel.
 *
 * Activity-scoped like the rest of Settings, so [start] resets it for every opening.
 */
@HiltViewModel
class CancellationFlowViewModel @Inject constructor(
    private val reporter: CancellationFlowReporter,
    private val practiceRepository: PracticeRepository,
    private val workoutPlanService: WorkoutPlanService,
    private val playSubscriptionManagement: PlaySubscriptionManagement,
    private val purchasesRepository: PurchasesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CancellationFlowUiState())
    val state: StateFlow<CancellationFlowUiState> = _state.asStateFlow()

    fun start() {
        // A plan refresh still owed from an earlier opening survives the reset.
        _state.value = CancellationFlowUiState(plansNeedRefresh = _state.value.plansNeedRefresh)
        reporter.report(CancellationFlowEvent.FLOW_OPENED)
    }

    fun selectReason(reason: CancellationReason) {
        _state.update { it.copy(selectedReason = reason) }
    }

    fun continueFromReason() {
        val reason = _state.value.selectedReason ?: return
        reporter.report(CancellationFlowEvent.REASON_SELECTED, reason = reason)

        when (reason) {
            CancellationReason.TOO_EXPENSIVE -> {
                reporter.report(CancellationFlowEvent.RETENTION_OFFER_SHOWN, reason = reason, action = ACTION_DISCOUNT_PAYWALL)
                _state.update { it.copy(showDiscountPaywall = true) }
            }
            CancellationReason.EXERCISES_TOO_HARD, CancellationReason.EXERCISES_TOO_EASY -> {
                reporter.report(
                    CancellationFlowEvent.RETENTION_OFFER_SHOWN,
                    reason = reason,
                    action = reason.retentionAdjustment?.action
                )
                _state.update { it.copy(step = CancellationStep.Retention(reason)) }
            }
            CancellationReason.OTHER -> _state.update { it.copy(step = CancellationStep.Feedback) }
        }
    }

    /**
     * [purchased] is true when the paywall completed with an acknowledged purchase. Restored or
     * existing access never completes the paywall, so it is not mistaken for a taken offer.
     */
    fun onDiscountPaywallFinished(purchased: Boolean) {
        // The paywall can report completion more than once (closing event, then its success dialog).
        if (!_state.value.showDiscountPaywall) return
        val reason = _state.value.selectedReason ?: return
        _state.update { it.copy(showDiscountPaywall = false) }

        if (purchased) {
            // Took the discount — nothing left to cancel.
            val purchase = purchasesRepository.purchaseFlow.value.maxByOrNull { it.purchaseTime }
            val analyticsParameters = buildMap {
                put(PARAM_PRODUCT_ID, purchase?.productId ?: "unknown")
                purchase?.orderNumber?.let { put(PARAM_TRANSACTION_ID, it) }
            }
            reporter.report(
                CancellationFlowEvent.RETENTION_ACTION_ACCEPTED,
                reason = reason,
                action = ACTION_DISCOUNT_PURCHASE,
                analyticsParameters = analyticsParameters
            )
            finish()
            return
        }

        _state.update { it.copy(step = CancellationStep.Retention(reason)) }
    }

    fun acceptAdjustment(reason: CancellationReason) {
        val adjustment = reason.retentionAdjustment ?: return
        _state.update { it.copy(step = CancellationStep.Applying) }

        viewModelScope.launch {
            apply(adjustment)
            reporter.report(
                CancellationFlowEvent.RETENTION_ACTION_ACCEPTED,
                reason = reason,
                action = adjustment.action,
                details = "intensity_${adjustment.intensity}"
            )
            _state.update { it.copy(step = CancellationStep.Applied(reason), plansNeedRefresh = true) }
        }
    }

    fun updateFeedback(text: String) {
        _state.update { it.copy(feedbackText = text) }
    }

    fun submitFeedback() {
        val trimmed = _state.value.feedbackText.trim()
        if (trimmed.isNotEmpty()) {
            reporter.report(CancellationFlowEvent.FEEDBACK_SUBMITTED, reason = CancellationReason.OTHER, feedback = trimmed)
        }
        continueCancellation(CancellationReason.OTHER)
    }

    fun continueCancellation(reason: CancellationReason) {
        if (_state.value.isOpeningPlayManagement) return
        _state.update { it.copy(isOpeningPlayManagement = true) }

        val didOpen = playSubscriptionManagement.open()
        _state.update { it.copy(isOpeningPlayManagement = false) }

        reporter.report(
            CancellationFlowEvent.CONTINUED_CANCELLATION,
            reason = reason,
            action = if (didOpen) ACTION_PLAY_MANAGEMENT_OPENED else ACTION_PLAY_MANAGEMENT_FAILED
        )

        if (didOpen) {
            finish()
        } else {
            _state.update { it.copy(showPlayFallback = true) }
        }
    }

    fun onPlansRefreshed() {
        _state.update { it.copy(plansNeedRefresh = false) }
    }

    fun dismissPlayFallback() {
        _state.update { it.copy(showPlayFallback = false) }
    }

    /** The user left without cancelling ([trigger] says how). */
    fun leave(trigger: String) {
        trackFlowAbandoned(trigger)
        finish()
    }

    /** Ends the flow without an abandon event: an offer was taken or the user is on their way to Play. */
    fun finish() {
        _state.update { it.copy(isFinished = true) }
    }

    private fun trackFlowAbandoned(trigger: String) {
        val stepName = when (_state.value.step) {
            CancellationStep.Reason -> "reason"
            is CancellationStep.Retention -> "retention"
            CancellationStep.Feedback -> "feedback"
            // Only the decision steps count as abandoning.
            CancellationStep.Applying, is CancellationStep.Applied -> return
        }
        reporter.reportAbandoned(step = stepName, reason = _state.value.selectedReason, trigger = trigger)
    }

    /**
     * Every program is re-generated at the new intensity, not just the one the user has open, so a
     * tab switch never finds the old difficulty waiting. Runs concurrently so the loading step is
     * one round trip; the server keeps each plan per user, so the home tabs reload it from cache.
     */
    private suspend fun apply(adjustment: IntensityAdjustment) {
        practiceRepository.setWorkoutIntensity(adjustment.intensity)

        coroutineScope {
            REGENERATED_PROGRAMS.map { kind ->
                async {
                    runCatching { workoutPlanService.fetchWorkoutPlan(kind, adjustment.intensity) }
                        .onFailure { Timber.w(it, "Could not regenerate the %s plan", kind) }
                }
            }.awaitAll()
        }
    }

    private companion object {
        val REGENERATED_PROGRAMS = listOf(PracticeKind.TAI_CHI, PracticeKind.WALKING, PracticeKind.CHAIR)

        const val ACTION_DISCOUNT_PAYWALL = "discount_paywall"
        const val ACTION_DISCOUNT_PURCHASE = "discount_purchase"
        const val ACTION_PLAY_MANAGEMENT_OPENED = "play_management_opened"
        const val ACTION_PLAY_MANAGEMENT_FAILED = "play_management_failed"

        const val PARAM_PRODUCT_ID = "product_id"
        const val PARAM_TRANSACTION_ID = "transaction_id"
    }
}
