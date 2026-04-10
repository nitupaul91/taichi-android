package taichi.walking.seniors.beginners.taichi.ui.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.taichi.ui.onboarding.screens.PaywallScreen
import taichi.walking.seniors.beginners.ui.rateapp.openPlayStore
import taichi.walking.seniors.beginners.util.sharePlainText

@Composable
fun TaiChiSettingsScreen(
    viewModel: TaiChiSettingsViewModel = hiltViewModel(),
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPaywall by remember { mutableStateOf(false) }
    val isPremium by viewModel.isPremium.collectAsState()

    LaunchedEffect(showPaywall) {
        onBottomBarVisibilityChange(!showPaywall)
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(top = statusBarPadding + 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A)
        )

        SectionLabel("SUBSCRIPTION")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.WorkspacePremium,
                iconColor = if (isPremium) Color(0xFF0BA56B) else Color(0xFFF5A623),
                iconBackground = if (isPremium) Color(0xFFE6F5EE) else Color(0xFFF9EFD9),
                title = if (isPremium) "Premium" else "Upgrade to Premium",
                subtitle = if (isPremium) "You have full access" else "Unlock all features",
                showChevron = !isPremium,
                onClick = { if (!isPremium) showPaywall = true }
            )
        }

        SectionLabel("COMMUNITY")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.Share,
                iconColor = Color(0xFF0BA56B),
                iconBackground = Color(0xFFE6F5EE),
                title = "Share App",
                subtitle = "Tell friends about Tai Chi",
                onClick = {
                    sharePlainText(
                        context = context,
                        text = "Try this Tai Chi app \uD83E\uDDD8\n${context.getString(taichi.walking.seniors.beginners.R.string.app_link)}",
                        title = "Share App"
                    )
                }
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Default.Star,
                iconColor = Color(0xFFF5A623),
                iconBackground = Color(0xFFFDF1DA),
                title = "Rate App",
                subtitle = "Leave a review on Google Play",
                onClick = { openPlayStore(context) }
            )
        }

        SectionLabel("ABOUT")
        SettingsCard {
            SettingsLinkRow(
                icon = Icons.Default.Description,
                iconColor = Color(0xFF78909C),
                iconBackground = Color(0xFFE8EDF1),
                title = "Terms of Service",
                onClick = { uriHandler.openUri("https://easytaichi.app/terms.html") }
            )
            DividerRow()
            SettingsLinkRow(
                icon = Icons.Default.PrivacyTip,
                iconColor = Color(0xFF78909C),
                iconBackground = Color(0xFFE8EDF1),
                title = "Privacy Policy",
                onClick = { uriHandler.openUri("https://easytaichi.app/privacy_policy.html") }
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Default.Email,
                iconColor = Color(0xFF5C6BC0),
                iconBackground = Color(0xFFE6E8F8),
                title = "Contact Us",
                subtitle = "Send feedback or report issues",
                onClick = { uriHandler.openUri(viewModel.buildContactEmailUri()) }
            )
            DividerRow()
            SettingsRow(
                icon = Icons.Default.Info,
                iconColor = Color(0xFF78909C),
                iconBackground = Color(0xFFE8EDF1),
                title = "App Version",
                subtitle = viewModel.appVersion,
                showChevron = false
            )
        }
    }

    if (showPaywall) {
        PaywallScreen(
            onClose = { showPaywall = false },
            onComplete = { showPaywall = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        ),
        color = Color(0xFF7D8A8F)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(vertical = 2.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBackground, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.size(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F3C42)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7D8A8F)
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFBBC5C9),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SettingsLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBackground, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.size(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2F3C42),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            tint = Color(0xFFBBC5C9),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp),
        color = Color(0xFFE4E8EA)
    )
}
