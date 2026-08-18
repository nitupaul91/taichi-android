package taichi.walking.seniors.beginners.taichi.ui.progress.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Restore merges the server's progress into a device that may already hold practice the server has
 * never seen. Getting this wrong deletes a user's streak silently, so the max-merge is pinned here.
 */
class PracticeProgressMergeTest {

    private fun day(date: String, taiChi: Int, walking: Int, chair: Int = 0) =
        PracticeDaySummary(LocalDate.parse(date), taiChi, walking, chair)

    private fun remote(date: String, taiChi: Int, walking: Int, chair: Int = 0) =
        ExerciseProgressRestoreDay(date, taiChi, walking, chair)

    @Test
    fun `local-only practice survives a restore`() {
        // The whole point: an offline session the server never saw must not be wiped.
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", 20, 0)),
            remote = emptyList()
        )
        assertEquals(listOf(day("2026-08-01", 20, 0)), merged)
    }

    @Test
    fun `remote-only practice is adopted`() {
        val merged = mergePracticeSummaries(
            local = emptyList(),
            remote = listOf(remote("2026-08-01", 15, 5))
        )
        assertEquals(listOf(day("2026-08-01", 15, 5)), merged)
    }

    @Test
    fun `the higher value wins per field, never the remote one`() {
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", 30, 2)),
            remote = listOf(remote("2026-08-01", 10, 25))
        )
        // taiChi keeps the bigger local 30; walking takes the bigger remote 25.
        assertEquals(listOf(day("2026-08-01", 30, 25)), merged)
    }

    @Test
    fun `a smaller remote value never lowers local progress`() {
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", 45, 45)),
            remote = listOf(remote("2026-08-01", 0, 0))
        )
        assertEquals(listOf(day("2026-08-01", 45, 45)), merged)
    }

    @Test
    fun `disjoint dates from both sides are all kept, sorted`() {
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-03", 10, 0), day("2026-08-01", 5, 0)),
            remote = listOf(remote("2026-08-02", 7, 0))
        )
        assertEquals(
            listOf(day("2026-08-01", 5, 0), day("2026-08-02", 7, 0), day("2026-08-03", 10, 0)),
            merged
        )
    }

    @Test
    fun `an unparseable remote date is skipped without losing the rest`() {
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", 10, 0)),
            remote = listOf(remote("not-a-date", 99, 99), remote("2026-08-02", 8, 0))
        )
        assertEquals(listOf(day("2026-08-01", 10, 0), day("2026-08-02", 8, 0)), merged)
    }

    @Test
    fun `merging is idempotent`() {
        val local = listOf(day("2026-08-01", 30, 25))
        val remote = listOf(remote("2026-08-01", 10, 25))
        val once = mergePracticeSummaries(local, remote)
        val twice = mergePracticeSummaries(once, remote)
        assertEquals(once, twice)
    }

    @Test
    fun `total minutes never decrease across a merge`() {
        val local = listOf(day("2026-08-01", 30, 10), day("2026-08-02", 20, 0))
        val remote = listOf(remote("2026-08-01", 5, 40), remote("2026-08-03", 12, 0))
        val merged = mergePracticeSummaries(local, remote)
        assertTrue(merged.sumOf { it.totalMinutes } >= local.sumOf { it.totalMinutes })
    }

    @Test
    fun `seated practice merges independently of the other programs`() {
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", taiChi = 30, walking = 0, chair = 5)),
            remote = listOf(remote("2026-08-01", taiChi = 0, walking = 0, chair = 20))
        )
        // Local tai chi is kept, remote chair wins because it is larger.
        assertEquals(listOf(day("2026-08-01", 30, 0, 20)), merged)
    }

    @Test
    fun `chair-only practice restored from iOS is adopted on Android`() {
        val merged = mergePracticeSummaries(
            local = emptyList(),
            remote = listOf(remote("2026-08-01", 0, 0, chair = 12))
        )
        assertEquals(listOf(day("2026-08-01", 0, 0, 12)), merged)
        assertEquals(12, merged.single().totalMinutes)
    }

    @Test
    fun `a device with no seated program never zeroes remote chair minutes`() {
        // Android before this change stored no chair value; the merge must still not lower it.
        val merged = mergePracticeSummaries(
            local = listOf(day("2026-08-01", 10, 0, chair = 0)),
            remote = listOf(remote("2026-08-01", 10, 0, chair = 45))
        )
        assertEquals(45, merged.single().chairMinutes)
    }
}
