package app.kilo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kilo.data.PrefsRepository
import app.kilo.data.WeightRepository
import app.kilo.domain.HistoryItem
import app.kilo.domain.WeightEntry
import app.kilo.domain.WeightInputResult
import app.kilo.domain.WeightTag
import app.kilo.domain.WeightUnit
import app.kilo.domain.buildHistory
import app.kilo.domain.formatWeight
import app.kilo.domain.lbToKg
import app.kilo.domain.parseWeightInput
import app.kilo.platform.HealthSync
import app.kilo.platform.NoOpHealthSync
import app.kilo.platform.extractImageCaptureDate
import app.kilo.ocr.WeightParser
import app.kilo.ocr.logOcr
import app.kilo.ocr.logOcrLines
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

import app.kilo.domain.exportToCsv
import app.kilo.domain.exportToJson
import app.kilo.domain.importFromCsv
import app.kilo.domain.importFromJson
import app.kilo.domain.mergeEntries

import app.kilo.domain.GoalProgress
import app.kilo.domain.PeriodSummary
import app.kilo.domain.StreakInfo
import app.kilo.domain.SummaryPeriod
import app.kilo.domain.buildPeriodSummary
import app.kilo.domain.computeGoalProgress
import app.kilo.domain.computeStreak
import app.kilo.domain.mergeHealthSamples
import app.kilo.platform.HealthProvider
import app.kilo.platform.NoOpReminderScheduler
import app.kilo.platform.ReminderScheduler
import kotlinx.coroutines.flow.collect

/** Localizable error keys; the UI maps them to string resources (no raw exceptions reach the UI). */
enum class UiError { EMPTY, NOT_A_NUMBER, OUT_OF_RANGE, OCR_NO_RESULT, OCR_FAILED }

/** Feedback hooks (implemented by platform haptics in the app, faked in tests). */
interface DashboardFeedback {
    fun success()
    fun scanComplete()
}

data class HeroState(
    val latest: HistoryItem,
    /** Calendar days between the latest entry and today. */
    val daysSince: Int,
)

data class SheetState(
    val input: String = "",
    val selectedDateMillis: Long? = null,
    val selectedTag: WeightTag? = null,
    val editingEntryId: String? = null,
    val error: UiError? = null,
    val notice: UiError? = null,
    val scanning: Boolean = false,
    val isPrefilledFromOcr: Boolean = false,
    val capturedInstant: Instant? = null,
    val isDateAutoDetected: Boolean = false,
) {
    val isEditing: Boolean get() = editingEntryId != null
}

data class BackupNotice(val count: Int, val isSuccess: Boolean)

data class HealthSyncNotice(val count: Int, val isSuccess: Boolean)

private data class GoalPrefsData(
    val entries: List<WeightEntry>,
    val unit: WeightUnit,
    val healthEnabled: Boolean,
    val goalKg: Double?,
    val goalStartKg: Double?,
)

data class UiState(
    val unit: WeightUnit = WeightUnit.KG,
    val history: List<HistoryItem> = emptyList(),
    val hero: HeroState? = null,
    val sheet: SheetState? = null,
    val isBackupSheetOpen: Boolean = false,
    val backupNotice: BackupNotice? = null,
    val isHealthSyncSupported: Boolean = false,
    val healthProvider: HealthProvider = HealthProvider.NONE,
    val healthSyncEnabled: Boolean = false,
    val isSyncingHealth: Boolean = false,
    val isImportingHealth: Boolean = false,
    val healthSyncNotice: HealthSyncNotice? = null,
    val isHealthOnboardingOpen: Boolean = false,
    val goal: GoalProgress? = null,
    val isGoalSheetOpen: Boolean = false,
    val goalInput: String = "",
    val goalError: UiError? = null,
    val summaryPeriod: SummaryPeriod = SummaryPeriod.WEEK,
    val summary: PeriodSummary? = null,
    val isReminderSupported: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 480,
    val streak: StreakInfo? = null,
    val isShareSheetOpen: Boolean = false,
)

