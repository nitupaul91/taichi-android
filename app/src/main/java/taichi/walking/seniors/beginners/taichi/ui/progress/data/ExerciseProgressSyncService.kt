package taichi.walking.seniors.beginners.taichi.ui.progress.data

import com.sageai.id.IDService
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseProgressSyncService @Inject constructor(
    private val exerciseProgressApi: ExerciseProgressApi,
    private val practiceRepository: PracticeRepository,
    private val idService: IDService
) {
    /**
     * Pulls server-side progress, merges it into local storage, then pushes the merged result back
     * so both sides converge. Safe to call on every home-screen open: the merge only ever raises
     * values, and the upload is skipped when nothing changed.
     *
     * @return true if local progress was updated.
     */
    suspend fun restoreAndMergeProgress(): Boolean {
        val userId = runCatching { idService.getUserID() }.getOrNull()
        if (userId.isNullOrBlank()) {
            Timber.w("Skipping exercise progress restore because user ID is unavailable")
            return false
        }

        val remote = try {
            val response = exerciseProgressApi.restoreProgress(userId)
            if (!response.isSuccessful) {
                Timber.w("Exercise progress restore failed with code %s", response.code())
                return false
            }
            response.body() ?: return false
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Exercise progress restore failed")
            return false
        }

        val changed = practiceRepository.mergeRemoteProgress(remote)
        if (changed) {
            // Push the merged view back so the server also learns about local-only practice.
            uploadProgress()
        }
        return changed
    }

    suspend fun uploadProgress() {
        val userId = runCatching { idService.getUserID() }.getOrNull()
        if (userId.isNullOrBlank()) {
            Timber.w("Skipping exercise progress upload because user ID is unavailable")
            return
        }

        val requestBody = ExerciseProgressSyncUploadBody(
            userId = userId,
            payload = practiceRepository.syncPayload()
        )

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val response = exerciseProgressApi.uploadProgress(requestBody)
                if (response.isSuccessful) {
                    return
                }
                Timber.w("Exercise progress upload failed with code %s on attempt %s", response.code(), attempt + 1)
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Exercise progress upload failed on attempt %s", attempt + 1)
            }

            if (attempt < MAX_ATTEMPTS - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 2_000L
    }
}
