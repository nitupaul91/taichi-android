package taichi.walking.seniors.beginners.taichi.printable

/** Finds the emoji in a string, so the PDF can set them apart from the surrounding weight. */
internal object EmojiRuns {

    /** Char-index ranges (inclusive) of each emoji sequence in [text], in order. */
    fun find(text: CharSequence): List<IntRange> {
        val runs = mutableListOf<IntRange>()
        var start = -1
        var end = -1
        var index = 0
        while (index < text.length) {
            val codePoint = Character.codePointAt(text, index)
            val next = index + Character.charCount(codePoint)
            when {
                isEmoji(codePoint) -> {
                    if (start < 0 || end != index) {
                        if (start >= 0) runs += start until end
                        start = index
                    }
                    end = next
                }
                // Joiners, variation selectors and skin tones extend the emoji right before them.
                start >= 0 && end == index && isSequencePart(codePoint) -> end = next
            }
            index = next
        }
        if (start >= 0) runs += start until end
        return runs
    }

    /** Pictographic symbols; Latin-1 symbols such as © or ° are left to the text font. */
    private fun isEmoji(codePoint: Int): Boolean =
        codePoint >= 0x2190 && Character.getType(codePoint) == Character.OTHER_SYMBOL.toInt()

    private fun isSequencePart(codePoint: Int): Boolean =
        codePoint == 0x200D || // zero-width joiner
            codePoint == 0xFE0E || codePoint == 0xFE0F || // text / emoji presentation
            codePoint == 0x20E3 || // combining keycap
            codePoint in 0x1F3FB..0x1F3FF || // skin tones
            codePoint in 0xE0020..0xE007F // tag sequences (subdivision flags)
}
