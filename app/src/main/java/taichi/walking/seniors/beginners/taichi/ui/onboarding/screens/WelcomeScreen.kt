package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingSocialProof
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.RatingStars

/**
 * iOS sets the serif lines in New York Medium. Android's system serif (Noto Serif) ships no medium
 * cut and draws Medium as Regular, which reads much lighter; SemiBold lands closest to the mockup.
 */
private val SerifMedium = FontWeight.SemiBold

private object WelcomePalette {
    val page = Color.White
    val ink = Color(0xFF142420)
    val caption = Color(0xFF808682)
    val laurel = Color(0xFFA7B2A4)
    val divider = Color(0xFFE5E4E2)
    val star = Color(0xFFEAB038)
    val buttonStart = Color(0xFF2FA58B)
    val buttonEnd = Color(0xFF67BEA8)
}

/**
 * Converts the iOS mockup's pixels (941 x 1672 canvas) to dp for this screen, as iOS
 * `WelcomeMetrics` does: every size and gap is a fraction of the screen height, horizontal margins
 * scale with the width, so the screen keeps the mockup's proportions on any phone.
 */
private class WelcomeMetrics(
    val width: Dp,
    val height: Dp,
    val statusBar: Dp,
    val navigationBar: Dp
) {
    fun y(px: Float): Dp = height * (px / CANVAS_HEIGHT)
    fun x(px: Float): Dp = width * (px / CANVAS_WIDTH)

    /** 60 px from the top in the mockup, but never under the status bar itself. */
    val topInset: Dp get() = max(statusBar + 2.dp, statusBar + y(36f) - 24.dp)

    /** 48 px from the physical bottom in the mockup; the navigation bar already provides most of it. */
    val bottomInset: Dp get() = max(y(48f), navigationBar + 8.dp)

    companion object {
        const val CANVAS_WIDTH = 941f
        const val CANVAS_HEIGHT = 1672f
    }
}

/**
 * The welcome screen, ported from iOS `WelcomeHeroScreen` (726db15, a51843f, b3ad4b3, 743afca):
 * hero photo drawn from the top edge under the status bar, the "Tai Chi" wordmark clear of it, the
 * promise, a social-proof row, Get Started. The block under the photo is bottom-anchored and the
 * screen scrolls when a small or zoomed display cannot fit it. Android keeps its legal notice under
 * the button (where iOS reserves the existing-member login row).
 */
@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WelcomePalette.page)
    ) {
        val m = WelcomeMetrics(
            width = maxWidth,
            height = maxHeight - navigationBar,
            statusBar = statusBar,
            navigationBar = navigationBar
        )
        val screenHeight = maxHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            WelcomeHeroPhoto(m)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight)
                    .padding(bottom = m.bottomInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_welcome_wordmark),
                    modifier = Modifier.padding(top = m.topInset + m.y(21f)),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = m.y(61f).fixedSp()
                    ),
                    color = WelcomePalette.ink
                )

                Spacer(modifier = Modifier.weight(1f))

                WelcomePromise(
                    m = m,
                    modifier = Modifier.padding(horizontal = m.x(60f))
                )

                WelcomeSocialProofRow(
                    m = m,
                    modifier = Modifier.padding(top = m.y(68f))
                )

                GetStartedButton(
                    m = m,
                    onClick = onContinue,
                    modifier = Modifier
                        .padding(top = m.y(81f))
                        .padding(horizontal = m.x(60f))
                )

                // Shown before any question is asked, so the answers that follow are given on notice
                // of the terms. The full pages are linked from Settings.
                Text(
                    text = stringResource(R.string.onboarding_welcome_legal),
                    style = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
                    color = WelcomePalette.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = m.y(30f))
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }
}

/**
 * The photo at mockup scale: as wide as the screen (wider on tall phones so the fade still ends
 * around the headline), its top 15.4 % above the screen, sky washed to white behind the wordmark.
 */
@Composable
private fun WelcomeHeroPhoto(m: WelcomeMetrics) {
    val aspectRatio = WelcomeMetrics.CANVAS_WIDTH / WelcomeMetrics.CANVAS_HEIGHT
    val photoHeight = max(m.width / aspectRatio, m.height * 0.9f)
    val photoWidth = photoHeight * aspectRatio
    val frameHeight = m.height * 0.75f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(frameHeight)
            .clipToBounds()
    ) {
        Image(
            painter = painterResource(id = R.drawable.welcome_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .wrapContentSize(align = Alignment.TopCenter, unbounded = true)
                .size(width = photoWidth, height = photoHeight)
                .offset(y = -(photoHeight * 0.154f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(m.y(300f))
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(WelcomePalette.page, WelcomePalette.page.copy(alpha = 0.85f), WelcomePalette.page.copy(alpha = 0f))
                    )
                )
        )
        // The photo's own fade ends a shade off pure white; this hides the frame edge.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(frameHeight * 0.3f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(WelcomePalette.page.copy(alpha = 0f), WelcomePalette.page)))
        )
    }
}

/**
 * Two serif lines, the word between asterisks italic (one string per line so a translation picks
 * its own word). Weight first: it is the top motive of the audience (iOS 743afca).
 */
