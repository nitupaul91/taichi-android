package taichi.walking.seniors.beginners.taichi.onboarding.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingSteps

/**
 * What every onboarding screen reads from the flow rather than hard-coding: the user's step list,
 * which the progress bars are drawn against (iOS `OnboardingRootView.navigationSteps`), and the
 * Remote Config switches for the question UI. Defaults are iOS's in-app defaults.
 */
data class OnboardingChrome(
    val navigationSteps: List<OnboardingRoutes> = OnboardingSteps.allCases,
    /** `onboarding_back_button_enabled`: a back arrow beside the progress bar. */
    val backButtonEnabled: Boolean = false,
    /**
     * `onboarding_hide_continue_on_single_choice_enabled`: single-choice questions drop their
     * Continue button and move on by themselves once an answer is picked.
     */
    val hideContinueOnSingleChoice: Boolean = false,
    /** `onboarding_option_icons_enabled`: an icon in front of each answer. */
    val optionIconsEnabled: Boolean = false
)

val LocalOnboardingChrome = compositionLocalOf { OnboardingChrome() }

/**
 * iOS `SeniorScreenTemplate.autoContinueOnSelection`. Returns what a single-choice answer calls after
 * it is picked: with the Continue button hidden, the screen advances once, 0.4 s later, so the
 * highlighted answer is seen first; another pick in that window changes the answer, not the timing.
 * With the button shown it does nothing.
 */
@Composable
fun rememberContinueOnSelection(onContinue: () -> Unit): () -> Unit {
    val enabled = LocalOnboardingChrome.current.hideContinueOnSingleChoice
    val latestOnContinue by rememberUpdatedState(onContinue)
    var triggered by remember { mutableStateOf(false) }

    LaunchedEffect(triggered) {
        if (triggered) {
            delay(SELECTION_CONTINUE_DELAY_MS)
            latestOnContinue()
        }
    }

    return remember(enabled) {
        { if (enabled) triggered = true }
    }
}

private const val SELECTION_CONTINUE_DELAY_MS = 400L
