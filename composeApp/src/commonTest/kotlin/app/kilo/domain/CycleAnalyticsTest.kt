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

    @Test
    fun twentyOneDayCycleBackwardLutealMapping() {
        // In a 21-day cycle:
        // ovulationDay = 21 - 14 = 7
        // Menstrual: 1..minOf(4, 2) = 1..2
        // Early Follicular: 3..maxOf(3, 4) = 3..4
        // Late Follicular: 5..6
        // Ovulation: 7..8
        // Early Luteal: 9..13
        // Mid Luteal: 14..17
        // Late PMS: 18..21

        assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(1, 21))
        assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(2, 21))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(3, 21))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(4, 21))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(5, 21))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(6, 21))
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(7, 21))
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(8, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(9, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(13, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(14, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(17, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(18, 21))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(21, 21))

        // Check advice nuances for 21-day cycle
        val day1Advice = adviceForDay(1, 21)
        assertEquals(CyclePhase.MENSTRUAL, day1Advice.phase)
        assertEquals(WorkoutIntensity.REST_RECOVERY, day1Advice.workoutIntensity)

        val day5Advice = adviceForDay(5, 21)
        assertEquals(CyclePhase.FOLLICULAR, day5Advice.phase)
        assertEquals(WorkoutIntensity.PEAK_POWER, day5Advice.workoutIntensity)

        val day7Advice = adviceForDay(7, 21)
        assertEquals(CyclePhase.OVULATION, day7Advice.phase)
        assertEquals("workout_caution_acl_ligament", day7Advice.workoutCautionKey)

        val day15Advice = adviceForDay(15, 21)
        assertTrue(day15Advice.waterRetentionRisk)
        assertEquals(WorkoutIntensity.MODERATE_STEADY, day15Advice.workoutIntensity)

        val day20Advice = adviceForDay(20, 21)
        assertTrue(day20Advice.waterRetentionRisk)
        assertEquals(WorkoutIntensity.REST_RECOVERY, day20Advice.workoutIntensity)
        assertEquals("workout_caution_catabolism_protein", day20Advice.workoutCautionKey)
    }

    @Test
    fun twentyEightDayCycleFullMicroPhases() {
        // In a 28-day cycle:
        // ovulationDay = 14
        // Menstrual: 1..4
        // Early Follicular: 5..11
        // Late Follicular: 12..13
        // Ovulation: 14..15
        // Early Luteal: 16..20
        // Mid Luteal: 21..24
        // Late PMS: 25..28

        for (day in 1..4) {
            assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(day, 28))
            assertFalse(adviceForDay(day, 28).waterRetentionRisk)
        }

        for (day in 5..11) {
            assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(day, 28))
            assertEquals(WorkoutIntensity.HIGH_INTENSITY, adviceForDay(day, 28).workoutIntensity)
        }

        for (day in 12..13) {
            assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(day, 28))
            assertEquals(WorkoutIntensity.PEAK_POWER, adviceForDay(day, 28).workoutIntensity)
        }

        for (day in 14..15) {
            assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(day, 28))
            assertEquals("workout_caution_acl_ligament", adviceForDay(day, 28).workoutCautionKey)
        }

        for (day in 16..20) {
            assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(day, 28))
            assertFalse(adviceForDay(day, 28).waterRetentionRisk)
            assertEquals("workout_caution_thermoregulation", adviceForDay(day, 28).workoutCautionKey)
        }

        for (day in 21..24) {
            assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(day, 28))
            assertTrue(adviceForDay(day, 28).waterRetentionRisk)
            assertEquals("workout_caution_rpe_elevation", adviceForDay(day, 28).workoutCautionKey)
        }

        for (day in 25..28) {
            assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(day, 28))
            assertTrue(adviceForDay(day, 28).waterRetentionRisk)
            assertEquals(WorkoutIntensity.REST_RECOVERY, adviceForDay(day, 28).workoutIntensity)
            assertEquals("workout_caution_catabolism_protein", adviceForDay(day, 28).workoutCautionKey)
        }
    }

    @Test
    fun thirtyFiveDayCycleBackwardLutealMapping() {
        // In a 35-day cycle:
        // ovulationDay = 35 - 14 = 21
        // Menstrual: 1..minOf(4, 16) = 1..4
        // Early Follicular: 5..18
        // Late Follicular: 19..20
        // Ovulation: 21..22
        // Early Luteal: 23..27
        // Mid Luteal: 28..31
        // Late PMS: 32..35

        assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(1, 35))
        assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(4, 35))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(5, 35))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(18, 35))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(19, 35))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(20, 35))
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(21, 35))
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(22, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(23, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(27, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(28, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(31, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(32, 35))
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(35, 35))

        // Peak power on days 19-20
        assertEquals(WorkoutIntensity.PEAK_POWER, adviceForDay(19, 35).workoutIntensity)
        assertEquals(WorkoutIntensity.PEAK_POWER, adviceForDay(20, 35).workoutIntensity)

        // Mid luteal water retention on days 28-31
        for (day in 28..31) {
            assertTrue(adviceForDay(day, 35).waterRetentionRisk)
        }

        // Late PMS deload on days 32-35
        for (day in 32..35) {
            assertTrue(adviceForDay(day, 35).waterRetentionRisk)
            assertEquals(WorkoutIntensity.REST_RECOVERY, adviceForDay(day, 35).workoutIntensity)
        }
    }

    @Test
    fun fortyTwoDayCycleBackwardLutealMapping() {
        // In a 42-day cycle:
        // ovulationDay = 42 - 14 = 28
        // Menstrual: 1..4
        // Early Follicular: 5..25
        // Late Follicular: 26..27
        // Ovulation: 28..29
        // Early Luteal: 30..34
        // Mid Luteal: 35..38
        // Late PMS: 39..42

        assertEquals(CyclePhase.MENSTRUAL, cyclePhaseForDay(1, 42))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(15, 42))
        assertEquals(CyclePhase.FOLLICULAR, cyclePhaseForDay(26, 42))
        assertEquals(WorkoutIntensity.PEAK_POWER, adviceForDay(26, 42).workoutIntensity)
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(28, 42))
        assertEquals(WorkoutIntensity.PEAK_POWER, adviceForDay(28, 42).workoutIntensity)
        assertEquals("workout_caution_acl_ligament", adviceForDay(28, 42).workoutCautionKey)
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(32, 42))
        assertFalse(adviceForDay(32, 42).waterRetentionRisk)
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(36, 42))
        assertTrue(adviceForDay(36, 42).waterRetentionRisk)
        assertEquals(CyclePhase.LUTEAL, cyclePhaseForDay(41, 42))
        assertTrue(adviceForDay(41, 42).waterRetentionRisk)
        assertEquals(WorkoutIntensity.REST_RECOVERY, adviceForDay(41, 42).workoutIntensity)
    }

    @Test
    fun boundaryClampingForOutOfRangeCycleLengths() {
        // Clamped to 21 when input is 15
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(7, 15))
        val adviceShort = adviceForDay(7, 15)
        assertEquals(CyclePhase.OVULATION, adviceShort.phase)

        // Clamped to 45 when input is 60
        // In 45-day cycle: ovulation is 45 - 14 = 31
        assertEquals(CyclePhase.OVULATION, cyclePhaseForDay(31, 60))
        val adviceLong = adviceForDay(31, 60)
        assertEquals(CyclePhase.OVULATION, adviceLong.phase)
    }
}
