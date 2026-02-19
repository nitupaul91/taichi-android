package taichi.walking.seniors.beginners.taichi.onboarding.state

data class OnboardingState(
    val ageRangeId: String? = null,
    val genderId: String? = null,
    val goals: Set<String> = emptySet(),
    val taiChiFamiliarityId: String? = null,
    val currentBodyTypeId: String? = null,
    val targetBodyTypeId: String? = null,
    val targetZones: Set<String> = emptySet(),
    val activityLevelId: String? = null,
    val walkDailyId: String? = null,
    val stairsFeelingId: String? = null,
    val squatAbilityId: String? = null,
    val rotateHeadId: String? = null,
    val armsForwardId: String? = null,
    val taiChiLevelId: String? = null,
    val betweenMealsId: String? = null,
    val sleepAmountId: String? = null,
    val waterIntakeId: String? = null,
    val dietTypes: Set<String> = emptySet(),
    val heightCm: Int = 165,
    val weightKg: Int = 70,
    val targetWeightKg: Int = 65,
    val motivationLevelId: String? = null,
    val exerciseBlockers: Set<String> = emptySet(),
    val email: String = ""
) {
    val bmi: Double
        get() {
            val heightM = heightCm.toDouble() / 100.0
            return if (heightM <= 0.0) 0.0 else weightKg / (heightM * heightM)
        }
}
