package taichi.walking.seniors.beginners

import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import androidx.lifecycle.ViewModel
import com.mobteq.billing.datastore.DataStorePrefs
import com.mobteq.billing.domain.repository.PurchasesRepository
import com.mobteq.billing.model.purchases.local.PurchaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val purchasesRepository: PurchasesRepository,
    private val purchaseManager: PurchaseManager,
    private val dataStorePrefs: DataStorePrefs,
    private val remoteConfig: AiAssistantRemoteConfig,
) : ViewModel() {

    init {
        activateRemoteConfig()
    }

    private fun activateRemoteConfig() {
        remoteConfig.fetchAndActivate()
    }

    fun endBillingConnection() {
        purchasesRepository.endConnection()
    }

    fun queryPurchases() {
        purchasesRepository.queryPurchases()
    }

    suspend fun shouldShowOnboarding() =
        dataStorePrefs.shouldShowOnboarding().first() && purchaseManager.isUserPremiumSubscribed().first().not()
}
