package taichi.walking.seniors.beginners.taichi.printable

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * iOS hands the PDF to the share sheet, which includes Print. Android's share sheet usually has no
 * print target, so the plan offers the system print dialog (which can also save a PDF) and sharing.
 */
internal object PrintablePlanDelivery {

    fun share(context: Context, file: File, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = PDF_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            // ClipData carries the read grant to the chooser's preview as well as the chosen app.
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, chooserTitle).apply {
            if (context.findActivity() == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
            .onFailure { Timber.e(it, "Unable to share the printable plan") }
    }

    /** Opens the system print dialog. Needs an Activity context, as PrintManager does. */
    fun print(context: Context, file: File, jobName: String) {
        val activity = context.findActivity() ?: return
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        // Opened now: a held descriptor keeps the PDF readable for the spooler even if the system
        // purges the cache directory while the print dialog is open.
        val document = runCatching { ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY) }
            .onFailure { Timber.e(it, "Unable to open the printable plan for printing") }
            .getOrNull() ?: return
        runCatching { printManager.print(jobName, PdfFilePrintAdapter(file.name, document), null) }
            .onFailure {
                Timber.e(it, "Unable to print the printable plan")
                document.close()
            }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private const val PDF_MIME_TYPE = "application/pdf"
}

/** Streams an already rendered PDF to the print spooler. */
private class PdfFilePrintAdapter(
    private val fileName: String,
    private val document: ParcelFileDescriptor
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(fileName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        // Always "changed": re-copying the finished file is cheap and keeps the spooler from ever
        // holding on to a stale or missing copy.
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            // A duplicate shares the read offset with the held descriptor, so rewind before copying.
            ParcelFileDescriptor.AutoCloseInputStream(document.dup()).use { input ->
                input.channel.position(0)
                FileOutputStream(destination.fileDescriptor).use { output -> input.copyTo(output) }
            }
            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
            } else {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (exception: IOException) {
            callback.onWriteFailed(exception.message)
        }
    }

    override fun onFinish() {
        runCatching { document.close() }
    }
}
