package taichi.walking.seniors.beginners.taichi.onboarding.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OnboardingProfileApi {

    /**
     * Android counterpart of the iOS app's `profile/iOS/upload`. The backend normalises this into
     * the same `onboarding_profiles` document both platforms share, which is the install cohort
     * every acquisition dashboard divides by.
     */
    @POST("profile/android/upload")
    suspend fun uploadOnboardingProfile(@Body body: OnboardingProfilePayload): Response<Unit>
}
