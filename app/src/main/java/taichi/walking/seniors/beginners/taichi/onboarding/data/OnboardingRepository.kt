package taichi.walking.seniors.beginners.taichi.onboarding.data

import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observeCompletion(): Flow<Boolean>
    suspend fun saveAnswers(state: OnboardingState)
    suspend fun markCompleted()
}
