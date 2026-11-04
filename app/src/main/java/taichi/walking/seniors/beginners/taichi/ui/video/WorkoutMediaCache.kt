package taichi.walking.seniors.beginners.taichi.ui.video

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.common.C
import androidx.media3.datasource.cache.CacheKeyFactory
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.core.net.toUri
import androidx.media3.datasource.DataSourceInputStream
import androidx.media3.datasource.HttpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import taichi.walking.seniors.beginners.taichi.ui.home.model.ExerciseDto
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext

/**
 * On-disk cache for workout videos, giving both offline playback and the "download this workout"
 * pre-fetch — the Android counterpart to iOS's `WorkoutMediaCacheService`.
 *
 * A single process-wide [SimpleCache] is mandatory: media3 throws if two instances point at the
 * same directory, and a workout screen can be recreated on rotation while a prefetch is running.
 */
object WorkoutMediaCache {

    /** Bounded so cached workouts can never grow without limit on a device short of space. */
    private const val MAX_CACHE_BYTES = 1_500L * 1024 * 1024

    @Volatile
    private var cache: SimpleCache? = null

    private fun cache(context: Context): SimpleCache =
        cache ?: synchronized(this) {
            cache ?: SimpleCache(
                File(context.applicationContext.cacheDir, "workout-media"),
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
                StandaloneDatabaseProvider(context.applicationContext)
            ).also { cache = it }
        }

