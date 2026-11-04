package taichi.walking.seniors.beginners.util

import android.content.Context
import android.content.res.Configuration
import com.mobteq.remoteconfig.RemoteConfig
import kotlin.math.min

/**
 * The `dynamic_type_enabled` A/B test from iOS (43f26fc):
 * - off (default): text renders at the app's own sizes, whatever the phone's font size, as iOS
 *   1.1.8 did; large phone font sizes overlapped the workout header and split labels mid-word.
 * - on: text follows the phone's font size, capped at iOS's xxxLarge (23 pt body over 17 pt).
 *
 * Applied to the activity's base context rather than a Compose LocalDensity so dialogs, bottom
 * sheets and other windows the activity opens get it too (iOS learned that a root-only cap misses
 * presented screens).
 */
object AppFontScale {
    const val MAX_FONT_SCALE = 23f / 17f

    fun wrap(base: Context): Context {
        val enabled = RemoteConfig.peekBoolean(DYNAMIC_TYPE_ENABLED)
        val configuration = Configuration(base.resources.configuration)
        val target = if (enabled) min(configuration.fontScale, MAX_FONT_SCALE) else 1f
        if (configuration.fontScale == target) return base
        configuration.fontScale = target
        return base.createConfigurationContext(configuration)
    }

    private const val DYNAMIC_TYPE_ENABLED = "dynamic_type_enabled"
}
