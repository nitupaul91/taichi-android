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
