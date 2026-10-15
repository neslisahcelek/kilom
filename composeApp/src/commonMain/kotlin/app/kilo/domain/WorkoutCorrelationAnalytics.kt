package app.kilo.domain

import kotlin.math.round

/**
 * DOMS (Delayed Onset Muscle Soreness) and EIMD (Exercise-Induced Muscle Damage) assessment.
 * Evaluates whether recent heavy compound resistance training explains acute scale weight elevation.
 */
data class DomsAssessment(
    val hasTrauma: Boolean,
    val recentVolumeKg: Double,
    val targetMuscleGroup: String?,
    val exerciseNames: List<String>,
    val expectedFluidWeightKg: Double,
)

/**
 * Summary of training volume and perceived exertion for a specific cycle phase.
 */
data class PhaseWorkoutSummary(
    val phase: CyclePhase,
    val workoutCount: Int,
    val totalVolumeKg: Double,
    val avgVolumePerWorkoutKg: Double,
    val avgRpe: Double?,
)

/**
 * Aggregated correlation between training sessions and cycle phases.
 */
data class CycleWorkoutCorrelation(
    val phaseSummaries: List<PhaseWorkoutSummary>,
    val peakVolumePhase: CyclePhase?,
    val prDistribution: Map<CyclePhase, Int>,
    val lutealRpeDifference: Double?,
)

/**
 * Relative strength progress tracking Epley 1RM relative to current bodyweight.
 */
data class RelativeStrengthProgress(
    val exerciseName: String,
    val bodyWeightKg: Double,
    val estimated1RmKg: Double,
    val relativeRatio: Double,
)

/**
 * High-level summary of a completed workout session (e.g., imported from Hevy or logged locally).
 */
data class WorkoutSessionSummary(
    val id: String,
    val title: String,
    val dateEpochDays: Long,
    val totalVolumeKg: Double,
    val isLegsOrBack: Boolean,
    val exerciseNames: List<String>,
    val maxWeights: Map<String, Double> = emptyMap(),
    val avgRpe: Double? = null,
)

/**
 * Evaluates DOMS-induced water retention risk.
 *
 * Scans the last 48 hours (epochDays in currentEpochDays - 2 until currentEpochDays) for a heavy
 * workout targeting major muscle groups (isLegsOrBack == true and totalVolumeKg >= 8000 kg).
 *
 * If weight increased by >= 0.3 kg vs previous weigh-in, confirms DOMS fluid retention.
 */
fun evaluateDomsRisk(
    workouts: List<WorkoutSessionSummary>,
    previousWeightKg: Double?,
    currentWeightKg: Double,
    currentEpochDays: Long,
): DomsAssessment {
    val qualifyingWorkouts = workouts.filter {
        it.dateEpochDays in (currentEpochDays - 2 until currentEpochDays) &&
            it.isLegsOrBack &&
            it.totalVolumeKg >= 8000.0
    }

    val recentVolumeKg = qualifyingWorkouts.sumOf { it.totalVolumeKg }
    val exerciseNames = qualifyingWorkouts.flatMap { it.exerciseNames }.distinct()
    val targetMuscleGroup = if (qualifyingWorkouts.isNotEmpty()) {
        val titles = qualifyingWorkouts.map { it.title }.filter { it.isNotBlank() }.distinct()
        titles.joinToString(", ").ifBlank { null }
    } else {
        null
    }

    val weightDiff = if (previousWeightKg != null) currentWeightKg - previousWeightKg else 0.0
    val weightIncreased = previousWeightKg != null && weightDiff >= (0.3 - 1e-6)
    val hasTrauma = qualifyingWorkouts.isNotEmpty() && weightIncreased
    val expectedFluidWeightKg = if (hasTrauma) {
        round(weightDiff * 100.0) / 100.0
    } else {
        0.0
    }

    return DomsAssessment(
        hasTrauma = hasTrauma,
        recentVolumeKg = recentVolumeKg,
        targetMuscleGroup = targetMuscleGroup,
        exerciseNames = exerciseNames,
        expectedFluidWeightKg = expectedFluidWeightKg,
    )
}

