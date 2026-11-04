package taichi.walking.seniors.beginners.taichi.printable

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.printablePlanDataStore by preferencesDataStore(name = "taichi_printable_plan_prefs")

/** Whether the user has already received the printable plan (iOS `hasDownloadedPrintablePlan`). */
@Singleton
class PrintablePlanStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()
    private var downloadedBeforeSession: Boolean? = null

    /**
     * Whether the plan was downloaded before this app process started, read once and then fixed
     * (iOS `PrintablePlanSession`). Lets the home card stay for the rest of the session in which the
     * user downloads it, and disappear from the next launch.
     */
    suspend fun downloadedBeforeThisSession(): Boolean = mutex.withLock {
        downloadedBeforeSession ?: hasDownloaded().also { downloadedBeforeSession = it }
    }

    suspend fun markDownloaded() {
        context.printablePlanDataStore.edit { it[HAS_DOWNLOADED] = true }
    }

    private suspend fun hasDownloaded(): Boolean =
        context.printablePlanDataStore.data.map { it[HAS_DOWNLOADED] ?: false }.first()

    private companion object {
        val HAS_DOWNLOADED = booleanPreferencesKey("has_downloaded_printable_plan")
    }
}
