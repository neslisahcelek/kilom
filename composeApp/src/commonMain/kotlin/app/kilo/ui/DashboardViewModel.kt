package app.kilo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kilo.data.PrefsRepository
import app.kilo.data.WeightRepository
import app.kilo.domain.HistoryItem
import app.kilo.domain.WeightEntry
import app.kilo.domain.WeightInputResult
import app.kilo.domain.WeightUnit
import app.kilo.domain.buildHistory
import app.kilo.domain.formatWeight
import app.kilo.domain.lbToKg
import app.kilo.domain.parseWeightInput
import app.kilo.ocr.WeightParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val error: UiError? = null,
    val notice: UiError? = null,
    val scanning: Boolean = false,
    val isPrefilledFromOcr: Boolean = false,
)

data class UiState(
    val unit: WeightUnit = WeightUnit.KG,
    val history: List<HistoryItem> = emptyList(),
    val hero: HeroState? = null,
    val sheet: SheetState? = null,
)

class DashboardViewModel(
    private val weights: WeightRepository,
    private val prefs: PrefsRepository,
    private val readText: suspend (ByteArray) -> List<String>,
    private val feedback: DashboardFeedback,
    private val now: () -> Instant = { Clock.System.now() },
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var scanJob: Job? = null
    private var scanCounter: Long = 0L

    init {
        viewModelScope.launch {
            combine(weights.entries, prefs.unit) { entries, unit -> entries to unit }
                .collect { (entries, unit) ->
                    val tz = timeZone()
                    val history = buildHistory(entries, tz)
                    val hero = history.firstOrNull()?.let {
                        val today = now().toLocalDateTime(tz).date.toEpochDays()
                        val last = it.entry.at.toLocalDateTime(tz).date.toEpochDays()
                        HeroState(it, (today - last).coerceAtLeast(0))
                    }
                    _state.update { it.copy(unit = unit, history = history, hero = hero) }
                }
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

    fun onInputChange(text: String) {
        _state.update { it.copy(sheet = it.sheet?.copy(input = text, error = null, isPrefilledFromOcr = false)) }
    }

    fun dismissSheet() {
        scanJob?.cancel()
        _state.update { it.copy(sheet = null) }
    }

    fun onImage(bytes: ByteArray) {
        scanJob?.cancel()
        val currentScanId = ++scanCounter
        _state.update { it.copy(sheet = SheetState(scanning = true, isPrefilledFromOcr = false)) }
        val job = viewModelScope.launch {
            val lines = try {
                readText(bytes)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                finishScan(currentScanId) { it.copy(scanning = false, notice = UiError.OCR_FAILED) }
                return@launch
            }
            ensureActive()
            val parsed = WeightParser.pick(lines, _state.value.unit)
            if (parsed == null) {
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
                feedback.scanComplete()
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
                weights.add(WeightEntry.create(r.kg, now()))
                _state.update { it.copy(sheet = null) }
                feedback.success()
            }
        }
    }

    fun delete(id: String) {
        weights.delete(id)
    }
}