/**
 * Correlates workouts with menstrual cycle phases.
 *
 * - Groups workouts into cycle phases for each workout's cycle day.
 * - Computes total and average volume per phase.
 * - Identifies personal records (PRs) achieved across cycle phases.
 * - Compares average RPE between Follicular and Luteal phases.
 */
fun analyzeCycleWorkoutCorrelation(
    workouts: List<WorkoutSessionSummary>,
    cycleStartEpochDays: Long,
    cycleLength: Int = 28,
): CycleWorkoutCorrelation {
    val clampedLength = cycleLength.coerceIn(21, 45)

    // Map each workout to its CyclePhase
    val workoutWithPhase = workouts.map { workout ->
        val daysDiff = workout.dateEpochDays - cycleStartEpochDays
        val cycleDay = ((daysDiff % clampedLength) + clampedLength) % clampedLength + 1
        val phase = cyclePhaseForDay(cycleDay = cycleDay.toInt(), cycleLength = clampedLength)
        workout to phase
    }

    // Phase summaries for all 4 cycle phases
    val phaseSummaries = CyclePhase.entries.map { phase ->
        val phaseWorkouts = workoutWithPhase.filter { it.second == phase }.map { it.first }
        val count = phaseWorkouts.size
        val totalVolume = phaseWorkouts.sumOf { it.totalVolumeKg }
        val avgVolume = if (count > 0) totalVolume / count else 0.0
        val rpeList = phaseWorkouts.mapNotNull { it.avgRpe }
        val avgRpe = if (rpeList.isNotEmpty()) rpeList.average() else null

        PhaseWorkoutSummary(
            phase = phase,
            workoutCount = count,
            totalVolumeKg = totalVolume,
            avgVolumePerWorkoutKg = avgVolume,
            avgRpe = avgRpe,
        )
    }

    val peakVolumePhase = phaseSummaries
        .filter { it.totalVolumeKg > 0.0 }
        .maxByOrNull { it.totalVolumeKg }
        ?.phase

    // PR distribution: count PRs achieved in each phase chronologically
    val prCounts = mutableMapOf<CyclePhase, Int>()
    val runningMaxWeights = mutableMapOf<String, Double>()
    val sortedWorkouts = workoutWithPhase.sortedBy { it.first.dateEpochDays }
    for ((workout, phase) in sortedWorkouts) {
        for ((exercise, maxWeight) in workout.maxWeights) {
            val prevMax = runningMaxWeights[exercise] ?: 0.0
            if (maxWeight > prevMax) {
                runningMaxWeights[exercise] = maxWeight
                prCounts[phase] = (prCounts[phase] ?: 0) + 1
            }
        }
    }

    val follicularAvgRpe = phaseSummaries.firstOrNull { it.phase == CyclePhase.FOLLICULAR }?.avgRpe
    val lutealAvgRpe = phaseSummaries.firstOrNull { it.phase == CyclePhase.LUTEAL }?.avgRpe
    val lutealRpeDifference = if (follicularAvgRpe != null && lutealAvgRpe != null) {
        val diff = lutealAvgRpe - follicularAvgRpe
        round(diff * 100.0) / 100.0
    } else {
        null
    }

    return CycleWorkoutCorrelation(
        phaseSummaries = phaseSummaries,
        peakVolumePhase = peakVolumePhase,
        prDistribution = prCounts.toMap(),
        lutealRpeDifference = lutealRpeDifference,
    )
}

/**
 * Calculates relative strength and estimated 1RM using the Epley formula:
 * 1RM = weightLiftedKg * (1.0 + reps / 30.0)
 * Relative Ratio = 1RM / bodyWeightKg
 */
fun calculateRelativeStrength(
    bodyWeightKg: Double,
    exerciseName: String,
    weightLiftedKg: Double,
    reps: Int,
): RelativeStrengthProgress {
    val estimated1Rm = weightLiftedKg * (1.0 + reps / 30.0)
    val ratio = if (bodyWeightKg > 0.0) estimated1Rm / bodyWeightKg else 0.0

    return RelativeStrengthProgress(
        exerciseName = exerciseName,
        bodyWeightKg = bodyWeightKg,
        estimated1RmKg = estimated1Rm,
        relativeRatio = ratio,
    )
}
