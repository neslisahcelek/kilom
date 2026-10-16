package app.kilo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutCorrelationAnalyticsTest {

    @Test
    fun domsEvaluationDetectsTraumaOnHeavyLegDayWithWeightSpike() {
        // Today is epoch day 100. Heavy leg workout was yesterday (day 99).
        val legWorkout = WorkoutSessionSummary(
            id = "w1",
            title = "Heavy Leg Day",
            dateEpochDays = 99,
            totalVolumeKg = 9500.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Barbell Squat", "Romanian Deadlift"),
            maxWeights = mapOf("Barbell Squat" to 120.0),
        )

        val assessment = evaluateDomsRisk(
            workouts = listOf(legWorkout),
            previousWeightKg = 65.0,
            currentWeightKg = 65.8, // +0.8 kg spike
            currentEpochDays = 100,
        )

        assertTrue(assessment.hasTrauma)
        assertEquals(9500.0, assessment.recentVolumeKg)
        assertEquals(0.8, assessment.expectedFluidWeightKg)
        assertEquals("Heavy Leg Day", assessment.targetMuscleGroup)
        assertEquals(listOf("Barbell Squat", "Romanian Deadlift"), assessment.exerciseNames)
    }

    @Test
    fun domsEvaluationNoTraumaWhenWeightDidNotIncrease() {
        val legWorkout = WorkoutSessionSummary(
            id = "w1",
            title = "Heavy Leg Day",
            dateEpochDays = 99,
            totalVolumeKg = 11000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat"),
        )

        // Weight only increased by 0.1 kg (below 0.3 kg threshold)
        val assessment = evaluateDomsRisk(
            workouts = listOf(legWorkout),
            previousWeightKg = 65.0,
            currentWeightKg = 65.1,
            currentEpochDays = 100,
        )

        assertFalse(assessment.hasTrauma)
        assertEquals(0.0, assessment.expectedFluidWeightKg)
    }

    @Test
    fun domsEvaluationNoTraumaWhenWorkoutIsOlderThan48Hours() {
        // Workout was 3 days ago (day 97 vs today 100)
        val legWorkout = WorkoutSessionSummary(
            id = "w1",
            title = "Heavy Leg Day",
            dateEpochDays = 97,
            totalVolumeKg = 12000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat"),
        )

        val assessment = evaluateDomsRisk(
            workouts = listOf(legWorkout),
            previousWeightKg = 65.0,
            currentWeightKg = 66.0,
            currentEpochDays = 100,
        )

        assertFalse(assessment.hasTrauma)
        assertEquals(0.0, assessment.recentVolumeKg)
        assertNull(assessment.targetMuscleGroup)
    }

    @Test
    fun domsEvaluationNoTraumaWhenNotLegsOrBack() {
        // Upper body / chest workout in last 48h with high volume
        val chestWorkout = WorkoutSessionSummary(
            id = "w2",
            title = "Chest & Arms",
            dateEpochDays = 99,
            totalVolumeKg = 12000.0,
            isLegsOrBack = false,
            exerciseNames = listOf("Bench Press", "Bicep Curls"),
        )

        val assessment = evaluateDomsRisk(
            workouts = listOf(chestWorkout),
            previousWeightKg = 65.0,
            currentWeightKg = 65.9,
            currentEpochDays = 100,
        )

        assertFalse(assessment.hasTrauma)
        assertEquals(0.0, assessment.recentVolumeKg)
    }

    @Test
    fun domsEvaluationNoTraumaWhenVolumeBelowThreshold() {
        // Leg workout with only 5000 kg volume (< 8000 kg threshold)
        val lightWorkout = WorkoutSessionSummary(
            id = "w3",
            title = "Light Legs",
            dateEpochDays = 99,
            totalVolumeKg = 5000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Lunge"),
        )

        val assessment = evaluateDomsRisk(
            workouts = listOf(lightWorkout),
            previousWeightKg = 65.0,
            currentWeightKg = 65.8,
            currentEpochDays = 100,
        )

        assertFalse(assessment.hasTrauma)
        assertEquals(0.0, assessment.recentVolumeKg)
    }

    @Test
    fun domsEvaluationNoTraumaWhenPreviousWeightNull() {
        val legWorkout = WorkoutSessionSummary(
            id = "w1",
            title = "Heavy Leg Day",
            dateEpochDays = 99,
            totalVolumeKg = 9500.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat"),
        )

        val assessment = evaluateDomsRisk(
            workouts = listOf(legWorkout),
            previousWeightKg = null,
            currentWeightKg = 65.8,
            currentEpochDays = 100,
        )

        assertFalse(assessment.hasTrauma)
        assertEquals(0.0, assessment.expectedFluidWeightKg)
    }

    @Test
    fun analyzeCycleWorkoutCorrelationCalculatesVolumeAndRpeDifference() {
        // Cycle starts at epoch day 1, length 28
        // Day 8 (Follicular): volume 10000, Squat 100, RPE 7.0
        val w1 = WorkoutSessionSummary(
            id = "1",
            title = "Lower Body Hypertrophy",
            dateEpochDays = 8,
            totalVolumeKg = 10000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat"),
            maxWeights = mapOf("Squat" to 100.0),
            avgRpe = 7.0,
        )
        // Day 12 (Follicular - Peak Power): volume 12000, Squat 105 (new PR!), RPE 7.0
        val w2 = WorkoutSessionSummary(
            id = "2",
            title = "Heavy Compounds",
            dateEpochDays = 12,
            totalVolumeKg = 12000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat"),
            maxWeights = mapOf("Squat" to 105.0),
            avgRpe = 7.0,
        )
        // Day 14 (Ovulation): volume 15000, Squat 110 (PR!), Deadlift 140 (PR!), RPE 8.0
        val w3 = WorkoutSessionSummary(
            id = "3",
            title = "Ovulation Power",
            dateEpochDays = 14,
            totalVolumeKg = 15000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Squat", "Deadlift"),
            maxWeights = mapOf("Squat" to 110.0, "Deadlift" to 140.0),
            avgRpe = 8.0,
        )
        // Day 22 (Luteal): volume 8000, Leg Press 200 (PR!), RPE 9.0
        val w4 = WorkoutSessionSummary(
            id = "4",
            title = "Luteal Machines",
            dateEpochDays = 22,
            totalVolumeKg = 8000.0,
            isLegsOrBack = true,
            exerciseNames = listOf("Leg Press"),
            maxWeights = mapOf("Leg Press" to 200.0),
            avgRpe = 9.0,
        )

        val correlation = analyzeCycleWorkoutCorrelation(
            workouts = listOf(w1, w2, w3, w4),
            cycleStartEpochDays = 1,
            cycleLength = 28,
        )

        // Peak total volume was in Follicular (22000 kg vs 15000 kg)
        assertEquals(CyclePhase.FOLLICULAR, correlation.peakVolumePhase)
        // Peak average volume per workout was in Ovulation (15000 kg/session vs 11000 kg/session)
        assertEquals(CyclePhase.OVULATION, correlation.peakAvgVolumePhase)

        // Follicular: 2 workouts, 22000 kg total, avg 11000 kg, avg RPE 7.0
        val follicular = correlation.phaseSummaries.first { it.phase == CyclePhase.FOLLICULAR }
        assertEquals(2, follicular.workoutCount)
        assertEquals(22000.0, follicular.totalVolumeKg)
        assertEquals(11000.0, follicular.avgVolumePerWorkoutKg)
        assertEquals(7.0, follicular.avgRpe)

        // Ovulation: 1 workout, 15000 kg total, avg RPE 8.0
        val ovulation = correlation.phaseSummaries.first { it.phase == CyclePhase.OVULATION }
        assertEquals(1, ovulation.workoutCount)
        assertEquals(15000.0, ovulation.totalVolumeKg)
        assertEquals(8.0, ovulation.avgRpe)

        // Luteal: 1 workout, 8000 kg total, avg RPE 9.0
        val luteal = correlation.phaseSummaries.first { it.phase == CyclePhase.LUTEAL }
        assertEquals(1, luteal.workoutCount)
        assertEquals(8000.0, luteal.totalVolumeKg)
        assertEquals(9.0, luteal.avgRpe)

        // Menstrual: 0 workouts
        val menstrual = correlation.phaseSummaries.first { it.phase == CyclePhase.MENSTRUAL }
        assertEquals(0, menstrual.workoutCount)
        assertEquals(0.0, menstrual.totalVolumeKg)
        assertNull(menstrual.avgRpe)

        // RPE difference between Luteal (9.0) and Follicular (7.0) = +2.0
        assertEquals(2.0, correlation.lutealRpeDifference)

        // PR distribution:
        // Follicular: Squat 100, then Squat 105 -> 2 PRs
        // Ovulation: Squat 110, Deadlift 140 -> 2 PRs
        // Luteal: Leg Press 200 -> 1 PR
        assertEquals(2, correlation.prDistribution[CyclePhase.FOLLICULAR])
        assertEquals(2, correlation.prDistribution[CyclePhase.OVULATION])
        assertEquals(1, correlation.prDistribution[CyclePhase.LUTEAL])
        assertEquals(0, correlation.prDistribution[CyclePhase.MENSTRUAL] ?: 0)

        // Anabolic PR share: (2 + 2) / 5 = 80%
        assertEquals(80, correlation.anabolicPrPercentage)

        // Anabolic avg volume: (10000 + 12000 + 15000) / 3 = 12333.33 kg
        // Luteal avg volume: 8000 kg
        // Diff %: ((12333.33 - 8000) / 8000) * 100 = +54%
        assertEquals(54, correlation.anabolicVsLutealVolumeDiffPercent)
    }

    @Test
    fun calculateRelativeStrengthCalculatesEpleyOneRmRatioAndTier() {
        // 100 kg x 6 reps on a 60 kg bodyweight:
        // 1RM = 100 * (1 + 6/30) = 120 kg
        // Relative ratio = 120 / 60 = 2.0 -> ELITE for Squat (>= 1.75)
        val progress1 = calculateRelativeStrength(
            bodyWeightKg = 60.0,
            exerciseName = "Barbell Back Squat",
            weightLiftedKg = 100.0,
            reps = 6,
            achievedInPhase = CyclePhase.FOLLICULAR,
        )

        assertEquals("Barbell Back Squat", progress1.exerciseName)
        assertEquals(60.0, progress1.bodyWeightKg)
        assertEquals(120.0, progress1.estimated1RmKg)
        assertEquals(2.0, progress1.relativeRatio)
        assertEquals(100.0, progress1.bestWeightKg)
        assertEquals(6, progress1.bestReps)
        assertEquals(CyclePhase.FOLLICULAR, progress1.achievedInPhase)
        assertEquals(StrengthTier.ELITE, progress1.tier)

        // 120 kg x 0 reps (or 1 rep baseline) on 64 kg bodyweight
        val progress2 = calculateRelativeStrength(
            bodyWeightKg = 64.0,
            exerciseName = "Hip Thrust",
            weightLiftedKg = 120.0,
            reps = 0,
        )
        assertEquals(120.0, progress2.estimated1RmKg)
        assertEquals(1.875, progress2.relativeRatio)
        assertEquals(StrengthTier.ELITE, progress2.tier)
    }

    @Test
    fun classifyStrengthTierHandlesMovementCategoriesCorrectly() {
        // Lower body compound (Squat / Deadlift): <0.85 Beginner, >=0.85 Intermediate, >=1.25 Advanced, >=1.75 Elite
        assertEquals(StrengthTier.BEGINNER, classifyStrengthTier("Squat", 0.70))
        assertEquals(StrengthTier.INTERMEDIATE, classifyStrengthTier("Squat", 1.00))
        assertEquals(StrengthTier.ADVANCED, classifyStrengthTier("Deadlift", 1.35))
        assertEquals(StrengthTier.ELITE, classifyStrengthTier("Hip Thrust", 1.80))

        // Upper body horizontal press/pull (Bench Press): <0.55 Beginner, >=0.55 Intermediate, >=0.85 Advanced, >=1.15 Elite
        assertEquals(StrengthTier.BEGINNER, classifyStrengthTier("Bench Press", 0.45))
        assertEquals(StrengthTier.INTERMEDIATE, classifyStrengthTier("Bench Press", 0.65))
        assertEquals(StrengthTier.ADVANCED, classifyStrengthTier("Bench Press", 0.90))
        assertEquals(StrengthTier.ELITE, classifyStrengthTier("Bench Press", 1.20))

        // Vertical press (Overhead Press): <0.35 Beginner, >=0.35 Intermediate, >=0.55 Advanced, >=0.80 Elite
        assertEquals(StrengthTier.BEGINNER, classifyStrengthTier("Overhead Press", 0.30))
        assertEquals(StrengthTier.INTERMEDIATE, classifyStrengthTier("Overhead Press", 0.40))
        assertEquals(StrengthTier.ADVANCED, classifyStrengthTier("Overhead Press", 0.60))
        assertEquals(StrengthTier.ELITE, classifyStrengthTier("Overhead Press", 0.85))
    }
}
