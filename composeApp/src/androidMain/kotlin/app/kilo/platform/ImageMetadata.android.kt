package app.kilo.platform

import androidx.exifinterface.media.ExifInterface
import kotlinx.datetime.Instant
import java.io.ByteArrayInputStream

actual fun extractImageCaptureDate(bytes: ByteArray): Instant? {
    if (bytes.isEmpty()) return null
    return try {
        ByteArrayInputStream(bytes).use { stream ->
            val exif = ExifInterface(stream)
            val dateStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.takeIf { it.isNotBlank() }
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            ExifDateParser.parse(dateStr)
        }
    } catch (_: Exception) {
        null
    }
}
