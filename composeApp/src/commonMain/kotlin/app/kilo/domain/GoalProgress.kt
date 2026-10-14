package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

/** Progress towards a goal weight. All weights are in kg. */
data class GoalProgress(
    val goalKg: Double,
    val startKg: Double,
    val currentKg: Double,
    val remainingKg: Double,
    val fraction: Float,
    val reached: Boolean,
    val estimatedDate: Instant?,
)

private const val GOAL_TREND_DAYS = 30
private const val GOAL_MIN_ENTRIES = 3
private const val GOAL_MIN_SPAN_DAYS = 7.0
private const val GOAL_MIN_SLOPE_KG_PER_DAY = 0.01
private const val GOAL_REACHED_EPSILON_KG = 0.05
private const val MILLIS_PER_DAY = 86_400_000.0

/** Validates a goal in kg against the allowed 20–500 kg range. */
fun validateGoal(kg: Double): Boolean = kg.isFinite() && kg.round2() in MIN_KG..MAX_KG

/**
 * Computes progress towards [goalKg]. [startKg] defaults to the oldest entry when null.
 * Returns null when no valid goal or no entries exist.
 */
fun computeGoalProgress(
    goalKg: Double?,
    startKg: Double?,
    entries: List<WeightEntry>,
    now: Instant,
    tz: TimeZone,
): GoalProgress? {
    if (goalKg == null || !validateGoal(goalKg) || entries.isEmpty()) return null

    val sorted = entries.sortedWith(compareBy<WeightEntry> { it.at }.thenBy { it.id })
    val current = sorted.last().kg
    val start = startKg?.takeIf { it.isFinite() } ?: sorted.first().kg

    val remaining = abs(current - goalKg)
    val reached = when {
        start > goalKg -> current <= goalKg
        start < goalKg -> current >= goalKg
        else -> remaining < GOAL_REACHED_EPSILON_KG
    }

    val total = abs(start - goalKg)
    val fraction = when {
        reached -> 1f
        total <= 0.0 -> 0f
        else -> {
            val towardGoal = if (start > goalKg) start - current else current - start
            (towardGoal / total).coerceIn(0.0, 1.0).toFloat()
        }
    }

    val eta = if (reached) null else estimateGoalDate(goalKg, current, sorted, now, tz)

    return GoalProgress(
        goalKg = goalKg,
        startKg = start,
        currentKg = current,
        remainingKg = remaining,
        fraction = fraction,
        reached = reached,
        estimatedDate = eta,
    )
}

private fun estimateGoalDate(
    goalKg: Double,
    currentKg: Double,
    sortedAscending: List<WeightEntry>,
    now: Instant,
    tz: TimeZone,
): Instant? {
    val recent = filterEntriesForTrend(sortedAscending, TrendRange.DAYS_30, now, tz)
    if (recent.size < GOAL_MIN_ENTRIES) return null

    val t0 = recent.first().at.toEpochMilliseconds()
    val xs = recent.map { (it.at.toEpochMilliseconds() - t0) / MILLIS_PER_DAY }
    if (xs.last() < GOAL_MIN_SPAN_DAYS) return null

    val ys = recent.map { it.kg }
    val meanX = xs.average()
    val meanY = ys.average()
    val denom = xs.sumOf { (it - meanX) * (it - meanX) }
    if (denom <= 0.0) return null
    val slope = xs.indices.sumOf { (xs[it] - meanX) * (ys[it] - meanY) } / denom // kg/day

    if (abs(slope) < GOAL_MIN_SLOPE_KG_PER_DAY) return null
    val needed = goalKg - currentKg
    if (needed * slope <= 0.0) return null

    val days = needed / slope
    if (!days.isFinite()) return null
    return now + (days * MILLIS_PER_DAY).toLong().milliseconds
}
