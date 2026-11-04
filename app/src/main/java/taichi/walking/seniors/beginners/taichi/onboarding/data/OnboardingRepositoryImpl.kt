package taichi.walking.seniors.beginners.taichi.onboarding.data

import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import com.google.gson.Gson
import com.sageai.notifications.worker.ConfigUploadWorkScheduler
import kotlinx.coroutines.flow.Flow
import taichi.walking.seniors.beginners.taichi.music.WorkoutMusicService

class OnboardingRepositoryImpl(
    private val prefs: OnboardingPrefs,
    private val configUploadWorkScheduler: ConfigUploadWorkScheduler,
    private val onboardingProfileSyncService: OnboardingProfileSyncService,
    private val workoutMusicService: WorkoutMusicService
) : OnboardingRepository {

    private val gson = Gson()

    override fun observeCompletion(): Flow<Boolean> = prefs.observeCompleted()

    override suspend fun saveAnswers(state: OnboardingState) {
        prefs.setAnswersJson(gson.toJson(state))
    }

    override suspend fun persistSnapshot(state: OnboardingState, completedStep: String) {
        // Local first: the upload is best-effort, but the answers must survive regardless.
        saveAnswers(state)
        // Music lives on the CDN, not in the APK; warm the cache while the user is busy answering
        // questions so the first workout has music even offline. Fire-and-forget, once per process.
        workoutMusicService.prefetchIfNeeded()
        configUploadWorkScheduler.scheduleOnce()
        onboardingProfileSyncService.uploadSnapshot(state, completedStep)
    }

    override suspend fun markCompleted() {
        prefs.setCompleted(true)
        configUploadWorkScheduler.scheduleOnce()
    }
}
