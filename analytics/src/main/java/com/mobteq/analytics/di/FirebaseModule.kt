package com.mobteq.analytics.di

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseAnalyticsModule {

    @Singleton
    @Provides
    fun providesFirebaseAnalytics() = Firebase.analytics
}
