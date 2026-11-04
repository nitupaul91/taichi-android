package taichi.walking.seniors.beginners.taichi.onboarding.model

/**
 * "Where did you hear about us?" answers (iOS `DiscoverySource`). The ids are the ones the Android
 * profile upload has always sent; [GOOGLE_PLAY] is the Android counterpart of iOS's `appStore`.
 */
object DiscoverySources {
    const val GOOGLE_PLAY = "google_play"
    const val GOOGLE = "google"
    const val TIKTOK = "tiktok"
    const val INSTAGRAM = "instagram"
    const val FACEBOOK = "facebook"
    const val TV_COMMERCIAL = "tv_commercial"
    const val TRAINER_THERAPIST = "trainer_therapist_recommendation"
    const val FRIENDS_FAMILY = "friends_family"
    const val OTHER = "other"

    /** iOS `standardSources` without "Other", in the same order. */
    val standardSources = listOf(GOOGLE_PLAY, GOOGLE, TIKTOK, INSTAGRAM, FACEBOOK, FRIENDS_FAMILY)

    /** Behind `show_extended_discovery_sources`. */
    val extendedSources = listOf(TV_COMMERCIAL, TRAINER_THERAPIST)

    /**
     * iOS `DiscoverySource.displayedSources`. A fixed list hands the top rows the answer by position,
     * which skews attribution, so every user gets their own order from [orderSeed] (the user id), the
     * same on every render and after back navigation; "Other" stays last. An extended source the
     * user already picked stays listed even if the flag turns off.
     *
     * The shuffle is iOS's exactly (FNV-1a seed, SplitMix64, Swift's `shuffled(using:)`), so a seed
     * gives the same order on both platforms.
     */
    fun displayedSources(
        includeExtendedSources: Boolean,
        selectedSource: String?,
        orderSeed: String
    ): List<String> {
        val showExtended = includeExtendedSources || selectedSource in extendedSources
        val sources = if (showExtended) standardSources + extendedSources else standardSources
        return swiftShuffled(sources, SeededRandomNumberGenerator(orderSeed)) + OTHER
    }

    /** 1-based row of [selectedSource] in [displayed], for the `answer_position` analytics parameter. */
    fun position(selectedSource: String?, displayed: List<String>): Int? =
        selectedSource?.let { displayed.indexOf(it) }?.takeIf { it >= 0 }?.plus(1)

    /** Swift's `MutableCollection.shuffle(using:)`: Fisher-Yates from the front. */
    private fun <T> swiftShuffled(items: List<T>, generator: SeededRandomNumberGenerator): List<T> {
        val result = items.toMutableList()
        var amount = result.size
        var current = 0
        while (amount > 1) {
            val random = generator.next(upperBound = amount.toULong()).toInt()
            amount -= 1
            val swapWith = current + random
            val tmp = result[current]
            result[current] = result[swapWith]
            result[swapWith] = tmp
            current += 1
        }
        return result
    }
}

/**
 * iOS's `SeededRandomNumberGenerator`: the seed string hashed with FNV-1a, stepped with SplitMix64.
 * [next] with an upper bound is Swift's `RandomNumberGenerator.next(upperBound:)` (Lemire's
 * multiply-shift with rejection), which `Int.random(in:using:)` and `shuffled(using:)` use.
 */
internal class SeededRandomNumberGenerator(seed: String) {
    private var state: ULong

    init {
        var hash = 0xcbf29ce484222325uL
        for (byte in seed.toByteArray(Charsets.UTF_8)) {
            hash = hash xor (byte.toULong() and 0xFFuL)
            hash *= 0x00000100000001b3uL
        }
        state = hash
    }

    fun next(): ULong {
        state += 0x9e3779b97f4a7c15uL
        var z = state
        z = (z xor (z shr 30)) * 0xbf58476d1ce4e5b9uL
        z = (z xor (z shr 27)) * 0x94d049bb133111ebuL
        return z xor (z shr 31)
    }

    fun next(upperBound: ULong): ULong {
        require(upperBound != 0uL) { "upperBound cannot be zero" }
        var random = next()
        var low = random * upperBound
        if (low < upperBound) {
            val threshold = (0uL - upperBound) % upperBound
            while (low < threshold) {
                random = next()
                low = random * upperBound
            }
        }
        return multiplyHigh(random, upperBound)
    }

    /** High 64 bits of the unsigned 128-bit product (Swift `multipliedFullWidth(by:).high`). */
    private fun multiplyHigh(a: ULong, b: ULong): ULong {
        val mask = 0xFFFFFFFFuL
        val aLo = a and mask
        val aHi = a shr 32
        val bLo = b and mask
        val bHi = b shr 32
        val loLo = aLo * bLo
        val hiLo = aHi * bLo
        val loHi = aLo * bHi
        val hiHi = aHi * bHi
        val cross = (loLo shr 32) + (hiLo and mask) + loHi
        return hiHi + (hiLo shr 32) + (cross shr 32)
    }
}
