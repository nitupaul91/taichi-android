package taichi.walking.seniors.beginners.taichi.onboarding.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.onboarding.analytics.OnboardingAnalyticsTracker
import taichi.walking.seniors.beginners.taichi.onboarding.analytics.OnboardingAnswerSummary
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.LocalOnboardingChrome
import taichi.walking.seniors.beginners.taichi.onboarding.viewmodel.OnboardingViewModel
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ActivityLevelScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.AgeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ArmsForwardScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.BetweenMealsScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.BmiScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.CurrentBodyTypeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.DietTypeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ExerciseBlockersScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.FamiliarityScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.GenderScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.GoalsScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.HeardAboutUsScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.HeightScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.LoadingScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.MotivationScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.NotificationPermissionScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.OnboardingPaywallStep
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PlanReadyScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ProgramSelectionScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.RotateHeadScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ShapeUpEventDateScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ShapeUpEventScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.SleepAmountScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.SocialProofScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.SquatAbilityScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.StairsFeelingScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.SummaryScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.TaiChiLevelScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.TargetBodyTypeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.TargetWeightScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.TargetZonesScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ValuePropMeditationScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ValuePropOnWayScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.WalkDailyScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.WaterIntakeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.WeightScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.WelcomeScreen

@Composable
fun OnboardingNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: OnboardingViewModel = hiltViewModel(),
    analyticsTracker: OnboardingAnalyticsTracker,
    onFinished: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val usesGoalsFirstOrder by viewModel.usesGoalsFirstOrder.collectAsState()
    val scope = rememberCoroutineScope()

    // This user's screens, recomputed like iOS's computed `navigationSteps`: the shape-up answer and
    // live Remote Config flags both change it, and the progress bars are drawn against it.
    val navigationSteps = OnboardingSteps.navigationSteps(
        goalsFirst = usesGoalsFirstOrder,
        flags = viewModel.flowFlags(),
        shapeUpEventId = state.shapeUpEventId
    )

    // Who started the next backward move: the on-screen arrow, or (when unset) the system back.
    val backSource = remember { BackSource() }

    // Track screen views and upload the profile snapshot on every step, as iOS does from
    // onChange(of: currentStep).
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val route = OnboardingRoutes.fromRoute(currentBackStackEntry?.destination?.route) ?: return@LaunchedEffect
        val steps = viewModel.navigationSteps()
        val ordered = viewModel.orderedSteps()
        val previous = analyticsTracker.lastTrackedStep
        val isBackward = previous != null && previous != route && ordered.indexOf(route) < ordered.indexOf(previous)
        if (isBackward && previous != null) {
            analyticsTracker.trackBackTap(from = previous, to = route, navigationSteps = steps, source = backSource.take())
        }
        analyticsTracker.trackScreenView(route, steps, direction = if (isBackward) "backward" else "forward")
        if (route == OnboardingRoutes.Paywall) {
            analyticsTracker.trackOnboardingCompleted(practiceKind = viewModel.analyticsPracticeKind())
        }
        viewModel.persistSnapshot(completedStep = route.analyticsStepName)
    }

    // The next screen comes from the step list, never a fixed successor. Only the screen on top may
    // advance, so a late callback (a double tap, a timer) cannot skip a screen.
    val goForward: (OnboardingRoutes) -> Unit = { from ->
        if (navController.currentDestination?.route == from.route) {
            if (from == OnboardingRoutes.Welcome) viewModel.decideStepOrder()
            viewModel.stepAfter(from)?.let { next ->
                OnboardingAnswerSummary.of(from, viewModel.state.value)?.let { summary ->
                    analyticsTracker.trackAnswerSubmitted(
                        step = from,
                        navigationSteps = viewModel.navigationSteps(),
                        summary = summary,
                        autoContinued = viewModel.isAutoContinued(from),
                        practiceKind = viewModel.analyticsPracticeKind(),
                        answerPosition = if (from == OnboardingRoutes.HeardAboutUs) viewModel.discoverySourcePosition() else null
                    )
                }
                navController.navigate(next.route)
            }
        }
    }
    val goBack: () -> Unit = {
        backSource.button()
        navController.popBackStack()
    }

    CompositionLocalProvider(LocalOnboardingChrome provides viewModel.chrome(navigationSteps)) {
        OnboardingNavHost(
            navController = navController,
            viewModel = viewModel,
            state = state,
            scope = scope,
            analyticsTracker = analyticsTracker,
            onFinished = onFinished,
            goForward = goForward,
            goBack = goBack
        )
    }
}

/** Remembers that the on-screen back arrow started a pop; any other pop is the system back. */
private class BackSource {
    private var fromButton = false

    fun button() {
        fromButton = true
    }

    fun take(): String = (if (fromButton) "button" else "system").also { fromButton = false }
}

