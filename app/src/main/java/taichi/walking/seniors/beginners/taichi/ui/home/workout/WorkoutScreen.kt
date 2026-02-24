package taichi.walking.seniors.beginners.taichi.ui.home.workout

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto

@Composable
fun WorkoutScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    onClose: () -> Unit,
    onComplete: () -> Unit
) {
    val model = remember(dayData.id) { WorkoutViewModel(dayNumber, dayData) }

    var playerReset by remember { mutableIntStateOf(0) }

    val currentExercise = model.currentExercise
    val isPlaying = model.isVideoPlaying
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    val activity = LocalContext.current as? ComponentActivity
    DisposableEffect(Unit) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        onDispose {
            model.stop()
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            )
        }
    }

    LaunchedEffect(dayData.id) {
        model.startTimer(
            onTick = { },
            onComplete = onComplete
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val currentVideo = currentExercise?.videoUrl
        val currentInstructionAudio = currentExercise?.stepsSpeechUrl
            ?: currentExercise?.hintSpeechUrl
            ?: currentExercise?.nameSpeechUrl
        if (currentVideo != null) {
            WorkoutVideoPlayer(
                videoUrl = currentVideo,
                instructionAudioUrl = currentInstructionAudio,
                isPlaying = isPlaying,
                isMuted = !model.isAudioEnabled,
                resetToken = playerReset
            )
        }

        // Tap anywhere on the video layer to pause/resume.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    model.toggleVideoPlayback()
                }
        )

        // Play icon overlay when paused
        if (model.isPaused) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            model.toggleVideoPlayback()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Gradient overlays for top and bottom controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding.calculateTopPadding())
                .padding(bottom = navBarPadding.calculateBottomPadding())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIcon(icon = Icons.Default.Close) { onClose() }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Day $dayNumber", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Exercise ${model.currentExerciseIndex + 1} of ${dayData.exercises.size}",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                CircleIcon(icon = if (model.isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff) {
                    model.toggleVoiceGuidance()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Instagram-style dotted progress bar
            InstagramStyleProgress(
                totalSegments = dayData.exercises.size,
                currentSegment = model.currentExerciseIndex,
                segmentProgress = model.currentExerciseProgress,
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = currentExercise?.name ?: "",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIcon(icon = Icons.Default.FastRewind) {
                    model.previousOrRestart()
                    playerReset++
                }
                CircleIcon(
                    icon = if (model.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    size = 80.dp
                ) {
                    model.togglePause()
                }
                CircleIcon(icon = Icons.Default.FastForward) {
                    val done = model.nextExercise()
                    playerReset++
                    if (done) onComplete()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InstagramStyleProgress(
    totalSegments: Int,
    currentSegment: Int,
    segmentProgress: Float,
    modifier: Modifier = Modifier
) {
    val progressColor = Color(0xFF0BA56B)
    val trackColor = Color.Gray.copy(alpha = 0.5f)
    val segmentGap = 4.dp

    Canvas(modifier = modifier) {
        val totalGapWidth = segmentGap.toPx() * (totalSegments - 1)
        val segmentWidth = (size.width - totalGapWidth) / totalSegments
        val segmentHeight = size.height
        val cornerRadius = CornerRadius(segmentHeight / 2, segmentHeight / 2)

        for (i in 0 until totalSegments) {
            val startX = i * (segmentWidth + segmentGap.toPx())

            // Draw track (dotted/dashed style)
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(startX, 0f),
                size = Size(segmentWidth, segmentHeight),
                cornerRadius = cornerRadius
            )

            // Draw progress
            val progress = when {
                i < currentSegment -> 1f
                i == currentSegment -> segmentProgress
                else -> 0f
            }

            if (progress > 0f) {
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(startX, 0f),
                    size = Size(segmentWidth * progress, segmentHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

@Composable
private fun WorkoutVideoPlayer(
    videoUrl: String,
    instructionAudioUrl: String?,
    isPlaying: Boolean,
    isMuted: Boolean,
    resetToken: Int
) {
    val playerState = rememberUpdatedState(isPlaying)
    val mutedState = rememberUpdatedState(isMuted)
    val context = LocalContext.current

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = isPlaying
            // Exercise guidance comes from backend MP3, keep video track silent.
            volume = 0f
        }
    }

    val instructionAudioPlayer = remember(instructionAudioUrl) {
        if (instructionAudioUrl.isNullOrBlank()) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(instructionAudioUrl))
                prepare()
                playWhenReady = isPlaying
                volume = if (isMuted) 0f else 1f
            }
        }
    }

    LaunchedEffect(resetToken) {
        player.seekTo(0)
        instructionAudioPlayer?.seekTo(0)
    }

    LaunchedEffect(playerState.value) {
        if (playerState.value) {
            player.play()
            instructionAudioPlayer?.play()
        } else {
            player.pause()
            instructionAudioPlayer?.pause()
        }
    }

    LaunchedEffect(mutedState.value) {
        instructionAudioPlayer?.volume = if (mutedState.value) 0f else 1f
    }

    DisposableEffect(player, instructionAudioPlayer) {
        onDispose {
            player.release()
            instructionAudioPlayer?.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        },
        update = { view ->
            view.player = player
        }
    )
}

@Composable
private fun CircleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp = 52.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(if (size > 60.dp) 36.dp else 28.dp)
        )
    }
}
