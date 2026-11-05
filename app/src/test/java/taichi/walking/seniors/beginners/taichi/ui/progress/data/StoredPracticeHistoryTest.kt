package taichi.walking.seniors.beginners.taichi.ui.progress.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The practice history users already have on their phones was written by release builds whose
 * StoredPracticeDaySummary fields R8 had renamed to a/b/c/d (1.0.9 mapping.txt). Losing it would
 * reset every updating user's Progress tab, so both shapes must decode.
 */
class StoredPracticeHistoryTest {

    private val gson = Gson()
    private val type = object : TypeToken<List<PracticeRepository.StoredPracticeDaySummary>>() {}.type

    private fun decode(json: String): List<PracticeRepository.StoredPracticeDaySummary> = gson.fromJson(json, type)

    @Test
    fun `history written by 1_0_9 release builds still decodes`() {
        val stored = decode("""[{"a":"2026-08-20","b":12,"c":5,"d":3},{"a":"2026-08-21","b":10,"c":0,"d":0}]""")

        assertEquals(
            listOf(
                PracticeRepository.StoredPracticeDaySummary("2026-08-20", 12, 5, 3),
                PracticeRepository.StoredPracticeDaySummary("2026-08-21", 10, 0, 0)
            ),
            stored
        )
    }

    @Test
    fun `history from builds before the seated program has no chair minutes`() {
        val stored = decode("""[{"a":"2026-05-02","b":8,"c":4}]""")

        assertEquals(listOf(PracticeRepository.StoredPracticeDaySummary("2026-05-02", 8, 4, 0)), stored)
    }

    @Test
    fun `new builds write the real names and read them back`() {
        val written = gson.toJson(listOf(PracticeRepository.StoredPracticeDaySummary("2026-11-05", 15, 0, 7)))

        assertEquals("""[{"date":"2026-11-05","taiChiMinutes":15,"walkingMinutes":0,"chairMinutes":7}]""", written)
        assertEquals(listOf(PracticeRepository.StoredPracticeDaySummary("2026-11-05", 15, 0, 7)), decode(written))
    }
}
