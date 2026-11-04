package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.R
import androidx.core.os.bundleOf
import com.mobteq.analytics.logAppEvent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutPreparationScreen
import taichi.walking.seniors.beginners.taichi.ui.video.WorkoutMediaCache
import coil.compose.AsyncImage
import taichi.walking.seniors.beginners.taichi.printable.PrintablePlanCard
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutCompletionScreen
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutPreviewScreen
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutScreen
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutAnalyticsContext
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PaywallScreen
import taichi.walking.seniors.beginners.ui.paywall.PaywallSource
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import androidx.activity.compose.BackHandler
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.ui.rateapp.RateAppDialogView

// Specific colors from the design (matching iOS)
private val TaiChiGreen = Color(0xFF0BA56B)
private val CompletedCircleBackground = Color(0xFFE6F5EE)
private val TimelineLineColor = Color(0xFFCFDED5)
private val FutureCircleBorder = Color(0xFFCFDED5)
private val FutureDayTextColor = Color(0xFF9AA8AD)

private const val POST_WORKOUT_PAYWALL_SHOWN = "post_workout_paywall_shown"

@Composable
fun JourneyHomeScreen(
    viewModel: JourneyHomeViewModel = hiltViewModel(),
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    PracticeJourneyHomeScreen(
        title = "Your Tai Chi Journey",
        practiceKind = PracticeKind.TAI_CHI,
        viewModel = viewModel,
        onBottomBarVisibilityChange = onBottomBarVisibilityChange
    )
}

@Composable
fun WalkingHomeScreen(
    viewModel: WalkingHomeViewModel = hiltViewModel(),
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    PracticeJourneyHomeScreen(
        title = "Tai Chi Walking",
        practiceKind = PracticeKind.WALKING,
        viewModel = viewModel,
        onBottomBarVisibilityChange = onBottomBarVisibilityChange
    )
}

@Composable
fun ChairHomeScreen(
    viewModel: ChairHomeViewModel = hiltViewModel(),
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    PracticeJourneyHomeScreen(
        title = stringResource(R.string.chair_tai_chi_title),
        practiceKind = PracticeKind.CHAIR,
        viewModel = viewModel,
        onBottomBarVisibilityChange = onBottomBarVisibilityChange
    )
}

