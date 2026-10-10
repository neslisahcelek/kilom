package app.kilo.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExifDateParserTest {

    private val fixedNow = Instant.parse("2026-10-10T12:00:00Z")
    private val utc = TimeZone.UTC

    @Test
    fun parsesStandardColonSeparatedExifDateTime() {
        val dateStr = "2024:05:15 08:30:45"
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2024-05-15T08:30:45Z"), parsed)
    }

    @Test
    fun parsesStandardDashSeparatedExifDateTime() {
        val dateStr = "2024-05-15 08:30:45"
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2024-05-15T08:30:45Z"), parsed)
    }

    @Test
    fun parsesLeapYearDate() {
        val dateStr = "2024:02:29 23:59:59"
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2024-02-29T23:59:59Z"), parsed)
    }

    @Test
    fun parsesWithSurroundingWhitespace() {
        val dateStr = "   2024:10:01 07:15:00   "
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2024-10-01T07:15:00Z"), parsed)
    }

    @Test
    fun parsesIsoTDelimiter() {
        val dateStr = "2024-10-01T07:15:00"
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2024-10-01T07:15:00Z"), parsed)
    }

    @Test
    fun respectsCustomTimeZone() {
        val istanbulTz = TimeZone.of("Europe/Istanbul")
        val dateStr = "2024:05:15 11:30:00"
        val parsed = ExifDateParser.parse(dateStr, timeZone = istanbulTz, now = { fixedNow })
        assertNotNull(parsed)
        val expected = LocalDateTime(2024, 5, 15, 11, 30, 0).toInstant(istanbulTz)
        assertEquals(expected, parsed)
    }

    @Test
    fun returnsNullForNullOrBlank() {
        assertNull(ExifDateParser.parse(null, timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("   ", timeZone = utc, now = { fixedNow }))
    }

    @Test
    fun returnsNullForMalformedStrings() {
        assertNull(ExifDateParser.parse("invalid-date", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("2024:05:15", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("08:30:45", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("2024/05/15 08:30:45", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("2024:13:01 10:00:00", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("2024:02:30 10:00:00", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("2024:05:15 25:00:00", timeZone = utc, now = { fixedNow }))
    }

    @Test
    fun returnsNullForUnrealisticPastDatesBeforeYear2000() {
        assertNull(ExifDateParser.parse("1999:12:31 23:59:59", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("1970:01:01 00:00:00", timeZone = utc, now = { fixedNow }))
        assertNull(ExifDateParser.parse("1900:01:01 12:00:00", timeZone = utc, now = { fixedNow }))
    }

    @Test
    fun acceptsYear2000OrLater() {
        val dateStr = "2000:01:01 00:00:00"
        val parsed = ExifDateParser.parse(dateStr, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2000-01-01T00:00:00Z"), parsed)
    }

    @Test
    fun allowsDatesUpTo5MinutesInFuture() {
        val validNearFuture = "2026:10:10 12:04:00"
        val parsed = ExifDateParser.parse(validNearFuture, timeZone = utc, now = { fixedNow })
        assertNotNull(parsed)
        assertEquals(Instant.parse("2026-10-10T12:04:00Z"), parsed)
    }

    @Test
    fun rejectsDatesMoreThan5MinutesInFuture() {
        val invalidFuture = "2026:10:10 12:06:00"
        assertNull(ExifDateParser.parse(invalidFuture, timeZone = utc, now = { fixedNow }))

        val farFuture = "2026:10:11 12:00:00"
        assertNull(ExifDateParser.parse(farFuture, timeZone = utc, now = { fixedNow }))
    }
}
