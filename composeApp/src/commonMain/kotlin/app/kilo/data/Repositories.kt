package app.kilo.data

import app.kilo.domain.WeightEntry
import app.kilo.domain.WeightUnit
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface WeightRepository {
    /** Sorted by `at` descending. */
    val entries: StateFlow<List<WeightEntry>>
    /** Adds, or replaces an entry with the same id. */
    fun add(entry: WeightEntry)
    fun delete(id: String)
}

@Serializable
private data class StoredEntry(val id: String, val kg: Double, val atMillis: Long)

class SettingsWeightRepository(
    private val settings: Settings,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : WeightRepository {

    private val serializer = ListSerializer(StoredEntry.serializer())
    private val _entries = MutableStateFlow(load())
    override val entries: StateFlow<List<WeightEntry>> = _entries.asStateFlow()

    override fun add(entry: WeightEntry) {
        update { list -> list.filterNot { it.id == entry.id } + entry }
    }

    override fun delete(id: String) {
        update { list -> list.filterNot { it.id == id } }
    }

    private fun update(transform: (List<WeightEntry>) -> List<WeightEntry>) {
        val next = sort(transform(_entries.value))
        settings.putString(KEY, json.encodeToString(serializer, next.map { StoredEntry(it.id, it.kg, it.at.toEpochMilliseconds()) }))
        _entries.value = next
    }

    private fun load(): List<WeightEntry> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            sort(json.decodeFromString(serializer, raw).map {
                WeightEntry(it.id, it.kg, Instant.fromEpochMilliseconds(it.atMillis))
            })
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun sort(list: List<WeightEntry>) =
        list.sortedWith(compareByDescending<WeightEntry> { it.at }.thenByDescending { it.id })

    private companion object {
        const val KEY = "weight_entries_v1"
    }
}

interface PrefsRepository {
    val unit: StateFlow<WeightUnit>
    fun setUnit(unit: WeightUnit)
}

class SettingsPrefsRepository(private val settings: Settings) : PrefsRepository {
    private val _unit = MutableStateFlow(
        settings.getStringOrNull(KEY)?.let { s -> WeightUnit.entries.firstOrNull { it.name == s } } ?: WeightUnit.KG
    )
    override val unit: StateFlow<WeightUnit> = _unit.asStateFlow()

    override fun setUnit(unit: WeightUnit) {
        settings.putString(KEY, unit.name)
        _unit.value = unit
    }

    private companion object {
        const val KEY = "weight_unit"
    }
}
