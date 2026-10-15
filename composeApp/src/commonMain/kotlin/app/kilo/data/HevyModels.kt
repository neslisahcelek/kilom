package app.kilo.data

import kotlinx.serialization.Serializable

@Serializable
enum class MuscleCategory {
    LEGS_GLUTES,
    BACK_PULL,
    CHEST_PUSH,
    SHOULDERS_ARMS,
    CORE_FULL_BODY,
    OTHER,
}

@Serializable
data class HevySet(
    val index: Int,
    val type: String,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null,
)

@Serializable
data class HevyExercise(
    val title: String,
    val category: MuscleCategory = MuscleCategory.OTHER,
    val sets: List<HevySet> = emptyList(),
    val totalVolumeKg: Double = 0.0,
    val maxWeightKg: Double = 0.0,
    val estimated1RmKg: Double = 0.0,
)

@Serializable
data class HevyWorkoutSession(
    val id: String,
    val title: String,
    val dateEpochDays: Long,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationMinutes: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val exercises: List<HevyExercise> = emptyList(),
)
