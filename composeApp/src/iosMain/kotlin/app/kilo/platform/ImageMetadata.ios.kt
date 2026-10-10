package app.kilo.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.datetime.Instant
import platform.CoreImage.CIImage
import platform.Foundation.NSData
import platform.Foundation.create
import platform.ImageIO.kCGImagePropertyExifDateTimeOriginal
import platform.ImageIO.kCGImagePropertyExifDictionary
import platform.ImageIO.kCGImagePropertyTIFFDateTime
import platform.ImageIO.kCGImagePropertyTIFFDictionary

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun extractImageCaptureDate(bytes: ByteArray): Instant? {
    if (bytes.isEmpty()) return null
    return try {
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        val ciImage = CIImage.imageWithData(data) ?: return null
        val properties = ciImage.properties ?: return null

        val exif = (properties[kCGImagePropertyExifDictionary] ?: properties["{Exif}"]) as? Map<*, *>
        val exifDate = (exif?.get(kCGImagePropertyExifDateTimeOriginal) ?: exif?.get("DateTimeOriginal")) as? String

        val tiff = (properties[kCGImagePropertyTIFFDictionary] ?: properties["{TIFF}"]) as? Map<*, *>
        val tiffDate = (tiff?.get(kCGImagePropertyTIFFDateTime) ?: tiff?.get("DateTime")) as? String

        val dateStr = exifDate?.takeIf { it.isNotBlank() } ?: tiffDate?.takeIf { it.isNotBlank() }
        ExifDateParser.parse(dateStr)
    } catch (_: Throwable) {
        null
    }
}
