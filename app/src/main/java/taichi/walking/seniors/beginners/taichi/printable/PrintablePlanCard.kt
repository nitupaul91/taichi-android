package taichi.walking.seniors.beginners.taichi.printable

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind

private val BrandGreen = Color(0xFF0BA56B)
private val CardGradient = listOf(Color(0xFF0BA56B), Color(0xFF2AB598), Color(0xFF56CFAF))
private val CrownGold = Color(0xFFFFD700)
private val CardShape = RoundedCornerShape(22.dp)

/**
 * Home-screen card for the printable 7-day plan (iOS `PrintablePlanCardView`). Renders nothing
 * unless the remote flag is on; see [printablePlanCardMode] for who sees which version.
 */
@Composable
fun PrintablePlanCard(
    plan: WorkoutPlanDto,
    practiceKind: PracticeKind,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrintablePlanViewModel = hiltViewModel(key = "printable_plan_${practiceKind.storageValue}")
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.refreshMode() }

    LaunchedEffect(state.generationFailed) {
        if (state.generationFailed) {
            Toast.makeText(context, R.string.printable_plan_generation_failed, Toast.LENGTH_LONG).show()
            viewModel.onGenerationFailureShown()
        }
    }

    state.readyPdf?.let { file ->
        PrintablePlanReadyDialog(
            onPrint = {
                if (!file.isFile) {
                    viewModel.onReadyFileMissing(plan, practiceKind)
                } else {
                    PrintablePlanDelivery.print(context, file, context.getString(documentNameRes(practiceKind)))
                    viewModel.onPrint(practiceKind)
                }
            },
            onShare = {
                if (!file.isFile) {
                    viewModel.onReadyFileMissing(plan, practiceKind)
                } else {
                    PrintablePlanDelivery.share(context, file, context.getString(R.string.printable_plan_share_chooser_title))
                    viewModel.onShare(practiceKind)
                }
            },
            onDismiss = viewModel::onReadyDismissed
        )
    }

    if (state.mode == PrintablePlanCardMode.HIDDEN) return

    PrintablePlanCardContent(
        isUnlock = state.mode == PrintablePlanCardMode.UNLOCK,
        isBusy = state.isGenerating,
        onTap = { viewModel.onCardTap(plan, practiceKind, onOpenPaywall) },
        modifier = modifier
    )
}

@Composable
private fun PrintablePlanCardContent(
    isUnlock: Boolean,
    isBusy: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "printablePlanCardScale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 14.dp,
                shape = CardShape,
                ambientColor = BrandGreen.copy(alpha = 0.25f),
                spotColor = BrandGreen.copy(alpha = 0.25f)
            )
            .clip(CardShape)
            .background(Brush.horizontalGradient(CardGradient))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isBusy,
                role = Role.Button,
                onClick = onTap
            )
            .defaultMinSize(minHeight = 200.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 130.dp, y = (-60).dp)
                .size(180.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint = CrownGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.printable_plan_card_badge),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = stringResource(R.string.printable_plan_card_title),
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(
                        if (isUnlock) R.string.printable_plan_card_benefit_unlock else R.string.printable_plan_card_benefit_download
                    ),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                if (isBusy) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(24.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isUnlock) Icons.Filled.LockOpen else Icons.Filled.Download,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(
                                if (isUnlock) R.string.printable_plan_card_action_unlock else R.string.printable_plan_card_action_download
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(52.dp)
                    .rotate(-8f)
            )
        }
    }
}

/** Offers print (system dialog, which can also save a PDF) or share for the generated plan. */
@Composable
private fun PrintablePlanReadyDialog(
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.printable_plan_ready_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.printable_plan_ready_message),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF6A7A80),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    DialogActionLabel(Icons.Filled.Print, stringResource(R.string.printable_plan_action_print))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onShare,
                    border = BorderStroke(1.5.dp, BrandGreen),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    DialogActionLabel(Icons.Filled.Share, stringResource(R.string.printable_plan_action_share))
                }
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = stringResource(R.string.close), color = Color(0xFF6A7A80), fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun DialogActionLabel(icon: ImageVector, text: String) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}
