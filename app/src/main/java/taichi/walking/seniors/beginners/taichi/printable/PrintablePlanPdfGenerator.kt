package taichi.walking.seniors.beginners.taichi.printable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.MetricAffectingSpan
import androidx.annotation.StringRes
import androidx.core.os.ConfigurationCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutDayDto
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeTitleService
import java.io.OutputStream
import java.text.NumberFormat
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Renders the printable 7-day plan, a port of iOS `PrintablePlanPDFGenerator.swift`.
 *
 * Same US Letter page in PostScript points, colors and page order: cover, about, how-to, one page
 * per three exercises of each day, an achievement page after days 1, 3 and 7, and a tracker. All
 * coordinates below are iOS's, so a layout change on either platform can be mirrored line by line.
 *
 * Images: PdfDocument stores bitmaps losslessly, so thumbnails are cropped to the visible frame and
 * sized near iOS's effective resolution (~100 dpi) rather than embedded whole.
 */
class PrintablePlanPdfGenerator(
    private val context: Context,
    private val imageSource: PrintablePlanImageSource
) {

    suspend fun writePdf(plan: WorkoutPlanDto, kind: PracticeKind, output: OutputStream) =
        withContext(Dispatchers.Default) {
            val days = plan.allDays.take(PLAN_DAYS)
            check(days.isNotEmpty()) { "No workout days available" }

            val logo = decodeResourceBitmap(context, R.drawable.printable_plan_logo, LOGO_MAX_PX)
            val cover = decodeResourceBitmap(context, R.drawable.printable_plan_cover, COVER_MAX_PX)
                ?.let { decoded ->
                    centerCrop(decoded, px(PAGE_WIDTH, COVER_PX_PER_PT), px(PAGE_HEIGHT, COVER_PX_PER_PT))
                        .also { decoded.recycle() }
                }
            val document = PdfDocument()
            try {
                val pages = PageSink(document)
                pages.add { renderCover(it, kind, cover, logo) }
                pages.add { renderAbout(it, kind, logo) }
                pages.add { renderHowTo(it, kind, logo) }

                days.forEachIndexed { index, day ->
                    val dayNumber = index + 1
                    // Fetched a day at a time: a plan holds ~80 thumbnails, too many to keep in memory.
                    val images = prefetchImages(day)
                    try {
                        renderDay(pages, dayNumber, day, images, logo)
                    } finally {
                        images.values.forEach(Bitmap::recycle)
                    }
                    if (PracticeTitleService.shouldUnlockBadge(dayNumber)) {
                        pages.add { renderAchievement(it, dayNumber, kind) }
                    }
                }

                pages.add { renderTracker(it, days, logo) }
                document.writeTo(output)
            } finally {
                document.close()
                logo?.recycle()
                cover?.recycle()
            }
        }

    // region Pages

    private fun renderCover(canvas: Canvas, kind: PracticeKind, cover: Bitmap?, logo: Bitmap?) {
        val pageRect = RectF(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT)
        if (cover != null) {
            canvas.drawBitmap(cover, null, pageRect, bitmapPaint)
            // Darker at the top (0.3) and bottom (0.5), lighter in the middle (0.1) — iOS draws the
            // same piecewise ramp as 40 strips.
            canvas.drawRect(pageRect, Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, PAGE_HEIGHT,
                    intArrayOf(black(0.3f), black(0.1f), black(0.1f), black(0.5f)),
                    floatArrayOf(0f, 0.35f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
            })
        } else {
            canvas.drawRect(pageRect, Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, PAGE_HEIGHT, BRAND_GREEN, BRAND_GREEN_LIGHT, Shader.TileMode.CLAMP)
            })
        }

        val topY = 44f
        if (logo != null) {
            val logoSize = 36f
            canvas.drawRoundedBitmap(logo, RectF(MARGIN_H, topY, MARGIN_H + logoSize, topY + logoSize), 8f)
            val namePaint = textPaint(13f, Weight.SEMIBOLD, Color.WHITE)
            canvas.drawTextTop(
                string(R.string.printable_plan_pdf_app_name),
                MARGIN_H + logoSize + 10f,
                topY + (logoSize - namePaint.lineHeight) / 2f,
                namePaint
            )
        }

        val titleLayout = layout(coverTitle(kind), textPaint(54f, Weight.HEAVY, Color.WHITE), CONTENT_WIDTH)
        canvas.drawLayout(titleLayout, MARGIN_H, PAGE_HEIGHT - 280f)

        val quoteLayout = layout(
            string(R.string.printable_plan_pdf_cover_quote),
            textPaint(14f, Weight.REGULAR, white(0.8f), italic = true),
            CONTENT_WIDTH,
            Layout.Alignment.ALIGN_CENTER
        )
        canvas.drawLayout(quoteLayout, MARGIN_H, PAGE_HEIGHT - 60f, maxHeight = 20f)
    }

    /** Page 2: headline, introduction, difficulty and focus, the store QR code and a disclaimer. */
    private fun renderAbout(canvas: Canvas, kind: PracticeKind, logo: Bitmap?) {
        var y = MARGIN_TOP
        canvas.drawPageLogo(logo, y)
        y += 38f

        val headline = layout(aboutHeadline(kind), textPaint(34f, Weight.BOLD, DARK_TEXT), CONTENT_WIDTH)
        canvas.drawLayout(headline, MARGIN_H, y)
        y += headline.height + 18f

        val columnWidth = (CONTENT_WIDTH - 24f) / 2f
        val bodyPaint = textPaint(11f, Weight.REGULAR, MEDIUM_TEXT)
        val left = layout(aboutBody(kind), bodyPaint, columnWidth, lineSpacing = 3f)
        val right = layout(aboutTrackerBlurb(kind), bodyPaint, columnWidth, lineSpacing = 3f)
        canvas.drawLayout(left, MARGIN_H, y)
        canvas.drawLayout(right, MARGIN_H + columnWidth + 24f, y)
        y += max(left.height, right.height) + 20f

        val repeat = layout(
            string(R.string.printable_plan_pdf_about_repeat),
            textPaint(11f, Weight.REGULAR, DARK_TEXT),
            CONTENT_WIDTH,
            lineSpacing = 2f
        )
        canvas.drawLayout(repeat, MARGIN_H, y)
        y += repeat.height + 24f

        // Level of difficulty: one filled dot of three.
        val cardHeight = 56f
        val halfWidth = CONTENT_WIDTH / 2f - 8f
        val difficultyRect = RectF(MARGIN_H, y, MARGIN_H + halfWidth, y + cardHeight)
        canvas.drawRoundRect(difficultyRect, 10f, 10f, fill(LIGHT_BG))
        val labelPaint = textPaint(9f, Weight.SEMIBOLD, MEDIUM_TEXT)
        canvas.drawTextTop(string(R.string.printable_plan_pdf_difficulty_label), difficultyRect.left + 12f, y + 32f, labelPaint)
        val dotSize = 14f
        for (i in 0 until 3) {
            val dotX = difficultyRect.left + 12f + i * (dotSize + 4f)
            canvas.drawRoundRect(
                RectF(dotX, y + 12f, dotX + dotSize, y + 12f + dotSize),
                4f, 4f,
                fill(if (i == 0) BRAND_GREEN else gray(0.82f))
            )
        }

        val focusRect = RectF(MARGIN_H + CONTENT_WIDTH / 2f + 8f, y, MARGIN_H + CONTENT_WIDTH, y + cardHeight)
        canvas.drawRoundRect(focusRect, 10f, 10f, fill(LIGHT_BG))
        canvas.drawTextTop(string(R.string.printable_plan_pdf_focus_label), focusRect.left + 12f, y + 8f, labelPaint)
        val chipPaint = textPaint(10f, Weight.MEDIUM, DARK_TEXT)
        var chipX = focusRect.left + 12f
        listOf(
            R.string.printable_plan_pdf_focus_breath,
            R.string.printable_plan_pdf_focus_movement,
            R.string.printable_plan_pdf_focus_balance
        ).forEach { chip ->
            canvas.drawRoundRect(RectF(chipX, y + 28f, chipX + 72f, y + 48f), 6f, 6f, fill(Color.WHITE))
            canvas.drawTextTop(string(chip), chipX + 6f, y + 30f, chipPaint)
            chipX += 76f
        }
        y += cardHeight + 28f

        // QR code and "download app" button on the left, disclaimer on the right.
        val bottomHeight = 140f
        val bottomColumnWidth = CONTENT_WIDTH / 2f - 12f
        val qrSize = 80f
        canvas.drawQrCode(string(R.string.app_link), RectF(MARGIN_H, y, MARGIN_H + qrSize, y + qrSize))

        val ctaX = MARGIN_H + qrSize + 14f
        val ctaWidth = bottomColumnWidth - qrSize - 14f
        canvas.drawRoundRect(RectF(ctaX, y + 16f, ctaX + ctaWidth, y + 52f), 10f, 10f, fill(BRAND_GREEN))
        val cta = layout(
            string(R.string.printable_plan_pdf_download_app),
            textPaint(11f, Weight.BOLD, Color.WHITE),
            ctaWidth,
            Layout.Alignment.ALIGN_CENTER
        )
        canvas.drawLayout(cta, ctaX, y + 26f, maxHeight = 16f)
        canvas.drawTextTop(
            string(R.string.printable_plan_pdf_scan_qr),
            ctaX + 10f,
            y + 56f,
            textPaint(9f, Weight.REGULAR, MEDIUM_TEXT)
        )

        val disclaimerRect = RectF(
            MARGIN_H + CONTENT_WIDTH / 2f + 12f,
            y,
            MARGIN_H + CONTENT_WIDTH / 2f + 12f + bottomColumnWidth,
            y + bottomHeight
        )
        canvas.drawRoundRect(disclaimerRect, 10f, 10f, stroke(gray(0.96f), 1f))
        canvas.drawTextTop(
            string(R.string.printable_plan_pdf_disclaimer_title),
            disclaimerRect.left + 14f,
            y + 12f,
            textPaint(13f, Weight.BOLD, DARK_TEXT)
        )
        val disclaimer = layout(
            context.getString(R.string.printable_plan_pdf_disclaimer_body, programName(kind)),
            textPaint(8.5f, Weight.REGULAR, MEDIUM_TEXT),
            bottomColumnWidth - 28f,
            lineSpacing = 2f
        )
        canvas.drawLayout(disclaimer, disclaimerRect.left + 14f, y + 32f, maxHeight = bottomHeight - 44f)
    }

    private fun renderHowTo(canvas: Canvas, kind: PracticeKind, logo: Bitmap?) {
        var y = MARGIN_TOP
        canvas.drawPageLogo(logo, y)
        y += 34f

        y = canvas.drawSectionTitle(string(R.string.printable_plan_pdf_how_to_title), y)
        y += 14f

        howToPoints(kind).forEach { (title, body) ->
            y = canvas.drawBulletPoint(string(title), string(body), y)
            y += 10f
        }
        y += 16f

        val boxHeight = 68f
        val box = RectF(MARGIN_H, y, MARGIN_H + CONTENT_WIDTH, y + boxHeight)
        canvas.drawRoundRect(box, 12f, 12f, fill(BRAND_GREEN_PALE))
        canvas.drawRoundRect(RectF(MARGIN_H, y, MARGIN_H + 4f, y + boxHeight), 2f, 2f, fill(BRAND_GREEN))
        val encouragement = layout(
            string(R.string.printable_plan_pdf_how_to_encouragement),
            textPaint(12f, Weight.MEDIUM, DARK_TEXT),
            box.width() - 36f,
            lineSpacing = 4f
        )
        canvas.drawLayout(encouragement, box.left + 18f, box.top + 14f, maxHeight = boxHeight - 28f)
    }

    /** One page per [EXERCISES_PER_PAGE] exercises; later pages get a lighter "(continued)" header. */
    private fun renderDay(
        pages: PageSink,
        dayNumber: Int,
        day: WorkoutDayDto,
        images: Map<String, Bitmap>,
        logo: Bitmap?
    ) {
        val duration = dayDuration(day)
        val chunks = day.exercises.chunked(EXERCISES_PER_PAGE).ifEmpty { listOf(emptyList()) }
        chunks.forEachIndexed { chunkIndex, chunk ->
            pages.add { canvas ->
                var y = MARGIN_TOP
                if (chunkIndex == 0) {
                    val headerHeight = 44f
                    canvas.drawRoundRect(RectF(MARGIN_H, y, MARGIN_H + CONTENT_WIDTH, y + headerHeight), 10f, 10f, fill(BRAND_GREEN))
                    canvas.drawTextTop(
                        context.getString(R.string.printable_plan_pdf_day_header, dayNumber, duration),
                        MARGIN_H + 14f,
                        y + 11f,
                        textPaint(18f, Weight.BOLD, Color.WHITE)
                    )
                    if (logo != null) {
                        val logoSize = 20f
                        val namePaint = textPaint(7f, Weight.SEMIBOLD, white(0.85f))
                        val appName = string(R.string.printable_plan_pdf_app_name)
                        val blockWidth = logoSize + 5f + namePaint.measureText(appName)
                        val logoX = MARGIN_H + CONTENT_WIDTH - blockWidth - 12f
                        val logoY = y + (headerHeight - logoSize) / 2f
                        canvas.drawRoundedBitmap(logo, RectF(logoX, logoY, logoX + logoSize, logoY + logoSize), 4f)
                        canvas.drawTextTop(appName, logoX + logoSize + 5f, y + (headerHeight - namePaint.lineHeight) / 2f, namePaint)
                    }
                    y += headerHeight + 8f
                } else {
                    val headerHeight = 36f
                    canvas.drawRoundRect(
                        RectF(MARGIN_H, y, MARGIN_H + CONTENT_WIDTH, y + headerHeight),
                        8f, 8f,
                        fill(withAlpha(BRAND_GREEN, 0.15f))
                    )
                    canvas.drawTextTop(
                        context.getString(R.string.printable_plan_pdf_day_header_continued, dayNumber, duration),
                        MARGIN_H + 12f,
                        y + 9f,
                        textPaint(14f, Weight.BOLD, BRAND_GREEN)
                    )
                    y += headerHeight + 8f
                }

                chunk.forEachIndexed { i, exercise ->
                    val columnX = MARGIN_H + i * (COLUMN_WIDTH + COLUMN_GAP)
                    canvas.drawExerciseCard(
                        exercise = exercise,
                        exerciseNumber = chunkIndex * EXERCISES_PER_PAGE + i + 1,
                        image = images[exercise.heroImageUrl],
                        top = y,
                        left = columnX
                    )
                }
            }
        }
    }

    private fun renderAchievement(canvas: Canvas, dayNumber: Int, kind: PracticeKind) {
        canvas.drawRect(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT, fill(BRAND_GREEN_PALE))
        val centerX = PAGE_WIDTH / 2f

        val badgeSize = 180f
        val badgeRect = RectF(centerX - badgeSize / 2f, 220f, centerX + badgeSize / 2f, 220f + badgeSize)
        val badge = decodeResourceBitmap(context, badgeDrawable(dayNumber), px(badgeSize, BADGE_PX_PER_PT))
        if (badge != null) {
            canvas.drawBitmap(badge, null, badgeRect, bitmapPaint)
            badge.recycle()
        } else {
            canvas.drawOval(badgeRect, fill(withAlpha(BRAND_GREEN, 0.15f)))
            canvas.drawOval(badgeRect, stroke(BRAND_GREEN, 3f))
            val check = layout("✓", textPaint(60f, Weight.REGULAR, DARK_TEXT), 60f, Layout.Alignment.ALIGN_CENTER)
            canvas.drawLayout(check, centerX - 30f, 270f)
        }

        val titlePaint = textPaint(32f, Weight.BOLD, DARK_TEXT)
        canvas.drawLayout(
            layout(context.getString(R.string.printable_plan_pdf_day_complete, dayNumber), titlePaint, CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER),
            MARGIN_H, 430f, maxHeight = 44f
        )

        val achievement = achievement(dayNumber, kind)
        canvas.drawLayout(
            layout(
                context.getString(R.string.printable_plan_pdf_achievement_title, achievement.icon, achievement.title),
                textPaint(18f, Weight.SEMIBOLD, BRAND_GREEN),
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_CENTER
            ),
            MARGIN_H, 484f, maxHeight = 28f
        )
        canvas.drawLayout(
            layout(
                achievement.encouragement,
                textPaint(14f, Weight.REGULAR, MEDIUM_TEXT),
                CONTENT_WIDTH - 80f,
                Layout.Alignment.ALIGN_CENTER,
                lineSpacing = 4f
            ),
            MARGIN_H + 40f, 524f, maxHeight = 50f
        )

        canvas.drawLine(centerX - 60f, 590f, centerX + 60f, 590f, stroke(withAlpha(BRAND_GREEN, 0.3f), 1f))

        val keepGoing = if (dayNumber < PLAN_DAYS) {
            context.getString(R.string.printable_plan_pdf_next_day, dayNumber + 1)
        } else {
            string(R.string.printable_plan_pdf_challenge_complete)
        }
        canvas.drawLayout(
            layout(keepGoing, textPaint(12f, Weight.MEDIUM, MEDIUM_TEXT), CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER),
            MARGIN_H, 608f, maxHeight = 20f
        )
    }

    private fun renderTracker(canvas: Canvas, days: List<WorkoutDayDto>, logo: Bitmap?) {
        var y = MARGIN_TOP
        canvas.drawPageLogo(logo, y)
        y += 34f

        canvas.drawLayout(
            layout(string(R.string.printable_plan_pdf_tracker_title), textPaint(22f, Weight.BOLD, DARK_TEXT), CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER),
            MARGIN_H, y, maxHeight = 30f
        )
        y += 34f
        canvas.drawLayout(
            layout(string(R.string.printable_plan_pdf_tracker_subtitle), textPaint(11f, Weight.REGULAR, MEDIUM_TEXT), CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER),
            MARGIN_H, y, maxHeight = 16f
        )
        y += 28f

        val rowHeight = 82f
        val checkboxSize = 20f
        val dayPaint = textPaint(15f, Weight.BOLD, DARK_TEXT)
        val feelPaint = textPaint(9f, Weight.MEDIUM, MEDIUM_TEXT)
        val dashPaint = stroke(gray(0.78f), 0.5f).apply { pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f) }

        days.forEachIndexed { index, day ->
            val background = TRACKER_ROW_COLORS[index % TRACKER_ROW_COLORS.size]
            val accent = TRACKER_ACCENT_COLORS[index % TRACKER_ACCENT_COLORS.size]

            canvas.drawRoundRect(RectF(MARGIN_H, y, MARGIN_H + CONTENT_WIDTH, y + rowHeight), 10f, 10f, fill(background))
            canvas.drawRoundRect(RectF(MARGIN_H, y, MARGIN_H + 4f, y + rowHeight), 2f, 2f, fill(accent))

            val checkboxX = MARGIN_H + 18f
            val checkboxY = y + 14f
            canvas.drawOval(
                RectF(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize),
                stroke(withAlpha(accent, 0.3f), 2f)
            )
            canvas.drawTextTop(
                context.getString(R.string.printable_plan_pdf_tracker_day, index + 1),
                checkboxX + checkboxSize + 10f,
                y + 14f,
                dayPaint
            )

            val durationPaint = textPaint(10f, Weight.MEDIUM, accent)
            val duration = context.getString(R.string.printable_plan_pdf_tracker_duration, dayDuration(day))
            canvas.drawTextTop(
                duration,
                MARGIN_H + CONTENT_WIDTH - durationPaint.measureText(duration) - 14f,
                y + 16f,
                durationPaint
            )

            val feelY = y + 46f
            canvas.drawTextTop(string(R.string.printable_plan_pdf_tracker_feel), MARGIN_H + 18f, feelY, feelPaint)
            canvas.drawLine(MARGIN_H + 110f, feelY + 10f, MARGIN_H + CONTENT_WIDTH - 14f, feelY + 10f, dashPaint)

            y += rowHeight + 6f
        }

        y += 10f
        canvas.drawLayout(
            layout(string(R.string.printable_plan_pdf_tracker_footer), textPaint(10f, Weight.MEDIUM, MEDIUM_TEXT), CONTENT_WIDTH, Layout.Alignment.ALIGN_CENTER),
            MARGIN_H, y, maxHeight = 16f
        )
    }

    // endregion

    // region Building blocks

    /** Fixed-height card: thumbnail with the name over a dark fade, numbered steps, duration. */
    private fun Canvas.drawExerciseCard(
        exercise: ExerciseDto,
        exerciseNumber: Int,
        image: Bitmap?,
        top: Float,
        left: Float
    ) {
        val padding = 6f
        val card = RectF(left, top, left + COLUMN_WIDTH, top + CARD_HEIGHT)
        drawRoundRect(card, 10f, 10f, fill(CARD_BG))
        drawRoundRect(card, 10f, 10f, stroke(gray(0.90f), 0.5f))

        val imageRect = RectF(left + padding, top + padding, left + COLUMN_WIDTH - padding, top + padding + THUMB_HEIGHT)
        if (image != null) {
            save()
            clipPath(Path().apply {
                addRoundRect(imageRect, floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f), Path.Direction.CW)
            })
            drawBitmap(image, null, imageRect, bitmapPaint)
            restore()
        } else {
            drawRoundRect(imageRect, 8f, 8f, fill(gray(0.91f)))
        }

        val fadeHeight = 44f
        drawRect(
            imageRect.left, imageRect.bottom - fadeHeight, imageRect.right, imageRect.bottom,
            Paint().apply {
                shader = LinearGradient(
                    0f, imageRect.bottom - fadeHeight, 0f, imageRect.bottom,
                    Color.TRANSPARENT, black(0.5f), Shader.TileMode.CLAMP
                )
            }
        )

        val nameInset = 8f
        val name = layout(
            exercise.name,
            textPaint(12f, Weight.BOLD, Color.WHITE),
            imageRect.width() - nameInset * 2f,
            maxLines = 2
        )
        val nameHeight = min(name.height.toFloat(), 32f)
        drawLayout(name, imageRect.left + nameInset, imageRect.bottom - nameHeight - 6f, maxHeight = 32f)

        val numberText = NumberFormat.getIntegerInstance(locale()).format(exerciseNumber)
        val numberPaint = textPaint(9f, Weight.BOLD, Color.WHITE)
        val badgeWidth = numberPaint.measureText(numberText) + 12f
        val badge = RectF(imageRect.left + 6f, imageRect.top + 6f, imageRect.left + 6f + badgeWidth, imageRect.top + 24f)
        drawRoundRect(badge, 9f, 9f, fill(BRAND_GREEN))
        drawTextTop(
            numberText,
            badge.centerX() - numberPaint.measureText(numberText) / 2f,
            badge.centerY() - numberPaint.lineHeight / 2f,
            numberPaint
        )

        val textX = left + padding + 4f
        val textWidth = COLUMN_WIDTH - padding * 2f - 8f
        val stepsTop = imageRect.bottom + 6f
        val durationBarHeight = 22f
        val stepsBottom = top + CARD_HEIGHT - padding - durationBarHeight - 4f
        val available = stepsBottom - stepsTop
        if (exercise.steps.isNotEmpty() && available > 10f) {
            val stepsText = exercise.steps
                .mapIndexed { i, step -> context.getString(R.string.printable_plan_pdf_exercise_step, i + 1, step) }
                .joinToString("\n")
            val steps = layout(stepsText, textPaint(10f, Weight.REGULAR, DARK_TEXT), textWidth, lineSpacing = 2f)
            drawLayout(steps, textX, stepsTop, maxHeight = available)
        }

        val durationY = top + CARD_HEIGHT - padding - durationBarHeight
        drawTextTop(exerciseDuration(exercise.lengthSeconds), textX, durationY + 4f, textPaint(9f, Weight.BOLD, BRAND_GREEN))
    }

    private fun Canvas.drawPageLogo(logo: Bitmap?, y: Float) {
        logo ?: return
        val logoSize = 22f
        drawRoundedBitmap(logo, RectF(MARGIN_H, y, MARGIN_H + logoSize, y + logoSize), 5f)
        drawTextTop(
            string(R.string.printable_plan_pdf_app_name),
            MARGIN_H + logoSize + 6f,
            y + 6f,
            textPaint(8f, Weight.SEMIBOLD, MEDIUM_TEXT)
        )
    }

    /** Title with a short green underline; returns the y below it. */
    private fun Canvas.drawSectionTitle(title: String, y: Float): Float {
        val titleLayout = layout(title, textPaint(22f, Weight.BOLD, DARK_TEXT), CONTENT_WIDTH)
        drawLayout(titleLayout, MARGIN_H, y)
        val lineY = y + titleLayout.height + 4f
        drawLine(MARGIN_H, lineY, MARGIN_H + 50f, lineY, stroke(BRAND_GREEN, 3f))
        return lineY + 6f
    }

    /** Green dot, bold title and wrapped body; returns the y below the body. */
    private fun Canvas.drawBulletPoint(title: String, body: String, y: Float): Float {
        val titlePaint = textPaint(13f, Weight.SEMIBOLD, DARK_TEXT)
        val dotSize = 6f
        drawOval(RectF(MARGIN_H, y + 5f, MARGIN_H + dotSize, y + 5f + dotSize), fill(BRAND_GREEN))

        val textX = MARGIN_H + 16f
        drawTextTop(title, textX, y, titlePaint)
        val bodyY = y + titlePaint.lineHeight + 2f
        val bodyLayout = layout(body, textPaint(11f, Weight.REGULAR, MEDIUM_TEXT), CONTENT_WIDTH - 16f, lineSpacing = 2f)
        drawLayout(bodyLayout, textX, bodyY)
        return bodyY + bodyLayout.height
    }

    private fun Canvas.drawRoundedBitmap(bitmap: Bitmap, rect: RectF, radius: Float) {
        save()
        clipPath(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) })
        drawBitmap(bitmap, null, rect, bitmapPaint)
        restore()
    }

    /** Dark modules on the white page, filled as one path so no seams show between modules. */
    private fun Canvas.drawQrCode(contents: String, rect: RectF) {
        val matrix = runCatching {
            QRCodeWriter().encode(
                contents,
                BarcodeFormat.QR_CODE,
                0,
                0,
                mapOf(
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN to 1
                )
            )
        }.getOrNull() ?: return

        val module = rect.width() / matrix.width
        val path = Path()
        for (row in 0 until matrix.height) {
            var column = 0
            while (column < matrix.width) {
                if (!matrix.get(column, row)) {
                    column++
                    continue
                }
                val runStart = column
                while (column < matrix.width && matrix.get(column, row)) column++
                path.addRect(
                    rect.left + runStart * module,
                    rect.top + row * module,
                    rect.left + column * module,
                    rect.top + (row + 1) * module,
                    Path.Direction.CW
                )
            }
        }
        drawPath(path, Paint().apply { color = Color.BLACK; style = Paint.Style.FILL })
    }

    // endregion

    // region Text

    private enum class Weight(val value: Int) { REGULAR(400), MEDIUM(500), SEMIBOLD(600), BOLD(700), HEAVY(800) }

    private val typefaces = mutableMapOf<Pair<Weight, Boolean>, Typeface>()

    /** iOS system-font weights mapped onto the platform sans-serif (Roboto). */
    private fun typeface(weight: Weight, italic: Boolean): Typeface = typefaces.getOrPut(weight to italic) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(Typeface.SANS_SERIF, weight.value, italic)
        } else {
            val style = if (italic) Typeface.ITALIC else Typeface.NORMAL
            when (weight) {
                Weight.REGULAR -> Typeface.create(Typeface.SANS_SERIF, style)
                Weight.MEDIUM, Weight.SEMIBOLD -> Typeface.create("sans-serif-medium", style)
                Weight.BOLD -> Typeface.create(Typeface.SANS_SERIF, if (italic) Typeface.BOLD_ITALIC else Typeface.BOLD)
                Weight.HEAVY -> Typeface.create("sans-serif-black", style)
            }
        }
    }

    private fun textPaint(size: Float, weight: Weight, color: Int, italic: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            typeface = typeface(weight, italic)
            this.color = color
        }

    private val TextPaint.lineHeight: Float
        get() = fontMetrics.let { it.descent - it.ascent }

    /** Like UIKit's `draw(at:)`: [top] is the top of the line box, not the baseline. */
    private fun Canvas.drawTextTop(text: String, x: Float, top: Float, paint: TextPaint) {
        if (EmojiRuns.find(text).isEmpty()) {
            drawText(text, x, top - paint.fontMetrics.ascent, paint)
        } else {
            // A one-line layout puts its first line's top at 0, matching the position above.
            drawLayout(layout(text, paint, paint.measureText(text) + 1f), x, top)
        }
    }

    /**
     * At weights of 600 and up the platform fakes bold on the color-emoji font, and Skia's PDF
     * backend then drops those glyphs. Emoji carry no weight anyway, so they are set regular.
     */
    private fun withRegularEmoji(text: CharSequence): CharSequence {
        val runs = EmojiRuns.find(text)
        if (runs.isEmpty()) return text
        return SpannableString(text).apply {
            runs.forEach { run ->
                setSpan(TypefaceOverrideSpan(typeface(Weight.REGULAR, italic = false)), run.first, run.last + 1, 0)
            }
        }
    }

    private class TypefaceOverrideSpan(private val typeface: Typeface) : MetricAffectingSpan() {
        override fun updateMeasureState(paint: TextPaint) {
            paint.typeface = typeface
        }

        override fun updateDrawState(paint: TextPaint) {
            paint.typeface = typeface
        }
    }

    private fun layout(
        rawText: CharSequence,
        paint: TextPaint,
        width: Float,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        lineSpacing: Float = 0f,
        maxLines: Int = Int.MAX_VALUE
    ): StaticLayout {
        val text = withRegularEmoji(rawText)
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, ceil(width).toInt().coerceAtLeast(1))
            .setAlignment(alignment)
            .setLineSpacing(lineSpacing, 1f)
            .setIncludePad(false)
            .apply {
                if (maxLines != Int.MAX_VALUE) {
                    setMaxLines(maxLines)
                    setEllipsize(TextUtils.TruncateAt.END)
                }
            }
            .build()
    }

    /**
     * Draws [layout] with its top-left at ([x], [y]). Like UIKit's `draw(in:)`, lines that do not fit
     * entirely within [maxHeight] are left out rather than cut through.
     */
    private fun Canvas.drawLayout(layout: StaticLayout, x: Float, y: Float, maxHeight: Float = Float.MAX_VALUE) {
        save()
        translate(x, y)
        if (layout.height > maxHeight) {
            var visibleBottom = 0
            for (line in 0 until layout.lineCount) {
                val bottom = layout.getLineBottom(line)
                if (bottom > maxHeight) break
                visibleBottom = bottom
            }
            clipRect(0f, 0f, layout.width.toFloat(), visibleBottom.toFloat())
        }
        layout.draw(this)
        restore()
    }

    // endregion

    // region Content

    private suspend fun prefetchImages(day: WorkoutDayDto): Map<String, Bitmap> = coroutineScope {
        val width = px(COLUMN_WIDTH - 12f, THUMB_PX_PER_PT)
        val height = px(THUMB_HEIGHT, THUMB_PX_PER_PT)
        day.exercises
            .map { it.heroImageUrl }
            .filter { it.isNotBlank() }
            .distinct()
            .map { url -> async { url to runCatching { imageSource.load(url, width, height) }.getOrNull() } }
            .awaitAll()
            .mapNotNull { (url, bitmap) -> bitmap?.let { url to it } }
            .toMap()
    }

    private fun dayDuration(day: WorkoutDayDto): String =
        context.resources.getQuantityString(R.plurals.printable_plan_pdf_day_minutes, day.durationInMinutes, day.durationInMinutes)

    private fun exerciseDuration(lengthSeconds: Int): String {
        val minutes = lengthSeconds / 60
        val seconds = lengthSeconds % 60
        return when {
            minutes > 0 && seconds > 0 -> context.getString(R.string.printable_plan_pdf_exercise_minutes_seconds, minutes, seconds)
            minutes > 0 -> context.getString(R.string.printable_plan_pdf_exercise_minutes, minutes)
            else -> context.getString(R.string.printable_plan_pdf_exercise_seconds, seconds)
        }
    }

    private class Achievement(val icon: String, val title: String, val encouragement: String)

    /** Title earned on [dayNumber], as on iOS's TitleService, with the copy from string resources. */
    private fun achievement(dayNumber: Int, kind: PracticeKind): Achievement {
        val model = PracticeTitleService.getTitle(dayNumber)
        val copy = when (model?.badgeId) {
            "initiate" -> R.string.printable_plan_pdf_achievement_initiate to R.string.printable_plan_pdf_encouragement_initiate
            "student" -> R.string.printable_plan_pdf_achievement_student to R.string.printable_plan_pdf_encouragement_student
            "practitioner" -> R.string.printable_plan_pdf_achievement_practitioner to R.string.printable_plan_pdf_encouragement_practitioner
            else -> null
        }
        return Achievement(
            icon = model?.icon ?: "🌿",
            title = copy?.first?.let(::string) ?: string(
                when (kind) {
                    PracticeKind.TAI_CHI -> R.string.printable_plan_pdf_achievement_fallback_tai_chi
                    PracticeKind.WALKING -> R.string.printable_plan_pdf_achievement_fallback_walking
                    PracticeKind.CHAIR -> R.string.printable_plan_pdf_achievement_fallback_chair
                }
            ),
            encouragement = string(copy?.second ?: R.string.printable_plan_pdf_encouragement_fallback)
        )
    }

    private fun badgeDrawable(dayNumber: Int): Int = when (dayNumber) {
        1 -> R.drawable.badge_one
        2, 3 -> R.drawable.badge_two
        else -> R.drawable.badge_three
    }

    private fun coverTitle(kind: PracticeKind) = string(
        when (kind) {
            PracticeKind.TAI_CHI -> R.string.printable_plan_pdf_cover_title_tai_chi
            PracticeKind.WALKING -> R.string.printable_plan_pdf_cover_title_walking
            PracticeKind.CHAIR -> R.string.printable_plan_pdf_cover_title_chair
        }
    )

    private fun programName(kind: PracticeKind) = string(
        when (kind) {
            PracticeKind.TAI_CHI -> R.string.printable_plan_pdf_program_tai_chi
            PracticeKind.WALKING -> R.string.printable_plan_pdf_program_walking
            PracticeKind.CHAIR -> R.string.printable_plan_pdf_program_chair
        }
    )

    private fun aboutHeadline(kind: PracticeKind) = string(
        when (kind) {
            PracticeKind.TAI_CHI -> R.string.printable_plan_pdf_about_headline_tai_chi
            PracticeKind.WALKING -> R.string.printable_plan_pdf_about_headline_walking
            PracticeKind.CHAIR -> R.string.printable_plan_pdf_about_headline_chair
        }
    )

    private fun aboutBody(kind: PracticeKind) = string(
        when (kind) {
            PracticeKind.TAI_CHI -> R.string.printable_plan_pdf_about_body_tai_chi
            PracticeKind.WALKING -> R.string.printable_plan_pdf_about_body_walking
            PracticeKind.CHAIR -> R.string.printable_plan_pdf_about_body_chair
        }
    )

    /** The walking and chair plans share one tracker paragraph; Tai Chi has its own shorter one. */
    private fun aboutTrackerBlurb(kind: PracticeKind) = string(
        if (kind == PracticeKind.TAI_CHI) {
            R.string.printable_plan_pdf_about_tracker_tai_chi
        } else {
            R.string.printable_plan_pdf_about_tracker_other
        }
    )

    /** "How to Use This Plan" bullets, tailored to how each program is practised. */
    private fun howToPoints(kind: PracticeKind): List<Pair<Int, Int>> = when (kind) {
        PracticeKind.TAI_CHI -> listOf(
            R.string.printable_plan_pdf_how_to_tai_chi_space_title to R.string.printable_plan_pdf_how_to_tai_chi_space_body,
            R.string.printable_plan_pdf_how_to_follow_each_day to R.string.printable_plan_pdf_how_to_tai_chi_follow_body,
            R.string.printable_plan_pdf_how_to_tai_chi_thumbnails_title to R.string.printable_plan_pdf_how_to_tai_chi_thumbnails_body,
            R.string.printable_plan_pdf_how_to_tai_chi_listen_title to R.string.printable_plan_pdf_how_to_tai_chi_listen_body,
            R.string.printable_plan_pdf_how_to_track_progress to R.string.printable_plan_pdf_how_to_tai_chi_tracker_body
        )
        PracticeKind.WALKING -> listOf(
            R.string.printable_plan_pdf_how_to_walking_path_title to R.string.printable_plan_pdf_how_to_walking_path_body,
            R.string.printable_plan_pdf_how_to_follow_each_day to R.string.printable_plan_pdf_how_to_walking_follow_body,
            R.string.printable_plan_pdf_how_to_use_cues to R.string.printable_plan_pdf_how_to_walking_cues_body,
            R.string.printable_plan_pdf_how_to_walking_slow_title to R.string.printable_plan_pdf_how_to_walking_slow_body,
            R.string.printable_plan_pdf_how_to_track_progress to R.string.printable_plan_pdf_how_to_tracker_body
        )
        PracticeKind.CHAIR -> listOf(
            R.string.printable_plan_pdf_how_to_chair_setup_title to R.string.printable_plan_pdf_how_to_chair_setup_body,
            R.string.printable_plan_pdf_how_to_follow_each_day to R.string.printable_plan_pdf_how_to_chair_follow_body,
            R.string.printable_plan_pdf_how_to_use_cues to R.string.printable_plan_pdf_how_to_chair_cues_body,
            R.string.printable_plan_pdf_how_to_chair_gentle_title to R.string.printable_plan_pdf_how_to_chair_gentle_body,
            R.string.printable_plan_pdf_how_to_track_progress to R.string.printable_plan_pdf_how_to_tracker_body
        )
    }

    private fun string(@StringRes id: Int): String = context.getString(id)

    private fun locale() = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: java.util.Locale.getDefault()

    // endregion

    /** Numbers pages and enforces PdfDocument's one-open-page-at-a-time rule. */
    private class PageSink(private val document: PdfDocument) {
        private var pageNumber = 0

        fun add(draw: (Canvas) -> Unit) {
            pageNumber++
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber).create()
            )
            try {
                draw(page.canvas)
            } finally {
                document.finishPage(page)
            }
        }
    }

    private companion object {
        const val PLAN_DAYS = 7
        const val EXERCISES_PER_PAGE = 3

        // US Letter in points, as on iOS.
        const val PAGE_WIDTH = 612f
        const val PAGE_HEIGHT = 792f
        const val MARGIN_H = 36f
        const val MARGIN_TOP = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_H * 2
        const val COLUMN_GAP = 10f
        const val COLUMN_WIDTH = (CONTENT_WIDTH - COLUMN_GAP * (EXERCISES_PER_PAGE - 1)) / EXERCISES_PER_PAGE
        const val THUMB_HEIGHT = 250f
        const val CARD_HEIGHT = 650f

        // Embedded pixels per point. PdfDocument stores bitmaps losslessly, so these trade print
        // sharpness against file size; thumbnails match the ~1.4 px/pt iOS ends up embedding.
        const val THUMB_PX_PER_PT = 1.4f
        const val COVER_PX_PER_PT = 1.25f
        const val BADGE_PX_PER_PT = 2f
        const val LOGO_MAX_PX = 160
        const val COVER_MAX_PX = 1600

        val BRAND_GREEN = Color.rgb(11, 165, 107)
        val BRAND_GREEN_LIGHT = Color.rgb(42, 181, 152)
        val BRAND_GREEN_PALE = Color.rgb(232, 245, 238)
        val DARK_TEXT = Color.rgb(26, 26, 26)
        val MEDIUM_TEXT = Color.rgb(106, 122, 128)
        val LIGHT_BG = Color.rgb(245, 247, 246)
        val CARD_BG = Color.rgb(248, 250, 249)

        val TRACKER_ROW_COLORS = intArrayOf(
            Color.rgb(232, 245, 238),
            Color.rgb(230, 243, 250),
            Color.rgb(245, 240, 230),
            Color.rgb(240, 232, 245)
        )
        val TRACKER_ACCENT_COLORS = intArrayOf(
            BRAND_GREEN,
            Color.rgb(52, 152, 219),
            Color.rgb(211, 168, 78),
            Color.rgb(155, 89, 182)
        )

        val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

        fun px(points: Float, pxPerPoint: Float): Int = (points * pxPerPoint).roundToInt()

        fun withAlpha(color: Int, alpha: Float): Int =
            Color.argb((alpha * 255).roundToInt(), Color.red(color), Color.green(color), Color.blue(color))

        fun black(alpha: Float) = withAlpha(Color.BLACK, alpha)
        fun white(alpha: Float) = withAlpha(Color.WHITE, alpha)
        fun gray(white: Float): Int = (white * 255).roundToInt().let { Color.rgb(it, it, it) }

        fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }

        fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = width
        }
    }
}