class DashboardViewModel(
    private val weights: WeightRepository,
    private val prefs: PrefsRepository,
    private val readText: suspend (ByteArray) -> List<String>,
    private val feedback: DashboardFeedback,
    private val healthSync: HealthSync = NoOpHealthSync,
    private val reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    private val extractDate: (ByteArray) -> Instant? = { extractImageCaptureDate(it) },
    private val now: () -> Instant = {
        Clock.System.now().let { Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond) }
    },
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : ViewModel() {

    private val _summaryPeriod = MutableStateFlow(SummaryPeriod.WEEK)

    private val _state = MutableStateFlow(
        UiState(
            isHealthSyncSupported = healthSync.isSupported,
            healthProvider = healthSync.provider,
            isReminderSupported = reminderScheduler.isSupported,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var scanJob: Job? = null
    private var scanCounter: Long = 0L

    init {
        viewModelScope.launch {
            val mainFlow = combine(
                weights.entries,
                prefs.unit,
                prefs.healthSyncEnabled,
                prefs.goalKg,
                prefs.goalStartKg,
            ) { entries, unit, healthEnabled, goalKg, goalStartKg ->
                GoalPrefsData(entries, unit, healthEnabled, goalKg, goalStartKg)
            }

            val auxFlow = combine(
                prefs.reminderEnabled,
                prefs.reminderMinutes,
                _summaryPeriod,
            ) { reminderEnabled, reminderMinutes, summaryPeriod ->
                Triple(reminderEnabled, reminderMinutes, summaryPeriod)
            }

            combine(mainFlow, auxFlow) { main, (reminderEnabled, reminderMinutes, summaryPeriod) ->
                val tz = timeZone()
                val history = buildHistory(main.entries, tz)
                val hero = history.firstOrNull()?.let {
                    val today = now().toLocalDateTime(tz).date
                    val last = it.entry.at.toLocalDateTime(tz).date
                    HeroState(it, last.daysUntil(today).coerceAtLeast(0))
                }
                val goal = computeGoalProgress(main.goalKg, main.goalStartKg, main.entries, now(), tz)
                val summary = buildPeriodSummary(main.entries, summaryPeriod, now(), tz)
                val streak = computeStreak(main.entries, now(), tz, goal)

                _state.update {
                    it.copy(
                        unit = main.unit,
                        history = history,
                        hero = hero,
                        isHealthSyncSupported = healthSync.isSupported,
                        healthProvider = healthSync.provider,
                        healthSyncEnabled = main.healthEnabled,
                        goal = goal,
                        summaryPeriod = summaryPeriod,
                        summary = summary,
                        streak = streak,
                        isReminderSupported = reminderScheduler.isSupported,
                        reminderEnabled = reminderEnabled,
                        reminderMinutes = reminderMinutes,
                    )
                }
            }.collect()
        }
    }

    fun setUnit(unit: WeightUnit) {
        val old = _state.value.unit
        if (old == unit) return
        // Re-express a typed value in the new unit so the number keeps its meaning.
        val sheet = _state.value.sheet
        val converted = sheet?.let { s ->
            (parseWeightInput(s.input, old) as? WeightInputResult.Valid)?.let { formatWeight(it.kg, unit) }
        }
        prefs.setUnit(unit)
        if (converted != null) _state.update { it.copy(sheet = it.sheet?.copy(input = converted, error = null)) }
    }

    fun openManual() {
        scanJob?.cancel()
        _state.update { it.copy(sheet = SheetState(isPrefilledFromOcr = false)) }
    }

    fun openEdit(entry: WeightEntry) {
        scanJob?.cancel()
        val tz = timeZone()
        val localDate = entry.at.toLocalDateTime(tz).date
        val dateMillis = localDate.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        val initialTag = WeightTag.fromId(entry.tag)
        _state.update {
            it.copy(
                sheet = SheetState(
                    input = formatWeight(entry.kg, it.unit),
                    selectedDateMillis = dateMillis,
                    selectedTag = initialTag,
                    editingEntryId = entry.id,
                    isPrefilledFromOcr = false,
                )
            )
        }
    }

    fun onTagSelected(tag: WeightTag?) {
        _state.update { it.copy(sheet = it.sheet?.copy(selectedTag = tag)) }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(sheet = it.sheet?.copy(input = text, error = null, isPrefilledFromOcr = false)) }
    }

    fun onDateSelected(millis: Long?) {
        _state.update { it.copy(sheet = it.sheet?.copy(selectedDateMillis = millis, isDateAutoDetected = false)) }
    }

    fun dismissSheet() {
        scanJob?.cancel()
        _state.update { it.copy(sheet = null) }
    }

    fun onImage(bytes: ByteArray) {
        scanJob?.cancel()
        val currentScanId = ++scanCounter
        logOcr("scan=$currentScanId started bytes=${bytes.size}")
        val captureInstant = extractDate(bytes)
        val initialSheetState = if (captureInstant != null) {
            val tz = timeZone()
            val localDate = captureInstant.toLocalDateTime(tz).date
            val dateMillis = localDate.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
            SheetState(
                scanning = true,
                isPrefilledFromOcr = false,
                selectedDateMillis = dateMillis,
                capturedInstant = captureInstant,
                isDateAutoDetected = true,
            )
        } else {
            SheetState(scanning = true, isPrefilledFromOcr = false)
        }
        _state.update { it.copy(sheet = initialSheetState) }
        val job = viewModelScope.launch {
            val lines = try {
                readText(bytes)
            } catch (e: CancellationException) {
                logOcr("scan=$currentScanId cancelled")
                throw e
            } catch (e: Exception) {
                logOcr("scan=$currentScanId recognition_failed exception=${e::class.simpleName}")
                finishScan(currentScanId) { it.copy(scanning = false, notice = UiError.OCR_FAILED) }
                return@launch
            }
            ensureActive()
            logOcrLines(currentScanId, lines)
            val parsed = WeightParser.pick(lines, _state.value.unit)
            if (parsed == null) {
                logOcr("scan=$currentScanId parser_no_result empty_recognition=${lines.isEmpty()}")
                finishScan(currentScanId) { it.copy(scanning = false, notice = UiError.OCR_NO_RESULT) }
                return@launch
            }
            val kg = if (parsed.unit == WeightUnit.KG) parsed.value else parsed.value.lbToKg()
            val unit = _state.value.unit
            val applied = finishScan(currentScanId) {
                it.copy(
                    scanning = false,
                    notice = null,
                    error = null,
                    input = formatWeight(kg, unit),
                    isPrefilledFromOcr = true,
                )
            }
            if (applied) {
                logOcr("scan=$currentScanId parser_success prefill_applied")
                feedback.scanComplete()
            } else {
                logOcr("scan=$currentScanId parser_success prefill_discarded")
            }
        }
        scanJob = job
    }

    /** Applies [block] to the sheet only if current scan matches [scanId] and sheet is still waiting. */
    private fun finishScan(scanId: Long, block: (SheetState) -> SheetState): Boolean {
        var applied = false
        _state.update { s ->
            val sheet = s.sheet
            if (scanId != scanCounter || sheet == null || !sheet.scanning) {
                s
            } else {
                applied = true
                s.copy(sheet = block(sheet))
            }
        }
        return applied
    }

    fun save() {
        val s = _state.value
        val sheet = s.sheet ?: return
        if (sheet.scanning) return
        when (val r = parseWeightInput(sheet.input, s.unit)) {
            is WeightInputResult.Invalid -> {
                val err = when (r.reason) {
                    WeightInputResult.Reason.EMPTY -> UiError.EMPTY
                    WeightInputResult.Reason.NOT_A_NUMBER -> UiError.NOT_A_NUMBER
                    WeightInputResult.Reason.OUT_OF_RANGE -> UiError.OUT_OF_RANGE
                }
                _state.update { it.copy(sheet = it.sheet?.copy(error = err)) }
            }
            is WeightInputResult.Valid -> {
                val existing = sheet.editingEntryId?.let { id ->
                    s.history.firstOrNull { it.entry.id == id }?.entry
                }
                val entryInstant = if (sheet.selectedDateMillis != null) {
                    val tz = timeZone()
                    val selectedLocalDate = Instant.fromEpochMilliseconds(sheet.selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
                    if (sheet.isDateAutoDetected && sheet.capturedInstant != null &&
                        selectedLocalDate == sheet.capturedInstant.toLocalDateTime(tz).date
                    ) {
                        sheet.capturedInstant
                    } else if (existing != null && selectedLocalDate == existing.at.toLocalDateTime(tz).date) {
                        existing.at
                    } else {
                        val todayLocalDate = now().toLocalDateTime(tz).date
                        val entryTime = if (selectedLocalDate == todayLocalDate) {
                            now().toLocalDateTime(tz).time
                        } else {
                            LocalTime(8, 0, 0)
                        }
                        selectedLocalDate.atTime(entryTime).toInstant(tz)
                    }
                } else if (sheet.isDateAutoDetected && sheet.capturedInstant != null) {
                    sheet.capturedInstant
                } else {
                    existing?.at ?: now()
                }

                val savedEntry = if (sheet.editingEntryId != null) {
                    val updatedEntry = WeightEntry(
                        id = sheet.editingEntryId,
                        kg = r.kg,
                        at = entryInstant,
                        tag = sheet.selectedTag?.id,
                    )
                    weights.add(updatedEntry)
                    updatedEntry
                } else {
                    val newEntry = WeightEntry.create(r.kg, entryInstant, tag = sheet.selectedTag?.id)
                    weights.add(newEntry)
                    newEntry
                }
                _state.update { it.copy(sheet = null) }
                feedback.success()
                if (_state.value.healthSyncEnabled) {
                    viewModelScope.launch {
                        healthSync.writeWeight(savedEntry.kg, savedEntry.at, savedEntry.id)
                    }
                }
            }
        }
    }

    fun delete(id: String) {
        weights.delete(id)
    }

    fun openBackup() {
        _state.update { it.copy(isBackupSheetOpen = true, backupNotice = null, healthSyncNotice = null) }
    }

    fun dismissBackup() {
        _state.update { it.copy(isBackupSheetOpen = false) }
    }

    fun clearBackupNotice() {
        _state.update { it.copy(backupNotice = null) }
    }

    fun setHealthSyncEnabled(enabled: Boolean) {
        if (!enabled) {
            prefs.setHealthSyncEnabled(false)
            return
        }
        viewModelScope.launch {
            val granted = healthSync.requestAuthorization()
            if (granted) {
                prefs.setHealthSyncEnabled(true)
                feedback.success()
            } else {
                prefs.setHealthSyncEnabled(false)
                _state.update { it.copy(healthSyncNotice = HealthSyncNotice(count = 0, isSuccess = false)) }
            }
        }
    }

    fun syncAllToHealth() {
        if (_state.value.isSyncingHealth) return
        viewModelScope.launch {
            _state.update { it.copy(isSyncingHealth = true, healthSyncNotice = null) }
            val auth = healthSync.requestAuthorization()
            if (!auth) {
                _state.update { it.copy(isSyncingHealth = false, healthSyncNotice = HealthSyncNotice(count = 0, isSuccess = false)) }
                return@launch
            }
            val entries = weights.entries.value
            val count = healthSync.writeWeights(entries)
            _state.update {
                it.copy(
                    isSyncingHealth = false,
                    healthSyncNotice = HealthSyncNotice(count = count, isSuccess = count > 0 || entries.isEmpty()),
                )
            }
            if (count > 0) feedback.success()
        }
    }

    fun clearHealthSyncNotice() {
        _state.update { it.copy(healthSyncNotice = null) }
    }

    fun openHealthOnboarding() {
        _state.update { it.copy(isHealthOnboardingOpen = true) }
    }

    fun dismissHealthOnboarding() {
        prefs.setHealthOnboardingSeen(true)
        _state.update { it.copy(isHealthOnboardingOpen = false) }
    }

    fun connectHealth(importAlso: Boolean) {
        viewModelScope.launch {
            val granted = healthSync.requestAuthorization()
            if (granted) {
                prefs.setHealthSyncEnabled(true)
                prefs.setHealthOnboardingSeen(true)
                _state.update { it.copy(isHealthOnboardingOpen = false) }
                feedback.success()
                if (importAlso) {
                    importFromHealth()
                }
            } else {
                prefs.setHealthSyncEnabled(false)
                prefs.setHealthOnboardingSeen(true)
                _state.update {
                    it.copy(
                        isHealthOnboardingOpen = false,
                        healthSyncNotice = HealthSyncNotice(count = 0, isSuccess = false),
                    )
                }
            }
        }
    }

    fun importFromHealth() {
        if (_state.value.isImportingHealth) return
        viewModelScope.launch {
            _state.update { it.copy(isImportingHealth = true, healthSyncNotice = null) }
            val auth = healthSync.requestAuthorization()
            if (!auth) {
                _state.update { it.copy(isImportingHealth = false, healthSyncNotice = HealthSyncNotice(count = 0, isSuccess = false)) }
                return@launch
            }
            val samples = healthSync.readWeights()
            val current = weights.entries.value
            val merged = mergeHealthSamples(current, samples)
            val importedCount = merged.size - current.size
            if (importedCount > 0) {
                weights.replaceEntries(merged)
                feedback.success()
            }
            _state.update {
                it.copy(
                    isImportingHealth = false,
                    healthSyncNotice = HealthSyncNotice(count = importedCount, isSuccess = importedCount > 0 || samples.isEmpty()),
                )
            }
        }
    }

    fun openGoalSheet() {
        val currentGoal = _state.value.goal?.goalKg
        val formatted = currentGoal?.let { formatWeight(it, _state.value.unit) } ?: ""
        _state.update {
            it.copy(
                isGoalSheetOpen = true,
                goalInput = formatted,
                goalError = null,
            )
        }
    }

    fun dismissGoalSheet() {
        _state.update { it.copy(isGoalSheetOpen = false, goalError = null) }
    }

    fun onGoalInputChange(text: String) {
        _state.update { it.copy(goalInput = text, goalError = null) }
    }

    fun saveGoal() {
        val s = _state.value
        when (val r = parseWeightInput(s.goalInput, s.unit)) {
            is WeightInputResult.Invalid -> {
                val err = when (r.reason) {
                    WeightInputResult.Reason.EMPTY -> UiError.EMPTY
                    WeightInputResult.Reason.NOT_A_NUMBER -> UiError.NOT_A_NUMBER
                    WeightInputResult.Reason.OUT_OF_RANGE -> UiError.OUT_OF_RANGE
                }
                _state.update { it.copy(goalError = err) }
            }
            is WeightInputResult.Valid -> {
                prefs.setGoalKg(r.kg)
                if (prefs.goalStartKg.value == null) {
                    val currentWeight = s.history.firstOrNull()?.entry?.kg ?: r.kg
                    prefs.setGoalStartKg(currentWeight)
                }
                _state.update { it.copy(isGoalSheetOpen = false, goalError = null) }
                feedback.success()
            }
        }
    }

    fun clearGoal() {
        prefs.setGoalKg(null)
        prefs.setGoalStartKg(null)
        _state.update { it.copy(isGoalSheetOpen = false, goalError = null) }
        feedback.success()
    }

    fun setSummaryPeriod(period: SummaryPeriod) {
        _summaryPeriod.value = period
    }

    fun openShareSheet() {
        _state.update { it.copy(isShareSheetOpen = true) }
    }

    fun dismissShareSheet() {
        _state.update { it.copy(isShareSheetOpen = false) }
    }

    fun setReminderEnabled(enabled: Boolean, title: String, body: String) {
        if (!enabled) {
            prefs.setReminderEnabled(false)
            reminderScheduler.cancel()
            return
        }
        viewModelScope.launch {
            val granted = reminderScheduler.requestPermission()
            if (granted) {
                prefs.setReminderEnabled(true)
                val mins = prefs.reminderMinutes.value
                reminderScheduler.schedule(mins / 60, mins % 60, title, body)
                feedback.success()
            } else {
                prefs.setReminderEnabled(false)
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int, title: String, body: String) {
        val mins = hour * 60 + minute
        prefs.setReminderMinutes(mins)
        if (prefs.reminderEnabled.value) {
            reminderScheduler.schedule(hour, minute, title, body)
        }
    }

    fun rescheduleReminderIfEnabled(title: String, body: String) {
        if (prefs.reminderEnabled.value) {
            val mins = prefs.reminderMinutes.value
            reminderScheduler.schedule(mins / 60, mins % 60, title, body)
        }
    }

    fun exportCsv(): String = exportToCsv(_state.value.history.map { it.entry }, timeZone())

    fun exportJson(): String = exportToJson(_state.value.history.map { it.entry })

    fun importData(content: String): Int {
        val tz = timeZone()
        val trimmed = content.trimStart()
        val isJson = trimmed.startsWith("{") || trimmed.startsWith("[")
        val imported = if (isJson) importFromJson(content) else importFromCsv(content, tz)
        if (imported.isEmpty()) {
            _state.update { it.copy(backupNotice = BackupNotice(count = 0, isSuccess = false)) }
            return 0
        }
        val current = _state.value.history.map { it.entry }
        val merged = mergeEntries(current, imported)
        weights.replaceEntries(merged)
        feedback.success()
        _state.update { it.copy(backupNotice = BackupNotice(count = imported.size, isSuccess = true)) }
        return imported.size
    }
}
