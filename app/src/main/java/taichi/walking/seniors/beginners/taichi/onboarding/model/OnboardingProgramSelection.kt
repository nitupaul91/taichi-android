package taichi.walking.seniors.beginners.taichi.onboarding.model

import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind

/**
 * The answer to "Choose your practice style" (iOS `OnboardingProgramSelection`). The [id] is the
 * wire value iOS uploads as `programSelection`, and it stays distinct from the practice the plan is
 * built for: "Let Us Decide" currently generates Tai Chi.
 */
enum class OnboardingProgramSelection(
    val id: String,
    val practiceKind: PracticeKind
) {
    TAI_CHI("tai_chi", PracticeKind.TAI_CHI),
    WALKING("tai_chi_walking", PracticeKind.WALKING),
    CHAIR("chair_tai_chi", PracticeKind.CHAIR),
    LET_US_DECIDE("let_us_decide", PracticeKind.TAI_CHI);

    companion object {
        fun fromId(id: String?): OnboardingProgramSelection? = entries.firstOrNull { it.id == id }
    }
}
