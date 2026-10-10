package app.kilo.platform

import kotlinx.datetime.Instant

expect fun extractImageCaptureDate(bytes: ByteArray): Instant?
