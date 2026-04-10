package taichi.walking.seniors.beginners

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
) : ViewModel() {

    fun endBillingConnection() {
        purchasesRepository.endConnection()
    }

    fun queryPurchases() {
        purchasesRepository.queryPurchases()
    }

    suspend fun shouldShowOnboarding() =
        dataStorePrefs.shouldShowOnboarding().first() && purchaseManager.isUserPremiumSubscribed().first().not()
}