    /**
     * Reads from disk when present and falls back to the network otherwise, writing as it goes — so
     * simply watching a workout also makes it available offline next time.
     */
    fun dataSourceFactory(context: Context): CacheDataSource.Factory {
        val upstream = DefaultDataSource.Factory(
            context.applicationContext,
            DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        )
        return CacheDataSource.Factory()
            .setCache(cache(context))
            .setUpstreamDataSourceFactory(upstream)
            // Never fail playback because the cache is unwritable; fall through to the network.
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * True when every byte of [url] is on disk.
     *
     * The default [CacheKeyFactory] keys by the URI string, so this is naturally scoped per exercise
     * — and therefore per day and per practice kind, since those map to different exercise URLs.
     */
    fun isFullyCached(context: Context, url: String): Boolean {
        val simpleCache = cache(context)
        val key = CacheKeyFactory.DEFAULT.buildCacheKey(DataSpec(url.toUri()))
        val length = simpleCache.getContentMetadata(key)
            .get(ContentMetadata.KEY_CONTENT_LENGTH, C.LENGTH_UNSET.toLong())
        if (length <= 0L) return false
        return simpleCache.getCachedBytes(key, 0, length) >= length
    }

    /**
     * Whether a whole workout is already available offline, so the download button can resume its
     * finished state instead of inviting a re-download of files that are already on disk.
     */
    suspend fun areAllCached(context: Context, urls: List<String>): Boolean =
        withContext(Dispatchers.IO) {
            urls.isNotEmpty() && urls.all { isFullyCached(context, it) }
        }

    /**
     * Downloads [urls] into the cache, reporting completed count as it goes. Failures are logged and
     * skipped rather than aborting the batch: one unreachable video should not block the rest of a
     * workout from being available offline.
     */
    suspend fun prefetch(
        context: Context,
        urls: List<String>,
        onProgress: (completed: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val factory = dataSourceFactory(context)
        urls.forEachIndexed { index, url ->
            try {
                CacheWriter(
                    factory.createDataSource(),
                    DataSpec(url.toUri()),
                    null,
                    null
                ).cache()
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Workout media prefetch failed for %s", url)
            }
            onProgress(index + 1, urls.size)
        }
    }

    // --- Whole-day download for the workout preparation screen (iOS WorkoutMediaCacheService) ---

    /** Result of [downloadAll]. A file the CDN does not have (403/404) is skipped, not failed. */
    data class DownloadOutcome(val completed: Int, val failed: Int, val skipped: Int, val total: Int) {
        val succeeded: Boolean get() = failed == 0
    }

    /**
     * URLs the CDN answered 403/404 for in this process. A missing spoken track must not block the
     * workout (iOS b9d27f5): the session plays without that narration.
     */
    private val missingUrls: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** Video, spoken name and spoken steps of each exercise, in playback order, without repeats. */
    fun mediaUrls(exercises: List<ExerciseDto>): List<String> =
        exercises.flatMap { listOf(it.videoUrl, it.nameSpeechUrl, it.stepsSpeechUrl) }
            .filter { it.isNotBlank() }
            .distinct()

    /** Every file of the day is on disk (or known missing), so the preparation step can be skipped. */
    suspend fun isDayReady(context: Context, urls: List<String>): Boolean = withContext(Dispatchers.IO) {
        urls.isNotEmpty() && urls.all { url ->
            url in missingUrls || runCatching { partsOf(context, url, offlineOnly = true) }
                .getOrNull()
                ?.all { isFullyCached(context, it) } == true
        }
    }

    /**
     * Downloads every file of [urls] into the cache. HLS playlists are expanded into their init and
     * media segments: caching only the .m3u8 (what [prefetch] does) leaves nothing playable offline.
     * [onProgress] gets a 0..1 fraction weighted like iOS: a video counts 10, a speech file 1.
     */
    suspend fun downloadAll(
        context: Context,
        urls: List<String>,
        onProgress: (Float) -> Unit
    ): DownloadOutcome = withContext(Dispatchers.IO) {
        val factory = dataSourceFactory(context)
        // CacheWriter reports every buffer it writes, thousands of times a second; forward only
        // steps of half a percent so the progress bar does not recompose on every buffer.
        var lastReported = -1f
        val report: (Float) -> Unit = { fraction ->
            if (fraction >= 1f || fraction - lastReported >= PROGRESS_STEP) {
                lastReported = fraction
                onProgress(fraction)
            }
        }
        val totalWeight = urls.sumOf { weightOf(it) }.coerceAtLeast(1.0)
        var doneWeight = 0.0
        var completed = 0
        var failed = 0
        var skipped = 0
        for (url in urls) {
            coroutineContext.ensureActive()
            val weight = weightOf(url)
            if (url in missingUrls) {
                skipped++
            } else {
                try {
                    val parts = partsOf(context, url, offlineOnly = false)
                    parts.forEachIndexed { index, part ->
                        coroutineContext.ensureActive()
                        // Progress within this file: earlier parts (playlist, init) are tiny, so
                        // the last part (the media segment) carries the file's whole weight.
                        val isLast = index == parts.lastIndex
                        CacheWriter(factory.createDataSource(), DataSpec(part.toUri()), null) { length, cached, _ ->
                            if (isLast && length > 0) {
                                report(((doneWeight + weight * cached / length) / totalWeight).toFloat())
                            }
                        }.cache()
                    }
                    completed++
                } catch (throwable: Throwable) {
                    coroutineContext.ensureActive() // leaving the screen is not a failed file
                    if (throwable.isMissingOnCdn()) {
                        missingUrls += url
                        skipped++
                    } else {
                        Timber.w(throwable, "Workout media download failed for %s", url)
                        failed++
                    }
                }
            }
            doneWeight += weight
            report((doneWeight / totalWeight).toFloat())
        }
        DownloadOutcome(completed = completed, failed = failed, skipped = skipped, total = urls.size)
    }

    /** [url] itself, plus the init and media segments it references when it is an HLS playlist. */
    private fun partsOf(context: Context, url: String, offlineOnly: Boolean): List<String> {
        if (!isHls(url)) return listOf(url)
        val factory = dataSourceFactory(context)
        if (offlineOnly) {
            if (!isFullyCached(context, url)) return listOf(url)
        } else {
            CacheWriter(factory.createDataSource(), DataSpec(url.toUri()), null, null).cache()
        }
        val playlist = DataSourceInputStream(factory.createDataSource(), DataSpec(url.toUri()))
            .use { it.readBytes().decodeToString() }
        val base = url.substringBefore('?').substringBeforeLast('/') + "/"
        val referenced = playlist.lineSequence().mapNotNull { raw ->
            val line = raw.trim()
            when {
                line.startsWith("#EXT-X-MAP:") -> line.substringAfter("URI=\"", "").substringBefore('"')
                line.isEmpty() || line.startsWith("#") -> null
                else -> line
            }?.takeIf { it.isNotEmpty() }
        }.map { if (it.contains("://")) it else base + it }.distinct().toList()
        return listOf(url) + referenced
    }

    private const val PROGRESS_STEP = 0.005f

    private fun isHls(url: String) = url.substringBefore('?').endsWith(".m3u8", ignoreCase = true)

    private fun weightOf(url: String): Double = if (isHls(url) || url.substringBefore('?').endsWith(".mp4")) 10.0 else 1.0

    private fun Throwable.isMissingOnCdn(): Boolean =
        generateSequence(this) { it.cause }
            .filterIsInstance<HttpDataSource.InvalidResponseCodeException>()
            .any { it.responseCode == 403 || it.responseCode == 404 }
}
