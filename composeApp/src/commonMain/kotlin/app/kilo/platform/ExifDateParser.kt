package app.kilo.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

object ExifDateParser {
    // Standard EXIF format: "yyyy:MM:dd HH:mm:ss" or ISO style "yyyy-MM-dd HH:mm:ss"
    private val EXIF_DATE_REGEX = Regex("""^(\d{4})[:\-](\d{2})[:\-](\d{2})[ T](\d{2}):(\d{2}):(\d{2})(?:\.\d+)?$""")

    fun parse(
        dateStr: String?,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        now: () -> Instant = {
            Clock.System.now().let { Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond) }
        },
    ): Instant? {
        if (dateStr.isNullOrBlank()) return null
        val match = EXIF_DATE_REGEX.matchEntire(dateStr.trim()) ?: return null
        val (yearStr, monthStr, dayStr, hourStr, minStr, secStr) = match.destructured

        return try {
            val year = yearStr.toInt()
            if (year < 2000) return null

            val month = monthStr.toInt()
            val day = dayStr.toInt()
            val hour = hourStr.toInt()
            val min = minStr.toInt()
            val sec = secStr.toInt()

            val localDateTime = LocalDateTime(year, month, day, hour, min, sec)
            val instant = localDateTime.toInstant(timeZone)

            val currentInstant = now()
            val maxAllowedInstant = currentInstant + 5.minutes
            if (instant > maxAllowedInstant) return null

            instant
        } catch (_: Exception) {
            null
        }
    }
}
