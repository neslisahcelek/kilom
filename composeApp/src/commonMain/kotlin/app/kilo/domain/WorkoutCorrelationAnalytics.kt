package app.kilo.domain

import app.kilo.data.HevyWorkoutSession
import app.kilo.data.MuscleCategory
import kotlin.math.max
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
    val peakPrPhase: CyclePhase? = null,
    val peakPrPercentage: Int = 0,
    val secondPeakPrPhase: CyclePhase? = null,
    val secondPeakPrPercentage: Int = 0,
    val prPattern: PrDistributionPattern = PrDistributionPattern.EMPTY,
    val anabolicPrPercentage: Int = 0,
    val anabolicVsLutealVolumeDiffPercent: Int? = null,
    val lutealRpeDifference: Double?,
)

/**
 * Data-driven pattern classification for personal records (PR) across cycle phases.
 * Evaluates whether PR distribution aligns with, challenges, or is independent of
 * classical literature expectations (McNulty et al., 2020).
 */
enum class PrDistributionPattern {
    EMPTY,
    FOLLICULAR_LUTEAL_DUAL,
    LUTEAL_DOMINANT,
    ANABOLIC_DOMINANT,
    FOLLICULAR_DOMINANT,
    OVULATION_DOMINANT,
    MENSTRUAL_DOMINANT,
    BALANCED,
}

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
 * Lower and upper relative-strength ratio bounds (1RM / Body Weight) for a specific [StrengthTier].
 * When [maxRatio] is null, the tier is open-ended (e.g., Elite >= eliteMin).
 */
data class TierBounds(
    val tier: StrengthTier,
    val minRatio: Double,
    val maxRatio: Double?,
)

/**
 * Normative relative strength thresholds (1RM / Body Weight) for a movement category and biological sex.
 * Based on NSCA (Haff & Triplett, 2016) and Rippetoe, Kilgore & Pendlay (2006).
 */
data class StrengthNorms(
    val isMale: Boolean,
    val intermediateMin: Double,
    val advancedMin: Double,
    val eliteMin: Double,
) {
    val tierBounds: List<TierBounds>
        get() = listOf(
            TierBounds(
                tier = StrengthTier.BEGINNER,
                minRatio = 0.0,
                maxRatio = round((intermediateMin - 0.01) * 100.0) / 100.0,
            ),
            TierBounds(
                tier = StrengthTier.INTERMEDIATE,
                minRatio = intermediateMin,
                maxRatio = round((advancedMin - 0.01) * 100.0) / 100.0,
            ),
            TierBounds(
                tier = StrengthTier.ADVANCED,
                minRatio = advancedMin,
                maxRatio = round((eliteMin - 0.01) * 100.0) / 100.0,
            ),
            TierBounds(
                tier = StrengthTier.ELITE,
                minRatio = eliteMin,
                maxRatio = null,
            ),
        )
}

/**
 * Relative strength progress tracking Epley 1RM relative to current bodyweight,
 * along with normative scale bounds, next tier progress, actual best set, and optional cycle phase context.
 */
data class RelativeStrengthProgress(
    val exerciseName: String,
    val bodyWeightKg: Double,
    val estimated1RmKg: Double,
    val relativeRatio: Double,
    val bestWeightKg: Double = 0.0,
    val bestReps: Int = 0,
    val achievedInPhase: CyclePhase? = null,
    val isMaleStandard: Boolean = false,
    val norms: StrengthNorms = strengthNormsFor(exerciseName, isMaleStandard),
    val tier: StrengthTier = classifyStrengthTier(exerciseName, relativeRatio, isMaleStandard),
    val nextTier: StrengthTier? = when (tier) {
        StrengthTier.BEGINNER -> StrengthTier.INTERMEDIATE
        StrengthTier.INTERMEDIATE -> StrengthTier.ADVANCED
        StrengthTier.ADVANCED -> StrengthTier.ELITE
        StrengthTier.ELITE -> null
    },
    val nextTierTargetRatio: Double? = when (tier) {
        StrengthTier.BEGINNER -> norms.intermediateMin
        StrengthTier.INTERMEDIATE -> norms.advancedMin
        StrengthTier.ADVANCED -> norms.eliteMin
        StrengthTier.ELITE -> null
    },
    val nextTierDeltaKg: Double? = nextTierTargetRatio?.let { targetRatio ->
        if (bodyWeightKg > 0.0) {
            val target1Rm = targetRatio * bodyWeightKg
            max(0.0, round((target1Rm - estimated1RmKg) * 10.0) / 10.0)
        } else {
            null
        }
    },
)

