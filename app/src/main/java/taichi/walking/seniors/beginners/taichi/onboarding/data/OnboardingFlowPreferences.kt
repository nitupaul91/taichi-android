package taichi.walking.seniors.beginners.taichi.onboarding.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flow decisions that must not change once the user is inside onboarding. iOS keeps the same value
 * in UserDefaults (`onboardingGoalsFirstOrder`).
 *
 * SharedPreferences rather than DataStore: the view model needs the value synchronously to build
 * the step list on its first frame.
 */
@Singleton
class OnboardingFlowPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Whether this user's onboarding uses the goals-first order. Decided once, on leaving the
     * welcome screen: Remote Config can activate during onboarding, and a live flag would reshuffle
     * the remaining screens under the user.
     */
    var usesGoalsFirstOrder: Boolean
        get() = prefs.getBoolean(KEY_GOALS_FIRST_ORDER, false)
        set(value) {
            prefs.edit().putBoolean(KEY_GOALS_FIRST_ORDER, value).apply()
        }

    private companion object {
        const val PREFS_NAME = "taichi_onboarding_flow"
        const val KEY_GOALS_FIRST_ORDER = "onboardingGoalsFirstOrder"
    }
}
