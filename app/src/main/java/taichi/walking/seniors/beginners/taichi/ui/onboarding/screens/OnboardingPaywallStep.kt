package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.ui.paywall.OnboardingPaywallPhase
import taichi.walking.seniors.beginners.ui.paywall.OnboardingPaywallStepViewModel
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineMode
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineScreen

/**
 * The paywall step at the end of onboarding: the optional free-trial timeline, then the paywall.
 * [onFinished] runs once, whichever way the paywall ends (closed, purchased, restored).
 */
@Composable
fun OnboardingPaywallStep(
    onFinished: () -> Unit,
    viewModel: OnboardingPaywallStepViewModel = hiltViewModel()
) {
    val phase by viewModel.phase.collectAsState()

    when (val current = phase) {
        OnboardingPaywallPhase.Starting -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        is OnboardingPaywallPhase.TrialTimeline -> {
            LaunchedEffect(current.details) { viewModel.onTrialTimelineViewed(current.details) }
            TrialTimelineScreen(
                details = current.details,
                mode = TrialTimelineMode.PRE_PURCHASE,
                reminderMessaging = current.reminderMessaging,
                progress = progressFor(OnboardingRoutes.Paywall),
                onPrimaryAction = { viewModel.onTrialTimelineContinued(current.details) }
            )
        }

        OnboardingPaywallPhase.Paywall -> PaywallScreen(
            onClose = onFinished,
            onComplete = onFinished,
            paywallVariant = RemotePaywallVariant.PRIMARY
        )
    }
}
