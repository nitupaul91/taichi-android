package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.DiscoverySources
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingAction
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.LocalOnboardingChrome
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.OnboardingQuestions
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.rememberContinueOnSelection

/** How a source's row is drawn: iOS `DiscoverySource.assetName` / `iconSystemName` / colours. */
private data class DiscoverySourceStyle(
    @StringRes val title: Int,
    @DrawableRes val brandIcon: Int? = null,
    val icon: ImageVector? = null,
    val isBrand: Boolean = brandIcon != null,
    val iconBackground: Color = Color.White,
    val iconTint: Color = Color(0xFF1F262B)
)

private fun styleFor(sourceId: String): DiscoverySourceStyle = when (sourceId) {
    // iOS shows Apple's logo for the App Store; the Play Store bag is the Android counterpart.
    DiscoverySources.GOOGLE_PLAY -> DiscoverySourceStyle(R.string.onboarding_discovery_google_play, icon = Icons.Default.Shop, isBrand = true)
    DiscoverySources.GOOGLE -> DiscoverySourceStyle(R.string.onboarding_discovery_google, brandIcon = R.drawable.ic_discovery_google)
    DiscoverySources.TIKTOK -> DiscoverySourceStyle(R.string.onboarding_discovery_tiktok, brandIcon = R.drawable.ic_discovery_tiktok)
    DiscoverySources.INSTAGRAM -> DiscoverySourceStyle(R.string.onboarding_discovery_instagram, brandIcon = R.drawable.ic_discovery_instagram)
    DiscoverySources.FACEBOOK -> DiscoverySourceStyle(R.string.onboarding_discovery_facebook, brandIcon = R.drawable.ic_discovery_facebook)
    DiscoverySources.TV_COMMERCIAL -> DiscoverySourceStyle(R.string.onboarding_discovery_tv_commercial, icon = Icons.Default.LiveTv)
    DiscoverySources.TRAINER_THERAPIST -> DiscoverySourceStyle(R.string.onboarding_discovery_trainer_therapist, icon = Icons.Default.MedicalServices)
    DiscoverySources.FRIENDS_FAMILY -> DiscoverySourceStyle(R.string.onboarding_discovery_friends_family, icon = Icons.Default.People)
    else -> DiscoverySourceStyle(
        R.string.onboarding_discovery_other,
        icon = Icons.Default.MoreHoriz,
        iconBackground = Color(0xFFEEF2F3),
        iconTint = Color(0xFF6A7B82)
    )
}

/**
 * iOS `HeardAboutUsScreen`: [sources] arrive already in this user's shuffled order (see
 * [DiscoverySources.displayedSources]). Every row carries its logo or icon regardless of
 * `onboarding_option_icons_enabled`, as on iOS.
 */
@Composable
fun HeardAboutUsScreen(
    state: OnboardingState,
    sources: List<String>,
    onSelect: (OnboardingAction) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val continueOnSelection = rememberContinueOnSelection(onContinue)

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.HeardAboutUs),
        title = stringResource(R.string.onboarding_heard_about_us_title),
        subtitle = stringResource(R.string.onboarding_heard_about_us_subtitle),
        onBack = onBack,
        showContinueButton = !LocalOnboardingChrome.current.hideContinueOnSingleChoice,
        continueEnabled = state.discoverySourceId != null,
        onContinue = onContinue
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            sources.forEach { sourceId ->
                DiscoverySourceRow(
                    style = styleFor(sourceId),
                    isSelected = state.discoverySourceId == sourceId,
                    onClick = {
                        onSelect(OnboardingAction.SingleSelect(OnboardingQuestions.HEARD_ABOUT_US, sourceId))
                        continueOnSelection()
                    }
                )
            }
        }
    }
}

@Composable
private fun DiscoverySourceRow(
    style: DiscoverySourceStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = if (isSelected) primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f),
                spotColor = if (isSelected) primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f)
            )
            .clip(shape)
            .background(if (isSelected) primary else Color.White)
            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE8EEF0), shape)
            .clickable(onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(style.iconBackground)
                .border(1.dp, if (style.isBrand) Color(0xFFDCE3E6) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                style.brandIcon != null -> Icon(
                    painter = painterResource(id = style.brandIcon),
                    contentDescription = null,
                    tint = style.iconTint,
                    modifier = Modifier.size(20.dp)
                )
                style.icon != null -> Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(style.title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, if (isSelected) Color.White else Color(0xFFE8EEF0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}
