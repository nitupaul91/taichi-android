package taichi.walking.seniors.beginners.taichi.onboarding.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingOption(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    @DrawableRes val imageRes: Int? = null
)
