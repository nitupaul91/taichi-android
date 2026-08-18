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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

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
}
