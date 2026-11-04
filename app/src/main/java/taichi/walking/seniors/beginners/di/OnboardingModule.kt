package taichi.walking.seniors.beginners.di

import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingPrefs
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingProfileSyncService
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepository
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepositoryImpl
import android.content.Context
import com.sageai.notifications.worker.ConfigUploadWorkScheduler
import taichi.walking.seniors.beginners.taichi.music.WorkoutMusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {

    @Provides
    @Singleton
    fun provideOnboardingPrefs(@ApplicationContext context: Context): OnboardingPrefs =
        OnboardingPrefs(context)

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        prefs: OnboardingPrefs,
        configUploadWorkScheduler: ConfigUploadWorkScheduler,
        onboardingProfileSyncService: OnboardingProfileSyncService,
        workoutMusicService: WorkoutMusicService
    ): OnboardingRepository =
        OnboardingRepositoryImpl(
            prefs,
            configUploadWorkScheduler,
            onboardingProfileSyncService,
            workoutMusicService
        )
}
