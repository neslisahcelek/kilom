package app.kilo.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class SummaryPeriod { WEEK, MONTH }

data class PeriodSummary(
    val period: SummaryPeriod,
    val entryCount: Int,
    val averageKg: Double,
    val minKg: Double,
    val maxKg: Double,
    /** Last minus first entry in the period; null with fewer than 2 entries. */
    val changeKg: Double?,
    /** Average of the preceding equally long period; null when it has no entries. */
    val previousAverageKg: Double?,
)

/**
 * Summarises the last 7 (WEEK) or 30 (MONTH) days, using the same date cutoff as [filterEntriesForTrend].
 * Returns null when the period has no entries.
 */
fun buildPeriodSummary(
    entries: List<WeightEntry>,
    period: SummaryPeriod,
    now: Instant,
    tz: TimeZone,
): PeriodSummary? {
    val days = when (period) {
        SummaryPeriod.WEEK -> 7
        SummaryPeriod.MONTH -> 30
    }
    val range = when (period) {
        SummaryPeriod.WEEK -> TrendRange.LAST_7
        SummaryPeriod.MONTH -> TrendRange.DAYS_30
    }
    val current = filterEntriesForTrend(entries, range, now, tz)
    if (current.isEmpty()) return null

    val nowDate = now.toLocalDateTime(tz).date
    val currentStart = nowDate.minus(days, DateTimeUnit.DAY)
    val previousStart = nowDate.minus(days * 2, DateTimeUnit.DAY)
    val previous = entries.filter {
        val d = it.at.toLocalDateTime(tz).date
        d >= previousStart && d < currentStart
    }

    val kgs = current.map { it.kg }
    return PeriodSummary(
        period = period,
        entryCount = current.size,
        averageKg = kgs.average().round2(),
        minKg = kgs.min(),
        maxKg = kgs.max(),
        changeKg = if (current.size >= 2) (current.last().kg - current.first().kg).round2() else null,
        previousAverageKg = if (previous.isEmpty()) null else previous.map { it.kg }.average().round2(),
    )
}
