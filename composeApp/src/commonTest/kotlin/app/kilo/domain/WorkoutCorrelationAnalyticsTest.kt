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

    @Test
    fun strengthNormsForProvidesSexStratifiedBoundsAndTierRanges() {
        val femaleSquat = strengthNormsFor("Squat", isMale = false)
        assertFalse(femaleSquat.isMale)
        assertEquals(0.85, femaleSquat.intermediateMin)
        assertEquals(1.25, femaleSquat.advancedMin)
        assertEquals(1.75, femaleSquat.eliteMin)

        val femaleBounds = femaleSquat.tierBounds
        assertEquals(4, femaleBounds.size)
        assertEquals(0.0, femaleBounds[0].minRatio)
        assertEquals(0.84, femaleBounds[0].maxRatio)
        assertEquals(0.85, femaleBounds[1].minRatio)
        assertEquals(1.24, femaleBounds[1].maxRatio)
        assertEquals(1.25, femaleBounds[2].minRatio)
        assertEquals(1.74, femaleBounds[2].maxRatio)
        assertEquals(1.75, femaleBounds[3].minRatio)
        assertNull(femaleBounds[3].maxRatio)

        val maleSquat = strengthNormsFor("Squat", isMale = true)
        assertTrue(maleSquat.isMale)
        assertEquals(1.25, maleSquat.intermediateMin)
        assertEquals(1.75, maleSquat.advancedMin)
        assertEquals(2.25, maleSquat.eliteMin)

        val maleBench = strengthNormsFor("Bench Press", isMale = true)
        assertEquals(0.85, maleBench.intermediateMin)
        assertEquals(1.20, maleBench.advancedMin)
        assertEquals(1.60, maleBench.eliteMin)

        val maleOhp = strengthNormsFor("Overhead Press", isMale = true)
        assertEquals(0.55, maleOhp.intermediateMin)
        assertEquals(0.80, maleOhp.advancedMin)
        assertEquals(1.05, maleOhp.eliteMin)
    }

    @Test
    fun calculateRelativeStrengthComputesNextTierProgressAndDeltaKg() {
        // 80 kg bodyweight male doing 80 kg Bench Press for 1 rep:
        // 1RM = 80 kg, ratio = 1.0 -> Intermediate (>= 0.85, < 1.20)
        // Next tier is Advanced (target: 1.20x BW = 96.0 kg)
        // Delta needed = 96.0 - 80.0 = 16.0 kg
        val progress = calculateRelativeStrength(
            bodyWeightKg = 80.0,
            exerciseName = "Barbell Bench Press",
            weightLiftedKg = 80.0,
            reps = 1,
            isMale = true,
        )

        assertEquals(80.0, progress.estimated1RmKg)
        assertEquals(1.0, progress.relativeRatio)
        assertEquals(StrengthTier.INTERMEDIATE, progress.tier)
        assertTrue(progress.isMaleStandard)
        assertEquals(StrengthTier.ADVANCED, progress.nextTier)
        assertEquals(1.20, progress.nextTierTargetRatio)
        assertEquals(16.0, progress.nextTierDeltaKg)

        // Elite male lifter doing 200 kg Squat at 80 kg BW (2.50x BW >= 2.25x Elite):
        val eliteProgress = calculateRelativeStrength(
            bodyWeightKg = 80.0,
            exerciseName = "Squat",
            weightLiftedKg = 200.0,
            reps = 1,
            isMale = true,
        )
        assertEquals(StrengthTier.ELITE, eliteProgress.tier)
        assertNull(eliteProgress.nextTier)
        assertNull(eliteProgress.nextTierTargetRatio)
        assertNull(eliteProgress.nextTierDeltaKg)
    }

    @Test
    fun analyzeGeneralWorkoutsComputesUniversalTonnageMuscleSharesAndPrs() {
        val s1 = app.kilo.data.HevyWorkoutSession(
            id = "s1",
            title = "Heavy Legs",
            dateEpochDays = 10,
            startTimeEpochMs = 1000L,
            endTimeEpochMs = 4600L,
            durationMinutes = 60,
            totalVolumeKg = 9000.0,
            exercises = listOf(
                app.kilo.data.HevyExercise(
                    title = "Squat",
                    category = app.kilo.data.MuscleCategory.LEGS_GLUTES,
                    sets = listOf(
                        app.kilo.data.HevySet(1, "normal", 100.0, 5, null),
                        app.kilo.data.HevySet(2, "normal", 110.0, 5, null), // 110 * (1 + 5/30) = 128.33
                    ),
                    maxWeightKg = 110.0,
                    estimated1RmKg = 128.33,
                    totalVolumeKg = 6000.0,
                ),
                app.kilo.data.HevyExercise(
                    title = "Romanian Deadlift",
                    category = app.kilo.data.MuscleCategory.LEGS_GLUTES,
                    sets = listOf(
                        app.kilo.data.HevySet(1, "normal", 80.0, 8, null),
                    ),
                    maxWeightKg = 80.0,
                    estimated1RmKg = 101.33,
                    totalVolumeKg = 3000.0,
                ),
            ),
        )

        val s2 = app.kilo.data.HevyWorkoutSession(
            id = "s2",
            title = "Push Day",
            dateEpochDays = 12,
            startTimeEpochMs = 5000L,
            endTimeEpochMs = 8600L,
            durationMinutes = 60,
            totalVolumeKg = 5000.0,
            exercises = listOf(
                app.kilo.data.HevyExercise(
                    title = "Bench Press",
                    category = app.kilo.data.MuscleCategory.CHEST_PUSH,
                    sets = listOf(
                        app.kilo.data.HevySet(1, "normal", 70.0, 8, null),
                        app.kilo.data.HevySet(2, "normal", 75.0, 6, null),
                    ),
                    maxWeightKg = 75.0,
                    estimated1RmKg = 90.0,
                    totalVolumeKg = 5000.0,
                ),
            ),
        )

        val analytics = analyzeGeneralWorkouts(listOf(s1, s2))

        assertEquals(2, analytics.sessionCount)
        assertEquals(14000.0, analytics.totalVolumeKg)
        assertEquals(7000.0, analytics.avgSessionVolumeKg)
        assertEquals(9000.0, analytics.maxSessionVolumeKg)
        assertEquals(9000.0, analytics.maxLegsOrBackSessionVolumeKg)

        // Muscle share: LEGS_GLUTES has 3 sets (60%), CHEST_PUSH has 2 sets (40%)
        val legsShare = analytics.muscleShares.first { it.category == app.kilo.data.MuscleCategory.LEGS_GLUTES }
        assertEquals(3, legsShare.setCount)
        assertEquals(9000.0, legsShare.totalVolumeKg)
        assertEquals(60, legsShare.sharePercent)

        val chestShare = analytics.muscleShares.first { it.category == app.kilo.data.MuscleCategory.CHEST_PUSH }
        assertEquals(2, chestShare.setCount)
        assertEquals(5000.0, chestShare.totalVolumeKg)
        assertEquals(40, chestShare.sharePercent)

        // Top PRs: Squat (128.33), RDL (101.33), Bench Press (90.0)
        assertEquals(3, analytics.topExercisePrs.size)
        assertEquals("Squat", analytics.topExercisePrs[0].exerciseName)
        assertEquals("Romanian Deadlift", analytics.topExercisePrs[1].exerciseName)
        assertEquals("Bench Press", analytics.topExercisePrs[2].exerciseName)
    }

    @Test
    fun calculateCycleWorkoutCorrelationClassifiesFollicularLutealDualPeakCorrectly() {
        // Simulating the user's exact data distribution:
        // Follicular: 39 PRs (44%), Luteal: 32 PRs (36%), Menstrual: 10 PRs (11%), Ovulation: 7 PRs (8%)
        // Total = 88 PRs
        val workouts = mutableListOf<WorkoutSessionSummary>()
        fun addPrs(days: List<Long>, count: Int, startWeight: Double, prefix: String) {
            for (i in 0 until count) {
                val d = days[i % days.size]
                val w = startWeight + i
                workouts.add(
                    WorkoutSessionSummary(
                        id = "${prefix}_$i",
                        title = "Session",
                        dateEpochDays = d,
                        totalVolumeKg = 5000.0,
                        isLegsOrBack = true,
                        exerciseNames = listOf("Ex_${prefix}_$i"),
                        maxWeights = mapOf("Ex_${prefix}_$i" to w),
                        avgRpe = 8.0,
                    )
                )
            }
        }
        val fDays = listOf(6L, 7L, 8L, 9L, 10L, 11L, 12L)
        val lDays = listOf(17L, 18L, 19L, 20L, 21L, 22L, 23L)
        val mDays = listOf(1L, 2L, 3L, 4L)
        val oDays = listOf(14L, 15L)

        addPrs(fDays, 39, 100.0, "f")
        addPrs(lDays, 32, 200.0, "l")
        addPrs(mDays, 10, 300.0, "m")
        addPrs(oDays, 7, 400.0, "o")

        val correlation = analyzeCycleWorkoutCorrelation(
            workouts = workouts,
            cycleStartEpochDays = 1L,
            cycleLength = 28,
        )
        assertEquals(88, correlation.prDistribution.values.sum())
        assertEquals(39, correlation.prDistribution[CyclePhase.FOLLICULAR])
        assertEquals(32, correlation.prDistribution[CyclePhase.LUTEAL])
        assertEquals(10, correlation.prDistribution[CyclePhase.MENSTRUAL])
        assertEquals(7, correlation.prDistribution[CyclePhase.OVULATION])

        assertEquals(CyclePhase.FOLLICULAR, correlation.peakPrPhase)
        assertEquals(44, correlation.peakPrPercentage)
        assertEquals(CyclePhase.LUTEAL, correlation.secondPeakPrPhase)
        assertEquals(36, correlation.secondPeakPrPercentage)
        assertEquals(PrDistributionPattern.FOLLICULAR_LUTEAL_DUAL, correlation.prPattern)
    }

    @Test
    fun calculateCycleWorkoutCorrelationClassifiesLutealDominantCorrectly() {
        val workouts = mutableListOf<WorkoutSessionSummary>()
        val lDays = listOf(17L, 18L, 19L, 20L, 21L, 22L)
        val fDays = listOf(6L, 7L)
        val mDays = listOf(1L, 2L)
        for (i in 0 until 6) {
            workouts.add(WorkoutSessionSummary("l_$i", "S", lDays[i], 5000.0, true, listOf("E$i"), mapOf("E$i" to 100.0 + i)))
        }
        for (i in 0 until 2) {
            workouts.add(WorkoutSessionSummary("f_$i", "S", fDays[i], 5000.0, true, listOf("F$i"), mapOf("F$i" to 200.0 + i)))
        }
        for (i in 0 until 2) {
            workouts.add(WorkoutSessionSummary("m_$i", "S", mDays[i], 5000.0, true, listOf("M$i"), mapOf("M$i" to 300.0 + i)))
        }
        val correlation = analyzeCycleWorkoutCorrelation(workouts, 1L, 28)
        assertEquals(10, correlation.prDistribution.values.sum())
        assertEquals(CyclePhase.LUTEAL, correlation.peakPrPhase)
        assertEquals(60, correlation.peakPrPercentage)
        assertEquals(PrDistributionPattern.LUTEAL_DOMINANT, correlation.prPattern)
    }

    @Test
    fun calculateCycleWorkoutCorrelationClassifiesEmptyWhenNoWorkouts() {
        val correlation = analyzeCycleWorkoutCorrelation(emptyList(), 1L, 28)
        assertEquals(0, correlation.prDistribution.values.sum())
        assertNull(correlation.peakPrPhase)
        assertEquals(0, correlation.peakPrPercentage)
        assertEquals(PrDistributionPattern.EMPTY, correlation.prPattern)
    }
}

