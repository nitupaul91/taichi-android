package taichi.walking.seniors.beginners.taichi.printable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind

class PrintablePlanCacheKeyTest {

    private val days = """[{"day":1,"lengthSeconds":540}]"""

    @Test
    fun `same plan, program and language reuse the cached file`() {
        assertEquals(
            PrintablePlanCacheKey.build(PracticeKind.TAI_CHI, "en-US", days),
            PrintablePlanCacheKey.build(PracticeKind.TAI_CHI, "en-US", days)
        )
    }

    @Test
    fun `a changed plan, program or language produces a new file`() {
        val base = PrintablePlanCacheKey.build(PracticeKind.TAI_CHI, "en-US", days)

        assertNotEquals(base, PrintablePlanCacheKey.build(PracticeKind.TAI_CHI, "en-US", days.replace("540", "600")))
        assertNotEquals(base, PrintablePlanCacheKey.build(PracticeKind.WALKING, "en-US", days))
        assertNotEquals(base, PrintablePlanCacheKey.build(PracticeKind.TAI_CHI, "de-DE", days))
    }

    @Test
    fun `keys start with the program prefix used to clean up older files`() {
        PracticeKind.entries.forEach { kind ->
            val key = PrintablePlanCacheKey.build(kind, "en", days)
            assertTrue(key.startsWith(PrintablePlanCacheKey.prefix(kind)), key)
            PracticeKind.entries.filter { it != kind }.forEach { other ->
                assertTrue(!key.startsWith(PrintablePlanCacheKey.prefix(other)), "$key vs $other")
            }
        }
    }

    @Test
    fun `language tags are reduced to path-safe characters`() {
        assertTrue(PrintablePlanCacheKey.build(PracticeKind.CHAIR, "sr-Latn-RS", days).startsWith("chair_sr-Latn-RS_"))
        assertTrue(PrintablePlanCacheKey.build(PracticeKind.CHAIR, "x/../y", days).startsWith("chair_x_y_"))
        assertTrue(PrintablePlanCacheKey.build(PracticeKind.CHAIR, "", days).startsWith("chair_und_"))
    }

    @Test
    fun `file names keep the localized title and drop characters share targets reject`() {
        assertEquals("Tai Chi 7-Day Plan.pdf", PrintablePlanCacheKey.fileName("Tai Chi 7-Day Plan"))
        assertEquals("Plan 7 jours.pdf", PrintablePlanCacheKey.fileName("Plan: 7 / jours"))
        assertEquals("Tai Chi Walking.pdf", PrintablePlanCacheKey.fileName("  Tai Chi\nWalking  "))
        assertEquals("printable-plan.pdf", PrintablePlanCacheKey.fileName("???"))
    }
}
