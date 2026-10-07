package app.kilo.data

import app.kilo.domain.WeightEntry
import app.kilo.domain.WeightUnit
import com.russhwolf.settings.MapSettings
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositoryTest {
    private fun e(id: String, kg: Double, iso: String) = WeightEntry(id, kg, Instant.parse(iso))

    @Test fun startsEmpty() {
        assertEquals(emptyList(), SettingsWeightRepository(MapSettings()).entries.value)
    }

    @Test fun addKeepsSortedDescending() {
        val repo = SettingsWeightRepository(MapSettings())
        repo.add(e("a", 70.0, "2024-01-01T08:00:00Z"))
        repo.add(e("c", 71.0, "2024-01-03T08:00:00Z"))
        repo.add(e("b", 70.5, "2024-01-02T08:00:00Z"))
        assertEquals(listOf("c", "b", "a"), repo.entries.value.map { it.id })
    }

    @Test fun persistsAcrossInstances() {
        val settings = MapSettings()
        val first = e("a", 70.25, "2024-01-01T08:00:00.123Z")
        SettingsWeightRepository(settings).add(first)
        val reloaded = SettingsWeightRepository(settings)
        assertEquals(listOf(first), reloaded.entries.value)
    }

    @Test fun deletePersists() {
        val settings = MapSettings()
        val repo = SettingsWeightRepository(settings)
        repo.add(e("a", 70.0, "2024-01-01T08:00:00Z"))
        repo.add(e("b", 71.0, "2024-01-02T08:00:00Z"))
        repo.delete("b")
        assertEquals(listOf("a"), repo.entries.value.map { it.id })
        assertEquals(listOf("a"), SettingsWeightRepository(settings).entries.value.map { it.id })
    }

    @Test fun deleteUnknownIdIsNoop() {
        val repo = SettingsWeightRepository(MapSettings())
        repo.add(e("a", 70.0, "2024-01-01T08:00:00Z"))
        repo.delete("zzz")
        assertEquals(1, repo.entries.value.size)
    }

    @Test fun addSameIdReplaces() {
        val repo = SettingsWeightRepository(MapSettings())
        repo.add(e("a", 70.0, "2024-01-01T08:00:00Z"))
        repo.add(e("a", 69.0, "2024-01-01T08:00:00Z"))
        assertEquals(listOf(69.0), repo.entries.value.map { it.kg })
    }

    @Test fun corruptDataFallsBackToEmpty() {
        val settings = MapSettings("weight_entries_v1" to "{not json")
        assertTrue(SettingsWeightRepository(settings).entries.value.isEmpty())
    }

    @Test fun prefsDefaultToKgAndPersist() {
        val settings = MapSettings()
        val prefs = SettingsPrefsRepository(settings)
        assertEquals(WeightUnit.KG, prefs.unit.value)
        prefs.setUnit(WeightUnit.LB)
        assertEquals(WeightUnit.LB, prefs.unit.value)
        assertEquals(WeightUnit.LB, SettingsPrefsRepository(settings).unit.value)
    }

    @Test fun prefsIgnoreUnknownStoredValue() {
        assertEquals(WeightUnit.KG, SettingsPrefsRepository(MapSettings("weight_unit" to "STONE")).unit.value)
    }
}
