package taichi.walking.seniors.beginners.taichi.ui.video

import android.content.Context
import android.view.LayoutInflater
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import taichi.walking.seniors.beginners.R

/**
 * @param cached routes media through [WorkoutMediaCache] so playback works offline and watching a
 *   workout populates the cache — workout videos, speech audio and background music all use it.
 */
fun buildAppExoPlayer(context: Context, cached: Boolean = false): ExoPlayer {
    val renderersFactory = DefaultRenderersFactory(context)
        .setEnableDecoderFallback(true)

    val builder = ExoPlayer.Builder(context, renderersFactory)
    if (cached) {
        builder.setMediaSourceFactory(
            DefaultMediaSourceFactory(WorkoutMediaCache.dataSourceFactory(context))
        )
    }
    return builder.build()
}

fun inflateAppPlayerView(context: Context): PlayerView {
    return LayoutInflater.from(context)
        .inflate(R.layout.view_app_player, null, false) as PlayerView
}
