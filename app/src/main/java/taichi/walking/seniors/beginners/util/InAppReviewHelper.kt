package taichi.walking.seniors.beginners.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object InAppReviewHelper {

    suspend fun launchIfAvailable(activity: Activity): Boolean =
        suspendCancellableCoroutine { continuation ->
            fun finish(result: Boolean) {
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            try {
                val manager = ReviewManagerFactory.create(activity)
                manager.requestReviewFlow()
                    .addOnCompleteListener { requestTask ->
                        if (!requestTask.isSuccessful) {
                            finish(false)
                            return@addOnCompleteListener
                        }

                        val reviewInfo = runCatching { requestTask.result }.getOrNull()
                        if (reviewInfo == null) {
                            finish(false)
                            return@addOnCompleteListener
                        }

                        try {
                            manager.launchReviewFlow(activity, reviewInfo)
                                .addOnCompleteListener { launchTask ->
                                    finish(launchTask.isSuccessful)
                                }
                        } catch (_: Throwable) {
                            finish(false)
                        }
                    }
            } catch (_: Throwable) {
                finish(false)
            }
        }
}
