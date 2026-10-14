package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PeriodSummaryTest {
    private val utc = TimeZone.UTC
    private val now = Instant.parse("2026-03-31T12:00:00Z")

    private fun e(id: String, kg: Double, iso: String) = WeightEntry(id, kg, Instant.parse(iso))

    @Test
    fun emptyReturnsNull() {
        assertNull(buildPeriodSummary(emptyList(), SummaryPeriod.WEEK, now, utc))
    }

    @Test
    fun entriesOutsidePeriodReturnNull() {
        val list = listOf(e("a", 80.0, "2026-01-01T08:00:00Z"))
        assertNull(buildPeriodSummary(list, SummaryPeriod.MONTH, now, utc))
    }

    @Test
    fun singleEntry() {
        val s = assertNotNull(buildPeriodSummary(listOf(e("a", 80.5, "2026-03-30T08:00:00Z")), SummaryPeriod.WEEK, now, utc))
        assertEquals(1, s.entryCount)
        assertEquals(80.5, s.averageKg)
        assertEquals(80.5, s.minKg)
        assertEquals(80.5, s.maxKg)
        assertNull(s.changeKg)
        assertNull(s.previousAverageKg)
    }

    @Test
    fun multipleEntriesUnsortedInput() {
        val list = listOf(
            e("c", 79.0, "2026-03-30T08:00:00Z"),
            e("a", 81.0, "2026-03-26T08:00:00Z"),
            e("b", 80.0, "2026-03-28T08:00:00Z"),
        )
        val s = assertNotNull(buildPeriodSummary(list, SummaryPeriod.WEEK, now, utc))
        assertEquals(3, s.entryCount)
        assertEquals(80.0, s.averageKg)
        assertEquals(79.0, s.minKg)
        assertEquals(81.0, s.maxKg)
        assertEquals(-2.0, s.changeKg)
    }

    @Test
    fun cutoffIsInclusiveByLocalDate() {
        // 7 days before 2026-03-31 is 2026-03-24 (included), 03-23 excluded
        val list = listOf(
            e("a", 90.0, "2026-03-23T23:59:00Z"),
            e("b", 80.0, "2026-03-24T00:00:00Z"),
        )
        val s = assertNotNull(buildPeriodSummary(list, SummaryPeriod.WEEK, now, utc))
        assertEquals(1, s.entryCount)
        assertEquals(80.0, s.averageKg)
        assertEquals(90.0, s.previousAverageKg)
    }

    @Test
    fun midnightBoundaryRespectsTimeZone() {
        val plus3 = UtcOffset(hours = 3).asTimeZone()
        // 2026-03-23T22:00Z is 2026-03-24 01:00 in +3 => included; in UTC it is 03-23 => excluded
        val list = listOf(e("a", 80.0, "2026-03-23T22:00:00Z"))
        assertNotNull(buildPeriodSummary(list, SummaryPeriod.WEEK, now, plus3))
        assertNull(buildPeriodSummary(list, SummaryPeriod.WEEK, now, utc))
    }

    @Test
    fun previousPeriodAverage() {
        val list = listOf(
            e("p1", 84.0, "2026-03-12T08:00:00Z"),
            e("p2", 82.0, "2026-03-20T08:00:00Z"),
            e("c1", 80.0, "2026-03-28T08:00:00Z"),
        )
        val s = assertNotNull(buildPeriodSummary(list, SummaryPeriod.WEEK, now, utc))
        // previous week window: 03-17..03-23 => only p2
        assertEquals(82.0, s.previousAverageKg)
        val m = assertNotNull(buildPeriodSummary(list, SummaryPeriod.MONTH, now, utc))
        assertEquals(3, m.entryCount)
        assertNull(m.previousAverageKg)
    }
}
