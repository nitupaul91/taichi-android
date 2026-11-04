package taichi.walking.seniors.beginners.taichi.printable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * iOS gates the whole printable plan on `show_printable_plan_paid_upsell`; Android keeps that and,
 * without a standalone product to sell, lets Premium unlock it while everyone else is sent to the
 * paywall.
 */
class PrintablePlanCardModeTest {

    @Test
    fun `flag off hides the card for everyone`() {
        listOf(true, false).forEach { premium ->
            listOf(true, false).forEach { downloaded ->
                assertEquals(
                    PrintablePlanCardMode.HIDDEN,
                    printablePlanCardMode(featureEnabled = false, isPremium = premium, downloadedBeforeThisSession = downloaded)
                )
            }
        }
    }

    @Test
    fun `premium users can download`() {
        assertEquals(
            PrintablePlanCardMode.DOWNLOAD,
            printablePlanCardMode(featureEnabled = true, isPremium = true, downloadedBeforeThisSession = false)
        )
    }

    @Test
    fun `a plan downloaded in an earlier session no longer shows the card`() {
        assertEquals(
            PrintablePlanCardMode.HIDDEN,
            printablePlanCardMode(featureEnabled = true, isPremium = true, downloadedBeforeThisSession = true)
        )
    }

    @Test
    fun `users without premium are offered the paywall, downloaded or not`() {
        listOf(true, false).forEach { downloaded ->
            assertEquals(
                PrintablePlanCardMode.UNLOCK,
                printablePlanCardMode(featureEnabled = true, isPremium = false, downloadedBeforeThisSession = downloaded)
            )
        }
    }
}
