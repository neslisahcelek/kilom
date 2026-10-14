package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreakAnalyticsTest {
    private val utc = TimeZone.UTC
    // Wednesday, March 25, 2026, 12:00:00 UTC
    private val now = Instant.parse("2026-03-25T12:00:00Z")

    private fun entry(id: String, kg: Double, iso: String) =
        WeightEntry(id = id, kg = kg, at = Instant.parse(iso))

    @Test
    fun emptyEntriesReturnsZeroStreak() {
        val info = computeStreak(emptyList(), now, utc)
        assertEquals(0, info.currentStreak)
        assertEquals(0, info.bestStreak)
        assertFalse(info.isTodayLogged)
        assertFalse(info.isStreakAtRisk)
        assertEquals(0, info.totalWeighIns)
        assertEquals(28, info.recentDays.size)
        assertTrue(info.activeMilestones.isEmpty())
    }

    @Test
    fun singleEntryToday() {
        val entries = listOf(entry("1", 75.0, "2026-03-25T08:00:00Z"))
        val info = computeStreak(entries, now, utc)

        assertEquals(1, info.currentStreak)
        assertEquals(1, info.bestStreak)
        assertTrue(info.isTodayLogged)
        assertFalse(info.isStreakAtRisk)
        assertEquals(1, info.totalWeighIns)
    }

    @Test
    fun singleEntryYesterdayIsAtRisk() {
        val entries = listOf(entry("1", 75.0, "2026-03-24T08:00:00Z"))
        val info = computeStreak(entries, now, utc)

        assertEquals(1, info.currentStreak)
        assertEquals(1, info.bestStreak)
        assertFalse(info.isTodayLogged)
        assertTrue(info.isStreakAtRisk)
    }

    @Test
    fun entryTwoDaysAgoBrokenStreak() {
        val entries = listOf(entry("1", 75.0, "2026-03-23T08:00:00Z"))
        val info = computeStreak(entries, now, utc)

        assertEquals(0, info.currentStreak)
        assertEquals(1, info.bestStreak)
        assertFalse(info.isTodayLogged)
        assertFalse(info.isStreakAtRisk)
    }

    @Test
    fun consecutiveSevenDaysEndingToday() {
        // March 19 to March 25 (7 consecutive days)
        val entries = (19..25).map { day ->
            entry("e$day", 75.0, "2026-03-${day.toString().padStart(2, '0')}T07:00:00Z")
        }
        val info = computeStreak(entries, now, utc)

        assertEquals(7, info.currentStreak)
        assertEquals(7, info.bestStreak)
        assertTrue(info.isTodayLogged)
        assertFalse(info.isStreakAtRisk)
        assertTrue(info.activeMilestones.any { it.type == MilestoneType.STREAK_7_DAYS })
    }

    @Test
    fun bestStreakPreservedWhenCurrentIsShorter() {
        // 5 consecutive days in early March (01 to 05), then 1 today
        val past = (1..5).map { day ->
            entry("p$day", 75.0, "2026-03-0${day}T07:00:00Z")
        }
        val current = listOf(entry("today", 74.5, "2026-03-25T07:00:00Z"))
        val info = computeStreak(past + current, now, utc)

        assertEquals(1, info.currentStreak)
        assertEquals(5, info.bestStreak)
    }

    @Test
    fun recentDaysGridAlignment() {
        val entries = listOf(
            entry("1", 75.0, "2026-03-25T08:00:00Z"),
        )
        val info = computeStreak(entries, now, utc)

        assertEquals(28, info.recentDays.size)
        // First day of grid should be a Monday (dayOfWeek = 1)
        assertEquals(1, info.recentDays.first().dayOfWeek)
        // Last day of grid should be a Sunday (dayOfWeek = 7)
        assertEquals(7, info.recentDays.last().dayOfWeek)

        val todayStatus = info.recentDays.first { it.isToday }
        assertEquals("2026-03-25", todayStatus.date.toString())
        assertTrue(todayStatus.hasWeighIn)
        assertFalse(todayStatus.isFuture)

        // Days after today should be flagged as isFuture
        val futureDays = info.recentDays.filter { it.isFuture }
        assertTrue(futureDays.isNotEmpty())
        futureDays.forEach {
            assertFalse(it.hasWeighIn)
            assertFalse(it.isToday)
        }
    }

    @Test
    fun weightLossMilestonesTriggered() {
        val entries = listOf(
            entry("1", 85.0, "2026-03-01T08:00:00Z"),
            entry("2", 79.5, "2026-03-25T08:00:00Z"),
        )
        val info = computeStreak(entries, now, utc)

        // 85.0 - 79.5 = 5.5 kg lost -> WEIGHT_LOSS_5KG milestone
        assertTrue(info.activeMilestones.any { it.type == MilestoneType.WEIGHT_LOSS_5KG })
    }

    @Test
    fun goalReachedMilestoneTriggered() {
        val goal = GoalProgress(
            goalKg = 75.0,
            startKg = 80.0,
            currentKg = 74.8,
            remainingKg = 0.0,
            fraction = 1.0f,
            reached = true,
            estimatedDate = null,
        )
        val entries = listOf(entry("1", 74.8, "2026-03-25T08:00:00Z"))
        val info = computeStreak(entries, now, utc, goal = goal)

        assertTrue(info.activeMilestones.any { it.type == MilestoneType.GOAL_REACHED })
    }
}
