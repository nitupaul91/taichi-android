package com.mobteq.billing.api

import com.google.gson.Gson
import com.mobteq.billing.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {

    const val URL_EMULATOR = "http://10.0.2.2:8080/"
    const val URL_SANDU = "http://192.168.0.113:8080"
    const val URL_SANDU_AIUD = "http://192.168.100.11:8080"
    const val URL_ACASA = "http://192.168.0.104:8080"
    const val URL_PAUL = "http://192.168.100.172:8080"

    const val URL_PROD = "https://taichi.aimobileapps.net/"

    const val BASE_URL = URL_PROD

    fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create(
                    Gson().newBuilder()
                        .setLenient()
                        .create()
                )
            )
            .baseUrl(BASE_URL)
            .client(getClient())
            .build()
    }

    private fun getClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val clientBuilder = OkHttpClient.Builder()

        // commented out because it breaks ask response streaming
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(interceptor)
        }

        return clientBuilder
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}