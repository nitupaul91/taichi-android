package taichi.walking.seniors.beginners.taichi.ui.settings.cancellation

import androidx.annotation.StringRes
import taichi.walking.seniors.beginners.R

/** Raw values are the wire format shared with iOS — keep them stable. */
enum class CancellationReason(val rawValue: String, @StringRes val titleRes: Int) {
    TOO_EXPENSIVE("too_expensive", R.string.cancel_flow_reason_too_expensive),
    EXERCISES_TOO_HARD("exercises_too_hard", R.string.cancel_flow_reason_exercises_too_hard),
    EXERCISES_TOO_EASY("exercises_too_easy", R.string.cancel_flow_reason_exercises_too_easy),
    OTHER("other", R.string.cancel_flow_reason_other);

    /** The intensity change offered to keep this user, if any. */
    val retentionAdjustment: IntensityAdjustment?
        get() = when (this) {
            EXERCISES_TOO_HARD -> IntensityAdjustment(
                intensity = "1",
                action = "easier_exercises",
                offerTitle = R.string.cancel_flow_easier_offer_title,
                offerSubtitle = R.string.cancel_flow_easier_offer_subtitle,
                acceptTitle = R.string.cancel_flow_easier_accept,
                confirmationTitle = R.string.cancel_flow_easier_confirmation_title,
                confirmationBody = R.string.cancel_flow_easier_confirmation_body
            )
            EXERCISES_TOO_EASY -> IntensityAdjustment(
                intensity = "5",
                action = "harder_exercises",
                offerTitle = R.string.cancel_flow_harder_offer_title,
                offerSubtitle = R.string.cancel_flow_harder_offer_subtitle,
                acceptTitle = R.string.cancel_flow_harder_accept,
                confirmationTitle = R.string.cancel_flow_harder_confirmation_title,
                confirmationBody = R.string.cancel_flow_harder_confirmation_body
            )
            TOO_EXPENSIVE, OTHER -> null
        }
}

data class IntensityAdjustment(
    /** One of the workout intensity values "1".."5" the plan endpoints take. */
    val intensity: String,
    val action: String,
    @StringRes val offerTitle: Int,
    @StringRes val offerSubtitle: Int,
    @StringRes val acceptTitle: Int,
    @StringRes val confirmationTitle: Int,
    @StringRes val confirmationBody: Int
)
