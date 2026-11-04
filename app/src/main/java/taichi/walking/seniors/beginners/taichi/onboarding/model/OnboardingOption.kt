package taichi.walking.seniors.beginners.taichi.onboarding.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingOption(
    val id: String,
    @StringRes val title: Int,
    @StringRes val subtitle: Int? = null,
    val icon: ImageVector? = null,
    @DrawableRes val imageRes: Int? = null
)
