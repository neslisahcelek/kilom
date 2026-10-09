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
    /** Replaces the entire entries set with [entries], sorting and persisting them. */
    fun replaceEntries(entries: List<WeightEntry>)
}

@Serializable
private data class StoredEntry(val id: String, val kg: Double, val atMillis: Long, val tag: String? = null)

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

    override fun replaceEntries(entries: List<WeightEntry>) {
        update { entries }
    }

    private fun update(transform: (List<WeightEntry>) -> List<WeightEntry>) {
        val next = sort(transform(_entries.value))
        settings.putString(KEY, json.encodeToString(serializer, next.map { StoredEntry(it.id, it.kg, it.at.toEpochMilliseconds(), it.tag) }))
        _entries.value = next
    }

    private fun load(): List<WeightEntry> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            sort(json.decodeFromString(serializer, raw).map {
                WeightEntry(it.id, it.kg, Instant.fromEpochMilliseconds(it.atMillis), it.tag)
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
    val healthSyncEnabled: StateFlow<Boolean>
    fun setHealthSyncEnabled(enabled: Boolean)
}

class SettingsPrefsRepository(private val settings: Settings) : PrefsRepository {
    private val _unit = MutableStateFlow(
        settings.getStringOrNull(KEY_UNIT)?.let { s -> WeightUnit.entries.firstOrNull { it.name == s } } ?: WeightUnit.KG
    )
    override val unit: StateFlow<WeightUnit> = _unit.asStateFlow()

    override fun setUnit(unit: WeightUnit) {
        settings.putString(KEY_UNIT, unit.name)
        _unit.value = unit
    }

    private val _healthSyncEnabled = MutableStateFlow(
        settings.getBoolean(KEY_HEALTH_SYNC, defaultValue = false)
    )
    override val healthSyncEnabled: StateFlow<Boolean> = _healthSyncEnabled.asStateFlow()

    override fun setHealthSyncEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_HEALTH_SYNC, enabled)
        _healthSyncEnabled.value = enabled
    }

    private companion object {
        const val KEY_UNIT = "weight_unit"
        const val KEY_HEALTH_SYNC = "health_sync_enabled"
    }
}
