package taichi.walking.seniors.beginners.taichi.ui.home.model

import com.google.gson.annotations.SerializedName

enum class ExerciseLimitation {
    @SerializedName("SHOULDER") SHOULDER,
    @SerializedName("ANKLE") ANKLE,
    @SerializedName("KNEE") KNEE,
    @SerializedName("BACK") BACK,
    @SerializedName("WRIST") WRIST,
    @SerializedName("HIP") HIP
}

enum class ExerciseGoal {
    @SerializedName("WELLNESS_LONGEVITY") WELLNESS_LONGEVITY,
    @SerializedName("BALANCE_MOBILITY") BALANCE_MOBILITY,
    @SerializedName("ENERGY_VITALITY") ENERGY_VITALITY,
    @SerializedName("WEIGHT_LOSS") WEIGHT_LOSS
}

data class GenerateWorkoutRequest(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("intensity") val intensity: Int? = null,
    @SerializedName("language") val language: String,
    @SerializedName("restrictions") val restrictions: List<ExerciseLimitation>,
    @SerializedName("goals") val goals: List<ExerciseGoal>
)

enum class ExerciseStage {
    @SerializedName("WARMUP") WARMUP,
    @SerializedName("PRIMARY") PRIMARY,
    @SerializedName("COOLDOWN") COOLDOWN
}

enum class ExerciseDifficulty {
    @SerializedName("LIGHT") LIGHT,
    @SerializedName("MODERATE") MODERATE,
    @SerializedName("CHALLENGING") CHALLENGING
}

data class ExerciseDto(
    @SerializedName("exerciseId") val exerciseId: String,
    @SerializedName("name") val name: String,
    @SerializedName("limitations") val limitations: List<ExerciseLimitation>,
    @SerializedName("steps") val steps: List<String>,
    @SerializedName("energyUse") val energyUse: Int,
    @SerializedName("lengthSeconds") val lengthSeconds: Int,
    @SerializedName("intensity") val intensity: ExerciseDifficulty,
    @SerializedName("goals") val goals: List<ExerciseGoal>,
    @SerializedName("stage") val stage: ExerciseStage,
    @SerializedName("heroImageUrl") val heroImageUrl: String,
    @SerializedName("hintSpeechUrl") val hintSpeechUrl: String,
    @SerializedName("stepsSpeechUrl") val stepsSpeechUrl: String,
    @SerializedName("nameSpeechUrl") val nameSpeechUrl: String,
    @SerializedName("videoUrl") val videoUrl: String
) {
    val id: String get() = exerciseId
    val duration: Int get() = lengthSeconds
    val coverUrl: String get() = heroImageUrl
    val calories: Int get() = energyUse
}

data class WorkoutDayDto(
    @SerializedName("day") val day: Int,
    @SerializedName("lengthSeconds") val lengthSeconds: Int,
    @SerializedName("exercises") val exercises: List<ExerciseDto>,
    @SerializedName("description") val description: String,
    @SerializedName("heroImageUrl") val heroImageUrl: String
) {
    val id: Int get() = day
    val durationInMinutes: Int get() = lengthSeconds / 60
    val durationFormatted: String
        get() = "${durationInMinutes} Min${if (durationInMinutes == 1) "" else "s"}"
    val firstExerciseName: String get() = exercises.firstOrNull()?.name ?: "Workout"
    val coverUrl: String get() = heroImageUrl
    val difficultyText: String
        get() = when (exercises.firstOrNull()?.intensity) {
            ExerciseDifficulty.LIGHT -> "Low"
            ExerciseDifficulty.MODERATE -> "Medium"
            ExerciseDifficulty.CHALLENGING -> "High"
            null -> ""
        }
}

data class WorkoutPhaseDto(
    @SerializedName("phase") val phase: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("days") val days: List<WorkoutDayDto>
) {
    val id: Int get() = phase
}

data class WorkoutPlanDto(
    @SerializedName("phases") val phases: List<WorkoutPhaseDto>,
    @SerializedName("intensity") val intensity: String? = null
) {
    val totalDays: Int get() = phases.sumOf { it.days.size }
    val allDays: List<WorkoutDayDto> get() = phases.flatMap { it.days }

    fun dayAt(index: Int): WorkoutDayDto? {
        if (index <= 0) return null
        return allDays.getOrNull(index - 1)
    }

    fun phaseForDay(dayNumber: Int): WorkoutPhaseDto? {
        var dayCount = 0
        phases.forEach { phase ->
            dayCount += phase.days.size
            if (dayNumber <= dayCount) return phase
        }
        return null
    }
}
