package app.kilo.ocr

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.ensureActive
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGColorSpaceCreateDeviceGray
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.CoreImage.kCIImageApplyOrientationProperty
import platform.CoreImage.createCGImage
import platform.Foundation.NSData
import platform.Vision.VNDetectRectanglesRequest
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRectangleObservation
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.coroutineContext

/** Bounded, local LCD fallback. Unit recognition and an explicit decimal are both required. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal suspend fun readDigitalScale(data: NSData): List<String> {
    val original = CIImage.imageWithData(data, options = mapOf(kCIImageApplyOrientationProperty to true)) ?: return emptyList()
    val largestDimension = original.extent.useContents { maxOf(size.width, size.height) }
    if (largestDimension <= 0.0) return emptyList()
    val detectionScale = minOf(1.0, 2000.0 / largestDimension)
    val source = original.imageByApplyingTransform(CGAffineTransformMakeScale(detectionScale, detectionScale))
    val context = CIContext(options = null)
    val rectangles = VNDetectRectanglesRequest().apply {
        minimumAspectRatio = 0.2f
        maximumAspectRatio = 0.9f
        minimumSize = 0.05f
        maximumObservations = 3u
    }
    val extent = source.extent
    val sourceImage = context.createCGImage(source, fromRect = extent) ?: return emptyList()
    try {
        if (!VNImageRequestHandler(cGImage = sourceImage, options = emptyMap<Any?, Any?>())
                .performRequests(listOf(rectangles), error = null)) return emptyList()
    } finally {
        CGImageRelease(sourceImage)
    }
    val candidates = (rectangles.results ?: emptyList<Any?>()).filterIsInstance<VNRectangleObservation>()
    logOcr("ios digital_fallback rectangles=${candidates.size}")
    val results = mutableSetOf<String>()
    for ((index, rectangle) in candidates.withIndex()) {
        coroutineContext.ensureActive()
        val imageWidth = extent.useContents { size.width }
        val imageHeight = extent.useContents { size.height }
        val aspect = rectangle.boundingBox.useContents { size.width * imageWidth / (size.height * imageHeight) }
        if (aspect !in 1.5..6.0) continue
        fun vector(point: kotlinx.cinterop.CValue<platform.CoreGraphics.CGPoint>) = point.useContents {
            CIVector(x = x * imageWidth, Y = y * imageHeight)
        }
        val corrected = source.imageByApplyingFilter("CIPerspectiveCorrection", withInputParameters = mapOf(
            "inputTopLeft" to vector(rectangle.topLeft), "inputTopRight" to vector(rectangle.topRight),
            "inputBottomLeft" to vector(rectangle.bottomLeft), "inputBottomRight" to vector(rectangle.bottomRight),
        ))
        val correctedWidth = corrected.extent.useContents { size.width }
        val correctedHeight = corrected.extent.useContents { size.height }
        if (correctedWidth <= 0.0 || correctedHeight <= 0.0) continue
        val scale = minOf(1.0, 1000.0 / maxOf(correctedWidth, correctedHeight))
        val screen = corrected.imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))
        val cgImage = context.createCGImage(screen, fromRect = screen.extent) ?: continue
        try {
            val width = CGImageGetWidth(cgImage).toInt()
            val height = CGImageGetHeight(cgImage).toInt()
            val textRequest = VNRecognizeTextRequest().apply {
                recognitionLevel = VNRequestTextRecognitionLevelAccurate
                usesLanguageCorrection = false
                recognitionLanguages = listOf("en-US")
            }
            if (!VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any?>())
                    .performRequests(listOf(textRequest), error = null)) continue
            val labels = (textRequest.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
                .mapNotNull { observation ->
                    val text = (observation.topCandidates(1u).firstOrNull() as? VNRecognizedText)?.string?.trim()
                    if (text == null || !Regex("(?i)^(kg|lbs?)$").matches(text)) null
                    else text.lowercase() to observation.boundingBox.useContents { origin.x }
                }
            logOcr("ios digital_fallback rectangle=$index unit_labels=${labels.size}")
            if (labels.size != 1 || width !in 80..1200 || height !in 60..1200) continue
            val pixels = ByteArray(width * height)
            val colorSpace = CGColorSpaceCreateDeviceGray() ?: continue
            try {
                pixels.usePinned { pinned ->
                    val bitmap = CGBitmapContextCreate(pinned.addressOf(0), width.toULong(), height.toULong(), 8u,
                        width.toULong(), colorSpace, CGImageAlphaInfo.kCGImageAlphaNone.value) ?: return@usePinned
                    try {
                        CGContextDrawImage(bitmap, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)
                    } finally {
                        CGContextRelease(bitmap)
                    }
                }
            } finally {
                CGColorSpaceRelease(colorSpace)
            }
            coroutineContext.ensureActive()
            val value = SevenSegmentReader.read(pixels, width, height, labels.single().second)
            logOcr("ios digital_fallback rectangle=$index segments_verified=${value != null}")
            if (value != null) results += "$value ${labels.single().first}"
        } finally {
            CGImageRelease(cgImage)
        }
    }
    // Conflicting LCD candidates require manual input, rather than choosing an arbitrary value.
    return results.singleOrNull()?.let { listOf(it) } ?: emptyList()
}
