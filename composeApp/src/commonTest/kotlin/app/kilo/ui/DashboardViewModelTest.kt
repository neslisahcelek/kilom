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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
        override fun success() { successCount++ }
        override fun scanComplete() { scanCount++ }
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

        override suspend fun writeWeight(kg: Double, timestamp: Instant): Boolean {
            writtenSamples.add(kg to timestamp)
            return true
        }

        override suspend fun writeWeights(entries: List<app.kilo.domain.WeightEntry>): Int {
            writtenBatches.add(entries)
            return entries.size
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
        healthSync: app.kilo.platform.HealthSync = app.kilo.platform.NoOpHealthSync,
        readText: suspend (ByteArray) -> List<String> = { emptyList() },
        extractDate: (ByteArray) -> Instant? = { null },
    ): DashboardViewModel = DashboardViewModel(
        weights = weights,
        prefs = prefs,
        readText = readText,
        feedback = FakeFeedback(),
        healthSync = healthSync,
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
}
