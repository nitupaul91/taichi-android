package com.mobteq.billing.di

import com.mobteq.billing.api.PurchaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun providesSubscriptionApi(retrofit: Retrofit): PurchaseApi {
        return retrofit.create(PurchaseApi::class.java)
    }

}