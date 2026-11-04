package taichi.walking.seniors.beginners.taichi.notifications.reminder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** The suggested time is the default the prompt offers, so it must match iOS's `PracticeReminder.suggestedTime`. */
class PracticeReminderTimesTest {

    private val zone = ZoneId.of("Europe/Bucharest")

    private fun at(hour: Int, minute: Int, second: Int = 0): ZonedDateTime =
        ZonedDateTime.of(2026, 11, 4, hour, minute, second, 123, zone)

    @Test
    fun `a stored preferred time wins, tomorrow`() {
        val suggested = PracticeReminderTimes.suggestedTime(at(21, 40), LocalTime.of(8, 30))

        assertEquals(ZonedDateTime.of(2026, 11, 5, 8, 30, 0, 0, zone), suggested)
    }

    @Test
    fun `without a preference it is now rounded to the nearest quarter hour, tomorrow`() {
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zone),
            PracticeReminderTimes.suggestedTime(at(10, 7, 42), preferredTime = null)
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 15, 0, 0, zone),
            PracticeReminderTimes.suggestedTime(at(10, 8), preferredTime = null)
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 30, 0, 0, zone),
            PracticeReminderTimes.suggestedTime(at(10, 37), preferredTime = null)
        )
    }

    @Test
    fun `rounding never rolls over into the next hour`() {
        val suggested = PracticeReminderTimes.suggestedTime(at(18, 55), preferredTime = null)

        assertEquals(ZonedDateTime.of(2026, 11, 5, 18, 45, 0, 0, zone), suggested)
    }

    @Test
    fun `a daytime trial start is reminded at the same time tomorrow`() {
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 14, 0, 0, 0, zone),
            PracticeReminderTimes.timeAfterTrialStart(at(14, 7), preferredTime = null)
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 19, 45, 0, 0, zone),
            PracticeReminderTimes.timeAfterTrialStart(at(19, 52), preferredTime = null)
        )
    }

    @Test
    fun `a trial started outside 9 to 20 is reminded at 10 the next morning`() {
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zone),
            PracticeReminderTimes.timeAfterTrialStart(at(22, 30), preferredTime = null)
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zone),
            PracticeReminderTimes.timeAfterTrialStart(at(8, 50), preferredTime = null)
        )
        assertEquals(
            ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, zone),
            PracticeReminderTimes.timeAfterTrialStart(at(13, 0), preferredTime = LocalTime.of(20, 0))
        )
    }
}
