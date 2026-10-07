package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

/**
 * Immutable weight log entry.
 * [kg] is always stored in kilograms.
 */
data class WeightEntry(
    val id: String,
    val kg: Double,
    val at: Instant,
) {
    companion object {
        fun create(kg: Double, at: Instant, random: Random = Random.Default): WeightEntry {
            val rand = random.nextLong().let { if (it == Long.MIN_VALUE) 0L else abs(it) }.toString(36)
            return WeightEntry(
                id = "${at.toEpochMilliseconds()}_$rand",
                kg = kg.round1(),
                at = at,
            )
        }
    }
}

enum class WeightUnit {
    KG, LB;
}

const val LB_PER_KG = 2.20462262185
const val MIN_KG = 20.0
const val MAX_KG = 500.0

fun Double.round1(): Double {
    val r = round(this * 10.0) / 10.0
    return if (r == 0.0) 0.0 else r
}

fun Double.kgToLb(): Double = this * LB_PER_KG
fun Double.lbToKg(): Double = this / LB_PER_KG

fun Double.kgTo(unit: WeightUnit): Double = when (unit) {
    WeightUnit.KG -> this.round1()
    WeightUnit.LB -> this.kgToLb().round1()
}

fun formatOneDecimal(v: Double): String {
    val rounded = v.round1()
    val raw = if (rounded == 0.0) "0.0" else rounded.toString()
    return if (!raw.contains('.')) "$raw.0" else raw
}

fun formatWeight(kg: Double, unit: WeightUnit): String =
    formatOneDecimal(kg.kgTo(unit))

fun formatWeightWithUnit(kg: Double, unit: WeightUnit): String =
    "${formatWeight(kg, unit)} ${unit.label}"

val WeightUnit.label: String get() = when (this) { WeightUnit.KG -> "kg"; WeightUnit.LB -> "lb" }

/** Change vs previous entry. [kg] is unrounded raw delta; [days] is a calendar-day difference. */
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
    Delta(cur.kg - it.kg, d)
}

/** Derived list row: entry plus delta against the next (older) item in the sorted list. */
data class HistoryItem(val entry: WeightEntry, val delta: Delta?)

/** Sorted by `at` descending (ties: id descending, for stability); previous = next element. */
fun buildHistory(entries: List<WeightEntry>, tz: TimeZone): List<HistoryItem> {
    val sorted = entries.sortedWith(compareByDescending<WeightEntry> { it.at }.thenByDescending { it.id })
    return sorted.mapIndexed { i, e -> HistoryItem(e, computeDelta(e, sorted.getOrNull(i + 1), tz)) }
}