@Composable
private fun PracticeJourneyHomeScreen(
    title: String,
    practiceKind: PracticeKind,
    viewModel: BaseJourneyHomeViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var previewDay by remember { mutableStateOf<WorkoutDayDto?>(null) }
    var workoutDay by remember { mutableStateOf<WorkoutDayDto?>(null) }
    var preparingDay by remember { mutableStateOf<WorkoutDayDto?>(null) }
    val beginScope = rememberCoroutineScope()
    val context = LocalContext.current
    var completedDay by remember { mutableStateOf<WorkoutDayDto?>(null) }
    var showPostWorkoutPaywall by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    // The completion screen's next step (reminder prompt, then home) once the post-workout paywall closes.
    var afterPostWorkoutPaywall by remember { mutableStateOf<(() -> Unit)?>(null) }
    var paywallSource by remember { mutableStateOf(PaywallSource.WORKOUT_PREVIEW) }
    var showIntensitySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.workoutPlan == null) viewModel.fetchWorkoutPlan()
    }

    LaunchedEffect(previewDay, preparingDay, workoutDay, completedDay, showPaywall) {
        val showBottomBar = previewDay == null &&
            preparingDay == null &&
            workoutDay == null &&
            completedDay == null &&
            !showPaywall
        onBottomBarVisibilityChange(showBottomBar)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF5FAF8),
                        Color(0xFFFAFDFC),
                        Color.White
                    )
                )
            )
    ) {
        when {
            state.isLoadingPlan -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TaiChiGreen
                )
            }
            state.workoutPlan != null -> {
                val plan = state.workoutPlan!!
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = statusBarPadding + 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp)
                ) {
                    item {

                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state.showIntensityWidget) {
                        item {
                            WorkoutIntensityWidget(intensity = state.workoutIntensity) {
                                showIntensitySheet = true
                                logAppEvent(
                                    "workout_intensity_widget_tap",
                                    bundleOf(
                                        "practice_kind" to viewModel.analyticsPracticeKind,
                                        "is_premium" to state.isPremium,
                                        "current_intensity" to WorkoutIntensityLevel.normalized(state.workoutIntensity).value
                                    )
                                )
                            }
                        }
                    }

                    item {
                        PrintablePlanCard(
                            plan = plan,
                            practiceKind = practiceKind,
                            onOpenPaywall = {
                                paywallSource = PaywallSource.PRINTABLE_PLAN
                                showPaywall = true
                            },
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    item {
                        val phase = plan.phaseForDay(state.currentDay)
                        if (phase != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.5.dp, TaiChiGreen),
                                color = Color.Transparent,
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Text(
                                    text = phase.name.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaiChiGreen,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    itemsIndexed(plan.allDays) { index, day ->
                        val dayNumber = index + 1
                        JourneyDayRow(
                            dayNumber = dayNumber,
                            day = day,
                            isCurrent = dayNumber == state.currentDay,
                            isCompleted = dayNumber < state.currentDay,
                            isLast = index == plan.allDays.size - 1,
                            onTap = { previewDay = day }
                        )
                    }
                }
            }
            else -> {
                ErrorMessage(onRetry = { viewModel.fetchWorkoutPlan() })
            }
        }
    }

    // Modal logic (kept standard as they are full screens)
    previewDay?.let { day ->
        WorkoutPreviewScreen(
            dayNumber = day.day,
            dayData = day,
            currentDay = state.currentDay,
            isPremium = state.isPremium,
            freeDaysCount = viewModel.freeDaysCount(),
            onClose = { previewDay = null },
            onBegin = {
                previewDay = null
                beginScope.launch {
                    // A day already on disk skips the preparation screen instead of flashing it.
                    val ready = WorkoutMediaCache.isDayReady(context, WorkoutMediaCache.mediaUrls(day.exercises))
                    if (ready) workoutDay = day else preparingDay = day
                }
            },
            onOpenPaywall = {
                paywallSource = PaywallSource.WORKOUT_PREVIEW
                showPaywall = true
            }
        )
    }
    preparingDay?.let { day ->
        WorkoutPreparationScreen(
            dayNumber = day.day,
            dayData = day,
            onReady = { preparingDay = null; workoutDay = day },
            onClose = { preparingDay = null; previewDay = day }
        )
    }
    workoutDay?.let { day ->
        WorkoutScreen(
            dayNumber = day.day,
            dayData = day,
            onClose = { workoutDay = null },
            onComplete = { workoutDay = null; completedDay = day; viewModel.completeWorkoutFlow(day) },
            musicOnByDefault = state.musicOnByDefault,
            resolveMusicTrackUrl = viewModel::musicTrackUrl,
            analyticsContext = WorkoutAnalyticsContext(
                isPremium = state.isPremium,
                intensity = state.workoutIntensity.orEmpty()
            )
        )
    }
    completedDay?.let { day ->
        WorkoutCompletionScreen(
            dayNumber = day.day,
            dayData = day,
            practiceKind = practiceKind,
            completionResult = state.completionResult,
            onReturnHome = {
                completedDay = null
                viewModel.clearCompletionResult()
            },
            beforeReminderPrompt = { continueFlow ->
                // iOS WorkoutCompletionView.handleDoneTap: a non-premium user gets the paywall
                // first; the reminder prompt (and then home) follows when it closes.
                if (!state.isPremium && viewModel.shouldShowPostWorkoutPaywall()) {
                    logAppEvent(POST_WORKOUT_PAYWALL_SHOWN, bundleOf("day_number" to day.day))
                    afterPostWorkoutPaywall = continueFlow
                    showPostWorkoutPaywall = true
                } else {
                    continueFlow()
                }
            }
        )
    }
    if (showPostWorkoutPaywall) {
        val leavePostWorkoutPaywall: () -> Unit = {
            showPostWorkoutPaywall = false
            val next = afterPostWorkoutPaywall
            afterPostWorkoutPaywall = null
            next?.invoke()
        }
        BackHandler(onBack = leavePostWorkoutPaywall)
        PaywallScreen(
            onClose = leavePostWorkoutPaywall,
            onComplete = leavePostWorkoutPaywall,
            paywallVariant = RemotePaywallVariant.ONBOARDING_LIMITED_OFFER,
            source = PaywallSource.POST_WORKOUT
        )
    }
    if (showIntensitySheet) {
        WorkoutIntensitySheet(
            currentIntensity = state.workoutIntensity,
            isApplying = state.isLoadingPlan,
            onApply = { intensity ->
                logAppEvent(
                    "workout_intensity_apply_tap",
                    bundleOf(
                        "practice_kind" to viewModel.analyticsPracticeKind,
                        "is_premium" to state.isPremium,
                        "new_intensity" to intensity,
                        "previous_intensity" to WorkoutIntensityLevel.normalized(state.workoutIntensity).value
                    )
                )
                showIntensitySheet = false
                viewModel.applyWorkoutIntensity(intensity)
            },
            onDismiss = { showIntensitySheet = false }
        )
    }
    if (showPaywall) {
        PaywallScreen(
            onClose = { showPaywall = false },
            onComplete = { showPaywall = false },
            source = paywallSource
        )
    }
    if (state.showRateDialog) {
        RateAppDialogView(
            onDismiss = viewModel::dismissRateDialog,
            onRateNow = viewModel::onRateDialogConfirmed
        )
    }
}

@Composable
private fun JourneyDayRow(
    dayNumber: Int,
    day: WorkoutDayDto,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isLast: Boolean,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Essential for the vertical line to span correctly
        verticalAlignment = Alignment.Top
    ) {
        // --- Timeline Column ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> TaiChiGreen
                            isCompleted -> CompletedCircleBackground
                            else -> Color.White.copy(alpha = 0.5f)
                        }
                    )
                    .then(
                        if (!isCompleted && !isCurrent)
                            Modifier.border(2.dp, FutureCircleBorder, CircleShape)
                        else
                            Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = TaiChiGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    isCurrent -> {
                        Text(
                            text = dayNumber.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {
                        Text(
                            text = dayNumber.toString(),
                            color = FutureDayTextColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!isLast) {
                DashedLine(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- Card Column ---
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .weight(1f)
                .height(340.dp)
                .clip(RoundedCornerShape(28.dp))
                .clickable { onTap() }
        ) {
            AsyncImage(
                model = day.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.2f))

                Text(
                    "Day $dayNumber",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    day.firstExerciseName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Glass Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassBadge(icon = Icons.Default.Schedule, text = day.durationFormatted)
                    if (day.difficultyText.isNotEmpty()) {
                        GlassBadge(icon = Icons.Default.SignalCellularAlt, text = day.difficultyText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = TaiChiGreen,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompleted) "Day $dayNumber Completed" else "Start Day $dayNumber",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.25f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun DashedLine(modifier: Modifier) {
    Canvas(modifier = modifier.width(2.dp).fillMaxHeight()) {
        drawLine(
            color = TimelineLineColor,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
}

@Composable
fun ErrorMessage(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(56.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Unable to load plan", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = TaiChiGreen)) {
            Text("Try Again")
        }
    }
}
