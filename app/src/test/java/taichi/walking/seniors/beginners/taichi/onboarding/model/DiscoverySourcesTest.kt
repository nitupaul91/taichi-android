package taichi.walking.seniors.beginners.taichi.onboarding.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiscoverySourcesTest {

    /** iOS raw value -> Android id, to compare against the Swift reference output. */
    private val iosToAndroid = mapOf(
        "appStore" to DiscoverySources.GOOGLE_PLAY,
        "google" to DiscoverySources.GOOGLE,
        "tiktok" to DiscoverySources.TIKTOK,
        "instagram" to DiscoverySources.INSTAGRAM,
        "facebook" to DiscoverySources.FACEBOOK,
        "friendsFamily" to DiscoverySources.FRIENDS_FAMILY,
        "tvCommercial" to DiscoverySources.TV_COMMERCIAL,
        "trainerTherapistRecommendation" to DiscoverySources.TRAINER_THERAPIST
    )

    /**
     * Produced by running iOS's SeededRandomNumberGenerator with Swift's `shuffled(using:)` (swift 6,
     * OnboardingDataModel.swift as of df6cb64) for these seeds: seed | standard | with extended.
     */
    private val swiftReference = listOf(
        "|facebook,google,instagram,tiktok,appStore,friendsFamily|tvCommercial,google,instagram,tiktok,friendsFamily,trainerTherapistRecommendation,appStore,facebook",
        "user-1|friendsFamily,tiktok,google,appStore,facebook,instagram|trainerTherapistRecommendation,instagram,google,tvCommercial,facebook,tiktok,friendsFamily,appStore",
        "8f14e45f-ceea-467a-9c1e-2b3d4e5f6a7b|instagram,facebook,tiktok,friendsFamily,google,appStore|facebook,tvCommercial,tiktok,trainerTherapistRecommendation,appStore,instagram,friendsFamily,google",
        "a1b2c3d4e5f60718|tiktok,google,friendsFamily,appStore,instagram,facebook|tiktok,google,trainerTherapistRecommendation,appStore,instagram,facebook,friendsFamily,tvCommercial"
    )

    @Test
    fun `the per-user order is the same as iOS for the same seed`() {
        swiftReference.forEach { line ->
            val (seed, standard, extended) = line.split("|")
            assertEquals(
                standard.split(",").map(iosToAndroid::getValue) + DiscoverySources.OTHER,
                DiscoverySources.displayedSources(includeExtendedSources = false, selectedSource = null, orderSeed = seed),
                "standard order for seed '$seed'"
            )
            assertEquals(
                extended.split(",").map(iosToAndroid::getValue) + DiscoverySources.OTHER,
                DiscoverySources.displayedSources(includeExtendedSources = true, selectedSource = null, orderSeed = seed),
                "extended order for seed '$seed'"
            )
        }
    }

    @Test
    fun `other is always last and the order is stable for a user`() {
        val first = DiscoverySources.displayedSources(true, null, "some-user")
        val again = DiscoverySources.displayedSources(true, DiscoverySources.TIKTOK, "some-user")
        assertEquals(DiscoverySources.OTHER, first.last())
        assertEquals(first, again)
    }

    @Test
    fun `extended sources show with the flag or when one was already picked`() {
        val standardOnly = DiscoverySources.displayedSources(false, null, "u")
        assertFalse(DiscoverySources.TV_COMMERCIAL in standardOnly)
        assertEquals(7, standardOnly.size)

        val picked = DiscoverySources.displayedSources(false, DiscoverySources.TV_COMMERCIAL, "u")
        assertTrue(DiscoverySources.TV_COMMERCIAL in picked)
        assertTrue(DiscoverySources.TRAINER_THERAPIST in picked)
    }

    @Test
    fun `answer position is the 1-based row`() {
        val displayed = listOf("a", "b", "other")
        assertEquals(2, DiscoverySources.position("b", displayed))
        assertEquals(3, DiscoverySources.position("other", displayed))
        assertNull(DiscoverySources.position(null, displayed))
        assertNull(DiscoverySources.position("missing", displayed))
    }
}
