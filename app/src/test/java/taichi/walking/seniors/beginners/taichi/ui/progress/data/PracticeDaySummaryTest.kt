package taichi.walking.seniors.beginners.taichi.ui.progress.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * `plusMinutes` replaced a hand-written per-kind `copy()` ladder in the repository, so every kind
 * must land in its own bucket — crediting a seated session to tai chi would corrupt the streak and
 * the totals silently.
 */
class PracticeDaySummaryTest {

    private val empty = PracticeDaySummary(LocalDate.parse("2026-08-01"), 0, 0)

    @Test
    fun `each kind accumulates into its own bucket`() {
        val summary = empty
            .plusMinutes(PracticeKind.TAI_CHI, 10)
            .plusMinutes(PracticeKind.WALKING, 20)
            .plusMinutes(PracticeKind.CHAIR, 30)

        assertEquals(10, summary.taiChiMinutes)
        assertEquals(20, summary.walkingMinutes)
        assertEquals(30, summary.chairMinutes)
    }

    @Test
    fun `repeated sessions of one kind add up`() {
        val summary = empty
            .plusMinutes(PracticeKind.CHAIR, 10)
            .plusMinutes(PracticeKind.CHAIR, 5)

        assertEquals(15, summary.chairMinutes)
        assertEquals(0, summary.taiChiMinutes)
    }

    @Test
    fun `total minutes counts all three programs`() {
        val summary = PracticeDaySummary(LocalDate.parse("2026-08-01"), 10, 20, 30)
        assertEquals(60, summary.totalMinutes)
    }

    @Test
    fun `minutesFor round-trips every kind`() {
        val summary = PracticeDaySummary(LocalDate.parse("2026-08-01"), 10, 20, 30)
        PracticeKind.entries.forEach { kind ->
            assertEquals(
                empty.plusMinutes(kind, summary.minutesFor(kind)).minutesFor(kind),
                summary.minutesFor(kind),
                "mismatch for $kind"
            )
        }
    }

    @Test
    fun `a seated-only day still counts as practice`() {
        val summary = empty.plusMinutes(PracticeKind.CHAIR, 8)
        assertEquals(true, summary.hasPractice())
    }
}
