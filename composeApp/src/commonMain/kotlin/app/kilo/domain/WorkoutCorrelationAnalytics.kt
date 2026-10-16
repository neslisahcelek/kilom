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
    val peakAvgVolumePhase: CyclePhase? = null,
    val prDistribution: Map<CyclePhase, Int>,
    val anabolicPrPercentage: Int = 0,
    val anabolicVsLutealVolumeDiffPercent: Int? = null,
    val lutealRpeDifference: Double?,
)

/**
 * Biomechanical relative strength classification tier (1RM / bodyweight ratio).
 */
enum class StrengthTier {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE,
}

/**
 * Relative strength progress tracking Epley 1RM relative to current bodyweight,
 * along with the actual best set and cycle phase context.
 */
data class RelativeStrengthProgress(
    val exerciseName: String,
    val bodyWeightKg: Double,
    val estimated1RmKg: Double,
    val relativeRatio: Double,
    val bestWeightKg: Double = 0.0,
    val bestReps: Int = 0,
    val achievedInPhase: CyclePhase? = null,
    val tier: StrengthTier = classifyStrengthTier(exerciseName, relativeRatio),
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
 * Classifies relative strength (1RM / Bodyweight) into biomechanical tiers
 * tailored to the movement pattern (lower-body compound, horizontal press/pull, vertical press).
 */
fun classifyStrengthTier(
    exerciseName: String,
    relativeRatio: Double,
): StrengthTier {
    val lower = exerciseName.lowercase()
    return when {
        lower.contains("overhead") || lower.contains("military") || lower.contains("ohp") || lower.contains("shoulder") -> {
            when {
                relativeRatio >= 0.80 -> StrengthTier.ELITE
                relativeRatio >= 0.55 -> StrengthTier.ADVANCED
                relativeRatio >= 0.35 -> StrengthTier.INTERMEDIATE
                else -> StrengthTier.BEGINNER
            }
        }
        lower.contains("bench") || lower.contains("chest") || lower.contains("row") || lower.contains("pull") -> {
            when {
                relativeRatio >= 1.15 -> StrengthTier.ELITE
                relativeRatio >= 0.85 -> StrengthTier.ADVANCED
                relativeRatio >= 0.55 -> StrengthTier.INTERMEDIATE
                else -> StrengthTier.BEGINNER
            }
        }
        else -> {
            // Squat, Deadlift, RDL, Hip Thrust, and general lower-body compounds
            when {
                relativeRatio >= 1.75 -> StrengthTier.ELITE
                relativeRatio >= 1.25 -> StrengthTier.ADVANCED
                relativeRatio >= 0.85 -> StrengthTier.INTERMEDIATE
                else -> StrengthTier.BEGINNER
            }
        }
    }
}

/**
 * Resolves the menstrual cycle phase for a given workout date.
 */
fun resolveWorkoutCyclePhase(
    workoutEpochDays: Long,
    cycleStartEpochDays: Long,
    cycleLength: Int = 28,
): CyclePhase {
    val clampedLength = cycleLength.coerceIn(21, 45)
    val daysDiff = workoutEpochDays - cycleStartEpochDays
    val cycleDay = ((daysDiff % clampedLength) + clampedLength) % clampedLength + 1
    return cyclePhaseForDay(cycleDay = cycleDay.toInt(), cycleLength = clampedLength)
}

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
 * - Computes Anabolic Window (Follicular + Ovulation) PR share and volume advantage vs Luteal.
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
        val phase = resolveWorkoutCyclePhase(
            workoutEpochDays = workout.dateEpochDays,
            cycleStartEpochDays = cycleStartEpochDays,
            cycleLength = clampedLength,
        )
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

    val peakAvgVolumePhase = phaseSummaries
        .filter { it.avgVolumePerWorkoutKg > 0.0 }
        .maxByOrNull { it.avgVolumePerWorkoutKg }
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

    val totalPrs = prCounts.values.sum()
    val anabolicPrs = (prCounts[CyclePhase.FOLLICULAR] ?: 0) + (prCounts[CyclePhase.OVULATION] ?: 0)
    val anabolicPrPercentage = if (totalPrs > 0) {
        round((anabolicPrs.toDouble() / totalPrs.toDouble()) * 100.0).toInt()
    } else {
        0
    }

    val anabolicWorkouts = workoutWithPhase.filter {
        it.second == CyclePhase.FOLLICULAR || it.second == CyclePhase.OVULATION
    }.map { it.first }
    val lutealWorkouts = workoutWithPhase.filter {
        it.second == CyclePhase.LUTEAL
    }.map { it.first }

    val anabolicVsLutealVolumeDiffPercent = if (anabolicWorkouts.isNotEmpty() && lutealWorkouts.isNotEmpty()) {
        val anabolicAvg = anabolicWorkouts.sumOf { it.totalVolumeKg } / anabolicWorkouts.size
        val lutealAvg = lutealWorkouts.sumOf { it.totalVolumeKg } / lutealWorkouts.size
        if (lutealAvg > 0.0) {
            round(((anabolicAvg - lutealAvg) / lutealAvg) * 100.0).toInt()
        } else {
            null
        }
    } else {
        null
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
        peakAvgVolumePhase = peakAvgVolumePhase,
        prDistribution = prCounts.toMap(),
        anabolicPrPercentage = anabolicPrPercentage,
        anabolicVsLutealVolumeDiffPercent = anabolicVsLutealVolumeDiffPercent,
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
    achievedInPhase: CyclePhase? = null,
): RelativeStrengthProgress {
    val estimated1Rm = weightLiftedKg * (1.0 + reps / 30.0)
    val ratio = if (bodyWeightKg > 0.0) estimated1Rm / bodyWeightKg else 0.0

    return RelativeStrengthProgress(
        exerciseName = exerciseName,
        bodyWeightKg = bodyWeightKg,
        estimated1RmKg = estimated1Rm,
        relativeRatio = ratio,
        bestWeightKg = weightLiftedKg,
        bestReps = reps,
        achievedInPhase = achievedInPhase,
        tier = classifyStrengthTier(exerciseName, ratio),
    )
}
