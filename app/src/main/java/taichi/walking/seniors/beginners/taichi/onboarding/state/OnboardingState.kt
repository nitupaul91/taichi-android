package taichi.walking.seniors.beginners.taichi.onboarding.state

import com.google.gson.annotations.SerializedName

data class OnboardingState(
    @SerializedName("ageRangeId") val ageRangeId: String? = null,
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
    @SerializedName("motivationLevelId") val motivationLevelId: String? = null,
    @SerializedName("exerciseBlockers") val exerciseBlockers: Set<String> = emptySet(),
    @SerializedName("email") val email: String = ""
) {
    val bmi: Double
        get() {
            val heightM = heightCm.toDouble() / 100.0
            return if (heightM <= 0.0) 0.0 else weightKg / (heightM * heightM)
        }
}
