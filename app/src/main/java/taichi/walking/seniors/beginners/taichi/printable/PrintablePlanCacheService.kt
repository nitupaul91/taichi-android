package taichi.walking.seniors.beginners.taichi.printable

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.os.ConfigurationCompat
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.ui.home.model.WorkoutPlanDto
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates the printable plan once per plan, program and language, then serves the cached file
 * (iOS `PrintablePlanCacheService`). The key covers the first seven days, so a new intensity or a
 * regenerated plan produces a fresh PDF while repeat taps reuse the last one.
 *
 * Files live under `cacheDir/printable_plan_cache/<key>/`, which the app's FileProvider exposes for
 * sharing; the PDF itself carries a readable, localized name because share targets show it.
 */
@Singleton
class PrintablePlanCacheService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val mutex = Mutex()
    private val generator by lazy { PrintablePlanPdfGenerator(context, CoilPrintablePlanImageSource(context)) }

    private val cacheRoot: File
        get() = File(context.cacheDir, CACHE_DIRECTORY)

    suspend fun generateOrLoadCachedPdf(plan: WorkoutPlanDto, kind: PracticeKind): File = mutex.withLock {
        withContext(Dispatchers.IO) {
            val key = PrintablePlanCacheKey.build(
                kind = kind,
                languageTag = languageTag(),
                firstDaysJson = gson.toJson(plan.allDays.take(PLAN_DAYS))
            )
            val directory = File(cacheRoot, key)
            val file = File(directory, PrintablePlanCacheKey.fileName(context.getString(documentNameRes(kind))))
            if (file.isFile && file.length() > 0) return@withContext file

            if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create $directory")
            val partial = File(directory, "${file.name}.partial")
            try {
                partial.outputStream().buffered().use { generator.writePdf(plan, kind, it) }
                if (!partial.renameTo(file)) throw IOException("Cannot move PDF into $file")
            } finally {
                partial.delete()
            }
            removeOtherEntries(kind, keep = key)
            file
        }
    }

    /** Keeps one PDF per program: older plans or languages of the same program are dropped. */
    private fun removeOtherEntries(kind: PracticeKind, keep: String) {
        cacheRoot.listFiles()
            ?.filter { it.name != keep && it.name.startsWith(PrintablePlanCacheKey.prefix(kind)) }
            ?.forEach { it.deleteRecursively() }
    }

    private fun languageTag(): String =
        ConfigurationCompat.getLocales(context.resources.configuration)[0]?.toLanguageTag().orEmpty()

    private companion object {
        const val CACHE_DIRECTORY = "printable_plan_cache"
        const val PLAN_DAYS = 7
    }
}

@StringRes
internal fun documentNameRes(kind: PracticeKind): Int = when (kind) {
    PracticeKind.TAI_CHI -> R.string.printable_plan_document_name_tai_chi
    PracticeKind.WALKING -> R.string.printable_plan_document_name_walking
    PracticeKind.CHAIR -> R.string.printable_plan_document_name_chair
}

/** Pure naming rules for the cache, kept apart so they can be unit tested on the JVM. */
internal object PrintablePlanCacheKey {
    /** Bump when the PDF layout changes so cached files are regenerated. */
    const val PDF_VERSION = "v1"

    fun prefix(kind: PracticeKind): String = "${kind.storageValue}_"

    fun build(kind: PracticeKind, languageTag: String, firstDaysJson: String): String {
        val language = languageTag
            .split(Regex("[^A-Za-z0-9_-]+"))
            .filter { it.isNotEmpty() }
            .joinToString("_")
            .ifEmpty { "und" }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((PDF_VERSION + language + kind.storageValue + firstDaysJson).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return prefix(kind) + language + "_" + digest
    }

    /** A file name safe on every share target, derived from the localized document name. */
    fun fileName(documentName: String): String {
        val safe = documentName
            .replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifEmpty { "printable-plan" }
        return "$safe.pdf"
    }
}
