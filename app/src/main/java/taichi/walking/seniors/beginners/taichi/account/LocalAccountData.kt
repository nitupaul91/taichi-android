package taichi.walking.seniors.beginners.taichi.account

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.billing.datastore.DataStorePrefs
import com.sageai.id.IDService
import dagger.hilt.android.qualifiers.ApplicationContext
import taichi.walking.seniors.beginners.taichi.onboarding.data.taichiOnboardingDataStore
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Everything on the device that ties it to an account, as wiped by an account deletion. */
@Singleton
class LocalAccountData @Inject constructor(
    @ApplicationContext private val context: Context,
    private val practiceRepository: PracticeRepository,
    private val dataStorePrefs: DataStorePrefs,
    private val idService: IDService,
    private val firebaseAnalytics: FirebaseAnalytics,
) {

    /**
     * Order matters, as on iOS: practice progress is erased before the new id exists, so nothing
     * of the old account can be uploaded under the new one, and the subscription relink is only
     * requested once the new id is in place, so it can never attach the purchase to an old id.
     */
    suspend fun wipe(retiredUserIds: Collection<String>) {
        // Onboarding answers and the completion flag; without the flag the launcher opens onboarding.
        context.taichiOnboardingDataStore.edit { it.clear() }

        practiceRepository.eraseAll()

        dataStorePrefs.setHasSyncedUserIdWithRemote(false)

        idService.resetUserId(retiredUserIds)

        // Re-attaches a still-active Play subscription to the new id on the next purchase query;
        // the subscription itself is untouched by account deletion.
        dataStorePrefs.setNeedsSubscriptionRelink(true)

        // Crashlytics follows the user id on its own (LoggingInitializer observes it).
        firebaseAnalytics.resetAnalyticsData()
    }
}
