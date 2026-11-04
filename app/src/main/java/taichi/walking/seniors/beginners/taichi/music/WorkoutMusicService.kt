package taichi.walking.seniors.beginners.taichi.music

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.taichi.ui.home.workout.WorkoutMusicTrack
import taichi.walking.seniors.beginners.taichi.ui.video.WorkoutMediaCache
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the workout background-music catalog. [prefetchIfNeeded] silently warms the on-disk media
 * cache while the user is busy with onboarding, and [urlFor] resolves a track for playback, which
 * streams through the same cache and therefore works offline once prefetched.
 *
 * Never throws and never surfaces errors: music is decorative, so every failure degrades to
 * "no music" — the same behaviour as the user picking Off.
 */
@Singleton
class WorkoutMusicService @Inject constructor(
    private val musicApi: MusicApi,
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {

    // Own scope, not a viewModelScope: the ~11 MB download must survive onboarding navigation.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefetchStarted = AtomicBoolean(false)
    private val gson = Gson()

    @Volatile
    private var trackUrlsById: Map<String, String> = emptyMap()

    /**
     * Fire-and-forget: refreshes the catalog from the backend and downloads every track not yet in
     * [WorkoutMediaCache]. Runs at most once per process, so it is safe to call on every
     * onboarding step.
     */
    fun prefetchIfNeeded() {
        if (!prefetchStarted.compareAndSet(false, true)) return
        scope.launch {
            val catalog = fetchCatalog().ifEmpty { readStoredCatalog() }
            val missing = catalog.map { it.url }
                .filterNot { WorkoutMediaCache.isFullyCached(context, it) }
            if (missing.isEmpty()) return@launch
            Timber.i("Prefetching %d workout music tracks", missing.size)
            WorkoutMediaCache.prefetch(context, missing) { _, _ -> }
        }
    }

    /** CDN URL for [track], or null when the catalog was never fetchable — music stays off. */
    suspend fun urlFor(track: WorkoutMusicTrack): String? = trackUrls()[track.id]

    /** Memory, then DataStore, then network — playback must not wait on a request when offline. */
    private suspend fun trackUrls(): Map<String, String> {
        trackUrlsById.takeIf { it.isNotEmpty() }?.let { return it }
        val stored = readStoredCatalog()
        if (stored.isNotEmpty()) {
            trackUrlsById = stored.associate { it.id to it.url }
            return trackUrlsById
        }
        return fetchCatalog().associate { it.id to it.url }
    }

    private suspend fun fetchCatalog(): List<MusicTrackDto> {
        val fetched = runCatching { musicApi.getTracks() }
            .onFailure { Timber.w(it, "Workout music catalog fetch failed") }
            .getOrDefault(emptyList())
        if (fetched.isNotEmpty()) {
            trackUrlsById = fetched.associate { it.id to it.url }
            writeStoredCatalog(fetched)
        }
        return fetched
    }

    private suspend fun readStoredCatalog(): List<MusicTrackDto> {
        val raw = runCatching { dataStore.data.first()[CATALOG_KEY] }.getOrNull()
            ?: return emptyList()
        val type = object : TypeToken<List<MusicTrackDto>>() {}.type
        return runCatching { gson.fromJson<List<MusicTrackDto>>(raw, type) }.getOrNull()
            ?: emptyList()
    }

    private suspend fun writeStoredCatalog(catalog: List<MusicTrackDto>) {
        runCatching { dataStore.edit { it[CATALOG_KEY] = gson.toJson(catalog) } }
            .onFailure { Timber.w(it, "Persisting workout music catalog failed") }
    }

    private companion object {
        val CATALOG_KEY = stringPreferencesKey("workout_music_catalog_json")
    }
}
