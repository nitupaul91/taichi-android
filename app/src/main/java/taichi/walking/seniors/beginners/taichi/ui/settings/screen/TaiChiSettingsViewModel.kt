package taichi.walking.seniors.beginners.taichi.ui.settings.screen

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobteq.billing.model.purchases.local.PurchaseManager
import taichi.walking.seniors.beginners.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaiChiSettingsViewModel @Inject constructor(
    private val purchaseManager: PurchaseManager
) : ViewModel() {

    val appVersion: String = BuildConfig.VERSION_NAME

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        viewModelScope.launch {
            purchaseManager.isUserPremiumSubscribed().collect { isPremium ->
                _isPremium.value = isPremium
            }
        }
    }

    fun buildContactEmailUri(): String {
        val subject = "Taichi - Feedback report"
        val body = buildString {
            append("OS: Android ${Build.VERSION.RELEASE}\n")
            append("Model: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Language: ${Locale.getDefault().language}\n")
            append("App version: $appVersion\n\n")
        }

        return "mailto:nativemobteq@gmail.com" +
            "?subject=${Uri.encode(subject)}" +
            "&body=${Uri.encode(body)}"
    }
}
