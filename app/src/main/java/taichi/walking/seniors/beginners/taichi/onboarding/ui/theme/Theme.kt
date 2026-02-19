package taichi.walking.seniors.beginners.taichi.onboarding.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TaiChiGreen,
    onPrimary = TaiChiSurface,
    secondary = TaiChiBlue,
    onSecondary = TaiChiSurface,
    background = TaiChiBackground,
    onBackground = TaiChiTextPrimary,
    surface = TaiChiSurface,
    onSurface = TaiChiTextPrimary,
    outline = TaiChiBorder
)

private val DarkColors = darkColorScheme(
    primary = TaiChiGreen,
    onPrimary = TaiChiSurface,
    secondary = TaiChiBlue,
    onSecondary = TaiChiSurface,
    background = Color(0xFF0F1C1A),
    onBackground = TaiChiSurface,
    surface = Color(0xFF162321),
    onSurface = TaiChiSurface,
    outline = TaiChiBorder
)

@Composable
fun TaiChiTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = TaiChiTypography,
        shapes = TaiChiShapes,
        content = content
    )
}
