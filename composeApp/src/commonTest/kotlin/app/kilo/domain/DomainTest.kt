package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun e(id: String, kg: Double, iso: String) = WeightEntry(id, kg, Instant.parse(iso))

class DeltaTest {
    private val utc = TimeZone.UTC
    private val plus3 = UtcOffset(hours = 3).asTimeZone()
    private val minus8 = UtcOffset(hours = -8).asTimeZone()

    @Test fun firstEntryHasNoDelta() {
        assertNull(computeDelta(e("a", 70.0, "2024-01-10T10:00:00Z"), null, utc))
    }

    @Test fun negativeDelta() {
        val d = computeDelta(e("b", 71.6, "2024-01-12T08:00:00Z"), e("a", 72.5, "2024-01-10T08:00:00Z"), utc)!!
        assertEquals(-0.9, d.kg.round2())
        assertEquals(2, d.days)
    }

    @Test fun positiveDelta() {
        val d = computeDelta(e("b", 72.5, "2024-01-11T08:00:00Z"), e("a", 72.1, "2024-01-10T08:00:00Z"), utc)!!
        assertEquals(0.4, d.kg.round2())
        assertEquals(1, d.days)
    }

    @Test fun zeroDeltaIsNotNegativeZero() {
        val d = computeDelta(e("b", 70.0, "2024-01-11T08:00:00Z"), e("a", 70.0, "2024-01-10T08:00:00Z"), utc)!!
        assertEquals(0.0, d.kg)
        assertEquals("0.00", d.format(WeightUnit.KG))
    }

    @Test fun sameDayIsZeroDays() {
        val d = computeDelta(e("b", 70.4, "2024-01-10T20:00:00Z"), e("a", 70.0, "2024-01-10T06:00:00Z"), utc)!!
        assertEquals(0, d.days)
    }

    @Test fun midnightBoundaryIsCalendarDayNotHours() {
        // 23:59 -> 00:01 is 2 minutes apart but 1 calendar day
        val d = computeDelta(e("b", 70.0, "2024-01-11T00:01:00Z"), e("a", 70.0, "2024-01-10T23:59:00Z"), utc)!!
        assertEquals(1, d.days)
    }

    @Test fun almostTwentyFourHoursCanBeSameDay() {
        val d = computeDelta(e("b", 70.0, "2024-01-10T23:59:00Z"), e("a", 70.0, "2024-01-10T00:00:00Z"), utc)!!
        assertEquals(0, d.days)
    }

    @Test fun timeZoneChangesDayDifference() {
        val prev = e("a", 70.0, "2024-01-10T23:30:00Z")
        val cur = e("b", 70.0, "2024-01-11T00:30:00Z")
        assertEquals(1, computeDelta(cur, prev, utc)!!.days)
        // +03:00: both on Jan 11 local
        assertEquals(0, computeDelta(cur, prev, plus3)!!.days)
        // -08:00: both on Jan 10 local
        assertEquals(0, computeDelta(cur, prev, minus8)!!.days)
    }

    @Test fun timeZoneCanCreateDayDifference() {
        val prev = e("a", 70.0, "2024-01-10T20:30:00Z")
        val cur = e("b", 70.0, "2024-01-10T21:30:00Z")
        assertEquals(0, computeDelta(cur, prev, utc)!!.days)
        // +03:00: 23:30 Jan 10 -> 00:30 Jan 11
        assertEquals(1, computeDelta(cur, prev, plus3)!!.days)
    }

    @Test fun acrossMonthAndYearBoundary() {
        val d = computeDelta(e("b", 70.0, "2024-03-01T10:00:00Z"), e("a", 70.0, "2024-02-28T10:00:00Z"), utc)!!
        assertEquals(2, d.days) // leap year
        val y = computeDelta(e("b", 70.0, "2025-01-01T10:00:00Z"), e("a", 70.0, "2024-12-31T10:00:00Z"), utc)!!
        assertEquals(1, y.days)
    }

    @Test fun deltaFormattingSignsAndUnits() {
        assertEquals("+0.40", Delta(0.4, 1).format(WeightUnit.KG))
        assertEquals("-1.20", Delta(-1.2, 1).format(WeightUnit.KG))
        assertEquals("+0.88", Delta(0.4, 1).format(WeightUnit.LB)) // 0.88 lb
        assertEquals("-2.65", Delta(-1.2, 1).format(WeightUnit.LB)) // 2.645 lb
    }
}

class ConversionTest {
    @Test fun kgToLbAndBack() {
        assertEquals(154.32, 70.0.kgTo(WeightUnit.LB))
        assertEquals(70.0, 154.3235.lbToKg().round2())
        assertEquals(220.46, 100.0.kgToLb().round2())
    }

    @Test fun kgToKgRounds() {
        assertEquals(72.46, 72.46.kgTo(WeightUnit.KG))
    }

    @Test fun round2() {
        assertEquals(0.0, (-0.004).round2())
        assertEquals(1.26, 1.26.round2())
    }

    @Test fun formatting() {
        assertEquals("70.00", formatWeight(70.0, WeightUnit.KG))
        assertEquals("72.46", formatWeight(72.46, WeightUnit.KG))
        assertEquals("154.32", formatWeight(70.0, WeightUnit.LB))
        assertEquals("70.00 kg", formatWeightWithUnit(70.0, WeightUnit.KG))
        assertEquals("154.32 lb", formatWeightWithUnit(70.0, WeightUnit.LB))
        assertEquals("-0.40", formatTwoDecimals(-0.4))
        assertEquals("0.00", formatTwoDecimals(0.0))
    }
}

