package taichi.walking.seniors.beginners.taichi.notifications.reminder

import com.mobteq.billing.domain.Product
import java.time.LocalDate
import java.time.ZonedDateTime

/** Pure rules for the "your trial ends in 2 days" reminder (iOS `TrialReminder`), unit tested. */
internal object TrialReminderTimes {
    private const val DAYS_BEFORE_END = 2L

    /** Used when the user has no practice history yet to infer an hour from. */
    private const val FALLBACK_PRACTICE_HOUR = 10

    private const val RECENT_PRACTICE_HOURS_LIMIT = 14
    private const val PRACTICE_COUNT_WINDOW_DAYS = 7L

    /**
     * Two days before the trial ends, at the hour the user usually practices. Falls back to the raw
     * offset if pinning the hour would put the reminder outside ([after], [trialEnd]).
     */
    fun reminderDate(trialEnd: ZonedDateTime, after: ZonedDateTime, typicalPracticeHour: Int?): ZonedDateTime {
        val raw = trialEnd.minusDays(DAYS_BEFORE_END)
        val pinned = raw
            .withHour(typicalPracticeHour ?: FALLBACK_PRACTICE_HOUR)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        return if (pinned.isAfter(after) && pinned.isBefore(trialEnd)) pinned else raw
    }

    /**
     * The hour the user most often practices at, from their recent sessions; null until they have
     * practiced once. Ties break toward the most recent hour, the last element of the window.
     */
    fun typicalPracticeHour(recentHours: List<Int>): Int? =
        recentHours
            .groupingBy { it }
            .eachCount()
            .entries
            .maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenBy { recentHours.lastIndexOf(it.key) })
            ?.key

    fun appendPracticeHour(recentHours: List<Int>, hour: Int): List<Int> =
        (recentHours + hour).takeLast(RECENT_PRACTICE_HOURS_LIMIT)

    /** Distinct days with practice from a week before [today] through [today], as iOS counts them. */
    fun practiceDayCount(practiceDays: Collection<LocalDate>, today: LocalDate): Int {
        val earliest = today.minusDays(PRACTICE_COUNT_WINDOW_DAYS)
        return practiceDays.toSet().count { !it.isBefore(earliest) }
    }

    /**
     * When a trial bought now ends: the start plus the offer's free phase. iOS reads the confirmed
     * expiry from the StoreKit transaction; Play's purchase carries none, so this is the projection
     * iOS falls back to as well.
     */
    fun trialEnd(product: Product, start: ZonedDateTime): ZonedDateTime? {
        val value = product.introOfferPeriodValue?.toLong()
        val projected = when (product.introOfferPeriodUnit) {
            "day" -> value?.let(start::plusDays)
            "week" -> value?.let(start::plusWeeks)
            "month" -> value?.let(start::plusMonths)
            "year" -> value?.let(start::plusYears)
            else -> null
        }
        return projected ?: product.freeTrial?.trim()?.toLongOrNull()?.let(start::plusDays)
    }
}
