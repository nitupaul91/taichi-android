package taichi.walking.seniors.beginners.ui.paywall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mobteq.billing.domain.Product
import com.mobteq.billing.service.PlayStoreBillingService.Companion.YEARLY_SUBSCRIPTION_V1
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The dates a free trial runs on (iOS TrialTimelineDetails). Built from the Play Billing product
 * details: the paywall only receives offers the user is eligible for, so a product that carries a
 * free-trial phase is one this user can actually start.
 */
data class TrialTimelineDetails(
    val productId: String,
    val trialStartMillis: Long,
    val reminderMillis: Long,
    val trialEndMillis: Long,
    val trialLengthDays: Int
) {
    companion object {
        private const val FREE_TRIAL_PAYMENT_MODE = "freeTrial"

        /** Null when [product] has no free-trial phase in the offer the user would buy. */
        fun make(
            product: Product,
            startMillis: Long,
            confirmedEndMillis: Long? = null
        ): TrialTimelineDetails? {
            if (product.introOfferPaymentMode != FREE_TRIAL_PAYMENT_MODE) return null
            val unit = product.introOfferPeriodUnit ?: return null
            val value = product.introOfferPeriodValue?.takeIf { it > 0 } ?: return null
            val projectedEndMillis = addPeriod(startMillis, unit, value) ?: return null
            val endMillis = confirmedEndMillis ?: projectedEndMillis

            return TrialTimelineDetails(
                productId = product.productId,
                trialStartMillis = startMillis,
                reminderMillis = TrialReminderDate.reminderMillis(before = endMillis, after = startMillis),
                trialEndMillis = endMillis,
                trialLengthDays = periodInDays(unit, value)
            )
        }

        /**
         * Any product that carries a free trial, preferring the yearly plan (iOS
         * OnboardingRootView.defaultTrialDetails), so the timeline never depends on one SKU.
         */
        fun preferredFor(products: List<Product>, startMillis: Long): TrialTimelineDetails? {
            val preferredFirst = products.filter { it.productId == YEARLY_SUBSCRIPTION_V1 } +
                products.filter { it.productId != YEARLY_SUBSCRIPTION_V1 }
            return preferredFirst.firstNotNullOfOrNull { make(it, startMillis) }
        }

        private fun addPeriod(startMillis: Long, unit: String, value: Int): Long? {
            val field = when (unit) {
                "day" -> Calendar.DAY_OF_YEAR
                "week" -> Calendar.WEEK_OF_YEAR
                "month" -> Calendar.MONTH
                "year" -> Calendar.YEAR
                else -> return null
            }
            return Calendar.getInstance().apply {
                timeInMillis = startMillis
                add(field, value)
            }.timeInMillis
        }

        /** Same rounding as iOS `SubscriptionPeriod.asDays`. */
        private fun periodInDays(unit: String, value: Int): Int = when (unit) {
            "week" -> value * 7
            "month" -> value * 30
            "year" -> value * 365
            else -> value
        }
    }
}

/**
 * When the "your trial is ending" reminder fires (iOS TrialReminder.reminderDate): two days before
 * the trial ends, at the usual practice hour, falling back to the raw offset when pinning the hour
 * would leave the trial window. The trial reminder scheduler should use this too, so the date the
 * timeline promises is the date that fires.
 */
object TrialReminderDate {
    const val DAYS_BEFORE_END = 2
    private const val FALLBACK_PRACTICE_HOUR = 10

    fun reminderMillis(
        before: Long,
        after: Long,
        practiceHour: Int = FALLBACK_PRACTICE_HOUR
    ): Long {
        val raw = Calendar.getInstance().apply {
            timeInMillis = before
            add(Calendar.DAY_OF_YEAR, -DAYS_BEFORE_END)
        }
        val pinned = (raw.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, practiceHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return if (pinned in (after + 1) until before) pinned else raw.timeInMillis
    }
}

/**
 * How confidently the app can promise a "your trial is ending" reminder (iOS
 * TrialReminderMessaging). Resolved before a timeline is shown so its copy never changes under the
 * user. [analyticsValue] matches the iOS case names sent as `reminder_messaging`.
 */
enum class TrialReminderMessaging(val analyticsValue: String) {
    AUTHORIZED("authorized"),
    PERMISSION_NEEDED("permissionNeeded"),
    UNAVAILABLE("unavailable");

    companion object {
        fun current(context: Context, remindersEnabled: Boolean): TrialReminderMessaging {
            if (!remindersEnabled) return UNAVAILABLE
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) return AUTHORIZED

            // Android 13+ asks at runtime, so a missing grant can still be requested. Below 13 a
            // disabled app means the user turned notifications off in system settings.
            val canStillAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            return if (canStillAsk) PERMISSION_NEEDED else UNAVAILABLE
        }
    }
}

/** "Nov 6" in the user's locale, like iOS `.dateTime.month(.abbreviated).day()`. */
fun formatShortTrialDate(millis: Long, locale: Locale = Locale.getDefault()): String {
    val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd")
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}
