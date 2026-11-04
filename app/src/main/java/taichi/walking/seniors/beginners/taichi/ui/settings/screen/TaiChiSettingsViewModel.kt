package taichi.walking.seniors.beginners.taichi.ui.settings.screen

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sageai.id.IDService
import com.mobteq.billing.model.purchases.local.PurchaseManager
import taichi.walking.seniors.beginners.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import taichi.walking.seniors.beginners.taichi.account.AccountDeletionException
import taichi.walking.seniors.beginners.taichi.account.AccountDeletionService
import taichi.walking.seniors.beginners.taichi.account.PlaySubscriptionManagement
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaiChiSettingsViewModel @Inject constructor(
    private val purchaseManager: PurchaseManager,
    private val idService: IDService,
    private val accountDeletionService: AccountDeletionService,
    private val playSubscriptionManagement: PlaySubscriptionManagement,
    private val analytics: SettingsAnalyticsTracker,
    private val remoteConfig: AiAssistantRemoteConfig
) : ViewModel() {

    val appVersion: String = BuildConfig.VERSION_NAME
    private var userId: String = "Unknown"

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private val _showDeleteAccountFailed = MutableStateFlow(false)
    val showDeleteAccountFailed: StateFlow<Boolean> = _showDeleteAccountFailed.asStateFlow()

    private val accountDeletedEvents = Channel<Unit>(Channel.BUFFERED)

    /** Emits once the account is deleted and local data wiped; the screen then restarts the app at onboarding. */
    val accountDeleted: Flow<Unit> = accountDeletedEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            purchaseManager.isUserPremiumSubscribed().collect { isPremium ->
                _isPremium.value = isPremium
            }
        }
        viewModelScope.launch {
            userId = runCatching { idService.getUserID() }.getOrDefault("Unknown")
        }
    }

    fun buildContactEmailUri(): String {
        val subject = "Taichi - Feedback report"
        val body = buildString {
            append("UserId: $userId\n")
            append("OS: Android ${Build.VERSION.RELEASE}\n")
            append("Model: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Language: ${Locale.getDefault().language}\n")
            append("App version: $appVersion\n\n")
        }

        return "mailto:hello@easytaichi.app" +
            "?subject=${Uri.encode(subject)}" +
            "&body=${Uri.encode(body)}"
    }

    /** Remote flag `show_settings_cancel_subscription_flow`; read each time Settings is shown, as on iOS. */
    fun isCancellationFlowEnabled(): Boolean = remoteConfig.showSettingsCancelSubscriptionFlow()

    fun onCancelSubscriptionTapped() {
        analytics.trackSubscriptionCancelTapped()
    }

    /** Returns false while a deletion is already running, in which case no confirmation is shown. */
    fun onDeleteAccountTapped(): Boolean {
        if (_isDeletingAccount.value) return false
        analytics.trackAccountDeleteTapped()
        return true
    }

    fun deleteAccount() {
        if (_isDeletingAccount.value) return
        _isDeletingAccount.value = true
        analytics.trackAccountDeleteConfirmed()

        viewModelScope.launch {
            // Once the server is asked to delete, the local wipe must follow even if the user leaves
            // the screen; otherwise the device keeps an id the server no longer knows.
            val outcome = withContext(NonCancellable) {
                try {
                    accountDeletionService.deleteAccount()
                    OUTCOME_DELETED
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    "failed:${(e as? AccountDeletionException)?.reason ?: e.javaClass.simpleName}"
                }
            }

            val didDelete = outcome == OUTCOME_DELETED
            _isDeletingAccount.value = false
            _showDeleteAccountFailed.value = !didDelete
            // After a deletion the analytics ids are already reset, so this lands on the new identity.
            analytics.trackAccountDeleteOutcome(outcome)

            if (didDelete) {
                accountDeletedEvents.send(Unit)
            }
        }
    }

    fun dismissDeleteAccountFailed() {
        _showDeleteAccountFailed.value = false
    }

    fun openManageSubscriptions() {
        playSubscriptionManagement.open()
    }

    private companion object {
        const val OUTCOME_DELETED = "deleted"
    }
}
