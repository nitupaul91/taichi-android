package com.mobteq.billing.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mobteq.billing.service.TrialTrackingWorker.Companion.KEY_PRODUCT_ID
import com.mobteq.billing.service.TrialTrackingWorker.Companion.KEY_RESCHEDULE_IF_NOT_RENEWED
import com.mobteq.billing.service.TrialTrackingWorker.Companion.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TrialTrackingWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleOnce(productId: String, rescheduleIfNotRenewed: Boolean) {
        Timber.tag(TAG).i("Scheduling trial renew tracking work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putString(KEY_PRODUCT_ID, productId)
            .putBoolean(KEY_RESCHEDULE_IF_NOT_RENEWED, rescheduleIfNotRenewed)
            .build()

        val work = OneTimeWorkRequestBuilder<TrialTrackingWorker>()
            .setInitialDelay(80, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    companion object {
        private const val WORK_NAME = "trial_tracking_work"
    }
}