package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import taichi.walking.seniors.beginners.R

/**
 * The exercise video laid out the way iOS does it (WorkoutView.videoSize): as wide as the screen
 * in portrait, as tall as the screen in landscape, centred, over the gray studio backdrop.
 *
 * The Android rendition has that backdrop baked in behind the instructor (Android cannot decode
 * the transparent iOS videos). To make the frame's edges disappear, the backdrop drawn here uses
 * the exact geometry the backend's encoder used (scripts/hls_rendition_android): a square centred
 * on the video, side = max(videoWidth * 19.5 / 9, videoHeight). Both scale with the video, so
 * they line up on every screen size.
 */
@Composable
fun WorkoutVideoStage(
    videoAspectRatio: Float,
    modifier: Modifier = Modifier,
    video: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BACKDROP_FALLBACK)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val videoSize = videoSize(videoAspectRatio)
        val side = maxOf(videoSize.width * BACKDROP_ASPECT, videoSize.height)
        Image(
            painter = painterResource(R.drawable.workout_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.requiredSize(side, side)
        )
        Box(modifier = Modifier.requiredSize(videoSize)) {
            video()
        }
    }
}

private fun BoxWithConstraintsScope.videoSize(aspect: Float): DpSize {
    val ratio = aspect.takeIf { it.isFinite() && it > 0f } ?: DEFAULT_VIDEO_ASPECT
    return if (maxWidth > maxHeight) {
        DpSize(maxHeight * ratio, maxHeight)
    } else {
        DpSize(maxWidth, maxWidth / ratio)
    }
}

/** Must match `backdropAspect` in the backend's transcode_android.swift. */
private const val BACKDROP_ASPECT = 19.5f / 9f

/** iOS's default until the player reports the real size. */
const val DEFAULT_VIDEO_ASPECT = 9f / 16f

/** iOS's videoBackdropFallbackColor, shown only where the backdrop image does not reach. */
private val BACKDROP_FALLBACK = Color(0xFF8E9192)
