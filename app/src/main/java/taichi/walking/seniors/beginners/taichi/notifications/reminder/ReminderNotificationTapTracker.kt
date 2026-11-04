package taichi.walking.seniors.beginners.taichi.notifications.reminder

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject

/**
 * Reports `notification_tapped` when an activity is opened from a reminder notification, so the
 * reminder experiments can measure opens the way iOS does.
 */
class ReminderNotificationTapTracker @Inject constructor(
    private val analytics: ReminderAnalyticsTracker
) {

    /**
     * Call from `onCreate` (fresh launches only) and `onNewIntent`. The extra is removed once
     * reported, so a recreated activity never counts the same tap twice.
     */
    fun trackIfFromReminder(intent: Intent?) {
        val notificationId = intent?.getStringExtra(ReminderNotificationWorker.EXTRA_NOTIFICATION_ID) ?: return
        intent.removeExtra(ReminderNotificationWorker.EXTRA_NOTIFICATION_ID)

        // iOS reports "active" for a tap while the app is on screen and "inactive" while it is
        // being brought up from the background or launched.
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        // The conversion notification opens a paywall, like iOS's taichi://paywall?source= link.
        val paywallSource = intent.getStringExtra(EXTRA_PAYWALL_SOURCE)
        analytics.trackNotificationTapped(
            notificationId = notificationId,
            hasDeeplink = paywallSource != null,
            deeplinkSource = paywallSource.orEmpty(),
            appState = if (isForeground) {
                ReminderAnalyticsTracker.APP_STATE_ACTIVE
            } else {
                ReminderAnalyticsTracker.APP_STATE_INACTIVE
            }
        )
    }

    private companion object {
        /** TaiChiHomeActivity's paywall-source extra, set by the conversion notification. */
        const val EXTRA_PAYWALL_SOURCE = "extra_paywall_source"
    }
}
