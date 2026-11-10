package taichi.walking.seniors.beginners.taichi.onboarding.model

import java.text.NumberFormat
import java.util.Locale

/**
 * The figures onboarding and the paywall show as social proof, combined across both stores: there
 * is no live source, so they are read off App Store Connect and the Play listing by hand.
 *
 * App Store (iOS WelcomeSocialProof, Oct 2026): 4.7 from 4,400+ ratings. Google Play (4 Nov
 * 2026): 4.1 from 212. Weighted: (4.7 x 4,400 + 4.1 x 212) / 4,612 = 4.67, so 4.7 from 4,600+
 * ratings. Every place that shows it says "App Store & Google Play"; it is never presented as the
 * Play rating alone, which is what users see on the Play listing.
 */
object OnboardingSocialProof {
    /** Average rating over both stores, see above. */
    const val RATING = 4.7

    /** Ratings behind [RATING] over both stores (4,612), shown rounded down with a "+". */
    const val RATINGS_COUNT = 4600

    /**
     * People who installed the app: iOS counted ~95K downloads on 15 Oct 2026 and shows "100K+";
     * with Android's 5K+ installs the two platforms are past 100K together.
     */
    const val MEMBERS = "100K+"

    /** The rating as the user's locale writes a one-decimal number (4.7, 4,7). */
    fun formattedRating(locale: Locale = Locale.getDefault()): String =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(RATING)

    /** The ratings count in the user's locale, without the "+" (4,600 / 4.600 / 4 600). */
    fun formattedRatingsCount(locale: Locale = Locale.getDefault()): String =
        NumberFormat.getIntegerInstance(locale).format(RATINGS_COUNT)

    /** Filled stars for [RATING], to the nearest half. */
    val filledStars: Double get() = Math.round(RATING * 2) / 2.0
}
