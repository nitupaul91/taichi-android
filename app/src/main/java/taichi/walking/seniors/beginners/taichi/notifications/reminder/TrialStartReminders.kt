package taichi.walking.seniors.beginners.taichi.notifications.reminder

import com.mobteq.billing.domain.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.util.AiAssistantRemoteConfig
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reminders armed when a free trial starts (iOS does this in PaywallView right after a confirmed
 * free-trial transaction).
 *
 * Play Billing does not say on the purchase whether it started a trial, so a trial start is a
 * purchase made from the paywall of a product whose offer has a free phase: the mapper only picks
 * a free phase from offers Play lists as available to this user.
 *
 * Runs on its own scope because the paywall closes, and its ViewModel goes away, right after the
 * purchase is acknowledged.
 */
@Singleton
class TrialStartReminders @Inject constructor(
    private val practiceReminderScheduler: PracticeReminderScheduler,
    private val trialReminderScheduler: TrialReminderScheduler,
    private val remoteConfig: AiAssistantRemoteConfig
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun onPurchaseAcknowledged(product: Product, purchasedAt: ZonedDateTime = ZonedDateTime.now()) {
        if (product.freeTrial.isNullOrBlank()) return

        scope.launch {
            if (remoteConfig.showTrialEndingReminder()) {
                TrialReminderTimes.trialEnd(product, start = purchasedAt)
                    ?.let { trialReminderScheduler.schedule(trialEnd = it) }
            }
            practiceReminderScheduler.scheduleAfterTrialStart(now = purchasedAt)
        }
    }
}
