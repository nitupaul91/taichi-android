package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.ui.paywall.OnboardingPaywallPhase
import taichi.walking.seniors.beginners.ui.paywall.OnboardingPaywallStepViewModel
import taichi.walking.seniors.beginners.ui.paywall.PaywallSource
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineMode
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineScreen

/**
 * The paywall step at the end of onboarding: the optional free-trial timeline, the paywall, and
 * the optional trial confirmation after a trial purchase. [onFinished] runs once, whichever way
 * the paywall ends (closed, purchased, restored).
 */
@Composable
fun OnboardingPaywallStep(
    onFinished: () -> Unit,
    viewModel: OnboardingPaywallStepViewModel = hiltViewModel()
) {
    val phase by viewModel.phase.collectAsState()
    val currentOnFinished by rememberUpdatedState(onFinished)

    when (val current = phase) {
        OnboardingPaywallPhase.Starting,
        OnboardingPaywallPhase.Finished -> Box(
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
            onClose = viewModel::onPaywallFinished,
            onComplete = viewModel::onPaywallFinished,
            paywallVariant = RemotePaywallVariant.PRIMARY,
            source = PaywallSource.ONBOARDING,
            onTrialPurchased = viewModel::onTrialPurchased
        )

        is OnboardingPaywallPhase.TrialConfirmation -> {
            LaunchedEffect(current.details) { viewModel.onTrialConfirmationViewed(current.details) }
            // The purchase is done: back must not return into the onboarding questions.
            BackHandler { viewModel.onTrialConfirmationContinued(current.details) }
            TrialTimelineScreen(
                details = current.details,
                mode = TrialTimelineMode.POST_PURCHASE_CONFIRMATION,
                reminderMessaging = current.reminderMessaging,
                onPrimaryAction = { viewModel.onTrialConfirmationContinued(current.details) }
            )
        }
    }

    LaunchedEffect(phase) {
        if (phase == OnboardingPaywallPhase.Finished) currentOnFinished()
    }
}
