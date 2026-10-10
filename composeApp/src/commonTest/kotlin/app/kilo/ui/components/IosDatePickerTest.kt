package app.kilo.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class IosDatePickerTest {

    @Test
    fun daysInMonthReturnsCorrectCountForStandardMonths() {
        // January, March, May, July, August, October, December -> 31
        assertEquals(31, daysInMonth(2026, 1))
        assertEquals(31, daysInMonth(2026, 3))
        assertEquals(31, daysInMonth(2026, 5))
        assertEquals(31, daysInMonth(2026, 7))
        assertEquals(31, daysInMonth(2026, 8))
        assertEquals(31, daysInMonth(2026, 10))
        assertEquals(31, daysInMonth(2026, 12))

        // April, June, September, November -> 30
        assertEquals(30, daysInMonth(2026, 4))
        assertEquals(30, daysInMonth(2026, 6))
        assertEquals(30, daysInMonth(2026, 9))
        assertEquals(30, daysInMonth(2026, 11))
    }

    @Test
    fun daysInMonthHandlesFebruaryLeapAndNonLeapYears() {
        // Non-leap year
        assertEquals(28, daysInMonth(2026, 2))
        assertEquals(28, daysInMonth(2025, 2))
        assertEquals(28, daysInMonth(2100, 2)) // Century non-leap

        // Leap year
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(29, daysInMonth(2028, 2))
        assertEquals(29, daysInMonth(2000, 2)) // Century leap
    }
}
