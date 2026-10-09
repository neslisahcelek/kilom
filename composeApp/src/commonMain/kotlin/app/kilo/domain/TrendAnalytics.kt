package app.kilo.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

enum class TrendRange {
    LAST_7,
    DAYS_30,
    DAYS_90,
    ALL,
}

data class TrendPoint(
    val instant: Instant,
    val kg: Double,
    val movingAverageKg: Double?,
    val tag: String? = null,
)

data class TrendChartData(
    val points: List<TrendPoint>,
    val minKg: Double,
    val maxKg: Double,
    val averageKg: Double,
)

/**
 * Filters [entries] based on [range] relative to [now] in [tz].
 * [entries] may be stored in descending order; returned entries are always sorted ascending for chart rendering.
 */
fun filterEntriesForTrend(
    entries: List<WeightEntry>,
    range: TrendRange,
    now: Instant,
    tz: TimeZone,
): List<WeightEntry> {
    if (entries.isEmpty()) return emptyList()

    val nowDate = now.toLocalDateTime(tz).date
    val filtered = when (range) {
        TrendRange.ALL -> entries
        TrendRange.LAST_7 -> {
            val cutoff = nowDate.minus(7, DateTimeUnit.DAY)
            entries.filter { it.at.toLocalDateTime(tz).date >= cutoff }
        }
        TrendRange.DAYS_30 -> {
            val cutoff = nowDate.minus(30, DateTimeUnit.DAY)
            entries.filter { it.at.toLocalDateTime(tz).date >= cutoff }
        }
        TrendRange.DAYS_90 -> {
            val cutoff = nowDate.minus(90, DateTimeUnit.DAY)
            entries.filter { it.at.toLocalDateTime(tz).date >= cutoff }
        }
    }

    return filtered.sortedWith(compareBy<WeightEntry> { it.at }.thenBy { it.id })
}

/**
 * Calculates trailing [windowDays]-day moving average for each point in [sortedAscending].
 * Returns null for points that do not have at least 2 entries within their trailing window.
 */
fun computeMovingAverage(
    sortedAscending: List<WeightEntry>,
    windowDays: Int = 7,
): List<Double?> {
    if (sortedAscending.isEmpty()) return emptyList()
    if (windowDays <= 0) return List(sortedAscending.size) { null }

    val windowDuration = windowDays.days

    return sortedAscending.mapIndexed { index, current ->
        val windowEntries = sortedAscending.subList(0, index + 1).filter {
            val diff = current.at - it.at
            diff in kotlin.time.Duration.ZERO..windowDuration
        }
        if (windowEntries.size >= 2) {
            windowEntries.map { it.kg }.average().round2()
        } else {
            null
        }
    }
}

/**
 * Builds [TrendChartData] from raw [entries] filtered by [range], or returns null if no entries fall in range.
 */
fun buildTrendChartData(
    entries: List<WeightEntry>,
    range: TrendRange,
    now: Instant,
    tz: TimeZone,
): TrendChartData? {
    val filtered = filterEntriesForTrend(entries, range, now, tz)
    if (filtered.isEmpty()) return null

    val movingAverages = computeMovingAverage(filtered, windowDays = 7)
    val points = filtered.mapIndexed { index, entry ->
        TrendPoint(
            instant = entry.at,
            kg = entry.kg,
            movingAverageKg = movingAverages.getOrNull(index),
            tag = entry.tag,
        )
    }

    val minKg = points.minOf { it.kg }
    val maxKg = points.maxOf { it.kg }
    val averageKg = points.map { it.kg }.average().round2()

    return TrendChartData(
        points = points,
        minKg = minKg,
        maxKg = maxKg,
        averageKg = averageKg,
    )
}
