package com.sageai.notifications.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import com.sageai.id.IDService
import com.sageai.notifications.api.ConfigApi
import com.sageai.notifications.api.model.UploadConfigBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Named

@HiltWorker
class ConfigUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val idService: IDService,
    private val configApi: ConfigApi,
    @Named("version_code") private val appVersionCode: Int
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).i("Executing fcm token upload")

        val fcmToken = getFcmToken()
        if (fcmToken == null) {
            Timber.tag(TAG).e("FCM token not available")
            return Result.retry()
        }

        val body = UploadConfigBody(
            userId = idService.getDeviceID(),
            region = Locale.getDefault().country.ifBlank { "UN" },
            fcmToken = fcmToken,
            deviceLanguage = Locale.getDefault().displayLanguage,
            appVersion = appVersionCode,
        )
        val response = configApi.uploadConfig(body)

        Timber.tag(TAG).d("API response success: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            return Result.retry()
        }

        return Result.success()
    }

    private suspend fun getFcmToken(): String? {
        // First try to get from input data (when triggered by onNewToken)
        val inputToken = inputData.getString(KEY_FCM_TOKEN)
        if (!inputToken.isNullOrBlank()) {
            return inputToken
        }

        // Otherwise fetch from Firebase
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get FCM token")
            null
        }
    }

    companion object {
        const val TAG = "ConfigUpload"

        const val KEY_FCM_TOKEN = "fcm_token"
    }
}
