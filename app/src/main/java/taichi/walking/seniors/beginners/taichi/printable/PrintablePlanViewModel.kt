package taichi.walking.seniors.beginners.taichi.printable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.billing.model.purchases.local.PurchaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.printable.PrintablePlanAnalyticsTracker.ShareMethod
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import timber.log.Timber
import java.io.File
import javax.inject.Inject

enum class PrintablePlanCardMode {
    HIDDEN,

    /** Premium: tap generates the PDF and offers print or share. */
    DOWNLOAD,

    /** No Premium: tap opens the paywall. */
    UNLOCK
}

/**
 * The home card under `show_printable_plan_paid_upsell`, iOS's single switch for the printable plan.
 *
 * iOS shows the card to Premium users only and sells non-owners a standalone `taichi_printable_plan`
 * product; Android has no such product, so Premium itself unlocks the plan. Users without Premium get
 * the card in its locked form, which opens the paywall, as iOS's printable-plan settings row does.
 * Once downloaded, the card stays for the rest of that session and is gone from the next launch.
 */
fun printablePlanCardMode(
    featureEnabled: Boolean,
    isPremium: Boolean,
    downloadedBeforeThisSession: Boolean
): PrintablePlanCardMode = when {
    !featureEnabled -> PrintablePlanCardMode.HIDDEN
    !isPremium -> PrintablePlanCardMode.UNLOCK
    downloadedBeforeThisSession -> PrintablePlanCardMode.HIDDEN
    else -> PrintablePlanCardMode.DOWNLOAD
}

@HiltViewModel
class PrintablePlanViewModel @Inject constructor(
    private val cacheService: PrintablePlanCacheService,
    private val store: PrintablePlanStore,
    private val analytics: PrintablePlanAnalyticsTracker,
    private val remoteConfig: AiAssistantRemoteConfig,
    private val purchaseManager: PurchaseManager
) : ViewModel() {

    data class UiState(
        val mode: PrintablePlanCardMode = PrintablePlanCardMode.HIDDEN,
        val isGenerating: Boolean = false,
        /** The PDF waiting for the user to print or share it. */
        val readyPdf: File? = null,
        val generationFailed: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Null until the first purchase-state emission, so the card never flashes in the wrong mode. */
    private var isPremium: Boolean? = null
    private var downloadedBeforeThisSession = false

    init {
        viewModelScope.launch {
            downloadedBeforeThisSession = store.downloadedBeforeThisSession()
            purchaseManager.isUserPremiumSubscribed().collect { premium ->
                isPremium = premium
                refreshMode()
            }
        }
    }

    /** Re-reads the remote flag, which can activate after this ViewModel was created. */
    fun refreshMode() {
        val premium = isPremium ?: return
        _state.update {
            it.copy(
                mode = printablePlanCardMode(
                    featureEnabled = remoteConfig.showPrintablePlanPaidUpsell(),
                    isPremium = premium,
                    downloadedBeforeThisSession = downloadedBeforeThisSession
                )
            )
        }
    }

    fun onCardTap(plan: WorkoutPlanDto, kind: PracticeKind, openPaywall: () -> Unit) {
        when (_state.value.mode) {
            PrintablePlanCardMode.HIDDEN -> Unit
            PrintablePlanCardMode.UNLOCK -> {
                analytics.trackUpsellTap(kind)
                openPaywall()
            }
            PrintablePlanCardMode.DOWNLOAD -> {
                if (_state.value.isGenerating) return
                analytics.trackCardTap(kind)
                generate(plan, kind)
            }
        }
    }

    /**
     * The system may purge `cacheDir` on a nearly full phone while the ready dialog is open, so a
     * print or share tap that finds the file gone builds it again instead of handing over nothing.
     */
    fun onReadyFileMissing(plan: WorkoutPlanDto, kind: PracticeKind) {
        _state.update { it.copy(readyPdf = null) }
        generate(plan, kind)
    }

    private fun generate(plan: WorkoutPlanDto, kind: PracticeKind) {
        if (_state.value.isGenerating) return
        _state.update { it.copy(isGenerating = true) }
        viewModelScope.launch {
            try {
                val file = cacheService.generateOrLoadCachedPdf(plan, kind)
                analytics.trackGenerated(kind)
                _state.update { it.copy(isGenerating = false, readyPdf = file) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Timber.e(throwable, "Printable plan PDF generation failed")
                _state.update { it.copy(isGenerating = false, generationFailed = true) }
            }
        }
    }

    fun onPrint(kind: PracticeKind) = onDelivered(kind, ShareMethod.PRINT)

    fun onShare(kind: PracticeKind) = onDelivered(kind, ShareMethod.SHARE)

    private fun onDelivered(kind: PracticeKind, method: ShareMethod) {
        analytics.trackShared(kind, method)
        onReadyDismissed()
    }

    /** Closing the ready dialog counts as downloaded, like dismissing the iOS share sheet. */
    fun onReadyDismissed() {
        if (_state.value.readyPdf == null) return
        _state.update { it.copy(readyPdf = null) }
        viewModelScope.launch { store.markDownloaded() }
    }

    fun onGenerationFailureShown() {
        _state.update { it.copy(generationFailed = false) }
    }
}
