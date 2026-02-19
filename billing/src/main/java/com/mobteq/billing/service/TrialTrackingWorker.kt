package com.mobteq.billing.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobteq.billing.api.PurchaseApi
import com.sageai.id.IDService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TrialTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val purchaseApi: PurchaseApi,
    private val billingAnalyticsTracker: BillingAnalyticsTracker,
    private val idService: IDService,
    private val trialTrackingWorkScheduler: TrialTrackingWorkScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).i("Executing trial renew tracking work")

        val response = purchaseApi.hasTrialRenewed(userId = idService.getUserID())

        Timber.tag(TAG).d("API response success: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            return Result.retry()
        }

        val hasTrialRenewed = response.body()
        if (hasTrialRenewed == true) {
            Timber.tag(TAG).d("Trial renewed. Tracking event")

            billingAnalyticsTracker.trackPurchase(getProductId())
        } else if (shouldRescheduleIfNotRenewed()) {
            Timber.tag(TAG).d("Trial not yet renewed. Rescheduling work once.")

            trialTrackingWorkScheduler.scheduleOnce(getProductId(), false)
        }

        return Result.success()
    }

    private fun getProductId(): String {
        return requireNotNull(inputData.getString(KEY_PRODUCT_ID))
    }

    private fun shouldRescheduleIfNotRenewed(): Boolean {
        return inputData.getBoolean(KEY_RESCHEDULE_IF_NOT_RENEWED, false)
    }

    companion object {
        const val TAG = "TrialTracking"

        const val KEY_PRODUCT_ID = "product_id"
        const val KEY_RESCHEDULE_IF_NOT_RENEWED = "reschedule_if_not_renewed"
    }
}