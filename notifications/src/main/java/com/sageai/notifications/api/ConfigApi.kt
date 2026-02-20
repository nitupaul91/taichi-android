package com.sageai.notifications.api

import com.sageai.notifications.api.model.UploadConfigBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ConfigApi {

    @POST("config/android/upload")
    suspend fun uploadConfig(
        @Body body: UploadConfigBody
    ): Response<ResponseBody>
}
