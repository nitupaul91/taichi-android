package taichi.walking.seniors.beginners.taichi.notifications.reminder

import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.roundToInt

/** Pure time rules for the practice reminder, kept apart from Android so they can be unit tested. */
internal object PracticeReminderTimes {
    private const val ROUNDING_MINUTES = 15
    private const val LATEST_ROUNDED_MINUTE = 45
    private val DAYTIME_HOURS = 9 until 20
    private const val MORNING_HOUR = 10

    /**
     * Roughly the same time tomorrow: the user's last chosen reminder time if they have one,
     * otherwise now rounded to the nearest quarter hour (never past :45, so the hour stays the same).
     */
    fun suggestedTime(now: ZonedDateTime, preferredTime: LocalTime?): ZonedDateTime {
        val tomorrow = now.plusDays(1).withSecond(0).withNano(0)

        if (preferredTime != null) {
            return tomorrow.withHour(preferredTime.hour).withMinute(preferredTime.minute)
        }

        val rounded = (now.minute.toDouble() / ROUNDING_MINUTES).roundToInt() * ROUNDING_MINUTES
        return tomorrow.withHour(now.hour).withMinute(minOf(rounded, LATEST_ROUNDED_MINUTE))
    }

    /** Roughly the same time tomorrow; a trial started late at night is reminded in the morning instead. */
    fun timeAfterTrialStart(now: ZonedDateTime, preferredTime: LocalTime?): ZonedDateTime {
        val sameTime = suggestedTime(now, preferredTime)
        if (sameTime.hour in DAYTIME_HOURS) return sameTime
        return sameTime.withHour(MORNING_HOUR).withMinute(0)
    }
}