@Composable
private fun OnboardingNavHost(
    navController: NavHostController,
    viewModel: OnboardingViewModel,
    state: OnboardingState,
    scope: CoroutineScope,
    analyticsTracker: OnboardingAnalyticsTracker,
    onFinished: () -> Unit,
    goForward: (OnboardingRoutes) -> Unit,
    goBack: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingRoutes.Welcome.route
    ) {
        composable(OnboardingRoutes.Welcome.route) {
            WelcomeScreen(
                onContinue = { goForward(OnboardingRoutes.Welcome) }
            )
        }
        composable(OnboardingRoutes.Gender.route) {
            GenderScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Gender) }
            )
        }
        composable(OnboardingRoutes.CurrentBodyType.route) {
            CurrentBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.CurrentBodyType) }
            )
        }
        composable(OnboardingRoutes.Goals.route) {
            GoalsScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Goals) }
            )
        }
        composable(OnboardingRoutes.TargetBodyType.route) {
            TargetBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.TargetBodyType) }
            )
        }
        composable(OnboardingRoutes.TargetZones.route) {
            TargetZonesScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.TargetZones) }
            )
        }
        composable(OnboardingRoutes.ProgramSelection.route) {
            ProgramSelectionScreen(
                state = state,
                onApply = { viewModel.applyProgramSelection(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ProgramSelection) }
            )
        }
        composable(OnboardingRoutes.HeardAboutUs.route) {
            val discoveryOrderSeed by viewModel.discoveryOrderSeed.collectAsState()
            HeardAboutUsScreen(
                state = state,
                sources = remember(discoveryOrderSeed, state.discoverySourceId) {
                    viewModel.displayedDiscoverySources()
                },
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.HeardAboutUs) }
            )
        }
        composable(OnboardingRoutes.ValuePropOnWay.route) {
            ValuePropOnWayScreen(
                state = state,
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ValuePropOnWay) }
            )
        }
        composable(OnboardingRoutes.Familiarity.route) {
            FamiliarityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Familiarity) }
            )
        }
        composable(OnboardingRoutes.ValuePropMeditation.route) {
            ValuePropMeditationScreen(
                state = state,
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ValuePropMeditation) }
            )
        }
        composable(OnboardingRoutes.ActivityLevel.route) {
            ActivityLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ActivityLevel) }
            )
        }
        composable(OnboardingRoutes.WalkDaily.route) {
            WalkDailyScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.WalkDaily) }
            )
        }
        composable(OnboardingRoutes.StairsFeeling.route) {
            StairsFeelingScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.StairsFeeling) }
            )
        }
        composable(OnboardingRoutes.SquatAbility.route) {
            SquatAbilityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.SquatAbility) }
            )
        }
        composable(OnboardingRoutes.RotateHead.route) {
            RotateHeadScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.RotateHead) }
            )
        }
        composable(OnboardingRoutes.ArmsForward.route) {
            ArmsForwardScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ArmsForward) }
            )
        }
        composable(OnboardingRoutes.TaiChiLevel.route) {
            TaiChiLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.TaiChiLevel) }
            )
        }
        composable(OnboardingRoutes.BetweenMeals.route) {
            BetweenMealsScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.BetweenMeals) }
            )
        }
        composable(OnboardingRoutes.SleepAmount.route) {
            SleepAmountScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.SleepAmount) }
            )
        }
        composable(OnboardingRoutes.WaterIntake.route) {
            WaterIntakeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.WaterIntake) }
            )
        }
        composable(OnboardingRoutes.DietType.route) {
            DietTypeScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.DietType) }
            )
        }
        composable(OnboardingRoutes.Height.route) {
            HeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Height) }
            )
        }
        composable(OnboardingRoutes.Weight.route) {
            WeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Weight) }
            )
        }
        composable(OnboardingRoutes.Bmi.route) {
            BmiScreen(
                state = state,
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Bmi) }
            )
        }
        composable(OnboardingRoutes.TargetWeight.route) {
            TargetWeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.TargetWeight) }
            )
        }
        composable(OnboardingRoutes.Age.route) {
            AgeScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Age) }
            )
        }
        composable(OnboardingRoutes.ShapeUpEvent.route) {
            ShapeUpEventScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ShapeUpEvent) }
            )
        }
        composable(OnboardingRoutes.ShapeUpEventDate.route) {
            ShapeUpEventDateScreen(
                state = state,
                onAction = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ShapeUpEventDate) }
            )
        }
        composable(OnboardingRoutes.Motivation.route) {
            MotivationScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Motivation) }
            )
        }
        composable(OnboardingRoutes.ExerciseBlockers.route) {
            ExerciseBlockersScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.ExerciseBlockers) }
            )
        }
        composable(OnboardingRoutes.Summary.route) {
            SummaryScreen(
                state = state,
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.Summary) }
            )
        }
        composable(OnboardingRoutes.NotificationPermission.route) {
            NotificationPermissionScreen(
                state = state,
                onBack = goBack,
                onPermissionResult = { promptShown, authorized, previousStatus ->
                    analyticsTracker.trackNotificationPermissionResult(
                        action = "continue",
                        promptShown = promptShown,
                        authorized = authorized,
                        previousStatus = previousStatus
                    )
                },
                onContinue = { goForward(OnboardingRoutes.NotificationPermission) }
            )
        }
        composable(OnboardingRoutes.SocialProof.route) {
            SocialProofScreen(
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.SocialProof) }
            )
        }
        composable(OnboardingRoutes.PlanReady.route) {
            PlanReadyScreen(
                onBack = goBack,
                onContinue = { goForward(OnboardingRoutes.PlanReady) }
            )
        }
        composable(OnboardingRoutes.Loading.route) {
            LoadingScreen(
                onContinue = {
                    // iOS reports the CTA it was left by; Android's plan screen moves on by itself.
                    if (navController.currentDestination?.route == OnboardingRoutes.Loading.route) {
                        analyticsTracker.trackPlanLoadingDone(cta = "auto_continue")
                    }
                    goForward(OnboardingRoutes.Loading)
                }
            )
        }
        composable(OnboardingRoutes.Paywall.route) {
            OnboardingPaywallStep(
                onFinished = {
                    scope.launch {
                        viewModel.completeOnboarding()
                        analyticsTracker.trackOnboardingComplete()
                        onFinished()
                    }
                }
            )
        }
    }
}