/**
 * Cycle-independent muscle category volume and set distribution.
 */
data class MuscleVolumeShare(
    val category: MuscleCategory,
    val setCount: Int,
    val totalVolumeKg: Double,
    val sharePercent: Int,
)

/**
 * Cycle-independent personal record summary for an exercise.
 */
data class ExercisePrSummary(
    val exerciseName: String,
    val maxWeightKg: Double,
    val estimated1RmKg: Double,
    val bestReps: Int,
    val prCount: Int,
)

/**
 * Cycle-independent universal workout analytics for both male and female users.
 */
data class GeneralWorkoutAnalytics(
    val sessionCount: Int,
    val totalVolumeKg: Double,
    val avgSessionVolumeKg: Double,
    val maxSessionVolumeKg: Double,
    val avgRpe: Double?,
    val totalPrCount: Int,
    val maxLegsOrBackSessionVolumeKg: Double,
    val muscleShares: List<MuscleVolumeShare>,
    val topExercisePrs: List<ExercisePrSummary>,
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
    val durationMinutes: Int = 0,
    val isFromHealth: Boolean = false,
)

/**
 * Returns sex-stratified normative relative strength thresholds (1RM / Body Weight)
 * based on NSCA (Haff & Triplett, 2016) and Rippetoe, Kilgore & Pendlay (2006).
 */
fun strengthNormsFor(
    exerciseName: String,
    isMale: Boolean = false,
): StrengthNorms {
    val lower = exerciseName.lowercase()
    return when {
        lower.contains("overhead") || lower.contains("military") || lower.contains("ohp") || lower.contains("shoulder") -> {
            if (isMale) {
                StrengthNorms(isMale = true, intermediateMin = 0.55, advancedMin = 0.80, eliteMin = 1.05)
            } else {
                StrengthNorms(isMale = false, intermediateMin = 0.35, advancedMin = 0.55, eliteMin = 0.80)
            }
        }
        lower.contains("bench") || lower.contains("chest") || lower.contains("row") || lower.contains("pull") -> {
            if (isMale) {
                StrengthNorms(isMale = true, intermediateMin = 0.85, advancedMin = 1.20, eliteMin = 1.60)
            } else {
                StrengthNorms(isMale = false, intermediateMin = 0.55, advancedMin = 0.85, eliteMin = 1.15)
            }
        }
        else -> {
            // Squat, Deadlift, RDL, Hip Thrust, and lower-body compounds
            if (isMale) {
                StrengthNorms(isMale = true, intermediateMin = 1.25, advancedMin = 1.75, eliteMin = 2.25)
            } else {
                StrengthNorms(isMale = false, intermediateMin = 0.85, advancedMin = 1.25, eliteMin = 1.75)
            }
        }
    }
}

/**
 * Classifies relative strength (1RM / Bodyweight) into biomechanical tiers
 * tailored to the movement pattern and biological sex standard.
 */
