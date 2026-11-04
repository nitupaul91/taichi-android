package taichi.walking.seniors.beginners.ui.paywall

import com.mobteq.billing.domain.OneTimePurchaseProduct
import com.mobteq.billing.domain.SubscriptionProduct
import com.mobteq.billing.service.PlayStoreBillingService.Companion.MONTHLY_SUBSCRIPTION_V1
import com.mobteq.billing.service.PlayStoreBillingService.Companion.YEARLY_SUBSCRIPTION_V1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Calendar

/**
 * The trial dates are promised to the user on the timeline, so they must come from the Play offer
 * the user would actually buy, and the reminder must land inside the trial window.
 */
class TrialTimelineDetailsTest {

    private val start: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 4, 15, 30, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun subscription(
        id: String,
        trialUnit: String? = "week",
        trialValue: Int? = 1
    ) = SubscriptionProduct(
        productId = id,
        productName = id,
        displayPrice = "€49.99",
        price = 49_990_000L,
        freeTrial = trialValue?.let { (it * 7).toString() },
        subscriptionPeriodUnit = "year",
        subscriptionPeriodValue = 1,
        introOfferPaymentMode = trialUnit?.let { "freeTrial" },
        introOfferPeriodUnit = trialUnit,
        introOfferPeriodValue = trialValue,
        offerIdToken = "token"
    )

    private fun calendarAt(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }

    @Test
    fun `a one-week trial ends seven days after it starts`() {
        val details = TrialTimelineDetails.make(subscription(YEARLY_SUBSCRIPTION_V1), start)!!

        val end = calendarAt(start).apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
        assertEquals(end, details.trialEndMillis)
        assertEquals(7, details.trialLengthDays)
        assertEquals(YEARLY_SUBSCRIPTION_V1, details.productId)
    }

    @Test
    fun `the reminder is two days before the end at the practice hour`() {
        val details = TrialTimelineDetails.make(subscription(YEARLY_SUBSCRIPTION_V1), start)!!

        val reminder = calendarAt(details.reminderMillis)
        assertEquals(9, reminder.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, reminder.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, reminder.get(Calendar.MINUTE))
    }

    @Test
    fun `a pinned hour outside the trial falls back to the raw offset`() {
        // A two-day trial started at 15:30: two days before the end is the start itself, and 10:00
        // that day is before the trial began, so the raw offset is kept.
        val twoDayTrial = subscription(MONTHLY_SUBSCRIPTION_V1, trialUnit = "day", trialValue = 2)
        val details = TrialTimelineDetails.make(twoDayTrial, start)!!

        assertEquals(start, details.reminderMillis)
    }

    @Test
    fun `a confirmed end date wins over the projected one`() {
        val confirmedEnd = start + 3 * 24 * 60 * 60 * 1000L
        val details = TrialTimelineDetails.make(
            subscription(YEARLY_SUBSCRIPTION_V1),
            start,
            confirmedEndMillis = confirmedEnd
        )!!

        assertEquals(confirmedEnd, details.trialEndMillis)
    }

    @Test
    fun `no free-trial phase means no timeline`() {
        assertNull(TrialTimelineDetails.make(subscription(YEARLY_SUBSCRIPTION_V1, trialUnit = null, trialValue = null), start))
        assertNull(
            TrialTimelineDetails.make(
                OneTimePurchaseProduct("lifetime", "Lifetime", "€99", 99_000_000L, null),
                start
            )
        )
    }

    @Test
    fun `the yearly plan is preferred when several carry a trial`() {
        val products = listOf(
            subscription(MONTHLY_SUBSCRIPTION_V1, trialUnit = "day", trialValue = 3),
            subscription(YEARLY_SUBSCRIPTION_V1)
        )

        assertEquals(YEARLY_SUBSCRIPTION_V1, TrialTimelineDetails.preferredFor(products, start)?.productId)
    }

    @Test
    fun `any plan with a trial is used when the yearly one has none`() {
        val products = listOf(
            subscription(YEARLY_SUBSCRIPTION_V1, trialUnit = null, trialValue = null),
            subscription(MONTHLY_SUBSCRIPTION_V1, trialUnit = "day", trialValue = 3)
        )

        val details = TrialTimelineDetails.preferredFor(products, start)!!
        assertEquals(MONTHLY_SUBSCRIPTION_V1, details.productId)
        assertEquals(3, details.trialLengthDays)
    }
}
