package taichi.walking.seniors.beginners.taichi.ui.settings.screen

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import taichi.walking.seniors.beginners.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaiChiSettingsViewModel @Inject constructor() : ViewModel() {

    val appVersion: String = BuildConfig.VERSION_NAME

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
