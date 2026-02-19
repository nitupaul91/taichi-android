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
    val language: String,
    val restrictions: List<ExerciseLimitation>,
    val goals: List<ExerciseGoal>
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
    val exerciseId: String,
    val name: String,
    val limitations: List<ExerciseLimitation>,
    val steps: List<String>,
    val energyUse: Int,
    val lengthSeconds: Int,
    val intensity: ExerciseDifficulty,
    val goals: List<ExerciseGoal>,
    val stage: ExerciseStage,
    val heroImageUrl: String,
    val hintSpeechUrl: String,
    val stepsSpeechUrl: String,
    val nameSpeechUrl: String,
    val videoUrl: String
) {
    val id: String get() = exerciseId
    val duration: Int get() = lengthSeconds
    val coverUrl: String get() = heroImageUrl
    val calories: Int get() = energyUse
}

data class WorkoutDayDto(
    val day: Int,
    val lengthSeconds: Int,
    val exercises: List<ExerciseDto>,
    val description: String,
    val heroImageUrl: String
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
    val phase: Int,
    val name: String,
    val description: String,
    val days: List<WorkoutDayDto>
) {
    val id: Int get() = phase
}

data class WorkoutPlanDto(
    val phases: List<WorkoutPhaseDto>
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
