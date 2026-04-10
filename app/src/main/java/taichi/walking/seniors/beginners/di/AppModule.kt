package taichi.walking.seniors.beginners.di

import taichi.walking.seniors.beginners.BuildConfig
import taichi.walking.seniors.beginners.data.api.UserApi
import taichi.walking.seniors.beginners.taichi.ui.progress.data.ExerciseProgressApi
import taichi.walking.seniors.beginners.taichi.ui.home.data.WorkoutApi
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mobteq.billing.api.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    fun provideRetrofit(): Retrofit {
        return RetrofitFactory.getRetrofitInstance()
    }

    @Provides
    fun provideGson(): Gson = Gson()

    @Provides
    fun providesUserRegistrationApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkoutApi(retrofit: Retrofit): WorkoutApi {
        return retrofit.create(WorkoutApi::class.java)
    }

    @Provides
    @Singleton
    fun provideExerciseProgressApi(retrofit: Retrofit): ExerciseProgressApi {
        return retrofit.create(ExerciseProgressApi::class.java)
    }

    @Provides
    @Named("version_code")
    fun provideVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }
}
