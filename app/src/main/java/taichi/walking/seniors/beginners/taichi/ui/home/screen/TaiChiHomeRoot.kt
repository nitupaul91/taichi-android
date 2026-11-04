package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.activity.compose.BackHandler
import taichi.walking.seniors.beginners.R
import androidx.compose.ui.res.stringResource
import com.mobteq.analytics.logAppEvent
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import taichi.walking.seniors.beginners.taichi.program.ProgramPreference
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import androidx.compose.foundation.background
import taichi.walking.seniors.beginners.taichi.ui.progress.screen.ProgressTabScreen
import taichi.walking.seniors.beginners.taichi.ui.settings.screen.TaiChiSettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.taichi.ui.home.activity.PaywallLaunchRequest
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PaywallScreen
import taichi.walking.seniors.beginners.ui.paywall.HomeEntryPaywallViewModel

internal enum class HomeTab(
    @androidx.annotation.StringRes val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val analyticsName: String
) {
    TaiChi(R.string.tab_tai_chi, Icons.Default.AccessibilityNew, "tai_chi"),
    Chair(R.string.tab_chair, Icons.Default.EventSeat, "chair"),
    Walking(R.string.tab_walking, Icons.Default.DirectionsWalk, "walking"),
    Progress(R.string.progress_title, Icons.Default.ShowChart, "progress"),
    Settings(R.string.settings_title, Icons.Default.Settings, "settings")
}

@Composable
fun TaiChiHomeRoot(
    pendingPaywallRequest: PaywallLaunchRequest? = null,
    onPaywallRequestConsumed: () -> Unit = {}
) {
    val tabsViewModel: HomeTabsViewModel = hiltViewModel()
    val visibleTabs = remember { tabsViewModel.visibleTabs() }
    var selected by remember { mutableStateOf(visibleTabs.first()) }
    var showBottomBar by remember { mutableStateOf(true) }
    var activePaywallRequest by remember { mutableStateOf<PaywallLaunchRequest?>(null) }

    LaunchedEffect(selected) {
        if (selected !in PRACTICE_TABS) showBottomBar = true
    }

    LaunchedEffect(pendingPaywallRequest) {
        if (pendingPaywallRequest != null) {
            activePaywallRequest = pendingPaywallRequest
        }
    }

    val dismissActivePaywall = {
        activePaywallRequest = null
        onPaywallRequestConsumed()
    }

    // Limited-offer paywall on the first home visit, re-entry paywall on later ones (iOS
    // JourneyHomeView). Never over a workout, another paywall or a notification's paywall.
    val entryPaywalls: HomeEntryPaywallViewModel = hiltViewModel()
    entryPaywalls.homeContext = HomeEntryPaywallViewModel.HomeContext(
        practiceKind = selected.practiceKind?.storageValue,
        canPresentPaywall = showBottomBar && activePaywallRequest == null && pendingPaywallRequest == null
    )
    LaunchedEffect(Unit) {
        entryPaywalls.onHomeShown()
        entryPaywalls.paywallRequests.collect { request ->
            if (activePaywallRequest == null && pendingPaywallRequest == null) {
                activePaywallRequest = request
            }
        }
    }

    BackHandler(enabled = activePaywallRequest != null) {
        dismissActivePaywall()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar && activePaywallRequest == null) {
                NavigationBar(
                    containerColor = Color(0xFFFDFEFD),
                    tonalElevation = 0.dp
                ) {
                    visibleTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = {
                                if (tab != selected) {
                                    // iOS tab_selected: only real changes, with iOS tab names.
                                    logAppEvent(
                                        "tab_selected",
                                        bundleOf(
                                            "tab" to tab.analyticsName,
                                            "tab_index" to visibleTabs.indexOf(tab),
                                            "previous_tab" to selected.analyticsName
                                        )
                                    )
                                }
                                selected = tab
                            },
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.titleRes)) },
                            label = { Text(stringResource(tab.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2FA36B),
                                selectedTextColor = Color(0xFF2FA36B),
                                indicatorColor = Color(0xFFDCEDE4),
                                unselectedIconColor = Color(0xFF262B30),
                                unselectedTextColor = Color(0xFF262B30)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selected) {
                HomeTab.TaiChi -> JourneyHomeScreen(
                    onBottomBarVisibilityChange = { shouldShow ->
                        showBottomBar = shouldShow
                    }
                )
                HomeTab.Chair -> ChairHomeScreen(
                    onBottomBarVisibilityChange = { shouldShow ->
                        showBottomBar = shouldShow
                    }
                )
                HomeTab.Walking -> WalkingHomeScreen(
                    onBottomBarVisibilityChange = { shouldShow ->
                        showBottomBar = shouldShow
                    }
                )
                HomeTab.Progress -> ProgressTabScreen()
                HomeTab.Settings -> TaiChiSettingsScreen(
                    onBottomBarVisibilityChange = { shouldShow ->
                        showBottomBar = shouldShow
                    }
                )
            }

            activePaywallRequest?.let { request ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    PaywallScreen(
                        paywallVariant = request.variant,
                        source = request.paywallSource,
                        sourceDetail = request.source,
                        onClose = dismissActivePaywall,
                        onComplete = dismissActivePaywall
                    )
                }
            }
        }
    }
}

/** Tabs that host the workout flow, which hides the bottom bar while a workout is running. */
private val PRACTICE_TABS = setOf(HomeTab.TaiChi, HomeTab.Chair, HomeTab.Walking)

private val HomeTab.practiceKind: PracticeKind?
    get() = when (this) {
        HomeTab.TaiChi -> PracticeKind.TAI_CHI
        HomeTab.Chair -> PracticeKind.CHAIR
        HomeTab.Walking -> PracticeKind.WALKING
        HomeTab.Progress, HomeTab.Settings -> null
    }

/**
 * Tab order follows iOS ContentView: the program picked in onboarding leads, then the other two
 * practice tabs, then Progress and Settings. Read once per home screen so tabs never reorder
 * under the user.
 */
@HiltViewModel
class HomeTabsViewModel @Inject constructor(
    private val programPreference: ProgramPreference,
    private val remoteConfig: AiAssistantRemoteConfig
) : ViewModel() {
    internal fun visibleTabs(): List<HomeTab> =
        programPreference.practiceTabOrder(remoteConfig.onboardingWalkingExperimentEnabled())
            .map { kind ->
                when (kind) {
                    PracticeKind.TAI_CHI -> HomeTab.TaiChi
                    PracticeKind.CHAIR -> HomeTab.Chair
                    PracticeKind.WALKING -> HomeTab.Walking
                }
            } + listOf(HomeTab.Progress, HomeTab.Settings)
}
