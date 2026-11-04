package taichi.walking.seniors.beginners.taichi.ui.home.workout

import androidx.annotation.StringRes
import taichi.walking.seniors.beginners.R

/**
 * Background music for a workout session, mirroring iOS's `WorkoutMusicTrack`. The [id]s are the
 * wire contract with the backend's `GET music/tracks` catalog (and match the iOS bundle names):
 * the tracks are no longer bundled in the APK — they live on the CDN and are prefetched during
 * onboarding by `WorkoutMusicService`.
 */
enum class WorkoutMusicTrack(
    val id: String,
    @StringRes val titleRes: Int
) {
    MORNING_MIST_ON_STONE("morning_mist_on_stone", R.string.music_morning_mist_on_stone),
    STRIKE_OF_THE_BAMBOO_BLADE("strike_of_the_bamboo_blade", R.string.music_strike_of_the_bamboo_blade),
    IRON_BAMBOO("iron_bamboo", R.string.music_iron_bamboo),
    SWORD_THROUGH_MORNING_MIST("sword_through_morning_mist", R.string.music_sword_through_morning_mist),
    ECHOES_OF_THE_JADE_COURTYARD("echoes_of_the_jade_courtyard", R.string.music_echoes_of_the_jade_courtyard),
    LANTERNS_OVER_QUIET_WATER("lanterns_over_quiet_water", R.string.music_lanterns_over_quiet_water),
    STILLNESS_BENEATH_THE_PINES("stillness_beneath_the_pines", R.string.music_stillness_beneath_the_pines)
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
