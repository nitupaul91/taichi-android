package taichi.walking.seniors.beginners.taichi.onboarding.state

import com.google.gson.annotations.SerializedName
import java.util.Locale

data class OnboardingState(
    @SerializedName("discoverySourceId") val discoverySourceId: String? = null,
    @SerializedName("actualAge") val actualAge: Int = 55,
    @SerializedName("genderId") val genderId: String? = null,
    @SerializedName("goals") val goals: Set<String> = emptySet(),
    @SerializedName("taiChiFamiliarityId") val taiChiFamiliarityId: String? = null,
    @SerializedName("currentBodyTypeId") val currentBodyTypeId: String? = null,
    @SerializedName("targetBodyTypeId") val targetBodyTypeId: String? = null,
    @SerializedName("targetZones") val targetZones: Set<String> = emptySet(),
    @SerializedName("activityLevelId") val activityLevelId: String? = null,
    @SerializedName("walkDailyId") val walkDailyId: String? = null,
    @SerializedName("stairsFeelingId") val stairsFeelingId: String? = null,
    @SerializedName("squatAbilityId") val squatAbilityId: String? = null,
    @SerializedName("rotateHeadId") val rotateHeadId: String? = null,
    @SerializedName("armsForwardId") val armsForwardId: String? = null,
    @SerializedName("taiChiLevelId") val taiChiLevelId: String? = null,
    @SerializedName("betweenMealsId") val betweenMealsId: String? = null,
    @SerializedName("sleepAmountId") val sleepAmountId: String? = null,
    @SerializedName("waterIntakeId") val waterIntakeId: String? = null,
    @SerializedName("dietTypes") val dietTypes: Set<String> = emptySet(),
    @SerializedName("heightCm") val heightCm: Int = 165,
    @SerializedName("weightKg") val weightKg: Int = 70,
    @SerializedName("targetWeightKg") val targetWeightKg: Int = 65,
    @SerializedName("useMetric") val useMetric: Boolean = defaultUseMetricUnits(),
    @SerializedName("shapeUpEventId") val shapeUpEventId: String? = null,
    @SerializedName("shapeUpEventDateMillis") val shapeUpEventDateMillis: Long = System.currentTimeMillis() + (56L * 24L * 60L * 60L * 1000L),
    @SerializedName("shapeUpEventDateSkipped") val shapeUpEventDateSkipped: Boolean = false,
    @SerializedName("motivationLevelId") val motivationLevelId: String? = null,
    @SerializedName("exerciseBlockers") val exerciseBlockers: Set<String> = emptySet(),
    @SerializedName("email") val email: String = ""
) {
    val bmi: Double
        get() {
            val heightM = heightCm.toDouble() / 100.0
            return if (heightM <= 0.0) 0.0 else weightKg / (heightM * heightM)
        }

    companion object {
        private fun defaultUseMetricUnits(): Boolean {
            return Locale.getDefault().country.uppercase(Locale.ROOT) !in setOf("US", "GB", "LR", "MM")
        }
    }
}
