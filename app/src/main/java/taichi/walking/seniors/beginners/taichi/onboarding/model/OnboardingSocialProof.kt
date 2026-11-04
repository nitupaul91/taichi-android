package taichi.walking.seniors.beginners.taichi.onboarding.model

import java.text.NumberFormat
import java.util.Locale

/**
 * The figures onboarding shows as social proof. iOS hard-codes its own (`WelcomeSocialProof`,
 * 4e26ec1): the US App Store rating and ratings count read off App Store Connect on a given day.
 * There is no live source on either platform, so these are the Google Play equivalents, read off
 * the public Play listing; update them by hand like iOS does.
 */
object OnboardingSocialProof {
    /** Google Play rating of taichi.walking.seniors.beginners: 4.1 (US listing, 4 Nov 2026). */
    const val PLAY_RATING = 4.1

    /** Ratings behind [PLAY_RATING]: 212 on 4 Nov 2026, shown rounded down. */
    const val PLAY_RATINGS_COUNT = "200+"

    /**
     * People who installed the app: iOS counted ~95K downloads on 15 Oct 2026 and shows "100K+";
     * with Android's 5K+ installs the two platforms are past 100K together.
     */
    const val MEMBERS = "100K+"

    /** The rating as the user's locale writes a one-decimal number (4.1, 4,1). */
    fun formattedPlayRating(locale: Locale = Locale.getDefault()): String =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(PLAY_RATING)

    /** Filled stars for [PLAY_RATING], to the nearest half: an honest 4.1 shows four, not five. */
    val filledStars: Double get() = Math.round(PLAY_RATING * 2) / 2.0
}
