package taichi.walking.seniors.beginners.taichi.onboarding.data

import com.sageai.id.IDService
import kotlinx.coroutines.delay
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads an onboarding snapshot after every step, mirroring what the iOS app does from
 * `onChange(of: currentStep)`. That cadence is the point: `onboarding_profiles` means "started
 * onboarding", so it is the install denominator for CPI/ROAS. Uploading only at the paywall would
 * redefine an Android "install" as "reached the paywall" and make the two platforms incomparable.
 *
 * Never throws: onboarding must keep working with no network. The answers are already persisted
 * locally by the repository, so a dropped upload is re-sent by the next step's snapshot.
 */
@Singleton
class OnboardingProfileSyncService @Inject constructor(
    private val onboardingProfileApi: OnboardingProfileApi,
    private val idService: IDService
) {
    suspend fun uploadSnapshot(state: OnboardingState, completedStep: String) {
        // getUserID(), NOT getDeviceID(): this must be the id purchases are recorded against, or the
        // profile lands on a different user document than the subscription and the install shows up
        // with no revenue.
        val userId = runCatching { idService.getUserID() }.getOrNull()
        if (userId.isNullOrBlank()) {
            Timber.w("Skipping onboarding profile upload because user ID is unavailable")
            return
        }

        val body = state.toOnboardingProfilePayload(userId = userId, completedStep = completedStep)

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val response = onboardingProfileApi.uploadOnboardingProfile(body)
                if (response.isSuccessful) {
                    return
                }
                Timber.w(
                    "Onboarding profile upload failed with code %s on attempt %s",
                    response.code(),
                    attempt + 1
                )
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Onboarding profile upload failed on attempt %s", attempt + 1)
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
