package app.kilo.data

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HevyCsvParserTest {

    @Test
    fun parseBasicWorkoutWithVolumesAnd1Rm() {
        val csv = """
            title,start_time,end_time,description,exercise_title,superset_id,exercise_notes,set_index,set_type,weight_kg,reps,distance_km,duration_seconds,rpe
            "Leg Day","2024-05-10 17:30:00","2024-05-10 18:30:00","Felt great","Barbell Squat","","Depth check",1,"warmup",60.0,10,"",0,
            "Leg Day","2024-05-10 17:30:00","2024-05-10 18:30:00","Felt great","Barbell Squat","","Depth check",2,"normal",100.0,8,"",0,8.0
            "Leg Day","2024-05-10 17:30:00","2024-05-10 18:30:00","Felt great","Barbell Squat","","Depth check",3,"normal",120.0,5,"",0,9.0
            "Leg Day","2024-05-10 17:30:00","2024-05-10 18:30:00","Felt great","Barbell Squat","","Depth check",4,"drop",90.0,10,"",0,9.5
        """.trimIndent()

        val workouts = HevyCsvParser.parse(csv)
        assertEquals(1, workouts.size)

        val workout = workouts[0]
        assertEquals("Leg Day", workout.title)
        assertEquals(60, workout.durationMinutes)
        assertEquals(1, workout.exercises.size)

        val exercise = workout.exercises[0]
        assertEquals("Barbell Squat", exercise.title)
        assertEquals(MuscleCategory.LEGS_GLUTES, exercise.category)
        assertEquals(4, exercise.sets.size)
        assertEquals(120.0, exercise.maxWeightKg)

        // Volume: warm up (60 * 10) excluded; 100*8 + 120*5 + 90*10 = 800 + 600 + 900 = 2300.0
        assertEquals(2300.0, exercise.totalVolumeKg)
        assertEquals(2300.0, workout.totalVolumeKg)

        // 1RM: 120 * (1 + 5/30) = 120 * 1.1666667 = 140.0
        assertEquals(140.0, exercise.estimated1RmKg)
    }

    @Test
    fun parseMultipleWorkoutsDescendingOrder() {
        val csv = """
            title,start_time,end_time,description,exercise_title,superset_id,exercise_notes,set_index,set_type,weight_kg,reps,distance_km,duration_seconds,rpe
            "Push Day","2024-05-10 10:00:00","2024-05-10 11:00:00","","Bench Press","","",1,"normal",80.0,10,"",0,
            "Pull Day","2024-05-12 10:00:00","2024-05-12 11:00:00","","Barbell Row","","",1,"normal",70.0,10,"",0,
            "Leg Day","2024-05-08 10:00:00","2024-05-08 11:00:00","","Squat","","",1,"normal",100.0,5,"",0,
        """.trimIndent()

        val workouts = HevyCsvParser.parse(csv)
        assertEquals(3, workouts.size)
        assertEquals("Pull Day", workouts[0].title)
        assertEquals("Push Day", workouts[1].title)
        assertEquals("Leg Day", workouts[2].title)
        assertTrue(workouts[0].dateEpochDays > workouts[1].dateEpochDays)
        assertTrue(workouts[1].dateEpochDays > workouts[2].dateEpochDays)
    }

    @Test
    fun inferMuscleCategoriesAccurately() {
        // LEGS_GLUTES
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Barbell Squat"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Romanian Deadlift"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Hip Thrust"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Leg Extension"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Standing Calf Raise"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Walking Lunge"))
        assertEquals(MuscleCategory.LEGS_GLUTES, HevyCsvParser.inferMuscleCategory("Bulgarian Split Squat"))

        // BACK_PULL
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Bent Over Barbell Row"))
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Lat Pulldown (Cable)"))
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Pull Up"))
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Chin Up"))
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Seated Cable Row"))
        assertEquals(MuscleCategory.BACK_PULL, HevyCsvParser.inferMuscleCategory("Face Pull"))

        // CHEST_PUSH
        assertEquals(MuscleCategory.CHEST_PUSH, HevyCsvParser.inferMuscleCategory("Barbell Bench Press"))
        assertEquals(MuscleCategory.CHEST_PUSH, HevyCsvParser.inferMuscleCategory("Incline Dumbbell Press"))
        assertEquals(MuscleCategory.CHEST_PUSH, HevyCsvParser.inferMuscleCategory("Push Up"))
        assertEquals(MuscleCategory.CHEST_PUSH, HevyCsvParser.inferMuscleCategory("Cable Fly"))

        // SHOULDERS_ARMS
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Overhead Press"))
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Dumbbell Shoulder Press"))
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Dumbbell Lateral Raise"))
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Bicep Curl (Dumbbell)"))
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Tricep Pushdown"))
        assertEquals(MuscleCategory.SHOULDERS_ARMS, HevyCsvParser.inferMuscleCategory("Dips"))

        // CORE_FULL_BODY
        assertEquals(MuscleCategory.CORE_FULL_BODY, HevyCsvParser.inferMuscleCategory("Plank"))
        assertEquals(MuscleCategory.CORE_FULL_BODY, HevyCsvParser.inferMuscleCategory("Abdominal Crunch"))
        assertEquals(MuscleCategory.CORE_FULL_BODY, HevyCsvParser.inferMuscleCategory("Hanging Leg Raise"))
        assertEquals(MuscleCategory.CORE_FULL_BODY, HevyCsvParser.inferMuscleCategory("Power Clean"))
        assertEquals(MuscleCategory.CORE_FULL_BODY, HevyCsvParser.inferMuscleCategory("Burpee"))

        // OTHER
        assertEquals(MuscleCategory.OTHER, HevyCsvParser.inferMuscleCategory("Treadmill Running"))
        assertEquals(MuscleCategory.OTHER, HevyCsvParser.inferMuscleCategory("Stationary Bike"))
    }

    @Test
    fun handlesQuotesCommasAndMultilineDescriptions() {
        val csv = """
            title,start_time,end_time,description,exercise_title,superset_id,exercise_notes,set_index,set_type,weight_kg,reps,distance_km,duration_seconds,rpe
            "Upper, Lower Mix","2024-06-01 10:00:00","2024-06-01 11:15:00","Heavy day,
            feeling tired but strong","Barbell Bench Press, Paused","","pause for 2 sec, good form",1,"normal",100.0,5,"",0,8.5
        """.trimIndent()

        val workouts = HevyCsvParser.parse(csv)
        assertEquals(1, workouts.size)
        val w = workouts[0]
        assertEquals("Upper, Lower Mix", w.title)
        assertEquals(75, w.durationMinutes)
        assertEquals(1, w.exercises.size)
        assertEquals("Barbell Bench Press, Paused", w.exercises[0].title)
        assertEquals(MuscleCategory.CHEST_PUSH, w.exercises[0].category)
        assertEquals(500.0, w.exercises[0].totalVolumeKg)
        assertEquals(8.5, w.exercises[0].sets[0].rpe)
    }

    @Test
    fun handlesIsoAndAlternativeDateFormats() {
        val csv = """
            title,start_time,end_time,description,exercise_title,superset_id,exercise_notes,set_index,set_type,weight_kg,reps,distance_km,duration_seconds,rpe
            "ISO Workout","2024-07-15T14:00:00Z","2024-07-15T15:00:00Z","","Deadlift","","",1,"normal",150.0,5,"",0,
            "Text Date Workout","10 May 2024, 18:00","10 May 2024, 19:00","","Squat","","",1,"normal",100.0,5,"",0,
        """.trimIndent()

        val workouts = HevyCsvParser.parse(csv)
        assertEquals(2, workouts.size)
        assertEquals("ISO Workout", workouts[0].title)
        assertEquals("Text Date Workout", workouts[1].title)
        assertTrue(workouts[0].startTimeEpochMs > 0)
        assertTrue(workouts[1].startTimeEpochMs > 0)
    }

    @Test
    fun handlesEmptyAndMalformedCsv() {
        assertTrue(HevyCsvParser.parse("").isEmpty())
        assertTrue(HevyCsvParser.parse("   \n\n  ").isEmpty())
        assertTrue(HevyCsvParser.parse("header1,header2\n").isEmpty())
    }

    @Test
    fun workoutRepositoryPersistsAndClears() = runTest {
        val settings = MapSettings()
        val repo = WorkoutRepositorySettings(settings)

        assertEquals(emptyList(), repo.workouts.first())

        val session = HevyWorkoutSession(
            id = "test_1",
            title = "Morning Workout",
            dateEpochDays = 19800L,
            startTimeEpochMs = 1715362200000L,
            endTimeEpochMs = 1715365800000L,
            durationMinutes = 60,
            totalVolumeKg = 2500.0,
            exercises = listOf(
                HevyExercise(
                    title = "Squat",
                    category = MuscleCategory.LEGS_GLUTES,
                    sets = listOf(HevySet(1, "normal", 100.0, 10)),
                    totalVolumeKg = 1000.0,
                    maxWeightKg = 100.0,
                    estimated1RmKg = 133.33,
                )
            ),
        )

        repo.saveWorkouts(listOf(session))
        val loaded = repo.workouts.first()
        assertEquals(1, loaded.size)
        assertEquals("test_1", loaded[0].id)
        assertEquals("Morning Workout", loaded[0].title)
        assertEquals(2500.0, loaded[0].totalVolumeKg)
        assertEquals(1, loaded[0].exercises.size)
        assertEquals(MuscleCategory.LEGS_GLUTES, loaded[0].exercises[0].category)

        // Reload from new repo instance with same settings
        val reloadedRepo = WorkoutRepositorySettings(settings)
        assertEquals(1, reloadedRepo.workouts.first().size)

        // Clear
        reloadedRepo.clearWorkouts()
        assertTrue(reloadedRepo.workouts.first().isEmpty())
        assertTrue(WorkoutRepositorySettings(settings).workouts.first().isEmpty())
    }

    @Test
    fun prefsRepositoryCycleLengthDaysAndAiCoach() = runTest {
        val settings = MapSettings()
        val prefs = SettingsPrefsRepository(settings)

        // Defaults
        assertEquals(28, prefs.cycleLengthDays.first())
        assertEquals(null, prefs.aiCoachApiKey.first())
        assertEquals("GEMINI", prefs.aiProvider.first())

        // Clamping cycleLengthDays between 21 and 45
        prefs.setCycleLengthDays(15)
        assertEquals(21, prefs.cycleLengthDays.first())
        assertEquals(21, prefs.cycleLength.value)

        prefs.setCycleLengthDays(50)
        assertEquals(45, prefs.cycleLengthDays.first())
        assertEquals(45, prefs.cycleLength.value)

        prefs.setCycleLengthDays(32)
        assertEquals(32, prefs.cycleLengthDays.first())
        assertEquals(32, prefs.cycleLength.value)

        // AI coach key
        prefs.setAiCoachApiKey("  sk-test-12345  ")
        assertEquals("sk-test-12345", prefs.aiCoachApiKey.first())

        prefs.setAiCoachApiKey(null)
        assertEquals(null, prefs.aiCoachApiKey.first())

        // AI provider
        prefs.setAiProvider("OPENAI")
        assertEquals("OPENAI", prefs.aiProvider.first())

        prefs.setAiProvider("")
        assertEquals("GEMINI", prefs.aiProvider.first())
    }

    @Test
    fun mergeWorkoutsPreservesHistoryAndUpgradesHealthSummariesWithRichCsv() {
        val oldCsvSession = HevyWorkoutSession(
            id = "hevy_1715000000000_OldLegDay",
            title = "Old Leg Day",
            dateEpochDays = 19850L,
            startTimeEpochMs = 1715000000000L,
            endTimeEpochMs = 1715003600000L,
            durationMinutes = 60,
            totalVolumeKg = 8500.0,
            exercises = listOf(
                HevyExercise(
                    title = "Barbell Squat",
                    category = MuscleCategory.LEGS_GLUTES,
                    sets = listOf(HevySet(1, "normal", 100.0, 10)),
                    totalVolumeKg = 1000.0,
                    maxWeightKg = 100.0,
                    estimated1RmKg = 133.33,
                )
            ),
            source = WorkoutSource.CSV,
        )

        val healthAutoSyncedSession = HevyWorkoutSession(
            id = "health_workout_1715362200000",
            title = "Hevy Antrenmanı (Apple Health)",
            dateEpochDays = 19853L,
            startTimeEpochMs = 1715362200000L, // May 10, 2024 17:30:00 UTC
            endTimeEpochMs = 1715365800000L,
            durationMinutes = 60,
            totalVolumeKg = 0.0,
            exercises = emptyList(),
            source = WorkoutSource.HEALTH,
        )

        val existing = listOf(healthAutoSyncedSession, oldCsvSession)

        // Incoming CSV has a workout at the same time as healthAutoSyncedSession (+2 min offset) and a brand new workout
        val incomingRichUpgrade = HevyWorkoutSession(
            id = "hevy_1715362320000_LegDay",
            title = "Leg Day",
            dateEpochDays = 19853L,
            startTimeEpochMs = 1715362320000L,
            endTimeEpochMs = 1715365920000L,
            durationMinutes = 60,
            totalVolumeKg = 9200.0,
            exercises = listOf(
                HevyExercise(
                    title = "Barbell Squat",
                    category = MuscleCategory.LEGS_GLUTES,
                    sets = listOf(HevySet(1, "normal", 120.0, 8)),
                    totalVolumeKg = 960.0,
                    maxWeightKg = 120.0,
                    estimated1RmKg = 152.0,
                )
            ),
            source = WorkoutSource.CSV,
        )

        val brandNewCsvSession = HevyWorkoutSession(
            id = "hevy_1715500000000_PushDay",
            title = "Push Day",
            dateEpochDays = 19855L,
            startTimeEpochMs = 1715500000000L,
            endTimeEpochMs = 1715503600000L,
            durationMinutes = 60,
            totalVolumeKg = 5400.0,
            exercises = listOf(
                HevyExercise(
                    title = "Bench Press",
                    category = MuscleCategory.CHEST_PUSH,
                    sets = listOf(HevySet(1, "normal", 80.0, 8)),
                    totalVolumeKg = 640.0,
                    maxWeightKg = 80.0,
                    estimated1RmKg = 101.33,
                )
            ),
            source = WorkoutSource.CSV,
        )

        val result = mergeWorkouts(existing, listOf(incomingRichUpgrade, brandNewCsvSession))

        // Old Leg Day is preserved, Health session is upgraded to rich CSV Leg Day, and Push Day is added -> total 3
        assertEquals(3, result.merged.size)
        assertEquals(1, result.addedCount)
        assertEquals(1, result.upgradedCount)

        // All 3 sessions should now be rich CSV sessions
        assertTrue(result.merged.all { it.source == WorkoutSource.CSV && it.exercises.isNotEmpty() })
        assertEquals("Push Day", result.merged[0].title)
        assertEquals("Leg Day", result.merged[1].title)
        assertEquals(9200.0, result.merged[1].totalVolumeKg)
        assertEquals("Old Leg Day", result.merged[2].title)

        // Verify that a subsequent Health sync for the same timestamp does NOT overwrite the rich CSV workout
        val reverseResult = mergeWorkouts(result.merged, listOf(healthAutoSyncedSession))
        assertEquals(3, reverseResult.merged.size)
        assertEquals(0, reverseResult.addedCount)
        assertEquals(0, reverseResult.upgradedCount)
        assertEquals(9200.0, reverseResult.merged[1].totalVolumeKg)
        assertEquals(WorkoutSource.CSV, reverseResult.merged[1].source)

        // Verify fallback matching by dateEpochDays when incoming CSV has startTimeEpochMs == 0L
        val healthOnDay20000 = healthAutoSyncedSession.copy(
            id = "health_day_20000",
            dateEpochDays = 20000L,
            startTimeEpochMs = 1728000000000L,
        )
        val csvWithoutTimeOnDay20000 = incomingRichUpgrade.copy(
            id = "hevy_0_DateOnly",
            title = "Date Only Workout",
            dateEpochDays = 20000L,
            startTimeEpochMs = 0L,
            endTimeEpochMs = 0L,
        )
        val dateFallbackResult = mergeWorkouts(listOf(healthOnDay20000), listOf(csvWithoutTimeOnDay20000))
        assertEquals(1, dateFallbackResult.merged.size)
        assertEquals(1, dateFallbackResult.upgradedCount)
        assertEquals(WorkoutSource.CSV, dateFallbackResult.merged.first().source)
    }
}
