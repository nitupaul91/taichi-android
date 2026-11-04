package taichi.walking.seniors.beginners.taichi.printable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Emoji drawn at weight 600+ vanish from the PDF (fake bold on the color-emoji font), so the
 * generator sets exactly these ranges regular. Every emoji in the printable copy must be found.
 */
class EmojiRunsTest {

    @Test
    fun `plain text has no emoji`() {
        assertEquals(emptyList<IntRange>(), EmojiRuns.find("DAY 1  ·  9 Mins © 2026 °"))
    }

    @Test
    fun `finds the emoji used by the printable plan`() {
        // 🕐 (surrogate pair) before a duration.
        assertEquals(listOf(0..1), EmojiRuns.find("🕐  45 sec"))
        // ⚖️ is a BMP symbol plus a variation selector.
        assertEquals(listOf(0..1), EmojiRuns.find("⚖️ Balance"))
        // 🌱 inside an achievement title.
        assertEquals(listOf(0..1), EmojiRuns.find("🌱  Initiate of Tai Chi"))
    }

    @Test
    fun `joined sequences stay one run and separate emoji stay apart`() {
        // 🧘🏽‍♀️: base, skin tone, zero-width joiner, female sign, variation selector.
        val joined = "🧘🏽‍♀️"
        assertEquals(listOf(0 until joined.length), EmojiRuns.find(joined))

        assertEquals(listOf(0..1, 3..4), EmojiRuns.find("🌱 🌿"))
    }

    @Test
    fun `a stray variation selector is not an emoji`() {
        assertEquals(emptyList<IntRange>(), EmojiRuns.find("a️"))
    }
}