fun classifyStrengthTier(
    exerciseName: String,
    relativeRatio: Double,
    isMale: Boolean = false,
): StrengthTier {
    val norms = strengthNormsFor(exerciseName = exerciseName, isMale = isMale)
    return when {
        relativeRatio >= norms.eliteMin -> StrengthTier.ELITE
        relativeRatio >= norms.advancedMin -> StrengthTier.ADVANCED
        relativeRatio >= norms.intermediateMin -> StrengthTier.INTERMEDIATE
        else -> StrengthTier.BEGINNER
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
 * Scans the last 48 hours (epochDays in currentEpochDays - 2 until currentEpochDays) for either:
 * 1. A rich CSV workout targeting major muscle groups (`isLegsOrBack == true` and `totalVolumeKg >= 8000 kg`), OR
 * 2. A Health-synced resistance workout (`isFromHealth == true` and `durationMinutes >= 35`).
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
        val inLast48h = it.dateEpochDays in (currentEpochDays - 2 until currentEpochDays)
        val csvHeavyCompound = it.isLegsOrBack && it.totalVolumeKg >= 8000.0
        val healthStrengthSession = it.isFromHealth && it.durationMinutes >= 35
        inLast48h && (csvHeavyCompound || healthStrengthSession)
    }

    val recentVolumeKg = qualifyingWorkouts.sumOf {
        if (it.totalVolumeKg > 0.0) it.totalVolumeKg else (it.durationMinutes * 180.0)
    }
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
    val sortedPrEntries = prCounts.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }

    val peakPrEntry = sortedPrEntries.getOrNull(0)
    val secondPeakPrEntry = sortedPrEntries.getOrNull(1)

    val peakPrPhase = peakPrEntry?.key
    val peakPrPercentage = if (totalPrs > 0 && peakPrEntry != null) {
        round((peakPrEntry.value.toDouble() / totalPrs.toDouble()) * 100.0).toInt()
    } else {
        0
    }

    val secondPeakPrPhase = secondPeakPrEntry?.key
    val secondPeakPrPercentage = if (totalPrs > 0 && secondPeakPrEntry != null) {
        round((secondPeakPrEntry.value.toDouble() / totalPrs.toDouble()) * 100.0).toInt()
    } else {
        0
    }

    val anabolicPrs = (prCounts[CyclePhase.FOLLICULAR] ?: 0) + (prCounts[CyclePhase.OVULATION] ?: 0)
    val anabolicPrPercentage = if (totalPrs > 0) {
        round((anabolicPrs.toDouble() / totalPrs.toDouble()) * 100.0).toInt()
    } else {
        0
    }

    val prPattern = when {
        totalPrs == 0 -> PrDistributionPattern.EMPTY
        peakPrPhase == CyclePhase.FOLLICULAR && secondPeakPrPhase == CyclePhase.LUTEAL && secondPeakPrPercentage >= 25 ->
            PrDistributionPattern.FOLLICULAR_LUTEAL_DUAL
        peakPrPhase == CyclePhase.LUTEAL && peakPrPercentage >= 35 ->
            PrDistributionPattern.LUTEAL_DOMINANT
        anabolicPrPercentage >= 65 ->
            PrDistributionPattern.ANABOLIC_DOMINANT
        peakPrPhase == CyclePhase.FOLLICULAR && peakPrPercentage >= 35 ->
            PrDistributionPattern.FOLLICULAR_DOMINANT
        peakPrPhase == CyclePhase.OVULATION && peakPrPercentage >= 35 ->
            PrDistributionPattern.OVULATION_DOMINANT
        peakPrPhase == CyclePhase.MENSTRUAL && peakPrPercentage >= 35 ->
            PrDistributionPattern.MENSTRUAL_DOMINANT
        else ->
            PrDistributionPattern.BALANCED
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
        peakPrPhase = peakPrPhase,
        peakPrPercentage = peakPrPercentage,
        secondPeakPrPhase = secondPeakPrPhase,
        secondPeakPrPercentage = secondPeakPrPercentage,
        prPattern = prPattern,
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
    isMale: Boolean = false,
): RelativeStrengthProgress {
    val estimated1Rm = if (reps <= 1) {
        weightLiftedKg
    } else {
        weightLiftedKg * (1.0 + reps / 30.0)
    }
    val ratio = if (bodyWeightKg > 0.0) estimated1Rm / bodyWeightKg else 0.0
    val norms = strengthNormsFor(exerciseName = exerciseName, isMale = isMale)

    return RelativeStrengthProgress(
        exerciseName = exerciseName,
        bodyWeightKg = bodyWeightKg,
        estimated1RmKg = estimated1Rm,
        relativeRatio = ratio,
        bestWeightKg = weightLiftedKg,
        bestReps = reps,
        achievedInPhase = achievedInPhase,
        isMaleStandard = isMale,
        norms = norms,
        tier = classifyStrengthTier(exerciseName = exerciseName, relativeRatio = ratio, isMale = isMale),
    )
}

/**
 * Analyzes workout sessions independently of menstrual cycle for universal (male & female) insights:
 * - Total and average session volume load (tonnage)
 * - Muscle group set and volume distribution
 * - Chronological Personal Records (PRs) and top exercises
 * - Maximum legs/back session volume for DOMS threshold comparison (8,000 kg)
 */