class InputValidationTest {
    private fun kg(text: String, unit: WeightUnit = WeightUnit.KG): Double? =
        (parseWeightInput(text, unit) as? WeightInputResult.Valid)?.kg

    private fun reason(text: String, unit: WeightUnit = WeightUnit.KG) =
        (parseWeightInput(text, unit) as WeightInputResult.Invalid).reason

    @Test fun acceptsDotAndComma() {
        assertEquals(72.5, kg("72.5"))
        assertEquals(72.5, kg("72,5"))
        assertEquals(72.0, kg("72"))
        assertEquals(72.5, kg("  72,5  "))
    }

    @Test fun roundsToTwoDecimals() {
        assertEquals(72.54, kg("72.544"))
        assertEquals(72.55, kg("72.546"))
    }

    @Test fun twoDecimalInputSurvivesSavingAndFormatting() {
        val value = kg("50,75")!!
        val entry = WeightEntry.create(value, Instant.parse("2024-01-01T08:00:00Z"))
        assertEquals(50.75, entry.kg)
        assertEquals("50.75", formatWeight(entry.kg, WeightUnit.KG))
        assertEquals("51.00", formatWeight(51.0, WeightUnit.KG))
    }

    @Test fun twoDecimalPoundsSurviveSavingAndFormatting() {
        val value = kg("150.25", WeightUnit.LB)!!
        val entry = WeightEntry.create(value, Instant.parse("2024-01-01T08:00:00Z"))
        assertEquals("150.25", formatWeight(entry.kg, WeightUnit.LB))
    }

    @Test fun rejectsEmptyAndGarbage() {
        assertEquals(WeightInputResult.Reason.EMPTY, reason(""))
        assertEquals(WeightInputResult.Reason.EMPTY, reason("   "))
        for (s in listOf("abc", "7 2", "1.2.3", "-70", "72kg", ".", "7,2,1")) {
            assertEquals(WeightInputResult.Reason.NOT_A_NUMBER, reason(s), s)
        }
    }

    @Test fun kgRangeBoundaries() {
        assertEquals(20.0, kg("20"))
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("19.9"))
        assertEquals(500.0, kg("500"))
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("500.1"))
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("0"))
    }

    @Test fun lbInputIsConvertedAndChecked() {
        val v = kg("150", WeightUnit.LB)!!
        assertEquals(150.0.lbToKg(), v)
        assertEquals(150.0, v.kgTo(WeightUnit.LB)) // round trip stays exact at display precision
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("43", WeightUnit.LB))
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("1200", WeightUnit.LB))
        assertTrue(kg("154,3", WeightUnit.LB) != null)
    }

    @Test fun lbValueThatIsTooHighInLbButFineInKg() {
        // 150 would be valid in either unit; 600 is invalid kg but valid lb
        assertEquals(WeightInputResult.Reason.OUT_OF_RANGE, reason("600", WeightUnit.KG))
        assertTrue(kg("600", WeightUnit.LB) != null)
    }
}

class HistoryTest {
    private val utc = TimeZone.UTC

    @Test fun emptyHistory() {
        assertEquals(emptyList<HistoryItem>(), buildHistory(emptyList(), utc))
    }

    @Test fun sortedDescendingWithDeltaAgainstNextOlder() {
        val a = e("a", 72.0, "2024-01-01T08:00:00Z")
        val b = e("b", 71.0, "2024-01-03T08:00:00Z")
        val c = e("c", 71.5, "2024-01-07T08:00:00Z")
        val h = buildHistory(listOf(b, c, a), utc)
        assertEquals(listOf("c", "b", "a"), h.map { it.entry.id })
        assertEquals(Delta(0.5, 4), h[0].delta)
        assertEquals(Delta(-1.0, 2), h[1].delta)
        assertNull(h[2].delta)
    }

    @Test fun backdatedEntryRecomputesNeighbours() {
        val a = e("a", 72.0, "2024-01-01T08:00:00Z")
        val c = e("c", 71.5, "2024-01-07T08:00:00Z")
        val mid = e("m", 70.0, "2024-01-04T08:00:00Z") // inserted later, but earlier date
        val h = buildHistory(listOf(a, c, mid), utc)
        assertEquals(listOf("c", "m", "a"), h.map { it.entry.id })
        assertEquals(Delta(1.5, 3), h[0].delta)
        assertEquals(Delta(-2.0, 3), h[1].delta)
    }

    @Test fun singleEntry() {
        val h = buildHistory(listOf(e("a", 70.0, "2024-01-01T08:00:00Z")), utc)
        assertEquals(1, h.size)
        assertNull(h[0].delta)
    }

    @Test fun sameInstantIsStable() {
        val x = e("x", 70.0, "2024-01-01T08:00:00Z")
        val y = e("y", 71.0, "2024-01-01T08:00:00Z")
        assertEquals(buildHistory(listOf(x, y), utc).map { it.entry.id }, buildHistory(listOf(y, x), utc).map { it.entry.id })
    }

    @Test fun createGeneratesDistinctIds() {
        val at = Instant.parse("2024-01-01T08:00:00Z")
        assertTrue(WeightEntry.create(70.0, at).id != WeightEntry.create(70.0, at).id)
    }
}
