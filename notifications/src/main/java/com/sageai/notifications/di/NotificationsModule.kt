package com.sageai.notifications.di

import com.sageai.notifications.api.ConfigApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.Retrofit
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class NotificationsModule {

    private val _logLevelFlow = MutableSharedFlow<String>()

    @Provides
    fun provideConfigApi(retrofit: Retrofit): ConfigApi {
        return retrofit.create(ConfigApi::class.java)
    }

    @Provides
    @Named("log_level_shared_flow")
    fun provideLogLevelSharedFlow(): MutableSharedFlow<String> {
        return _logLevelFlow
    }

    @Provides
    @Named("log_level_flow")
    fun provideLogLevelFlow(): Flow<String> {
        return _logLevelFlow
    }
}