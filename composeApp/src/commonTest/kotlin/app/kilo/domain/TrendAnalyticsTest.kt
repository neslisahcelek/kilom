package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrendAnalyticsTest {
    private val utc = TimeZone.UTC
    private val plus3 = UtcOffset(hours = 3).asTimeZone()

    private fun entry(id: String, kg: Double, iso: String, tag: String? = null): WeightEntry =
        WeightEntry(id, kg, Instant.parse(iso), tag)

    // ==========================================
    // 1. Empty Entries
    // ==========================================

    @Test
    fun emptyEntriesReturnEmptyAndNull() {
        val now = Instant.parse("2024-05-10T12:00:00Z")

        for (range in TrendRange.entries) {
            assertEquals(emptyList(), filterEntriesForTrend(emptyList(), range, now, utc))
            assertNull(buildTrendChartData(emptyList(), range, now, utc))
        }

        assertEquals(emptyList(), computeMovingAverage(emptyList()))
    }

    // ==========================================
    // 2. Single Entry
    // ==========================================

    @Test
    fun singleEntryHandling() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val e = entry("e1", 75.5, "2024-05-10T08:00:00Z", tag = "morning_fasted")

        val filtered = filterEntriesForTrend(listOf(e), TrendRange.LAST_7, now, utc)
        assertEquals(1, filtered.size)
        assertEquals("e1", filtered[0].id)

        val ma = computeMovingAverage(filtered, windowDays = 7)
        assertEquals(listOf(null), ma)

        val chartData = buildTrendChartData(listOf(e), TrendRange.LAST_7, now, utc)
        assertNotNull(chartData)
        assertEquals(1, chartData.points.size)
        assertEquals(75.5, chartData.minKg)
        assertEquals(75.5, chartData.maxKg)
        assertEquals(75.5, chartData.averageKg)
        assertEquals(75.5, chartData.points[0].kg)
        assertEquals("morning_fasted", chartData.points[0].tag)
        assertNull(chartData.points[0].movingAverageKg)
    }

    // ==========================================
    // 3. Sorting & Order
    // ==========================================

    @Test
    fun entriesStoredDescendingAreReturnedSortedAscending() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val e1 = entry("1", 72.0, "2024-05-01T08:00:00Z")
        val e2 = entry("2", 71.5, "2024-05-03T08:00:00Z")
        val e3 = entry("3", 71.0, "2024-05-07T08:00:00Z")

        // Input in descending order (newest first)
        val descending = listOf(e3, e2, e1)
        val result = filterEntriesForTrend(descending, TrendRange.ALL, now, utc)

        assertEquals(listOf("1", "2", "3"), result.map { it.id })
    }

    // ==========================================
    // 4. Range Filtering
    // ==========================================

    @Test
    fun rangeFiltering7Days() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val inRange1 = entry("in1", 70.0, "2024-05-10T08:00:00Z") // 0 days ago (today)
        val inRange2 = entry("in2", 70.5, "2024-05-05T08:00:00Z") // 5 days ago
        val inRange3 = entry("in3", 71.0, "2024-05-03T08:00:00Z") // 7 days ago (boundary)
        val outOfRange = entry("out", 71.5, "2024-05-02T08:00:00Z") // 8 days ago

        val entries = listOf(inRange1, inRange2, inRange3, outOfRange)
        val filtered = filterEntriesForTrend(entries, TrendRange.LAST_7, now, utc)

        assertEquals(listOf("in3", "in2", "in1"), filtered.map { it.id })
    }

    @Test
    fun rangeFiltering30And90Days() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val eToday = entry("today", 70.0, "2024-05-10T08:00:00Z")
        val e20Days = entry("20d", 71.0, "2024-04-20T08:00:00Z")
        val e45Days = entry("45d", 72.0, "2024-03-26T08:00:00Z")
        val e100Days = entry("100d", 73.0, "2024-01-31T08:00:00Z")

        val allEntries = listOf(eToday, e20Days, e45Days, e100Days)

        val last7 = filterEntriesForTrend(allEntries, TrendRange.LAST_7, now, utc)
        assertEquals(listOf("today"), last7.map { it.id })

        val last30 = filterEntriesForTrend(allEntries, TrendRange.DAYS_30, now, utc)
        assertEquals(listOf("20d", "today"), last30.map { it.id })

        val last90 = filterEntriesForTrend(allEntries, TrendRange.DAYS_90, now, utc)
        assertEquals(listOf("45d", "20d", "today"), last90.map { it.id })

        val all = filterEntriesForTrend(allEntries, TrendRange.ALL, now, utc)
        assertEquals(listOf("100d", "45d", "20d", "today"), all.map { it.id })
    }

    @Test
    fun rangeFilteringTimeZoneBoundary() {
        // In UTC: 2024-05-03 22:00 is May 3 (7 days ago from May 10 12:00 UTC)
        // In +03:00: 2024-05-03 22:00 UTC is May 4 01:00 (6 days ago from May 10)
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val e = entry("tz", 70.0, "2024-05-03T22:00:00Z")

        val filteredUtc = filterEntriesForTrend(listOf(e), TrendRange.LAST_7, now, utc)
        assertEquals(1, filteredUtc.size)

        val filteredPlus3 = filterEntriesForTrend(listOf(e), TrendRange.LAST_7, now, plus3)
        assertEquals(1, filteredPlus3.size)
    }

    // ==========================================
    // 5. 7-Day Moving Average Calculation
    // ==========================================

    @Test
    fun movingAverageRequiresAtLeastTwoPoints() {
        val entries = listOf(
            entry("1", 80.0, "2024-05-01T08:00:00Z"),
            entry("2", 78.0, "2024-05-02T08:00:00Z"),
            entry("3", 76.0, "2024-05-03T08:00:00Z"),
        )

        val ma = computeMovingAverage(entries, windowDays = 7)
        assertEquals(3, ma.size)
        // First entry has only 1 point in its trailing 7-day window -> null
        assertNull(ma[0])
        // Second entry: (80.0 + 78.0) / 2 = 79.0
        assertEquals(79.0, ma[1])
        // Third entry: (80.0 + 78.0 + 76.0) / 3 = 78.0
        assertEquals(78.0, ma[2])
    }

    @Test
    fun movingAverageSlidesBeyondWindow() {
        val entries = listOf(
            entry("1", 80.0, "2024-05-01T08:00:00Z"),
            entry("2", 80.0, "2024-05-02T08:00:00Z"),
            // Day 10 is 9 days after Day 1 (outside 7-day window) and 8 days after Day 2 (outside 7-day window)
            entry("3", 70.0, "2024-05-10T08:00:00Z"),
            // Day 11 is 1 day after Day 10 (inside 7-day window)
            entry("4", 72.0, "2024-05-11T08:00:00Z"),
        )

        val ma = computeMovingAverage(entries, windowDays = 7)
        assertNull(ma[0])
        assertEquals(80.0, ma[1])
        // Day 10 has only itself in trailing 7 days -> null
        assertNull(ma[2])
        // Day 11 has Day 10 & Day 11 -> (70.0 + 72.0) / 2 = 71.0
        assertEquals(71.0, ma[3])
    }

    @Test
    fun movingAverageWithCustomWindowAndInvalidWindow() {
        val entries = listOf(
            entry("1", 80.0, "2024-05-01T08:00:00Z"),
            entry("2", 78.0, "2024-05-02T08:00:00Z"),
        )

        // Invalid window <= 0
        val maZero = computeMovingAverage(entries, windowDays = 0)
        assertEquals(listOf(null, null), maZero)

        // 1-day window
        val ma1 = computeMovingAverage(entries, windowDays = 1)
        assertEquals(listOf(null, 79.0), ma1)
    }

    // ==========================================
    // 6. Min/Max Bounds & Chart Data Construction
    // ==========================================

    @Test
    fun minMaxAndAverageBoundsCalculation() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val entries = listOf(
            entry("1", 70.0, "2024-05-01T08:00:00Z"),
            entry("2", 75.5, "2024-05-04T08:00:00Z"),
            entry("3", 68.2, "2024-05-08T08:00:00Z"),
            entry("4", 72.3, "2024-05-10T08:00:00Z"),
        )

        val data = buildTrendChartData(entries, TrendRange.DAYS_30, now, utc)
        assertNotNull(data)
        assertEquals(4, data.points.size)
        assertEquals(68.2, data.minKg)
        assertEquals(75.5, data.maxKg)
        // Average: (70.0 + 75.5 + 68.2 + 72.3) / 4 = 286.0 / 4 = 71.5
        assertEquals(71.5, data.averageKg)
    }

    @Test
    fun identicalWeightsProduceSameMinMaxAverage() {
        val now = Instant.parse("2024-05-10T12:00:00Z")
        val entries = listOf(
            entry("1", 70.0, "2024-05-08T08:00:00Z"),
            entry("2", 70.0, "2024-05-09T08:00:00Z"),
            entry("3", 70.0, "2024-05-10T08:00:00Z"),
        )

        val data = buildTrendChartData(entries, TrendRange.LAST_7, now, utc)
        assertNotNull(data)
        assertEquals(70.0, data.minKg)
        assertEquals(70.0, data.maxKg)
        assertEquals(70.0, data.averageKg)
    }
}