fun analyzeGeneralWorkouts(
    workouts: List<HevyWorkoutSession>,
): GeneralWorkoutAnalytics {
    if (workouts.isEmpty()) {
        return GeneralWorkoutAnalytics(
            sessionCount = 0,
            totalVolumeKg = 0.0,
            avgSessionVolumeKg = 0.0,
            maxSessionVolumeKg = 0.0,
            avgRpe = null,
            totalPrCount = 0,
            maxLegsOrBackSessionVolumeKg = 0.0,
            muscleShares = emptyList(),
            topExercisePrs = emptyList(),
        )
    }

    val sessionCount = workouts.size
    val totalVolumeKg = workouts.sumOf { it.totalVolumeKg }
    val sessionsWithVolume = workouts.count { it.totalVolumeKg > 0.0 }
    val avgSessionVolumeKg = if (sessionsWithVolume > 0) totalVolumeKg / sessionsWithVolume else 0.0
    val maxSessionVolumeKg = workouts.maxOfOrNull { it.totalVolumeKg } ?: 0.0

    val allRpes = workouts.flatMap { s -> s.exercises.flatMap { e -> e.sets.mapNotNull { it.rpe } } }
    val avgRpe = if (allRpes.isNotEmpty()) round(allRpes.average() * 10.0) / 10.0 else null

    val legsOrBackSessions = workouts.filter { session ->
        session.exercises.any {
            it.category == MuscleCategory.LEGS_GLUTES || it.category == MuscleCategory.BACK_PULL
        }
    }
    val maxLegsOrBackSessionVolumeKg = legsOrBackSessions.maxOfOrNull { it.totalVolumeKg } ?: 0.0

    // Muscle group set & volume share
    val trackedCategories = listOf(
        MuscleCategory.LEGS_GLUTES,
        MuscleCategory.BACK_PULL,
        MuscleCategory.CHEST_PUSH,
        MuscleCategory.SHOULDERS_ARMS,
        MuscleCategory.CORE_FULL_BODY,
    )
    val allExercises = workouts.flatMap { it.exercises }
    val totalSets = allExercises.sumOf { it.sets.size }.coerceAtLeast(1)

    val muscleShares = trackedCategories.map { category ->
        val catExercises = allExercises.filter { it.category == category }
        val setCount = catExercises.sumOf { it.sets.size }
        val catVolume = catExercises.sumOf { it.totalVolumeKg }
        val sharePct = round((setCount.toDouble() / totalSets.toDouble()) * 100.0).toInt()
        MuscleVolumeShare(
            category = category,
            setCount = setCount,
            totalVolumeKg = catVolume,
            sharePercent = sharePct,
        )
    }.filter { it.setCount > 0 }.sortedByDescending { it.setCount }

    // Chronological PR count and best exercise records
    val sortedChronologically = workouts.sortedWith(
        compareBy<HevyWorkoutSession> { it.dateEpochDays }.thenBy { it.startTimeEpochMs }
    )
    val runningMaxWeight = mutableMapOf<String, Double>()
    val prCountByExercise = mutableMapOf<String, Int>()
    val best1RmByExercise = mutableMapOf<String, Double>()
    val bestRepsByExercise = mutableMapOf<String, Int>()

    for (session in sortedChronologically) {
        for (exercise in session.exercises) {
            val title = exercise.title.trim()
            if (title.isBlank()) continue
            val prevMax = runningMaxWeight[title] ?: 0.0
            if (exercise.maxWeightKg > prevMax && exercise.maxWeightKg > 0.0) {
                runningMaxWeight[title] = exercise.maxWeightKg
                prCountByExercise[title] = (prCountByExercise[title] ?: 0) + 1
            }
            val currentBest1Rm = best1RmByExercise[title] ?: 0.0
            if (exercise.estimated1RmKg > currentBest1Rm) {
                best1RmByExercise[title] = exercise.estimated1RmKg
                val maxSet = exercise.sets.maxByOrNull { it.weightKg }
                bestRepsByExercise[title] = maxSet?.reps ?: 1
            }
        }
    }

    val totalPrCount = prCountByExercise.values.sum()
    val topExercisePrs = runningMaxWeight.entries
        .map { (name, maxW) ->
            ExercisePrSummary(
                exerciseName = name,
                maxWeightKg = maxW,
                estimated1RmKg = best1RmByExercise[name] ?: maxW,
                bestReps = bestRepsByExercise[name] ?: 1,
                prCount = prCountByExercise[name] ?: 1,
            )
        }
        .sortedByDescending { it.estimated1RmKg }
        .take(6)

    return GeneralWorkoutAnalytics(
        sessionCount = sessionCount,
        totalVolumeKg = totalVolumeKg,
        avgSessionVolumeKg = avgSessionVolumeKg,
        maxSessionVolumeKg = maxSessionVolumeKg,
        avgRpe = avgRpe,
        totalPrCount = totalPrCount,
        maxLegsOrBackSessionVolumeKg = maxLegsOrBackSessionVolumeKg,
        muscleShares = muscleShares,
        topExercisePrs = topExercisePrs,
    )
}

