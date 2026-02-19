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
    fun scheduleOnce(fcmToken: String) {
        Timber.tag(TAG).i("Scheduling fcm token upload work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder().putString(KEY_FCM_TOKEN, fcmToken).build()

        val work = OneTimeWorkRequestBuilder<ConfigUploadWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        val enqueueUniqueWork = WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            work
        )
        println(enqueueUniqueWork)
    }

    companion object {
        private const val WORK_NAME = "config_upload_work"
    }
}