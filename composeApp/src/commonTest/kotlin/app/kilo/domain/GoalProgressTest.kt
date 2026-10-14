package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoalProgressTest {
    private val utc = TimeZone.UTC
    private val now = Instant.parse("2026-03-31T12:00:00Z")

    private fun e(id: String, kg: Double, iso: String) = WeightEntry(id, kg, Instant.parse(iso))

    private fun losing() = listOf(
        e("a", 90.0, "2026-03-01T08:00:00Z"),
        e("b", 88.0, "2026-03-15T08:00:00Z"),
        e("c", 86.0, "2026-03-29T08:00:00Z"),
    )

    @Test
    fun emptyListOrNoGoalReturnsNull() {
        assertNull(computeGoalProgress(80.0, null, emptyList(), now, utc))
        assertNull(computeGoalProgress(null, null, losing(), now, utc))
        assertNull(computeGoalProgress(10.0, null, losing(), now, utc))
        assertNull(computeGoalProgress(600.0, null, losing(), now, utc))
    }

    @Test
    fun weightLossProgress() {
        val p = assertNotNull(computeGoalProgress(80.0, null, losing(), now, utc))
        assertEquals(90.0, p.startKg)
        assertEquals(86.0, p.currentKg)
        assertEquals(6.0, p.remainingKg, 1e-9)
        assertEquals(0.4f, p.fraction, 1e-4f)
        assertFalse(p.reached)
    }

    @Test
    fun weightGainProgress() {
        val entries = listOf(
            e("a", 60.0, "2026-03-01T08:00:00Z"),
            e("b", 65.0, "2026-03-29T08:00:00Z"),
        )
        val p = assertNotNull(computeGoalProgress(70.0, null, entries, now, utc))
        assertEquals(0.5f, p.fraction, 1e-4f)
        assertEquals(5.0, p.remainingKg, 1e-9)
        assertFalse(p.reached)
    }

    @Test
    fun goalReachedAndOvershoot() {
        val entries = losing() + e("d", 79.0, "2026-03-30T08:00:00Z")
        val p = assertNotNull(computeGoalProgress(80.0, null, entries, now, utc))
        assertTrue(p.reached)
        assertEquals(1f, p.fraction)
        assertNull(p.estimatedDate)
        assertEquals(1.0, p.remainingKg, 1e-9)
    }

    @Test
    fun singleEntryHasNoEstimate() {
        val p = assertNotNull(computeGoalProgress(80.0, null, listOf(e("a", 90.0, "2026-03-30T08:00:00Z")), now, utc))
        assertEquals(0f, p.fraction)
        assertNull(p.estimatedDate)
        assertFalse(p.reached)
    }

    @Test
    fun explicitStartKgIsUsed() {
        val p = assertNotNull(computeGoalProgress(80.0, 100.0, losing(), now, utc))
        assertEquals(100.0, p.startKg)
        assertEquals(0.7f, p.fraction, 1e-4f)
    }

    @Test
    fun movingAwayFromGoalGivesZeroFractionAndNoEstimate() {
        val entries = listOf(
            e("a", 85.0, "2026-03-05T08:00:00Z"),
            e("b", 87.0, "2026-03-15T08:00:00Z"),
            e("c", 89.0, "2026-03-29T08:00:00Z"),
        )
        val p = assertNotNull(computeGoalProgress(80.0, 85.0, entries, now, utc))
        assertEquals(0f, p.fraction)
        assertNull(p.estimatedDate)
    }

    @Test
    fun estimatedDateFromLinearSlope() {
        // exactly -1 kg per 7 days => 86 -> 80 needs 42 days after last entry value
        val entries = listOf(
            e("a", 90.0, "2026-03-01T12:00:00Z"),
            e("b", 89.0, "2026-03-08T12:00:00Z"),
            e("c", 88.0, "2026-03-15T12:00:00Z"),
            e("d", 87.0, "2026-03-22T12:00:00Z"),
            e("f", 86.0, "2026-03-29T12:00:00Z"),
        )
        val p = assertNotNull(computeGoalProgress(80.0, null, entries, now, utc))
        val eta = assertNotNull(p.estimatedDate)
        assertEquals(Instant.parse("2026-05-12T12:00:00Z"), eta)
    }

    @Test
    fun noEstimateWhenSpanTooShortOrTooFewEntries() {
        val shortSpan = listOf(
            e("a", 90.0, "2026-03-27T08:00:00Z"),
            e("b", 89.0, "2026-03-28T08:00:00Z"),
            e("c", 88.0, "2026-03-29T08:00:00Z"),
        )
        assertNull(computeGoalProgress(80.0, null, shortSpan, now, utc)?.estimatedDate)
        val two = listOf(
            e("a", 90.0, "2026-03-10T08:00:00Z"),
            e("b", 88.0, "2026-03-29T08:00:00Z"),
        )
        assertNull(computeGoalProgress(80.0, null, two, now, utc)?.estimatedDate)
    }
}
