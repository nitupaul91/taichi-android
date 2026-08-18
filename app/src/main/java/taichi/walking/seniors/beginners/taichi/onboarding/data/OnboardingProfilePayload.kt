package taichi.walking.seniors.beginners.taichi.onboarding.data

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
 */
data class OnboardingProfilePayload(
    val userId: String?,
    val completedStep: String,
    val discoverySource: String?,
    val age: Int,
    val gender: String?,
    val goals: Set<String>,
    val taiChiFamiliarity: String?,
    val currentBodyType: String?,
    val targetBodyType: String?,
    val targetZones: Set<String>,
    val activityLevel: String?,
    val dailyWalking: String?,
    val stairsFeeling: String?,
    val squatsAbility: String?,
    val canRotateHead: String?,
    val canHoldArms: String?,
    val taiChiLevel: String?,
    val betweenMealsFeeling: String?,
    val sleepAmount: String?,
    val waterIntake: String?,
    val dietTypes: Set<String>,
    val height: Int,
    val weight: Double,
    val targetWeight: Double,
    val shapeUpEvent: String?,
    val shapeUpEventDate: String?,
    val motivationLevel: String?,
    val exerciseBlockers: Set<String>,
    val useMetric: Boolean?
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
