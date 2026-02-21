package com.sageai.notifications.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sageai.notifications.worker.ConfigUploadWorker.Companion.KEY_FCM_TOKEN
import com.sageai.notifications.worker.ConfigUploadWorker.Companion.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class ConfigUploadWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleOnce(fcmToken: String? = null) {
        Timber.tag(TAG).i("Scheduling fcm token upload work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dataBuilder = Data.Builder()
        if (fcmToken != null) {
            dataBuilder.putString(KEY_FCM_TOKEN, fcmToken)
        }

        val work = OneTimeWorkRequestBuilder<ConfigUploadWorker>()
            .setConstraints(constraints)
            .setInputData(dataBuilder.build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    companion object {
        private const val WORK_NAME = "config_upload_work"
    }
}