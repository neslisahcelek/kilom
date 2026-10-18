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
enum class WorkoutSource {
    CSV,
    HEALTH,
}

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
    val source: WorkoutSource = WorkoutSource.CSV,
)

data class WorkoutMergeResult(
    val merged: List<HevyWorkoutSession>,
    val addedCount: Int,
    val upgradedCount: Int,
) {
    val totalAffectedCount: Int get() = addedCount + upgradedCount
}

/**
 * Intelligently merges [incoming] workout sessions into [existing] sessions without losing history.
 *
 * Rules:
 * - Sessions match if they share the same `id` OR if their `startTimeEpochMs` are within ±30 minutes
 *   (1,800,000 ms), or on the same `dateEpochDays` when one of the timestamps is 0.
 * - Rich `CSV` workouts (containing exercises/sets/1RM) ALWAYS take precedence over summary-only
 *   `HEALTH` workouts.
 * - If an incoming `CSV` workout matches an existing `HEALTH` workout, the `HEALTH` workout is
 *   upgraded in-place with the rich `CSV` workout.
 * - If an incoming `HEALTH` workout matches an existing `CSV` workout, the existing `CSV` workout
 *   is preserved intact.
 */
fun mergeWorkouts(
    existing: List<HevyWorkoutSession>,
    incoming: List<HevyWorkoutSession>,
): WorkoutMergeResult {
    val result = existing.toMutableList()
    var addedCount = 0
    var upgradedCount = 0

    for (session in incoming) {
        val matchIndex = result.indexOfFirst { current ->
            isSameWorkoutSession(current, session)
        }

        if (matchIndex >= 0) {
            val current = result[matchIndex]
            val currentIsRich = current.source == WorkoutSource.CSV && current.exercises.isNotEmpty()
            val incomingIsRich = session.source == WorkoutSource.CSV && session.exercises.isNotEmpty()

            when {
                // Upgrade a Health summary workout with a rich CSV workout
                !currentIsRich && incomingIsRich -> {
                    result[matchIndex] = session
                    upgradedCount++
                }
                // Update existing CSV workout if incoming CSV has more sets/volume or updated exercises
                currentIsRich && incomingIsRich -> {
                    val currentSetCount = current.exercises.sumOf { it.sets.size }
                    val incomingSetCount = session.exercises.sumOf { it.sets.size }
                    if (incomingSetCount > currentSetCount || session.totalVolumeKg != current.totalVolumeKg) {
                        result[matchIndex] = session
                        upgradedCount++
                    } else {
                        result[matchIndex] = session
                    }
                }
                // Both are Health summaries -> keep latest duration/title
                !currentIsRich && !incomingIsRich -> {
                    if (session.durationMinutes > current.durationMinutes) {
                        result[matchIndex] = session
                    }
                }
                // currentIsRich && !incomingIsRich -> keep current rich CSV workout intact!
            }
        } else {
            result.add(session)
            addedCount++
        }
    }

    val sorted = result.sortedWith(
        compareByDescending<HevyWorkoutSession> { it.dateEpochDays }
            .thenByDescending { it.startTimeEpochMs }
            .thenByDescending { it.id }
    )

    return WorkoutMergeResult(
        merged = sorted,
        addedCount = addedCount,
        upgradedCount = upgradedCount,
    )
}

private fun isSameWorkoutSession(a: HevyWorkoutSession, b: HevyWorkoutSession): Boolean {
    if (a.id == b.id) return true

    // Match if start times are within 30 minutes (1,800,000 ms) of each other
    if (a.startTimeEpochMs > 0L && b.startTimeEpochMs > 0L) {
        val diffMs = kotlin.math.abs(a.startTimeEpochMs - b.startTimeEpochMs)
        if (diffMs <= 30 * 60 * 1000L) return true
    }

    // Also check if time ranges overlap (e.g., HealthKit start/end vs Hevy CSV start/end)
    if (a.startTimeEpochMs > 0L && a.endTimeEpochMs > a.startTimeEpochMs &&
        b.startTimeEpochMs > 0L && b.endTimeEpochMs > b.startTimeEpochMs
    ) {
        val overlapStart = kotlin.math.max(a.startTimeEpochMs, b.startTimeEpochMs)
        val overlapEnd = kotlin.math.min(a.endTimeEpochMs, b.endTimeEpochMs)
        if (overlapEnd > overlapStart) return true
    }

    // Fallback when one or both sessions only have a date (startTimeEpochMs == 0L)
    if ((a.startTimeEpochMs == 0L || b.startTimeEpochMs == 0L) &&
        a.dateEpochDays > 0L && a.dateEpochDays == b.dateEpochDays
    ) {
        if (a.source != b.source || a.title.equals(b.title, ignoreCase = true)) {
            return true
        }
    }

    return false
}

