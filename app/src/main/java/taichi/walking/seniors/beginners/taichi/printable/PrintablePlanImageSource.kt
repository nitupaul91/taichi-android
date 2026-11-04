package taichi.walking.seniors.beginners.taichi.printable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import kotlin.math.max
import kotlin.math.roundToInt

/** Supplies exercise thumbnails to [PrintablePlanPdfGenerator]. */
fun interface PrintablePlanImageSource {
    /**
     * The image at [url] center-cropped to exactly [widthPx] x [heightPx], or null when it cannot be
     * loaded. The caller owns (and recycles) the returned bitmap.
     */
    suspend fun load(url: String, widthPx: Int, heightPx: Int): Bitmap?
}

/**
 * Loads through the app's Coil image loader, so thumbnails the home and preview screens already
 * fetched come from Coil's disk cache instead of the network.
 */
class CoilPrintablePlanImageSource(private val context: Context) : PrintablePlanImageSource {
    override suspend fun load(url: String, widthPx: Int, heightPx: Int): Bitmap? {
        if (url.isBlank()) return null
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(widthPx, heightPx)
            .scale(Scale.FILL)
            // Hardware bitmaps cannot be drawn on PdfDocument's software canvas.
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null
        // Always a new bitmap: the decoded one may be shared with Coil's memory cache.
        return centerCrop(bitmap, widthPx, heightPx)
    }
}

/** Scales [source] to cover [widthPx] x [heightPx] and crops the overflow evenly (aspect fill). */
internal fun centerCrop(source: Bitmap, widthPx: Int, heightPx: Int): Bitmap {
    val scale = max(widthPx / source.width.toFloat(), heightPx / source.height.toFloat())
    val srcWidth = (widthPx / scale).roundToInt().coerceIn(1, source.width)
    val srcHeight = (heightPx / scale).roundToInt().coerceIn(1, source.height)
    val left = (source.width - srcWidth) / 2
    val top = (source.height - srcHeight) / 2
    val output = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    Canvas(output).drawBitmap(
        source,
        Rect(left, top, left + srcWidth, top + srcHeight),
        Rect(0, 0, widthPx, heightPx),
        Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    )
    return output
}

/**
 * Decodes a bundled image without density scaling, subsampled to roughly [maxDimensionPx] so a
 * 1024 px badge in `drawable/` does not inflate to 3072 px on an xxhdpi phone.
 */
internal fun decodeResourceBitmap(context: Context, @DrawableRes resId: Int, maxDimensionPx: Int): Bitmap? =
    runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.resources.openRawResource(resId).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDimensionPx) sampleSize *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inScaled = false
        }
        context.resources.openRawResource(resId).use { BitmapFactory.decodeStream(it, null, options) }
    }.getOrNull()
