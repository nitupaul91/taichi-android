package taichi.walking.seniors.beginners.taichi.program

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeProgramVariant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Which program the user came for, as iOS's `acquisitionKeyword` (UserDefaults) holds it:
 * "tai_chi", "tai_chi_walking" or "chair_tai_chi". Set by the onboarding program picker. It
 * decides which practice tab opens first and the programVariant reported with progress uploads.
 *
 * SharedPreferences rather than DataStore: the home screen reads it synchronously to build its tabs.
 */
@Singleton
class ProgramPreference @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var acquisitionKeyword: String
        get() = prefs.getString(KEY_ACQUISITION_KEYWORD, null).orEmpty()
        set(value) {
            prefs.edit().putString(KEY_ACQUISITION_KEYWORD, value).apply()
        }

    private val keyword: String get() = acquisitionKeyword.lowercase()

    val isChairKeyword: Boolean get() = keyword.contains("chair")

    val isWalkingKeyword: Boolean get() = keyword == KEYWORD_WALKING

    /** iOS PracticeProgressStore.currentProgramVariant. */
    val programVariant: PracticeProgramVariant
        get() = when {
            isWalkingKeyword -> PracticeProgramVariant.TAI_CHI_WALKING
            isChairKeyword -> PracticeProgramVariant.CHAIR_TAI_CHI
            else -> PracticeProgramVariant.TAI_CHI
        }

    /**
     * Practice tabs in iOS ContentView order: the chosen program first. Walking leads only while
     * the `onboarding_walking_experiment_enabled` test is on, as on iOS.
     */
    fun practiceTabOrder(walkingExperimentEnabled: Boolean): List<PracticeKind> = when {
        isChairKeyword -> listOf(PracticeKind.CHAIR, PracticeKind.TAI_CHI, PracticeKind.WALKING)
        isWalkingKeyword && walkingExperimentEnabled ->
            listOf(PracticeKind.WALKING, PracticeKind.TAI_CHI, PracticeKind.CHAIR)
        else -> listOf(PracticeKind.TAI_CHI, PracticeKind.CHAIR, PracticeKind.WALKING)
    }

    fun setProgram(kind: PracticeKind) {
        acquisitionKeyword = when (kind) {
            PracticeKind.TAI_CHI -> KEYWORD_TAI_CHI
            PracticeKind.WALKING -> KEYWORD_WALKING
            PracticeKind.CHAIR -> KEYWORD_CHAIR
        }
    }

    companion object {
        private const val PREFS_NAME = "taichi_program"
        private const val KEY_ACQUISITION_KEYWORD = "acquisitionKeyword"
        const val KEYWORD_TAI_CHI = "tai_chi"
        const val KEYWORD_WALKING = "tai_chi_walking"
        const val KEYWORD_CHAIR = "chair_tai_chi"
    }
}
