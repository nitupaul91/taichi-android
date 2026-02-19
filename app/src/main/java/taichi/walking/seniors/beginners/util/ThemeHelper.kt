package taichi.walking.seniors.beginners.util

import androidx.appcompat.app.AppCompatDelegate
import com.mobteq.billing.datastore.DataStorePrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ThemeHelper @Inject constructor(
    private val dataStorePrefs: DataStorePrefs
) {

    fun getAvailableThemeOptions(): List<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            listOf(
                LIGHT_MODE,
                DARK_MODE,
                SYSTEM_MODE
            )
        } else {
            listOf(
                LIGHT_MODE,
                DARK_MODE,
            )
        }

    }

    fun getThemePreference(): Flow<String> {
        return dataStorePrefs.getCurrentTheme().map {
            if (it == null) {
                getDefaultTheme()
            } else {
                it
            }
        }
    }

    suspend fun applyTheme(themeOption: String) {
        applyThemeInternal(themeOption)

        dataStorePrefs.setCurrentTheme(themeOption)
    }

    fun applySavedTheme() {
        GlobalScope.launch {
            val themePreference = getThemePreference().first()

            applyThemeInternal(themePreference)
        }
    }

    private fun applyThemeInternal(themeOption: String) {
        when (themeOption) {
            LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun getDefaultTheme(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            SYSTEM_MODE
        } else {
            LIGHT_MODE
        }
    }

    companion object {
        const val LIGHT_MODE = "light"
        const val DARK_MODE = "dark"
        const val SYSTEM_MODE = "system"
    }
}