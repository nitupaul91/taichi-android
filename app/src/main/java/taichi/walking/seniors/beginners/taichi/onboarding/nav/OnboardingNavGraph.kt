package taichi.walking.seniors.beginners.taichi.onboarding.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.*
import taichi.walking.seniors.beginners.taichi.onboarding.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinished: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = OnboardingRoutes.Welcome.route
    ) {
        composable(OnboardingRoutes.Welcome.route) {
            WelcomeScreen(
                onContinue = { navController.navigateNext(OnboardingRoutes.Welcome) }
            )
        }
        composable(OnboardingRoutes.Age.route) {
            AgeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Age) }
            )
        }
        composable(OnboardingRoutes.Gender.route) {
            GenderScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Gender) }
            )
        }
        composable(OnboardingRoutes.SocialProof.route) {
            SocialProofScreen(
                progress = progressOf(OnboardingRoutes.SocialProof),
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SocialProof) }
            )
        }
        composable(OnboardingRoutes.CommunityValue.route) {
            CommunityValueScreen(
                progress = progressOf(OnboardingRoutes.CommunityValue),
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.CommunityValue) }
            )
        }
        composable(OnboardingRoutes.Goals.route) {
            GoalsScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Goals) }
            )
        }
        composable(OnboardingRoutes.ValuePropOnWay.route) {
            ValuePropOnWayScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropOnWay) }
            )
        }
        composable(OnboardingRoutes.Familiarity.route) {
            FamiliarityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Familiarity) }
            )
        }
        composable(OnboardingRoutes.ValuePropMeditation.route) {
            ValuePropMeditationScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropMeditation) }
            )
        }
        composable(OnboardingRoutes.CurrentBodyType.route) {
            CurrentBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.CurrentBodyType) }
            )
        }
        composable(OnboardingRoutes.TargetBodyType.route) {
            TargetBodyTypeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetBodyType) }
            )
        }
        composable(OnboardingRoutes.TargetZones.route) {
            TargetZonesScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetZones) }
            )
        }
        composable(OnboardingRoutes.ActivityLevel.route) {
            ActivityLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ActivityLevel) }
            )
        }
        composable(OnboardingRoutes.WalkDaily.route) {
            WalkDailyScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.WalkDaily) }
            )
        }
        composable(OnboardingRoutes.StairsFeeling.route) {
            StairsFeelingScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.StairsFeeling) }
            )
        }
        composable(OnboardingRoutes.SquatAbility.route) {
            SquatAbilityScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SquatAbility) }
            )
        }
        composable(OnboardingRoutes.RotateHead.route) {
            RotateHeadScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.RotateHead) }
            )
        }
        composable(OnboardingRoutes.ArmsForward.route) {
            ArmsForwardScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ArmsForward) }
            )
        }
        composable(OnboardingRoutes.TaiChiLevel.route) {
            TaiChiLevelScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TaiChiLevel) }
            )
        }
        composable(OnboardingRoutes.ValuePropPlanAdjusted.route) {
            ValuePropPlanAdjustedScreen(
                state = state,
                progress = progressOf(OnboardingRoutes.ValuePropPlanAdjusted),
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropPlanAdjusted) }
            )
        }
        composable(OnboardingRoutes.BetweenMeals.route) {
            BetweenMealsScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.BetweenMeals) }
            )
        }
        composable(OnboardingRoutes.SleepAmount.route) {
            SleepAmountScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.SleepAmount) }
            )
        }
        composable(OnboardingRoutes.WaterIntake.route) {
            WaterIntakeScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.WaterIntake) }
            )
        }
        composable(OnboardingRoutes.DietType.route) {
            DietTypeScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.DietType) }
            )
        }
        composable(OnboardingRoutes.ValuePropAlmostDone.route) {
            ValuePropAlmostDoneScreen(
                progress = progressOf(OnboardingRoutes.ValuePropAlmostDone),
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ValuePropAlmostDone) }
            )
        }
        composable(OnboardingRoutes.Height.route) {
            HeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Height) }
            )
        }
        composable(OnboardingRoutes.Weight.route) {
            WeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Weight) }
            )
        }
        composable(OnboardingRoutes.Bmi.route) {
            BmiScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Bmi) }
            )
        }
        composable(OnboardingRoutes.TargetWeight.route) {
            TargetWeightScreen(
                state = state,
                onChange = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.TargetWeight) }
            )
        }
        composable(OnboardingRoutes.Motivation.route) {
            MotivationScreen(
                state = state,
                onSelect = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Motivation) }
            )
        }
        composable(OnboardingRoutes.ExerciseBlockers.route) {
            ExerciseBlockersScreen(
                state = state,
                onToggle = { viewModel.onAction(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ExerciseBlockers) }
            )
        }
        composable(OnboardingRoutes.Summary.route) {
            SummaryScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.Summary) }
            )
        }
        composable(OnboardingRoutes.PlanReady.route) {
            PlanReadyScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.PlanReady) }
            )
        }
        composable(OnboardingRoutes.Loading.route) {
            LoadingScreen(
                onContinue = { navController.navigateNext(OnboardingRoutes.Loading) }
            )
        }
        composable(OnboardingRoutes.ImprovementGraph.route) {
            ImprovementGraphScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigateNext(OnboardingRoutes.ImprovementGraph) }
            )
        }
        composable(OnboardingRoutes.Paywall.route) {
            PaywallScreen(
                onClose = {
                    scope.launch {
                        viewModel.completeOnboarding()
                        onFinished()
                    }
                },
                onComplete = {
                    scope.launch {
                        viewModel.completeOnboarding()
                        onFinished()
                    }
                }
            )
        }
    }
}

private fun progressOf(route: OnboardingRoutes): Float = route.index.toFloat() / route.total.toFloat()

private fun NavHostController.navigateNext(route: OnboardingRoutes) {
    val next = OnboardingRoutes.nextOf(route.route)
    if (next != null) navigate(next.route)
}
