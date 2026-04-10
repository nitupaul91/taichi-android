package taichi.walking.seniors.beginners.taichi.ui.home.workout

import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player.Listener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import taichi.walking.seniors.beginners.taichi.ui.video.buildAppExoPlayer
import taichi.walking.seniors.beginners.taichi.ui.video.inflateAppPlayerView
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto

@Composable
fun WorkoutScreen(
    dayNumber: Int,
    dayData: WorkoutDayDto,
    onClose: () -> Unit,
    onComplete: () -> Unit
) {
    val model = remember(dayData.id) { WorkoutViewModel(dayNumber, dayData) }
    val context = LocalContext.current
    var cuePlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val currentExercise = model.currentExercise
    val isPlaying = model.isVideoPlaying
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val lifecycleOwner = LocalLifecycleOwner.current

    val activity = context as? ComponentActivity
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                model.pauseForBackground()
                cuePlayer?.release()
                cuePlayer = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cuePlayer?.release()
            cuePlayer = null
        }
    }

    LaunchedEffect(dayData.id) {
        model.start(
            onTick = { },
            onComplete = onComplete
        )
    }

    LaunchedEffect(model.audioCueEvent?.token) {
        val cueEvent = model.audioCueEvent ?: return@LaunchedEffect
        cuePlayer?.release()
        cuePlayer = MediaPlayer.create(context, cueEvent.cue.resId)?.apply {
            setOnCompletionListener { player ->
                player.release()
                if (cuePlayer === player) {
                    cuePlayer = null
                }
            }
            start()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val currentVideo = currentExercise?.videoUrl
        val currentInstructionAudio = if (model.isShowingPreview) {
            null
        } else {
            currentExercise?.stepsSpeechUrl
                ?: currentExercise?.hintSpeechUrl
                ?: currentExercise?.nameSpeechUrl
        }
        if (currentVideo != null) {
            WorkoutVideoPlayer(
                videoUrl = currentVideo,
                instructionAudioUrl = currentInstructionAudio,
                isPlaying = isPlaying,
                isMuted = !model.isAudioEnabled,
                resetToken = model.videoResetTrigger,
                onInstructionDurationResolved = model::updateInstructionAudioDuration
            )
        }

        // Tap anywhere on the video layer to pause/resume.
        if (!model.isShowingPreview) {
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
        }

        // Play icon overlay when paused
        if (model.isPaused && !model.isShowingPreview) {
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
                Text(
                    text = "Exercise ${model.currentExerciseIndex + 1}/${dayData.exercises.size}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
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

            Spacer(modifier = Modifier.height(20.dp))

            if (model.isShowingPreview) {
                Text(
                    text = if (model.currentExerciseIndex == 0) "Let's Begin" else "UP NEXT",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentExercise?.name ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (model.previewCountdown > 0) model.previewCountdown.toString() else " ",
                    color = Color.White,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                )
            } else {
                Text(
                    text = formatTime(
                        remainingSecondsForCurrentExercise(
                            currentExercise,
                            model.exerciseElapsedSeconds
                        )
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentExercise?.name ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (model.isShowingPreview) {
                Spacer(modifier = Modifier.weight(1f))
                PreviewOverlay(
                    onStart = { model.finishPreview(onTick = { }, onComplete = onComplete) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIcon(icon = Icons.Default.FastRewind) {
                        model.previousOrRestart(onTick = { }, onComplete = onComplete)
                    }
                    CircleIcon(
                        icon = if (model.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        size = 80.dp
                    ) {
                        model.togglePause()
                    }
                    CircleIcon(icon = Icons.Default.FastForward) {
                        val done = model.nextExercise(onTick = { }, onComplete = onComplete)
                        if (done) onComplete()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private fun remainingSecondsForCurrentExercise(
    exercise: taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDto?,
    elapsedSeconds: Int
): Int {
    return ((exercise?.duration ?: 0) - elapsedSeconds).coerceAtLeast(0)
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@Composable
private fun PreviewOverlay(
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(
            onClick = onStart,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0BA56B),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Start",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
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
    resetToken: Int,
    onInstructionDurationResolved: (Int?) -> Unit
) {
    val playerState = rememberUpdatedState(isPlaying)
    val mutedState = rememberUpdatedState(isMuted)
    val context = LocalContext.current

    val player = remember(videoUrl, resetToken) {
        buildAppExoPlayer(context).apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = isPlaying
            // Exercise guidance comes from backend MP3, keep video track silent.
            volume = 0f
        }
    }

    val instructionAudioPlayer = remember(instructionAudioUrl, resetToken) {
        if (instructionAudioUrl.isNullOrBlank()) {
            null
        } else {
            buildAppExoPlayer(context).apply {
                setMediaItem(MediaItem.fromUri(instructionAudioUrl))
                prepare()
                playWhenReady = isPlaying
                volume = if (isMuted) 0f else 1f
            }
        }
    }

    LaunchedEffect(instructionAudioUrl, resetToken) {
        if (instructionAudioUrl.isNullOrBlank()) {
            onInstructionDurationResolved(null)
        }
    }

    LaunchedEffect(playerState.value) {
        if (playerState.value) {
            player.playWhenReady = true
            player.play()
            instructionAudioPlayer?.playWhenReady = true
            instructionAudioPlayer?.play()
        } else {
            player.playWhenReady = false
            player.pause()
            instructionAudioPlayer?.playWhenReady = false
            instructionAudioPlayer?.pause()
        }
    }

    LaunchedEffect(mutedState.value) {
        instructionAudioPlayer?.volume = if (mutedState.value) 0f else 1f
    }

    DisposableEffect(instructionAudioPlayer) {
        if (instructionAudioPlayer == null) {
            onDispose { }
        } else {
            val listener = object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val durationMs = instructionAudioPlayer.duration
                        val durationSeconds = if (durationMs > 0L) {
                            kotlin.math.ceil(durationMs / 1000.0).toInt()
                        } else {
                            null
                        }
                        onInstructionDurationResolved(durationSeconds)
                    }
                }
            }
            instructionAudioPlayer.addListener(listener)
            onDispose {
                instructionAudioPlayer.removeListener(listener)
            }
        }
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
            inflateAppPlayerView(ctx).apply {
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
