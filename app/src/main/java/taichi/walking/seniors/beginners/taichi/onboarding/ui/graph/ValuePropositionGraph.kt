package taichi.walking.seniors.beginners.taichi.onboarding.ui.graph

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun ValuePropositionGraph(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val animation = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animation.snapTo(0f)
        animation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            if (data.size < 2) return@Canvas

            val maxValue = data.maxOrNull() ?: 1f
            val minValue = data.minOrNull() ?: 0f
            val range = max(1f, maxValue - minValue)
            val stepX = size.width / (data.size - 1)

            val fullPath = Path()
            data.forEachIndexed { index, value ->
                val x = stepX * index
                val normalized = (value - minValue) / range
                val y = size.height - normalized * (size.height * 0.84f) - 10f

                if (index == 0) {
                    fullPath.moveTo(x, y)
                } else {
                    val prevX = stepX * (index - 1)
                    val prevValue = data[index - 1]
                    val prevNorm = (prevValue - minValue) / range
                    val prevY = size.height - prevNorm * (size.height * 0.84f) - 10f
                    val c1 = Offset(prevX + stepX / 2f, prevY)
                    val c2 = Offset(x - stepX / 2f, y)
                    fullPath.cubicTo(c1.x, c1.y, c2.x, c2.y, x, y)
                }
            }

            val pathMeasure = PathMeasure()
            pathMeasure.setPath(fullPath, false)
            val shownPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animation.value, shownPath, true)

            val fillPath = Path().apply {
                addPath(shownPath)
                lineTo(size.width * animation.value, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF3BC9A7).copy(alpha = 0.35f),
                        androidx.compose.ui.graphics.Color(0xFF3B7FC9).copy(alpha = 0.12f),
                        androidx.compose.ui.graphics.Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height
                )
            )

            drawPath(
                path = shownPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF2DBA9E),
                        androidx.compose.ui.graphics.Color(0xFF4E7DBA)
                    )
                ),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
