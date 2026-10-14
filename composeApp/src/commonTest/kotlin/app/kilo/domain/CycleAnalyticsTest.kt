package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CycleAnalyticsTest {
    private val utc = TimeZone.UTC
    // Wednesday, March 25, 2026, 12:00:00 UTC
    private val now = Instant.parse("2026-03-25T12:00:00Z")

    @Test
    fun disabledOrBlankDateReturnsEmptyState() {
        val s1 = computeCycleState(false, null, 28, 5, now, utc)
        assertFalse(s1.isEnabled)
        assertNull(s1.currentDayInfo)

        val s2 = computeCycleState(true, "", 28, 5, now, utc)
        assertFalse(s2.isEnabled)
        assertNull(s2.currentDayInfo)

        val s3 = computeCycleState(true, "invalid-date", 28, 5, now, utc)
        assertFalse(s3.isEnabled)
    }

    @Test
    fun dayOneMenstrualPhase() {
        // Last period started today: March 25, 2026 -> Day 1
        val state = computeCycleState(true, "2026-03-25", 28, 5, now, utc)
        assertTrue(state.isEnabled)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(1, info.cycleDay)
        assertEquals(CyclePhase.MENSTRUAL, info.phase)
        assertTrue(info.isPeriodDay)
        assertTrue(info.isToday)
        assertFalse(info.advice.waterRetentionRisk)
        assertEquals(EnergyLevel.LOW_REST, info.advice.energyLevel)
        assertEquals(WorkoutIntensity.REST_RECOVERY, info.advice.workoutIntensity)
        assertEquals("workout_caution_pelvic_pressure", info.advice.workoutCautionKey)
    }

    @Test
    fun dayEightFollicularPhase() {
        // Last period started 7 days ago: March 18, 2026 -> Day 8
        val state = computeCycleState(true, "2026-03-18", 28, 5, now, utc)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(8, info.cycleDay)
        assertEquals(CyclePhase.FOLLICULAR, info.phase)
        assertFalse(info.isPeriodDay)
        assertFalse(info.advice.waterRetentionRisk)
        assertEquals(EnergyLevel.RISING, info.advice.energyLevel)
        assertEquals(WorkoutIntensity.HIGH_INTENSITY, info.advice.workoutIntensity)
        assertNull(info.advice.workoutCautionKey)
    }

    @Test
    fun dayFourteenOvulationPhase() {
        // Last period started 13 days ago: March 12, 2026 -> Day 14
        val state = computeCycleState(true, "2026-03-12", 28, 5, now, utc)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(14, info.cycleDay)
        assertEquals(CyclePhase.OVULATION, info.phase)
        assertFalse(info.isPeriodDay)
        assertEquals(EnergyLevel.PEAK, info.advice.energyLevel)
        assertEquals(WorkoutIntensity.PEAK_POWER, info.advice.workoutIntensity)
        assertEquals("workout_caution_acl_ligament", info.advice.workoutCautionKey)
    }

    @Test
    fun dayTwentyThreeLutealWaterRetentionActive() {
        // Last period started 22 days ago: March 3, 2026 -> Day 23
        val state = computeCycleState(true, "2026-03-03", 28, 5, now, utc)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(23, info.cycleDay)
        assertEquals(CyclePhase.LUTEAL, info.phase)
        assertTrue(info.advice.waterRetentionRisk)
        assertTrue(state.isWaterRetentionActive)
        assertEquals(WorkoutIntensity.MODERATE_STEADY, info.advice.workoutIntensity)
        assertEquals("workout_caution_rpe_elevation", info.advice.workoutCautionKey)
    }

    @Test
    fun dayTwentySevenLutealLatePms() {
        // Last period started 26 days ago: February 27, 2026 -> Day 27 (Feb 2026 has 28 days)
        val state = computeCycleState(true, "2026-02-27", 28, 5, now, utc)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(27, info.cycleDay)
        assertEquals(CyclePhase.LUTEAL, info.phase)
        assertTrue(info.advice.waterRetentionRisk)
        assertEquals(2, state.daysUntilNextPeriod)
        assertEquals(WorkoutIntensity.REST_RECOVERY, info.advice.workoutIntensity)
        assertEquals("workout_caution_catabolism_protein", info.advice.workoutCautionKey)
    }

    @Test
    fun cycleWrapsAroundMultipleCycles() {
        // 56 days ago (exactly 2 complete 28-day cycles): January 28, 2026 -> Day 1 of 3rd cycle
        val state = computeCycleState(true, "2026-01-28", 28, 5, now, utc)
        val info = assertNotNull(state.currentDayInfo)

        assertEquals(1, info.cycleDay)
        assertEquals(CyclePhase.MENSTRUAL, info.phase)
    }

    @Test
    fun customCycleLengthThirtyTwoDays() {
        // In a 32-day cycle, ovulation is at Day 18 (32 - 14)
        val lastPeriod = LocalDate(2026, 3, 1)
        val day18 = LocalDate(2026, 3, 18)
        val dayInfo = computeCycleDayInfo(
            lastPeriodDate = lastPeriod,
            targetDate = day18,
            cycleLength = 32,
            periodDuration = 5,
            today = day18,
        )

        assertEquals(18, dayInfo.cycleDay)
        assertEquals(CyclePhase.OVULATION, dayInfo.phase)
    }
}
