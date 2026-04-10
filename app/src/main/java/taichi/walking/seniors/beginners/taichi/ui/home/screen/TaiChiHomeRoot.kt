package taichi.walking.seniors.beginners.taichi.ui.home.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import taichi.walking.seniors.beginners.taichi.ui.progress.screen.ProgressTabScreen
import taichi.walking.seniors.beginners.taichi.ui.settings.screen.TaiChiSettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DirectionsWalk
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

private enum class HomeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TaiChi("Tai Chi", Icons.Default.AccessibilityNew),
    Walking("Walking", Icons.Default.DirectionsWalk),
    Progress("Progress", Icons.Default.ShowChart),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun TaiChiHomeRoot(
    pendingPaywallRequest: PaywallLaunchRequest? = null,
    onPaywallRequestConsumed: () -> Unit = {}
) {
    var selected by remember { mutableStateOf(HomeTab.TaiChi) }
    var showBottomBar by remember { mutableStateOf(true) }
    var activePaywallRequest by remember { mutableStateOf<PaywallLaunchRequest?>(null) }

    LaunchedEffect(selected) {
        if (selected != HomeTab.TaiChi && selected != HomeTab.Walking) showBottomBar = true
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
                    HomeTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
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
                        onClose = dismissActivePaywall,
                        onComplete = dismissActivePaywall
                    )
                }
            }
        }
    }
}
