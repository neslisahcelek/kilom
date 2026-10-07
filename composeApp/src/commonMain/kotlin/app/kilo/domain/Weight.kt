package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

/** A stored weigh-in. Weight is ALWAYS kg. */
data class WeightEntry(val id: String, val kg: Double, val at: Instant) {
    companion object {
        fun create(kg: Double, at: Instant, random: Random = Random.Default): WeightEntry =
            WeightEntry("${at.toEpochMilliseconds()}-${random.nextLong().toULong().toString(16)}", kg, at)
    }
}

enum class WeightUnit { KG, LB }

const val LB_PER_KG = 2.20462262

const val MIN_KG = 20.0
const val MAX_KG = 500.0

/** Rounds to 1 decimal place. */
fun Double.round1(): Double = (this * 10).roundToInt() / 10.0

fun Double.kgToLb(): Double = this * LB_PER_KG
fun Double.lbToKg(): Double = this / LB_PER_KG

/** Converts a kg value to [unit] and rounds to 1 decimal. */
fun Double.kgTo(unit: WeightUnit): Double = when (unit) {
    WeightUnit.KG -> this.round1()
    WeightUnit.LB -> this.kgToLb().round1()
}

/** Locale-independent 1-decimal formatting ("72.5", "-0.4", "70.0"). */
fun formatOneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val a = abs(tenths)
    return "$sign${a / 10}.${a % 10}"
}

/** Formats a kg value in the given unit WITHOUT suffix, e.g. "154.3". */
fun formatWeight(kg: Double, unit: WeightUnit): String = formatOneDecimal(kg.kgTo(unit))

/** Formats with unit suffix, e.g. "72.5 kg". */
fun formatWeightWithUnit(kg: Double, unit: WeightUnit): String =
    "${formatWeight(kg, unit)} ${unit.label}"

val WeightUnit.label: String get() = when (this) { WeightUnit.KG -> "kg"; WeightUnit.LB -> "lb" }

/** Change vs previous entry. [kg] is rounded to 1 decimal; [days] is a calendar-day difference. */
data class Delta(val kg: Double, val days: Int)

/** Delta value converted to [unit], 1 decimal. */
fun Delta.valueIn(unit: WeightUnit): Double = kg.kgTo(unit)

/** Signed delta string: "+0.4", "-1.2", "0.0". */
fun Delta.format(unit: WeightUnit): String {
    val v = valueIn(unit)
    val s = formatOneDecimal(v)
    return if (v > 0) "+$s" else if (v == 0.0) "0.0" else s
}

fun computeDelta(cur: WeightEntry, prev: WeightEntry?, tz: TimeZone): Delta? = prev?.let {
    val d = cur.at.toLocalDateTime(tz).date.toEpochDays() - it.at.toLocalDateTime(tz).date.toEpochDays()
    Delta((cur.kg - it.kg).round1(), d.toInt())
}

/** Derived list row: entry plus delta against the next (older) item in the sorted list. */
data class HistoryItem(val entry: WeightEntry, val delta: Delta?)

/** Sorted by `at` descending (ties: id descending, for stability); previous = next element. */
fun buildHistory(entries: List<WeightEntry>, tz: TimeZone): List<HistoryItem> {
    val sorted = entries.sortedWith(compareByDescending<WeightEntry> { it.at }.thenByDescending { it.id })
    return sorted.mapIndexed { i, e -> HistoryItem(e, computeDelta(e, sorted.getOrNull(i + 1), tz)) }
}
