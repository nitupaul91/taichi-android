package taichi.walking.seniors.beginners.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
class Strings @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    fun getString(@StringRes resId: Int): String {
        return applicationContext.getString(resId)
    }

    fun getString(@StringRes resId: Int, arg1: String, arg2: String): String {
        return applicationContext.getString(resId, arg1, arg2)
    }

    fun getString(@StringRes resId: Int, arg: String): String {
        return applicationContext.getString(resId, arg)
    }

    fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String {
        return applicationContext.resources.getQuantityString(resId, quantity, quantity)
    }

    fun getLocale(): Locale {
        return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            applicationContext.resources.configuration.locale
        } else {
            applicationContext.resources.configuration.locales.get(0)
        }
    }
}