@Composable
private fun WelcomePromise(m: WelcomeMetrics, modifier: Modifier = Modifier) {
    val lines = listOf(
        stringResource(R.string.onboarding_welcome_promise_line_1),
        stringResource(R.string.onboarding_welcome_promise_line_2)
    )
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        lines.forEach { line ->
            Box(modifier = Modifier.height(m.y(89f)), contentAlignment = Alignment.Center) {
                ShrinkToFitText(
                    text = promiseLine(line),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = SerifMedium,
                        fontSize = m.y(84f).fixedSp(),
                        color = WelcomePalette.ink,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

/** `Lose *Weight*`: the starred word regular italic, the rest medium. */
private fun promiseLine(markup: String): AnnotatedString = buildAnnotatedString {
    markup.split('*').forEachIndexed { index, part ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal)) { append(part) }
        } else {
            append(part)
        }
    }
}

/**
 * Three columns with hairline dividers: members, the store rating in laurels, session length. The
 * dividers sit at 31 % and 69 % of the row, as in the mockup.
 */
@Composable
private fun WelcomeSocialProofRow(m: WelcomeMetrics, modifier: Modifier = Modifier) {
    val rowWidth = m.width - m.x(40f) * 2
    val sideWidth = rowWidth * 0.31f

    Row(
        modifier = modifier.width(rowWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WelcomeStat(
            value = OnboardingSocialProof.MEMBERS,
            caption = stringResource(R.string.onboarding_welcome_members_caption),
            m = m,
            modifier = Modifier.width(sideWidth)
        )
        WelcomeDivider(m)
        WelcomeRating(m = m, modifier = Modifier.weight(1f))
        WelcomeDivider(m)
        WelcomeStat(
            value = stringResource(R.string.onboarding_welcome_minutes_value),
            caption = stringResource(R.string.onboarding_welcome_minutes_caption),
            m = m,
            modifier = Modifier.width(sideWidth)
        )
    }
}

@Composable
private fun WelcomeDivider(m: WelcomeMetrics) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(m.y(80f))
            .background(WelcomePalette.divider)
    )
}

@Composable
private fun WelcomeStat(value: String, caption: String, m: WelcomeMetrics, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(m.y(6f))
    ) {
        ShrinkToFitText(
            text = AnnotatedString(value),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = SerifMedium,
                fontSize = m.y(50f).fixedSp(),
                color = WelcomePalette.ink
            )
        )
        ShrinkToFitText(
            text = AnnotatedString(caption),
            style = TextStyle(fontSize = max(m.y(24f), 12.dp).fixedSp(), color = WelcomePalette.caption)
        )
    }
}

/** iOS shows the App Store rating here; Android shows the Google Play one (see OnboardingSocialProof). */
@Composable
private fun WelcomeRating(m: WelcomeMetrics, modifier: Modifier = Modifier) {
    val smallText = max(m.y(22f), 11.dp)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(m.x(10f), Alignment.CenterHorizontally)
    ) {
        Laurel(R.drawable.ic_laurel_left, m)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(m.y(4f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(m.x(6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Shop,
                    contentDescription = null,
                    tint = WelcomePalette.caption,
                    modifier = Modifier.size(smallText)
                )
                Text(
                    text = stringResource(R.string.onboarding_discovery_google_play),
                    style = TextStyle(fontSize = smallText.fixedSp(), color = WelcomePalette.caption),
                    maxLines = 1,
                    softWrap = false
                )
            }
            Text(
                text = stringResource(R.string.onboarding_welcome_rating_format, OnboardingSocialProof.formattedPlayRating()),
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = SerifMedium,
                    fontSize = m.y(34f).fixedSp(),
                    color = WelcomePalette.ink
                ),
                maxLines = 1,
                softWrap = false
            )
            RatingStars(
                filled = OnboardingSocialProof.filledStars,
                starSize = max(m.y(22f), 10.dp),
                color = WelcomePalette.star,
                spacing = m.x(3f)
            )
        }

        Laurel(R.drawable.ic_laurel_right, m)
    }
}

@Composable
private fun Laurel(resource: Int, m: WelcomeMetrics) {
    Image(
        painter = painterResource(id = resource),
        contentDescription = null,
        colorFilter = ColorFilter.tint(WelcomePalette.laurel),
        modifier = Modifier.size(width = m.y(40f), height = m.y(80f))
    )
}

@Composable
private fun GetStartedButton(m: WelcomeMetrics, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val height = max(m.y(118f), 56.dp)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = WelcomePalette.buttonStart.copy(alpha = 0.28f),
                spotColor = WelcomePalette.buttonStart.copy(alpha = 0.28f)
            )
            .background(
                Brush.horizontalGradient(listOf(WelcomePalette.buttonStart, WelcomePalette.buttonEnd)),
                CircleShape
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White)
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_get_started),
            style = TextStyle(fontSize = max(m.y(36f), 18.dp).fixedSp(), fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * One line that shrinks (down to 70 %) rather than wrapping or clipping, like SwiftUI's
 * `lineLimit(1).minimumScaleFactor(0.7)`; long translations of the promise need it.
 */
@Composable
private fun ShrinkToFitText(text: AnnotatedString, style: TextStyle, minScale: Float = 0.7f) {
    var scale by remember(text, style) { mutableFloatStateOf(1f) }
    Text(
        text = text,
        style = style.copy(fontSize = style.fontSize * scale),
        maxLines = 1,
        softWrap = false,
        onTextLayout = { layout ->
            if (layout.hasVisualOverflow && scale > minScale) {
                scale = (scale - 0.05f).coerceAtLeast(minScale)
            }
        }
    )
}

/**
 * The mockup's sizes are fractions of the screen, so text is sized in dp and does not grow again
 * with the system font scale (iOS uses fixed point sizes on this screen too).
 */
@Composable
private fun Dp.fixedSp(): TextUnit = with(LocalDensity.current) { this@fixedSp.toSp() }

