package taichi.walking.seniors.beginners.taichi.ui.video

import android.content.Context
import android.view.LayoutInflater
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import taichi.walking.seniors.beginners.R

fun buildAppExoPlayer(context: Context): ExoPlayer {
    val renderersFactory = DefaultRenderersFactory(context)
        .setEnableDecoderFallback(true)

    return ExoPlayer.Builder(context, renderersFactory).build()
}

fun inflateAppPlayerView(context: Context): PlayerView {
    return LayoutInflater.from(context)
        .inflate(R.layout.view_app_player, null, false) as PlayerView
}
