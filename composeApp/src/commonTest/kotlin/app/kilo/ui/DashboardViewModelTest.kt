package app.kilo.ui

import app.kilo.data.SettingsPrefsRepository
import app.kilo.data.SettingsWeightRepository
import app.kilo.domain.WeightTag
import app.kilo.domain.WeightUnit
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.kilo.data.HevyWorkoutSession
import app.kilo.data.HevyExercise
import app.kilo.data.HevySet
import app.kilo.data.MuscleCategory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedNow = Instant.parse("2026-10-07T12:00:00Z")
    private val utc = TimeZone.UTC

    private class FakeFeedback : DashboardFeedback {
        var successCount = 0
        var scanCount = 0
        var lightCount = 0
        override fun success() { successCount++ }
        override fun scanComplete() { scanCount++ }
        override fun light() { lightCount++ }
    }

    private class FakeWorkoutRepository : app.kilo.data.WorkoutRepository {
        private val _workouts = MutableStateFlow<List<HevyWorkoutSession>>(emptyList())
        override val workouts: StateFlow<List<HevyWorkoutSession>> = _workouts.asStateFlow()

        override suspend fun saveWorkouts(workouts: List<HevyWorkoutSession>) {
            _workouts.value = workouts
        }

        override suspend fun clearWorkouts() {
            _workouts.value = emptyList()
        }
    }

    private class FakeHealthSync(
        override val isSupported: Boolean = true,
        var authResult: Boolean = true,
    ) : app.kilo.platform.HealthSync {
        var requestedAuth = false
        val writtenSamples = mutableListOf<Pair<Double, Instant>>()
        val writtenBatches = mutableListOf<List<app.kilo.domain.WeightEntry>>()

        override suspend fun requestAuthorization(): Boolean {
            requestedAuth = true
            return authResult
        }

        override suspend fun writeWeight(kg: Double, timestamp: Instant, syncId: String?): Boolean {
            writtenSamples.add(kg to timestamp)
            return true
        }

        var readSamples: List<app.kilo.platform.HealthWeightSample> = emptyList()
        var readWorkoutSamples: List<app.kilo.platform.HealthWorkoutSample> = emptyList()

        override suspend fun readWeights(since: Instant?): List<app.kilo.platform.HealthWeightSample> = readSamples

        override suspend fun readWorkouts(since: Instant?): List<app.kilo.platform.HealthWorkoutSample> = readWorkoutSamples

        override suspend fun writeWeights(entries: List<app.kilo.domain.WeightEntry>): Int {
            writtenBatches.add(entries)
            return entries.size
        }
    }

    private class FakeReminderScheduler(
        override val isSupported: Boolean = true,
        var permissionResult: Boolean = true,
    ) : app.kilo.platform.ReminderScheduler {
        var requestedPermission = false
        var scheduledTime: Pair<Int, Int>? = null
        var scheduledTitle: String? = null
        var scheduledBody: String? = null
        var cancelled = false

        override suspend fun requestPermission(): Boolean {
            requestedPermission = true
            return permissionResult
        }

        override fun schedule(hour: Int, minute: Int, title: String, body: String) {
            scheduledTime = hour to minute
            scheduledTitle = title
            scheduledBody = body
        }

        override fun cancel() {
            cancelled = true
            scheduledTime = null
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        weights: SettingsWeightRepository = SettingsWeightRepository(MapSettings()),
        prefs: SettingsPrefsRepository = SettingsPrefsRepository(MapSettings()),
        workoutRepository: app.kilo.data.WorkoutRepository = FakeWorkoutRepository(),
        healthSync: app.kilo.platform.HealthSync = app.kilo.platform.NoOpHealthSync,
        reminderScheduler: app.kilo.platform.ReminderScheduler = app.kilo.platform.NoOpReminderScheduler,
        readText: suspend (ByteArray) -> List<String> = { emptyList() },
        extractDate: (ByteArray) -> Instant? = { null },
    ): DashboardViewModel = DashboardViewModel(
        weights = weights,
        prefs = prefs,
        workoutRepository = workoutRepository,
        readText = readText,
        feedback = FakeFeedback(),
        healthSync = healthSync,
        reminderScheduler = reminderScheduler,
        extractDate = extractDate,
        now = { fixedNow },
        timeZone = { utc },
    )

    @Test
    fun saveWithoutCustomDateUsesNow() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        vm.openManual()
        vm.onInputChange("72.5")
        vm.save()
        advanceUntilIdle()

        val entries = weights.entries.value
        assertEquals(1, entries.size)
        assertEquals(72.5, entries[0].kg)
        assertEquals(fixedNow, entries[0].at)
        assertNull(vm.state.value.sheet)
    }

    @Test
    fun saveWithSelectedDateUsesBackdatedInstant() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        // Say, user picks 2026-10-04 (3 days ago)
        val selectedDateMillis = Instant.parse("2026-10-04T00:00:00Z").toEpochMilliseconds()

        vm.openManual()
        vm.onInputChange("74.0")
        vm.onDateSelected(selectedDateMillis)
        vm.save()
        advanceUntilIdle()

        val entries = weights.entries.value
        assertEquals(1, entries.size)
        assertEquals(74.0, entries[0].kg)

        val entryDate = entries[0].at.toLocalDateTime(utc).date
        assertEquals(2026, entryDate.year)
        assertEquals(10, entryDate.monthNumber)
        assertEquals(4, entryDate.dayOfMonth)
    }

    @Test
    fun backdatedEntryOrdersChronologicallyInHistory() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        // 1. Add entry for today (70.0 kg)
        vm.openManual()
        vm.onInputChange("70.0")
        vm.save()
        advanceUntilIdle()

        // 2. Add backdated entry for yesterday (71.0 kg)
        val yesterdayMillis = Instant.parse("2026-10-06T00:00:00Z").toEpochMilliseconds()
        vm.openManual()
        vm.onInputChange("71.0")
        vm.onDateSelected(yesterdayMillis)
        vm.save()
        advanceUntilIdle()

        val history = vm.state.value.history
        assertEquals(2, history.size)
        // Newest entry first: 70.0 kg (today), previous is 71.0 kg (yesterday)
        assertEquals(70.0, history[0].entry.kg)
        assertEquals(71.0, history[1].entry.kg)

        // Delta for today vs yesterday: 70.0 - 71.0 = -1.0 kg, 1 day
        val delta = history[0].delta
        assertTrue(delta != null)
        assertEquals(-1.0, delta.kg)
        assertEquals(1, delta.days)
    }

    @Test
    fun saveWithTagCreatesEntryWithTag() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        vm.openManual()
        vm.onInputChange("80.5")
        vm.onTagSelected(WeightTag.MORNING_FASTED)
        vm.save()
        advanceUntilIdle()

        val entries = weights.entries.value
        assertEquals(1, entries.size)
        assertEquals(80.5, entries[0].kg)
        assertEquals("morning_fasted", entries[0].tag)
    }

    @Test
    fun editEntryUpdatesExistingEntryAndTag() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        // 1. Add entry
        vm.openManual()
        vm.onInputChange("72.0")
        vm.onTagSelected(WeightTag.MORNING_FASTED)
        vm.save()
        advanceUntilIdle()

        val originalEntry = weights.entries.value[0]
        assertEquals("morning_fasted", originalEntry.tag)

        // 2. Open edit
        vm.openEdit(originalEntry)
        assertEquals(originalEntry.id, vm.state.value.sheet?.editingEntryId)
        assertEquals(WeightTag.MORNING_FASTED, vm.state.value.sheet?.selectedTag)
        assertEquals("72.00", vm.state.value.sheet?.input)

        // 3. Modify weight and tag
        vm.onInputChange("71.5")
        vm.onTagSelected(WeightTag.POST_WORKOUT)
        vm.save()
        advanceUntilIdle()

        val updatedEntries = weights.entries.value
        assertEquals(1, updatedEntries.size)
        val updated = updatedEntries[0]
        assertEquals(originalEntry.id, updated.id)
        assertEquals(71.5, updated.kg)
        assertEquals(originalEntry.at, updated.at)
        assertEquals("post_workout", updated.tag)
    }

    @Test
    fun editEntryCanClearTag() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        vm.openManual()
        vm.onInputChange("68.0")
        vm.onTagSelected(WeightTag.WATER_RETENTION)
        vm.save()
        advanceUntilIdle()

        val entry = weights.entries.value[0]
        assertEquals("water_retention", entry.tag)

        vm.openEdit(entry)
        vm.onTagSelected(null)
        vm.save()
        advanceUntilIdle()

        val updated = weights.entries.value[0]
        assertNull(updated.tag)
    }

    @Test
    fun exportCsvAndJsonProducesNonEmptyContent() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        vm.openManual()
        vm.onInputChange("75.0")
        vm.onTagSelected(WeightTag.MORNING_FASTED)
        vm.save()
        advanceUntilIdle()

        val csv = vm.exportCsv()
        assertTrue(csv.contains("date,time,weight_kg,tag"))
        assertTrue(csv.contains("75.00"))
        assertTrue(csv.contains("morning_fasted"))

        val json = vm.exportJson()
        assertTrue(json.contains("\"kg\": 75.0"))
        assertTrue(json.contains("\"tag\": \"morning_fasted\""))
    }

    @Test
    fun importDataMergesEntriesAndSetsNotice() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        val csvData = """
            date,time,weight_kg,tag
            2026-10-01,08:00:00,74.20,morning_fasted
            2026-10-02,08:30:00,73.80,post_workout
        """.trimIndent()

        val count = vm.importData(csvData)
        advanceUntilIdle()

        assertEquals(2, count)
        assertEquals(2, weights.entries.value.size)
        assertEquals(2, vm.state.value.history.size)
        assertEquals(true, vm.state.value.backupNotice?.isSuccess)
        assertEquals(2, vm.state.value.backupNotice?.count)
    }

    @Test
    fun importInvalidDataSetsFailureNotice() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        val count = vm.importData("invalid csv without valid rows")
        advanceUntilIdle()

        assertEquals(0, count)
        assertEquals(0, weights.entries.value.size)
        assertEquals(false, vm.state.value.backupNotice?.isSuccess)
        assertEquals(0, vm.state.value.backupNotice?.count)
    }

    @Test
    fun backupSheetStateToggles() = runTest(testDispatcher) {
        val vm = createViewModel()
        assertEquals(false, vm.state.value.isBackupSheetOpen)

        vm.openBackup()
        assertEquals(true, vm.state.value.isBackupSheetOpen)

        vm.dismissBackup()
        assertEquals(false, vm.state.value.isBackupSheetOpen)
    }

    @Test
    fun healthSyncDisabledByDefault() = runTest(testDispatcher) {
        val fakeHealth = FakeHealthSync(isSupported = true)
        val vm = createViewModel(healthSync = fakeHealth)
        advanceUntilIdle()

        assertEquals(true, vm.state.value.isHealthSyncSupported)
        assertEquals(false, vm.state.value.healthSyncEnabled)
    }

    @Test
    fun enableHealthSyncRequestsAuthAndEnablesState() = runTest(testDispatcher) {
        val fakeHealth = FakeHealthSync(isSupported = true, authResult = true)
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs, healthSync = fakeHealth)
        advanceUntilIdle()

        vm.setHealthSyncEnabled(true)
        advanceUntilIdle()

        assertEquals(true, fakeHealth.requestedAuth)
        assertEquals(true, vm.state.value.healthSyncEnabled)
        assertEquals(true, prefs.healthSyncEnabled.value)

        vm.setHealthSyncEnabled(false)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.healthSyncEnabled)
        assertEquals(false, prefs.healthSyncEnabled.value)
    }

    @Test
    fun saveWritesToHealthSyncWhenEnabled() = runTest(testDispatcher) {
        val fakeHealth = FakeHealthSync(isSupported = true, authResult = true)
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs, healthSync = fakeHealth)
        advanceUntilIdle()

        vm.setHealthSyncEnabled(true)
        advanceUntilIdle()

        vm.openManual()
        vm.onInputChange("72.5")
        vm.save()
        advanceUntilIdle()

        assertEquals(1, fakeHealth.writtenSamples.size)
        assertEquals(72.5, fakeHealth.writtenSamples[0].first)
    }

    @Test
    fun syncAllToHealthWritesExistingEntries() = runTest(testDispatcher) {
        val fakeHealth = FakeHealthSync(isSupported = true, authResult = true)
        val weights = SettingsWeightRepository(MapSettings())
        weights.add(app.kilo.domain.WeightEntry.create(70.0, fixedNow))
        weights.add(app.kilo.domain.WeightEntry.create(71.0, fixedNow))

        val vm = createViewModel(weights = weights, healthSync = fakeHealth)
        advanceUntilIdle()

        vm.syncAllToHealth()
        advanceUntilIdle()

        assertEquals(1, fakeHealth.writtenBatches.size)
        assertEquals(2, fakeHealth.writtenBatches[0].size)
        assertEquals(2, vm.state.value.healthSyncNotice?.count)
        assertEquals(true, vm.state.value.healthSyncNotice?.isSuccess)
    }

    @Test
    fun onImageWithExifDateSetsAutoDetectDateAndPreservesInstantOnSave() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val photoCaptureInstant = Instant.parse("2026-10-05T08:15:30Z")
        val vm = createViewModel(
            weights = weights,
            readText = { listOf("75.4 kg") },
            extractDate = { photoCaptureInstant },
        )
        advanceUntilIdle()

        vm.onImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        val sheet = vm.state.value.sheet
        assertNotNull(sheet)
        assertEquals(true, sheet.isDateAutoDetected)
        assertEquals(photoCaptureInstant, sheet.capturedInstant)
        assertEquals("75.40", sheet.input)

        // Save
        vm.save()
        advanceUntilIdle()

        val entries = weights.entries.value
        assertEquals(1, entries.size)
        assertEquals(75.4, entries[0].kg)
        assertEquals(photoCaptureInstant, entries[0].at)
    }

    @Test
    fun onDateSelectedOverridesAutoDetectedFlag() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val photoCaptureInstant = Instant.parse("2026-10-05T08:15:30Z")
        val vm = createViewModel(
            weights = weights,
            readText = { listOf("75.4 kg") },
            extractDate = { photoCaptureInstant },
        )
        advanceUntilIdle()

        vm.onImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertEquals(true, vm.state.value.sheet?.isDateAutoDetected)

        // User manually changes date
        val newDateMillis = Instant.parse("2026-10-04T00:00:00Z").toEpochMilliseconds()
        vm.onDateSelected(newDateMillis)

        assertEquals(false, vm.state.value.sheet?.isDateAutoDetected)
    }

    @Test
    fun healthOnboardingAndImportFlow() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val prefs = SettingsPrefsRepository(MapSettings())
        val healthSync = FakeHealthSync(isSupported = true, authResult = true)
        healthSync.readSamples = listOf(
            app.kilo.platform.HealthWeightSample(72.0, Instant.parse("2026-10-06T09:00:00Z"), "external")
        )
        val vm = createViewModel(weights = weights, prefs = prefs, healthSync = healthSync)
        advanceUntilIdle()

        // Open onboarding
        vm.openHealthOnboarding()
        assertEquals(true, vm.state.value.isHealthOnboardingOpen)

        // Connect and import past
        vm.connectHealth(importAlso = true)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isHealthOnboardingOpen)
        assertEquals(true, prefs.healthSyncEnabled.value)
        assertEquals(true, prefs.healthOnboardingSeen.value)
        assertEquals(1, weights.entries.value.size)
        assertEquals(72.0, weights.entries.value.first().kg)
    }

    @Test
    fun goalWeightFlow() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        weights.add(app.kilo.domain.WeightEntry.create(80.0, fixedNow))
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(weights = weights, prefs = prefs)
        advanceUntilIdle()

        assertNull(vm.state.value.goal)

        vm.openGoalSheet()
        assertEquals(true, vm.state.value.isGoalSheetOpen)

        vm.onGoalInputChange("75.0")
        vm.saveGoal()
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isGoalSheetOpen)
        val goal = vm.state.value.goal
        assertNotNull(goal)
        assertEquals(75.0, goal.goalKg)
        assertEquals(5.0, goal.remainingKg)
        assertEquals(false, goal.reached)

        // Clear goal
        vm.clearGoal()
        advanceUntilIdle()
        assertNull(vm.state.value.goal)
    }

    @Test
    fun periodSummaryFlow() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        weights.add(app.kilo.domain.WeightEntry.create(75.0, fixedNow))
        weights.add(app.kilo.domain.WeightEntry.create(76.0, Instant.parse("2026-10-05T12:00:00Z")))
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        val summary = vm.state.value.summary
        assertNotNull(summary)
        assertEquals(2, summary.entryCount)
        assertEquals(75.5, summary.averageKg)
        assertEquals(75.0, summary.minKg)
        assertEquals(76.0, summary.maxKg)

        // Switch to Month
        vm.setSummaryPeriod(app.kilo.domain.SummaryPeriod.MONTH)
        advanceUntilIdle()
        assertEquals(app.kilo.domain.SummaryPeriod.MONTH, vm.state.value.summaryPeriod)
    }

    @Test
    fun reminderFlow() = runTest(testDispatcher) {
        val prefs = SettingsPrefsRepository(MapSettings())
        val reminder = FakeReminderScheduler(isSupported = true, permissionResult = true)
        val vm = createViewModel(prefs = prefs, reminderScheduler = reminder)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.reminderEnabled)

        vm.setReminderEnabled(true, "Weigh-in", "Check scale")
        advanceUntilIdle()

        assertEquals(true, prefs.reminderEnabled.value)
        assertEquals(true, reminder.requestedPermission)
        assertEquals(8 to 0, reminder.scheduledTime) // default 480 mins = 08:00

        // Disable
        vm.setReminderEnabled(false, "Weigh-in", "Check scale")
        advanceUntilIdle()
        assertEquals(false, prefs.reminderEnabled.value)
        assertEquals(true, reminder.cancelled)
    }

    @Test
    fun streakFlow() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        weights.add(app.kilo.domain.WeightEntry.create(75.0, fixedNow))
        weights.add(app.kilo.domain.WeightEntry.create(75.5, Instant.parse("2026-10-06T12:00:00Z")))
        val vm = createViewModel(weights = weights)
        advanceUntilIdle()

        val streak = vm.state.value.streak
        assertNotNull(streak)
        assertEquals(2, streak.currentStreak)
        assertEquals(2, streak.bestStreak)
        assertEquals(true, streak.isTodayLogged)
        assertEquals(false, streak.isStreakAtRisk)
        assertEquals(2, streak.totalWeighIns)
    }

    @Test
    fun shareSheetOpenAndDismiss() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isShareSheetOpen)

        vm.openShareSheet()
        assertEquals(true, vm.state.value.isShareSheetOpen)

        vm.dismissShareSheet()
        assertEquals(false, vm.state.value.isShareSheetOpen)
    }

    @Test
    fun cycleTrackingFlow() = runTest(testDispatcher) {
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs)
        advanceUntilIdle()

        // Initially disabled
        assertEquals(false, vm.state.value.cycle.isEnabled)
        assertEquals(false, vm.state.value.isCycleSheetOpen)
        assertEquals(false, vm.state.value.isCycleSetupOpen)

        // Setup dialog
        vm.openCycleSetup()
        assertEquals(true, vm.state.value.isCycleSetupOpen)
        vm.dismissCycleSetup()
        assertEquals(false, vm.state.value.isCycleSetupOpen)

        // Enable cycle tracking
        vm.enableCycleTracking(LocalDate.parse("2026-09-25"))
        advanceUntilIdle()

        assertEquals(true, prefs.cycleEnabled.value)
        assertEquals("2026-09-25", prefs.cycleLastPeriodDate.value)
        val cycle = vm.state.value.cycle
        assertEquals(true, cycle.isEnabled)
        assertNotNull(cycle.currentDayInfo)
        // Fixed now is 2026-10-07. Period was 2026-09-25 -> day 13
        assertEquals(13, cycle.currentDayInfo.cycleDay)

        // Open sheet
        vm.openCycleSheet()
        assertEquals(true, vm.state.value.isCycleSheetOpen)

        vm.dismissCycleSheet()
        assertEquals(false, vm.state.value.isCycleSheetOpen)

        // Disable
        vm.disableCycleTracking()
        advanceUntilIdle()
        assertEquals(false, prefs.cycleEnabled.value)
        assertEquals(false, vm.state.value.cycle.isEnabled)
    }

    @Test
    fun cycleWaterRetentionAutoSuggestsTag() = runTest(testDispatcher) {
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs)
        advanceUntilIdle()

        // Fixed now is 2026-10-07. Last period on 2026-09-14 -> day 24 (luteal mid/late, water retention active)
        vm.enableCycleTracking(LocalDate.parse("2026-09-14"))
        advanceUntilIdle()

        val cycle = vm.state.value.cycle
        assertEquals(true, cycle.isEnabled)
        assertEquals(true, cycle.isWaterRetentionActive)

        // When opening manual entry, WATER_RETENTION tag should be auto-suggested
        vm.openManual()
        val sheet = vm.state.value.sheet
        assertNotNull(sheet)
        assertEquals(WeightTag.WATER_RETENTION, sheet.selectedTag)
    }

    @Test
    fun cycleLengthAdjustmentAndClamping() = runTest(testDispatcher) {
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs)
        advanceUntilIdle()

        vm.enableCycleTracking(LocalDate.parse("2026-09-25"))
        advanceUntilIdle()

        assertEquals(28, vm.state.value.cycle.cycleLength)

        // Adjust to 32 days
        vm.setCycleLengthDays(32)
        advanceUntilIdle()

        assertEquals(32, vm.state.value.cycle.cycleLength)
        assertEquals(32, prefs.cycleLength.value)

        // Clamping lower bound (21)
        vm.setCycleLengthDays(15)
        advanceUntilIdle()
        assertEquals(21, vm.state.value.cycle.cycleLength)

        // Clamping upper bound (45)
        vm.setCycleLengthDays(50)
        advanceUntilIdle()
        assertEquals(45, vm.state.value.cycle.cycleLength)
    }

    @Test
    fun cycleCardDismissalAndVisibility() = runTest(testDispatcher) {
        val prefs = SettingsPrefsRepository(MapSettings())
        val vm = createViewModel(prefs = prefs)
        advanceUntilIdle()

        // 1. Initial state: Visible by default for unspecified users
        assertEquals(true, vm.state.value.isCycleCardVisible)
        assertEquals(app.kilo.data.UserGender.UNSPECIFIED, vm.state.value.userGender)

        // 2. Dismiss card (e.g. user taps "✕" or "Gizle")
        vm.dismissCycleCard()
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isCycleCardVisible)
        assertEquals(app.kilo.data.UserGender.MALE, vm.state.value.userGender)
        assertEquals(true, prefs.cycleDismissed.value)

        // 3. Re-enable visibility from settings
        vm.setCycleCardVisible(true)
        advanceUntilIdle()

        assertEquals(true, vm.state.value.isCycleCardVisible)
        assertEquals(app.kilo.data.UserGender.FEMALE, vm.state.value.userGender)
        assertEquals(false, prefs.cycleDismissed.value)

        // 4. Select male gender -> automatically hides card
        vm.setUserGender(app.kilo.data.UserGender.MALE)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isCycleCardVisible)
        assertEquals(true, prefs.cycleDismissed.value)

        // 5. Setup cycle tracking -> confirms female and reveals card
        vm.enableCycleTracking(LocalDate.parse("2026-09-25"))
        advanceUntilIdle()

        assertEquals(true, vm.state.value.isCycleCardVisible)
        assertEquals(app.kilo.data.UserGender.FEMALE, vm.state.value.userGender)
        assertEquals(false, prefs.cycleDismissed.value)
    }

    @Test
    fun hevyCsvImportAndClear() = runTest(testDispatcher) {
        val workoutRepo = FakeWorkoutRepository()
        val vm = createViewModel(workoutRepository = workoutRepo)
        advanceUntilIdle()

        assertEquals(0, vm.workouts.value.size)
        assertEquals(0, vm.state.value.workouts.size)
        assertNull(vm.state.value.workoutImportNotice)

        // 1. Invalid / empty CSV sets failure notice
        val emptyCount = vm.importHevyCsv("not a valid csv")
        advanceUntilIdle()
        assertEquals(0, emptyCount)
        assertEquals(BackupNotice(count = 0, isSuccess = false), vm.state.value.workoutImportNotice)

        val csvSample = """
            title,start_time,end_time,description,exercise_title,superset_id,exercise_notes,set_index,set_type,weight_kg,reps,distance_km,duration_seconds,rpe
            "Leg Day","2026-10-06 10:00:00","2026-10-06 11:15:00","","Barbell Squat",,"",1,"normal",100,10,,,8.0
            "Leg Day","2026-10-06 10:00:00","2026-10-06 11:15:00","","Barbell Squat",,"",2,"normal",100,10,,,8.5
        """.trimIndent()

        // 2. Valid CSV sets success notice and populates workouts
        val count = vm.importHevyCsv(csvSample)
        advanceUntilIdle()

        assertEquals(1, count)
        assertEquals(BackupNotice(count = 1, isSuccess = true), vm.state.value.workoutImportNotice)
        assertEquals(1, vm.workouts.value.size)
        assertEquals(1, vm.state.value.workouts.size)
        val session = vm.workouts.value.first()
        assertEquals("Leg Day", session.title)
        assertEquals(1, session.exercises.size)
        assertEquals("Barbell Squat", session.exercises.first().title)
        assertEquals(2000.0, session.totalVolumeKg)

        // 3. Clear workouts resets notice and workouts
        vm.clearWorkouts()
        advanceUntilIdle()

        assertEquals(0, vm.workouts.value.size)
        assertEquals(0, vm.state.value.workouts.size)
        assertNull(vm.state.value.workoutImportNotice)
    }

    @Test
    fun domsFluidRetentionEvaluationAndDismiss() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings())
        val workoutRepo = FakeWorkoutRepository()
        val vm = createViewModel(weights = weights, workoutRepository = workoutRepo)
        advanceUntilIdle()

        // 1. Log previous weigh-in (70.0 kg, 2 days ago)
        val twoDaysAgo = Instant.parse("2026-10-05T08:00:00Z")
        weights.add(app.kilo.domain.WeightEntry.create(70.0, twoDaysAgo))

        // 2. Log current weigh-in today (71.2 kg, +1.2 kg)
        weights.add(app.kilo.domain.WeightEntry.create(71.2, fixedNow))
        advanceUntilIdle()

        // Without a heavy workout, DOMS trauma is false
        assertEquals(false, vm.domsAssessment.value?.hasTrauma)

        // 3. Add heavy leg workout yesterday (2026-10-06, volume >= 8000 kg)
        val yesterdayDate = LocalDate.parse("2026-10-06")
        val heavyLegWorkout = HevyWorkoutSession(
            id = "leg_session_1",
            title = "Heavy Squats & Deadlifts",
            dateEpochDays = yesterdayDate.toEpochDays().toLong(),
            startTimeEpochMs = 1791280000000L,
            endTimeEpochMs = 1791284000000L,
            totalVolumeKg = 9500.0,
            exercises = listOf(
                HevyExercise(
                    title = "Barbell Squat",
                    category = MuscleCategory.LEGS_GLUTES,
                    totalVolumeKg = 9500.0,
                    sets = listOf(HevySet(1, "normal", 100.0, 10, 8.0)),
                )
            ),
        )

        workoutRepo.saveWorkouts(listOf(heavyLegWorkout))
        advanceUntilIdle()

        // DOMS assessment should confirm trauma!
        val doms = vm.domsAssessment.value
        assertNotNull(doms)
        assertEquals(true, doms.hasTrauma)
        assertEquals(true, vm.state.value.domsAssessment?.hasTrauma)
        assertEquals(1.2, doms.expectedFluidWeightKg)
        assertEquals(9500.0, doms.recentVolumeKg)
        assertEquals(false, vm.state.value.isDomsAlertDismissed)

        // Dismiss alert
        vm.dismissDomsAlert()
        assertEquals(true, vm.state.value.isDomsAlertDismissed)
    }

    @Test
    fun correlationSheetOpenAndDismiss() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(false, vm.state.value.isCorrelationSheetOpen)

        vm.openCorrelationSheet()
        assertEquals(true, vm.state.value.isCorrelationSheetOpen)

        vm.dismissCorrelationSheet()
        assertEquals(false, vm.state.value.isCorrelationSheetOpen)

        // Opening correlation directly from BackupSheet closes backup and opens correlation
        vm.openBackup()
        assertEquals(true, vm.state.value.isBackupSheetOpen)
        vm.openCorrelationFromBackup()
        assertEquals(false, vm.state.value.isBackupSheetOpen)
        assertEquals(true, vm.state.value.isCorrelationSheetOpen)
    }

    @Test
    fun syncWorkoutsFromHealthMergesWithoutOverwritingExistingCsvWorkouts() = runTest(testDispatcher) {
        val workoutRepo = FakeWorkoutRepository()
        val existingCsvSession = HevyWorkoutSession(
            id = "hevy_csv_1",
            title = "Leg Day",
            dateEpochDays = LocalDate.parse("2026-10-05").toEpochDays().toLong(),
            startTimeEpochMs = Instant.parse("2026-10-05T10:00:00Z").toEpochMilliseconds(),
            endTimeEpochMs = Instant.parse("2026-10-05T11:00:00Z").toEpochMilliseconds(),
            durationMinutes = 60,
            totalVolumeKg = 8500.0,
            exercises = listOf(
                HevyExercise(
                    title = "Barbell Squat",
                    category = MuscleCategory.LEGS_GLUTES,
                    totalVolumeKg = 8500.0,
                    sets = listOf(HevySet(1, "normal", 100.0, 10, 8.0)),
                )
            ),
            source = app.kilo.data.WorkoutSource.CSV,
        )
        workoutRepo.saveWorkouts(listOf(existingCsvSession))

        val healthSync = FakeHealthSync(isSupported = true, authResult = true).apply {
            readWorkoutSamples = listOf(
                // Overlapping with existing CSV session -> should NOT overwrite rich CSV
                app.kilo.platform.HealthWorkoutSample(
                    id = "health_1",
                    title = "Hevy · Apple Health",
                    startInstant = Instant.parse("2026-10-05T10:05:00Z"),
                    endInstant = Instant.parse("2026-10-05T11:05:00Z"),
                    durationMinutes = 60,
                    sourceId = "com.hevy.app",
                ),
                // Brand new session on next day -> should be added as HEALTH summary
                app.kilo.platform.HealthWorkoutSample(
                    id = "health_2",
                    title = "Hevy · Apple Health",
                    startInstant = Instant.parse("2026-10-06T15:00:00Z"),
                    endInstant = Instant.parse("2026-10-06T15:50:00Z"),
                    durationMinutes = 50,
                    sourceId = "com.hevy.app",
                ),
            )
        }

        val prefs = SettingsPrefsRepository(MapSettings()).apply {
            setHealthSyncEnabled(true)
        }

        val vm = createViewModel(
            prefs = prefs,
            workoutRepository = workoutRepo,
            healthSync = healthSync,
        )
        advanceUntilIdle()

        assertEquals(2, vm.workouts.value.size)
        val oct5 = vm.workouts.value.first { it.dateEpochDays == LocalDate.parse("2026-10-05").toEpochDays().toLong() }
        assertEquals(app.kilo.data.WorkoutSource.CSV, oct5.source)
        assertEquals(8500.0, oct5.totalVolumeKg)
        assertEquals(1, oct5.exercises.size)

        val oct6 = vm.workouts.value.first { it.dateEpochDays == LocalDate.parse("2026-10-06").toEpochDays().toLong() }
        assertEquals(app.kilo.data.WorkoutSource.HEALTH, oct6.source)
        assertEquals(50, oct6.durationMinutes)
    }

    @Test
    fun incomingFileBufferAutomaticallyMergesSharedHevyCsvAndUpgradesHealthWorkout() = runTest(testDispatcher) {
        val workoutRepo = FakeWorkoutRepository()
        val healthSummary = HevyWorkoutSession(
            id = "health_oct6",
            title = "Hevy · Apple Health",
            dateEpochDays = LocalDate.parse("2026-10-06").toEpochDays().toLong(),
            startTimeEpochMs = Instant.parse("2026-10-06T15:00:00Z").toEpochMilliseconds(),
            endTimeEpochMs = Instant.parse("2026-10-06T15:50:00Z").toEpochMilliseconds(),
            durationMinutes = 50,
            totalVolumeKg = 0.0,
            exercises = emptyList(),
            source = app.kilo.data.WorkoutSource.HEALTH,
        )
        workoutRepo.saveWorkouts(listOf(healthSummary))

        val vm = createViewModel(workoutRepository = workoutRepo)
        advanceUntilIdle()

        val sharedCsv = """
            "title","start_time","end_time","description","exercise_title","superset_id","exercise_notes","set_index","set_type","weight_kg","reps","distance_km","duration_seconds","rpe"
            "Upper Power","6 Oct 2026, 15:02","6 Oct 2026, 15:52","","Bench Press (Barbell)","","","0","normal","80","8","","","8.5"
        """.trimIndent()

        app.kilo.platform.IncomingFileBuffer.onFileReceived(sharedCsv)
        advanceUntilIdle()

        assertEquals(1, vm.workouts.value.size)
        val upgraded = vm.workouts.value.first()
        assertEquals(app.kilo.data.WorkoutSource.CSV, upgraded.source)
        assertEquals("Upper Power", upgraded.title)
        assertEquals(640.0, upgraded.totalVolumeKg)
        assertEquals(true, vm.state.value.isCorrelationSheetOpen)
        assertEquals(1, vm.state.value.workoutImportNotice?.upgradedCount)
    }

    @Test
    fun analyzeLiveFrameAndOnImageUseReferenceKgToRepairSevenSegmentLeadingDigit() = runTest(testDispatcher) {
        val weights = SettingsWeightRepository(MapSettings()).apply {
            add(app.kilo.domain.WeightEntry("prev_1", 72.1, Instant.parse("2026-10-05T08:00:00Z")))
        }
        val vm = createViewModel(
            weights = weights,
            readText = { listOf("22.4 kg") }, // Leading '7' misread as '2'
        )
        advanceUntilIdle()

        val liveDetected = vm.analyzeLiveFrame(byteArrayOf(1, 2, 3))
        assertEquals(72.4, liveDetected)

        vm.onImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()
        assertEquals("72.40", vm.state.value.sheet?.input)
        assertEquals(true, vm.state.value.sheet?.isPrefilledFromOcr)
        // Must not auto-save without user confirmation
        assertEquals(1, vm.state.value.history.size)
    }

    @Test
    fun onLiveWeightLockedOpensEntrySheetForUserConfirmationAndCancelsInFlightScan() = runTest(testDispatcher) {
        val ocrGate = kotlinx.coroutines.CompletableDeferred<List<String>>()
        val vm = createViewModel(
            readText = { ocrGate.await() },
        )
        advanceUntilIdle()

        // Start a slow in-flight OCR scan
        vm.onImage(byteArrayOf(9, 9))
        assertEquals(true, vm.state.value.sheet?.scanning)

        // Lock a live weight before the slow scan finishes
        vm.onLiveWeightLocked(68.4)
        val sheetAfterLock = vm.state.value.sheet
        assertEquals(false, sheetAfterLock?.scanning)
        assertEquals("68.40", sheetAfterLock?.input)
        assertEquals(true, sheetAfterLock?.isPrefilledFromOcr)
        assertEquals(0, vm.state.value.history.size)

        // Completing the old scan must NOT overwrite the locked live weight
        ocrGate.complete(listOf("55.0 kg"))
        advanceUntilIdle()
        assertEquals("68.40", vm.state.value.sheet?.input)
    }
}
