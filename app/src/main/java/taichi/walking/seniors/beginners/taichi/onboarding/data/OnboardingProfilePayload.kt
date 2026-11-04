package taichi.walking.seniors.beginners.taichi.onboarding.data

import com.google.gson.annotations.SerializedName
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The onboarding answers in the shape both consumers expect: the remote paywall webview and
 * `POST profile/android/upload` on the backend.
 *
 * Field names are the wire contract for both, so renaming one breaks the paywall's JS and the
 * server's deserialization at the same time. The backend normalises the four fields it stores
 * differently (body-type option ids to indices, "yes"/"no" to booleans) — see
 * AndroidOnboardingUploadRequest server-side; nothing needs converting here.
 *
 * Every field needs @SerializedName: Gson serializes by reflection, and the release build's R8
 * pass renames unannotated fields, which shipped a payload the server rejected (missing userId).
 */
data class OnboardingProfilePayload(
    @SerializedName("userId") val userId: String?,
    @SerializedName("completedStep") val completedStep: String,
    @SerializedName("discoverySource") val discoverySource: String?,
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String?,
    @SerializedName("goals") val goals: Set<String>,
    @SerializedName("taiChiFamiliarity") val taiChiFamiliarity: String?,
    @SerializedName("currentBodyType") val currentBodyType: String?,
    @SerializedName("targetBodyType") val targetBodyType: String?,
    @SerializedName("targetZones") val targetZones: Set<String>,
    @SerializedName("activityLevel") val activityLevel: String?,
    @SerializedName("dailyWalking") val dailyWalking: String?,
    @SerializedName("stairsFeeling") val stairsFeeling: String?,
    @SerializedName("squatsAbility") val squatsAbility: String?,
    @SerializedName("canRotateHead") val canRotateHead: String?,
    @SerializedName("canHoldArms") val canHoldArms: String?,
    @SerializedName("taiChiLevel") val taiChiLevel: String?,
    @SerializedName("betweenMealsFeeling") val betweenMealsFeeling: String?,
    @SerializedName("sleepAmount") val sleepAmount: String?,
    @SerializedName("waterIntake") val waterIntake: String?,
    @SerializedName("dietTypes") val dietTypes: Set<String>,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Double,
    @SerializedName("targetWeight") val targetWeight: Double,
    @SerializedName("shapeUpEvent") val shapeUpEvent: String?,
    @SerializedName("shapeUpEventDate") val shapeUpEventDate: String?,
    @SerializedName("motivationLevel") val motivationLevel: String?,
    @SerializedName("exerciseBlockers") val exerciseBlockers: Set<String>,
    @SerializedName("useMetric") val useMetric: Boolean?
)

/**
 * @param completedStep the onboarding step the user has just reached, matching the step vocabulary
 *   iOS reports (`OnboardingRoutes.analyticsStepName`), so both platforms land in one funnel.
 */
internal fun OnboardingState.toOnboardingProfilePayload(
    userId: String?,
    completedStep: String
): OnboardingProfilePayload {
    return OnboardingProfilePayload(
        userId = userId,
        completedStep = completedStep,
        discoverySource = discoverySourceId,
        age = actualAge,
        gender = genderId,
        goals = goals,
        taiChiFamiliarity = taiChiFamiliarityId,
        currentBodyType = currentBodyTypeId,
        targetBodyType = targetBodyTypeId,
        targetZones = targetZones,
        activityLevel = activityLevelId,
        dailyWalking = walkDailyId,
        stairsFeeling = stairsFeelingId,
        squatsAbility = squatAbilityId,
        canRotateHead = rotateHeadId,
        canHoldArms = armsForwardId,
        taiChiLevel = taiChiLevelId,
        betweenMealsFeeling = betweenMealsId,
        sleepAmount = sleepAmountId,
        waterIntake = waterIntakeId,
        dietTypes = dietTypes,
        height = heightCm,
        weight = weightKg.toDouble(),
        targetWeight = targetWeightKg.toDouble(),
        shapeUpEvent = shapeUpEventId,
        shapeUpEventDate = shapeUpEventDateMillis
            .takeIf { !shapeUpEventDateSkipped }
            ?.let { formatIsoUtc(it) },
        motivationLevel = motivationLevelId,
        exerciseBlockers = exerciseBlockers,
        useMetric = useMetric
    )
}

/**
 * Deliberately NOT java.time.Instant: this module is minSdk 23 with no core library desugaring, so
 * java.time throws NoClassDefFoundError below API 26. This runs on every onboarding step, so a
 * crash here would take out the whole funnel rather than one screen.
 */
private fun formatIsoUtc(epochMillis: Long): String {
    val format = SimpleDateFormat(ISO_UTC_PATTERN, Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(epochMillis))
}

private const val ISO_UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
