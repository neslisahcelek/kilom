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
    val healthOnboardingSeen: StateFlow<Boolean>
    fun setHealthOnboardingSeen(seen: Boolean)
    val goalKg: StateFlow<Double?>
    fun setGoalKg(goalKg: Double?)
    val goalStartKg: StateFlow<Double?>
    fun setGoalStartKg(startKg: Double?)
    val reminderEnabled: StateFlow<Boolean>
    fun setReminderEnabled(enabled: Boolean)
    val reminderMinutes: StateFlow<Int>
    fun setReminderMinutes(minutes: Int)
    val cycleEnabled: StateFlow<Boolean>
    fun setCycleEnabled(enabled: Boolean)
    val cycleLastPeriodDate: StateFlow<String?>
    fun setCycleLastPeriodDate(isoDate: String?)
    val cycleLength: StateFlow<Int>
    fun setCycleLength(days: Int)
    val cyclePeriodDuration: StateFlow<Int>
    fun setCyclePeriodDuration(days: Int)
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

    private val _healthOnboardingSeen = MutableStateFlow(
        settings.getBoolean(KEY_HEALTH_ONBOARDING_SEEN, defaultValue = false)
    )
    override val healthOnboardingSeen: StateFlow<Boolean> = _healthOnboardingSeen.asStateFlow()

    override fun setHealthOnboardingSeen(seen: Boolean) {
        settings.putBoolean(KEY_HEALTH_ONBOARDING_SEEN, seen)
        _healthOnboardingSeen.value = seen
    }

    private val _goalKg = MutableStateFlow(
        settings.getStringOrNull(KEY_GOAL_KG)?.toDoubleOrNull()
    )
    override val goalKg: StateFlow<Double?> = _goalKg.asStateFlow()

    override fun setGoalKg(goalKg: Double?) {
        if (goalKg != null) {
            settings.putString(KEY_GOAL_KG, goalKg.toString())
        } else {
            settings.remove(KEY_GOAL_KG)
        }
        _goalKg.value = goalKg
    }

    private val _goalStartKg = MutableStateFlow(
        settings.getStringOrNull(KEY_GOAL_START_KG)?.toDoubleOrNull()
    )
    override val goalStartKg: StateFlow<Double?> = _goalStartKg.asStateFlow()

    override fun setGoalStartKg(startKg: Double?) {
        if (startKg != null) {
            settings.putString(KEY_GOAL_START_KG, startKg.toString())
        } else {
            settings.remove(KEY_GOAL_START_KG)
        }
        _goalStartKg.value = startKg
    }

    private val _reminderEnabled = MutableStateFlow(
        settings.getBoolean(KEY_REMINDER_ENABLED, defaultValue = false)
    )
    override val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    override fun setReminderEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_REMINDER_ENABLED, enabled)
        _reminderEnabled.value = enabled
    }

    private val _reminderMinutes = MutableStateFlow(
        settings.getInt(KEY_REMINDER_MINUTES, defaultValue = DEFAULT_REMINDER_MINUTES)
    )
    override val reminderMinutes: StateFlow<Int> = _reminderMinutes.asStateFlow()

    override fun setReminderMinutes(minutes: Int) {
        settings.putInt(KEY_REMINDER_MINUTES, minutes)
        _reminderMinutes.value = minutes
    }

    private val _cycleEnabled = MutableStateFlow(
        settings.getBoolean(KEY_CYCLE_ENABLED, defaultValue = false)
    )
    override val cycleEnabled: StateFlow<Boolean> = _cycleEnabled.asStateFlow()

    override fun setCycleEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_CYCLE_ENABLED, enabled)
        _cycleEnabled.value = enabled
    }

    private val _cycleLastPeriodDate = MutableStateFlow(
        settings.getStringOrNull(KEY_CYCLE_LAST_PERIOD_DATE)
    )
    override val cycleLastPeriodDate: StateFlow<String?> = _cycleLastPeriodDate.asStateFlow()

    override fun setCycleLastPeriodDate(isoDate: String?) {
        if (isoDate != null) {
            settings.putString(KEY_CYCLE_LAST_PERIOD_DATE, isoDate)
        } else {
            settings.remove(KEY_CYCLE_LAST_PERIOD_DATE)
        }
        _cycleLastPeriodDate.value = isoDate
    }

    private val _cycleLength = MutableStateFlow(
        settings.getInt(KEY_CYCLE_LENGTH, defaultValue = DEFAULT_CYCLE_LENGTH)
    )
    override val cycleLength: StateFlow<Int> = _cycleLength.asStateFlow()

    override fun setCycleLength(days: Int) {
        settings.putInt(KEY_CYCLE_LENGTH, days)
        _cycleLength.value = days
    }

    private val _cyclePeriodDuration = MutableStateFlow(
        settings.getInt(KEY_CYCLE_PERIOD_DURATION, defaultValue = DEFAULT_PERIOD_DURATION)
    )
    override val cyclePeriodDuration: StateFlow<Int> = _cyclePeriodDuration.asStateFlow()

    override fun setCyclePeriodDuration(days: Int) {
        settings.putInt(KEY_CYCLE_PERIOD_DURATION, days)
        _cyclePeriodDuration.value = days
    }

    private companion object {
        const val KEY_UNIT = "weight_unit"
        const val KEY_HEALTH_SYNC = "health_sync_enabled"
        const val KEY_HEALTH_ONBOARDING_SEEN = "health_onboarding_seen"
        const val KEY_GOAL_KG = "goal_kg"
        const val KEY_GOAL_START_KG = "goal_start_kg"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_MINUTES = "reminder_minutes"
        const val DEFAULT_REMINDER_MINUTES = 480 // 08:00 AM
        const val KEY_CYCLE_ENABLED = "cycle_enabled"
        const val KEY_CYCLE_LAST_PERIOD_DATE = "cycle_last_period_date"
        const val KEY_CYCLE_LENGTH = "cycle_length"
        const val KEY_CYCLE_PERIOD_DURATION = "cycle_period_duration"
        const val DEFAULT_CYCLE_LENGTH = 28
        const val DEFAULT_PERIOD_DURATION = 5
    }
}
