package taichi.walking.seniors.beginners.taichi.onboarding.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.onboarding.analytics.OnboardingAnalyticsTracker
import taichi.walking.seniors.beginners.taichi.onboarding.viewmodel.OnboardingViewModel
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.AgeScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ActivityLevelScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.ArmsForwardScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.BmiScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.BetweenMealsScreen
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
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PaywallScreen
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PlanReadyScreen
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
    val scope = rememberCoroutineScope()

    // Track screen views
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val currentRoute = currentBackStackEntry?.destination?.route
        val route = OnboardingRoutes.all.find { it.route == currentRoute } ?: return@LaunchedEffect
        analyticsTracker.trackScreenView(
            screenIndex = route.index,
            stepName = route.analyticsStepName
        )
        viewModel.persistSnapshot()
    }

    NavHost(
        navController = navController,
        startDestination = OnboardingRoutes.Welcome.route
    ) {
        composable(OnboardingRoutes.Welcome.route) {
            WelcomeScreen(
                onContinue = { navController.navigateNext(OnboardingRoutes.Welcome, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Gender.route) {
            GenderScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Gender, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.CurrentBodyType.route) {
            CurrentBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.CurrentBodyType, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Goals.route) {
            GoalsScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Goals, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.TargetBodyType.route) {
            TargetBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetBodyType, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.TargetZones.route) {
            TargetZonesScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetZones, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.HeardAboutUs.route) {
            HeardAboutUsScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.HeardAboutUs, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ValuePropOnWay.route) {
            ValuePropOnWayScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropOnWay, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Familiarity.route) {
            FamiliarityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Familiarity, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ValuePropMeditation.route) {
            ValuePropMeditationScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropMeditation, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ActivityLevel.route) {
            ActivityLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ActivityLevel, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.WalkDaily.route) {
            WalkDailyScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.WalkDaily, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.StairsFeeling.route) {
            StairsFeelingScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.StairsFeeling, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.SquatAbility.route) {
            SquatAbilityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SquatAbility, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.RotateHead.route) {
            RotateHeadScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.RotateHead, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ArmsForward.route) {
            ArmsForwardScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ArmsForward, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.TaiChiLevel.route) {
            TaiChiLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TaiChiLevel, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.BetweenMeals.route) {
            BetweenMealsScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.BetweenMeals, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.SleepAmount.route) {
            SleepAmountScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SleepAmount, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.WaterIntake.route) {
            WaterIntakeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.WaterIntake, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.DietType.route) {
            DietTypeScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.DietType, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Height.route) {
            HeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Height, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Weight.route) {
            WeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Weight, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Bmi.route) {
            BmiScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Bmi, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.TargetWeight.route) {
            TargetWeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetWeight, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Age.route) {
            AgeScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Age, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ShapeUpEvent.route) {
            ShapeUpEventScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ShapeUpEvent, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ShapeUpEventDate.route) {
            ShapeUpEventDateScreen(
                state = state,
                onAction = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ShapeUpEventDate, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Motivation.route) {
            MotivationScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Motivation, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.ExerciseBlockers.route) {
            ExerciseBlockersScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ExerciseBlockers, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Summary.route) {
            SummaryScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Summary, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.NotificationPermission.route) {
            NotificationPermissionScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.NotificationPermission, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.SocialProof.route) {
            SocialProofScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SocialProof, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.PlanReady.route) {
            PlanReadyScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.PlanReady, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Loading.route) {
            LoadingScreen(
                onContinue = { navController.navigateNext(OnboardingRoutes.Loading, state.shapeUpEventId) }
            )
        }
        composable(OnboardingRoutes.Paywall.route) {
            PaywallScreen(
                onClose = {
                    scope.launch {
                        viewModel.completeOnboarding()
                        analyticsTracker.trackOnboardingComplete()
                        onFinished()
                    }
                },
                onComplete = {
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

private fun NavHostController.navigateNext(route: OnboardingRoutes, shapeUpEventId: String?) {
    val next = OnboardingRoutes.nextOf(route.route, shapeUpEventId)
    if (next != null) navigate(next.route)
}
