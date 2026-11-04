package taichi.walking.seniors.beginners.taichi.onboarding.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Five stars filled up to [filled] (fractions draw part of a star), so a rating that is not 5
 * does not show five full stars.
 */
@Composable
fun RatingStars(
    filled: Double,
    starSize: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    spacing: Dp = 3.dp,
    emptyColor: Color = color.copy(alpha = 0.28f)
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(5) { index ->
            val fraction = (filled - index).coerceIn(0.0, 1.0).toFloat()
            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = emptyColor,
                    modifier = Modifier.size(starSize)
                )
                if (fraction > 0f) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(starSize)
                            .drawWithContent {
                                clipRect(right = size.width * fraction) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    )
                }
            }
        }
    }
}
