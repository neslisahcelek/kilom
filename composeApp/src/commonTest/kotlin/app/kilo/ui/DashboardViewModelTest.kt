package app.kilo.ui

import app.kilo.data.SettingsPrefsRepository
import app.kilo.data.SettingsWeightRepository
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
    ): DashboardViewModel = DashboardViewModel(
        weights = weights,
        prefs = prefs,
        readText = { emptyList() },
        feedback = FakeFeedback(),
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
}
