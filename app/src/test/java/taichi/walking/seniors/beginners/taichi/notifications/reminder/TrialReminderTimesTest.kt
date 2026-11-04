package taichi.walking.seniors.beginners.taichi.notifications.reminder

import com.mobteq.billing.domain.SubscriptionProduct
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Mirrors iOS `TrialReminder.reminderDate` and `PracticeProgressStore.typicalPracticeHour / practiceDayCount`. */
class TrialReminderTimesTest {

    private val zone = ZoneId.of("Europe/Bucharest")

    private fun at(day: Int, hour: Int, minute: Int = 0): ZonedDateTime =
        ZonedDateTime.of(2026, 11, day, hour, minute, 0, 0, zone)

    @Test
    fun `the reminder is two days before the end, at the usual practice hour`() {
        val reminder = TrialReminderTimes.reminderDate(
            trialEnd = at(day = 11, hour = 15, minute = 20),
            after = at(day = 4, hour = 15, minute = 20),
            typicalPracticeHour = 18
        )

        assertEquals(at(day = 9, hour = 18), reminder)
    }

    @Test
    fun `without practice history it pins to 10 in the morning`() {
        val reminder = TrialReminderTimes.reminderDate(
            trialEnd = at(day = 11, hour = 15, minute = 20),
            after = at(day = 4, hour = 15, minute = 20),
            typicalPracticeHour = null
        )

        assertEquals(at(day = 9, hour = 10), reminder)
    }

    @Test
    fun `an hour that is already past falls back to the raw two-day offset`() {
        val reminder = TrialReminderTimes.reminderDate(
            trialEnd = at(day = 11, hour = 15, minute = 20),
            after = at(day = 9, hour = 12),
            typicalPracticeHour = 8
        )

        assertEquals(at(day = 9, hour = 15, minute = 20), reminder)
    }

    @Test
    fun `the typical hour is the most frequent, ties going to the most recent`() {
        assertNull(TrialReminderTimes.typicalPracticeHour(emptyList()))
        assertEquals(9, TrialReminderTimes.typicalPracticeHour(listOf(18, 9, 9, 18, 9)))
        assertEquals(18, TrialReminderTimes.typicalPracticeHour(listOf(9, 18, 9, 18)))
    }

    @Test
    fun `only the latest 14 practice hours are kept`() {
        val hours = (0 until 14).fold(emptyList<Int>()) { acc, hour -> TrialReminderTimes.appendPracticeHour(acc, hour) }

        assertEquals((1 until 14).toList() + 20, TrialReminderTimes.appendPracticeHour(hours, 20))
    }

    @Test
    fun `practice days count today and the seven days before it, once each`() {
        val today = LocalDate.of(2026, 11, 11)
        val days = listOf(
            today,
            today,
            today.minusDays(3),
            today.minusDays(7),
            today.minusDays(8)
        )

        assertEquals(3, TrialReminderTimes.practiceDayCount(days, today))
    }

    @Test
    fun `the trial ends after the offer's free phase`() {
        val start = at(day = 4, hour = 15, minute = 20)

        assertEquals(at(day = 7, hour = 15, minute = 20), TrialReminderTimes.trialEnd(product("day", 3, "3"), start))
        assertEquals(at(day = 18, hour = 15, minute = 20), TrialReminderTimes.trialEnd(product("week", 2, "14"), start))
        assertEquals(
            ZonedDateTime.of(2026, 12, 4, 15, 20, 0, 0, zone),
            TrialReminderTimes.trialEnd(product("month", 1, "30"), start)
        )
        assertEquals(at(day = 11, hour = 15, minute = 20), TrialReminderTimes.trialEnd(product(null, null, "7"), start))
        assertNull(TrialReminderTimes.trialEnd(product(null, null, null), start))
    }

    private fun product(unit: String?, value: Int?, freeTrialDays: String?) = SubscriptionProduct(
        productId = "taichi_yearly_v1",
        productName = "Yearly",
        displayPrice = "$39.99",
        price = 39_990_000L,
        freeTrial = freeTrialDays,
        subscriptionPeriodUnit = "year",
        subscriptionPeriodValue = 1,
        introOfferPaymentMode = "freeTrial",
        introOfferPeriodUnit = unit,
        introOfferPeriodValue = value,
        offerIdToken = "token"
    )
}
