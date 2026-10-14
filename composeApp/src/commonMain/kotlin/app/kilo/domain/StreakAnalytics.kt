package app.kilo.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Status of a specific day in the streak / habit matrix. */
data class DayStatus(
    val date: LocalDate,
    val hasWeighIn: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean = false,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
)

enum class MilestoneType {
    STREAK_3_DAYS,
    STREAK_7_DAYS,
    STREAK_14_DAYS,
    STREAK_30_DAYS,
    WEIGHT_LOSS_1KG,
    WEIGHT_LOSS_3KG,
    WEIGHT_LOSS_5KG,
    WEIGHT_LOSS_10KG,
    GOAL_REACHED,
}

data class Milestone(
    val type: MilestoneType,
    val valueLabel: String,
    val emoji: String,
)

/** Aggregated streak and consistency information. */
data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int,
    val isTodayLogged: Boolean,
    val isStreakAtRisk: Boolean, // Yesterday logged, today not yet logged
    val recentDays: List<DayStatus>, // 28 days (4 weeks: Mon..Sun)
    val totalWeighIns: Int,
    val activeMilestones: List<Milestone>,
)

private const val MATRIX_WEEKS = 4
private const val DAYS_IN_WEEK = 7
private const val MATRIX_DAYS_TOTAL = MATRIX_WEEKS * DAYS_IN_WEEK

/**
 * Computes the streak and habit matrix for [entries] relative to [now] in [tz].
 * Pure Kotlin, 100% on-device and deterministic.
 */
fun computeStreak(
    entries: List<WeightEntry>,
    now: Instant,
    tz: TimeZone,
    goal: GoalProgress? = null,
): StreakInfo {
    val today = now.toLocalDateTime(tz).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val entryDates = entries.map { it.at.toLocalDateTime(tz).date }.toSet()

    val isTodayLogged = today in entryDates
    val isYesterdayLogged = yesterday in entryDates
    val isStreakAtRisk = !isTodayLogged && isYesterdayLogged

    val currentStreak = when {
        isTodayLogged -> countConsecutiveBackwards(start = today, entryDates = entryDates)
        isYesterdayLogged -> countConsecutiveBackwards(start = yesterday, entryDates = entryDates)
        else -> 0
    }

    val bestStreak = computeBestStreak(entryDates, currentStreak)
    val recentDays = buildRecentDays(today = today, entryDates = entryDates)
    val milestones = evaluateMilestones(
        entries = entries,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        goal = goal,
    )

    return StreakInfo(
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        isTodayLogged = isTodayLogged,
        isStreakAtRisk = isStreakAtRisk,
        recentDays = recentDays,
        totalWeighIns = entries.size,
        activeMilestones = milestones,
    )
}

private fun countConsecutiveBackwards(start: LocalDate, entryDates: Set<LocalDate>): Int {
    var count = 0
    var current = start
    while (current in entryDates) {
        count++
        current = current.minus(1, DateTimeUnit.DAY)
    }
    return count
}

private fun computeBestStreak(entryDates: Set<LocalDate>, currentStreak: Int): Int {
    if (entryDates.isEmpty()) return 0
    val sortedDates = entryDates.sorted()
    var maxStreak = 1
    var runningStreak = 1

    for (i in 1 until sortedDates.size) {
        val prev = sortedDates[i - 1]
        val curr = sortedDates[i]
        if (curr == prev.plus(1, DateTimeUnit.DAY)) {
            runningStreak++
            if (runningStreak > maxStreak) {
                maxStreak = runningStreak
            }
        } else if (curr > prev.plus(1, DateTimeUnit.DAY)) {
            runningStreak = 1
        }
    }

    return maxOf(maxStreak, currentStreak)
}

private fun buildRecentDays(today: LocalDate, entryDates: Set<LocalDate>): List<DayStatus> {
    // Week starts on Monday (ordinal = 0 -> 1).
    val todayIsoDay = today.dayOfWeek.ordinal + 1
    val currentWeekMonday = today.minus(todayIsoDay - 1, DateTimeUnit.DAY)
    // 4 weeks total: 3 past weeks + current week.
    val startMonday = currentWeekMonday.minus((MATRIX_WEEKS - 1) * DAYS_IN_WEEK, DateTimeUnit.DAY)

    return (0 until MATRIX_DAYS_TOTAL).map { offset ->
        val date = startMonday.plus(offset, DateTimeUnit.DAY)
        DayStatus(
            date = date,
            hasWeighIn = date in entryDates,
            isToday = (date == today),
            isFuture = (date > today),
            dayOfWeek = date.dayOfWeek.ordinal + 1,
        )
    }
}

private fun evaluateMilestones(
    entries: List<WeightEntry>,
    currentStreak: Int,
    bestStreak: Int,
    goal: GoalProgress?,
): List<Milestone> {
    val result = mutableListOf<Milestone>()

    // Goal reached
    if (goal != null && goal.reached) {
        result.add(
            Milestone(
                type = MilestoneType.GOAL_REACHED,
                valueLabel = "${goal.goalKg} kg",
                emoji = "🏁",
            )
        )
    }

    // Streak milestones (highest reached)
    val maxStreak = maxOf(currentStreak, bestStreak)
    when {
        maxStreak >= 30 -> result.add(Milestone(MilestoneType.STREAK_30_DAYS, "30", "👑"))
        maxStreak >= 14 -> result.add(Milestone(MilestoneType.STREAK_14_DAYS, "14", "⚡"))
        maxStreak >= 7 -> result.add(Milestone(MilestoneType.STREAK_7_DAYS, "7", "🔥"))
        maxStreak >= 3 -> result.add(Milestone(MilestoneType.STREAK_3_DAYS, "3", "🌱"))
    }

    // Weight loss milestones
    if (entries.size >= 2) {
        val sorted = entries.sortedWith(compareBy<WeightEntry> { it.at }.thenBy { it.id })
        val startKg = goal?.startKg ?: sorted.first().kg
        val currentKg = sorted.last().kg
        val lossKg = (startKg - currentKg).round2()

        when {
            lossKg >= 10.0 -> result.add(Milestone(MilestoneType.WEIGHT_LOSS_10KG, "-10 kg", "🏆"))
            lossKg >= 5.0 -> result.add(Milestone(MilestoneType.WEIGHT_LOSS_5KG, "-5 kg", "🎯"))
            lossKg >= 3.0 -> result.add(Milestone(MilestoneType.WEIGHT_LOSS_3KG, "-3 kg", "✨"))
            lossKg >= 1.0 -> result.add(Milestone(MilestoneType.WEIGHT_LOSS_1KG, "-1 kg", "🎈"))
        }
    }

    return result
}
