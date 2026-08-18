package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import taichi.walking.seniors.beginners.R

/**
 * Background music for a workout session, mirroring iOS's `WorkoutMusicTrack`. The raw resource
 * names match the iOS bundle names so the two platforms stay comparable when a track is added,
 * removed or re-cut.
 */
enum class WorkoutMusicTrack(
    @RawRes val resId: Int,
    @StringRes val titleRes: Int
) {
    MORNING_MIST_ON_STONE(R.raw.morning_mist_on_stone, R.string.music_morning_mist_on_stone),
    STRIKE_OF_THE_BAMBOO_BLADE(R.raw.strike_of_the_bamboo_blade, R.string.music_strike_of_the_bamboo_blade),
    IRON_BAMBOO(R.raw.iron_bamboo, R.string.music_iron_bamboo),
    SWORD_THROUGH_MORNING_MIST(R.raw.sword_through_morning_mist, R.string.music_sword_through_morning_mist),
    ECHOES_OF_THE_JADE_COURTYARD(R.raw.echoes_of_the_jade_courtyard, R.string.music_echoes_of_the_jade_courtyard),
    LANTERNS_OVER_QUIET_WATER(R.raw.lanterns_over_quiet_water, R.string.music_lanterns_over_quiet_water),
    STILLNESS_BENEATH_THE_PINES(R.raw.stillness_beneath_the_pines, R.string.music_stillness_beneath_the_pines)
}

/** Off, shuffle through everything, or lock to one track — the three iOS modes. */
sealed interface WorkoutMusicMode {
    data object Off : WorkoutMusicMode
    data object Shuffle : WorkoutMusicMode
    data class Track(val track: WorkoutMusicTrack) : WorkoutMusicMode
}

/**
 * Quiet enough to sit under the instructor's voice. Matches iOS's `musicVolume`; raising it makes
 * the spoken guidance hard to follow for the older users this app targets.
 */
const val WORKOUT_MUSIC_VOLUME = 0.35f
