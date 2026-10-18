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

/** Bounded, local LCD fallback. Rectifies screen perspective and runs Vision & SevenSegmentReader. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal suspend fun readDigitalScale(source: CIImage, context: CIContext): List<String> {
    val largestDimension = source.extent.useContents { maxOf(size.width, size.height) }
    if (largestDimension <= 0.0) return emptyList()
    val detectionScale = minOf(1.0, 1500.0 / largestDimension)
    val detectionImage = if (detectionScale < 1.0) {
        source.imageByApplyingTransform(CGAffineTransformMakeScale(detectionScale, detectionScale))
    } else {
        source
    }
    val rectangles = VNDetectRectanglesRequest().apply {
        minimumAspectRatio = 0.1f
        maximumAspectRatio = 1.0f
        minimumSize = 0.02f
        maximumObservations = 10u
    }
    val extent = detectionImage.extent
    val sourceImage = context.createCGImage(detectionImage, fromRect = extent) ?: return emptyList()
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
        if (aspect !in 1.2..7.0) continue
        fun vector(point: kotlinx.cinterop.CValue<platform.CoreGraphics.CGPoint>) = point.useContents {
            CIVector(x = x * imageWidth, Y = y * imageHeight)
        }
        val corrected = detectionImage.imageByApplyingFilter("CIPerspectiveCorrection", withInputParameters = mapOf(
            "inputTopLeft" to vector(rectangle.topLeft), "inputTopRight" to vector(rectangle.topRight),
            "inputBottomLeft" to vector(rectangle.bottomLeft), "inputBottomRight" to vector(rectangle.bottomRight),
        ))
        val correctedWidth = corrected.extent.useContents { size.width }
        val correctedHeight = corrected.extent.useContents { size.height }
        if (correctedWidth <= 0.0 || correctedHeight <= 0.0) continue
        val scale = minOf(1.0, 1000.0 / maxOf(correctedWidth, correctedHeight))
        val screen = corrected.imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))

        // 1. Run Vision text recognition on screen (both raw and contrast-enhanced)
        val textRequest = VNRecognizeTextRequest().apply {
            recognitionLevel = VNRequestTextRecognitionLevelAccurate
            usesLanguageCorrection = false
            recognitionLanguages = listOf("en-US")
            minimumTextHeight = 0.01f
            revision = 3u
        }

        val cgImage = context.createCGImage(screen, fromRect = screen.extent) ?: continue
        try {
            val width = CGImageGetWidth(cgImage).toInt()
            val height = CGImageGetHeight(cgImage).toInt()

            if (VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any?>())
                    .performRequests(listOf(textRequest), error = null)) {
                val obs = (textRequest.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
                for (o in obs) {
                    o.topCandidates(5u).mapNotNull { (it as? VNRecognizedText)?.string?.trim() }.forEach {
                        if (it.isNotEmpty()) results += it
                    }
                }
            }

            // Also test contrast-boosted LCD screen with Vision
            val enhancedScreen = screen.imageByApplyingFilter(
                "CIColorControls",
                withInputParameters = mapOf("inputContrast" to 2.4, "inputSaturation" to 0.0)
            )
            val enhancedCg = context.createCGImage(enhancedScreen, fromRect = enhancedScreen.extent)
            if (enhancedCg != null) {
                try {
                    val enhancedReq = VNRecognizeTextRequest().apply {
                        recognitionLevel = VNRequestTextRecognitionLevelAccurate
                        usesLanguageCorrection = false
                        recognitionLanguages = listOf("en-US")
                        minimumTextHeight = 0.01f
                        revision = 3u
                    }
                    if (VNImageRequestHandler(cGImage = enhancedCg, options = emptyMap<Any?, Any?>())
                            .performRequests(listOf(enhancedReq), error = null)) {
                        val obs = (enhancedReq.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
                        for (o in obs) {
                            o.topCandidates(5u).mapNotNull { (it as? VNRecognizedText)?.string?.trim() }.forEach {
                                if (it.isNotEmpty()) results += it
                            }
                        }
                    }
                } finally {
                    CGImageRelease(enhancedCg)
                }
            }

            // 2. Run SevenSegmentReader fallback on rectified grayscale pixels (unit-label agnostic)
            extractSevenSegmentFromCgImage(cgImage, width, height)?.let { value ->
                logOcr("ios digital_fallback rectangle=$index segments_verified=true")
                results += value
            }
        } finally {
            CGImageRelease(cgImage)
        }
    }

    // 3. Direct SevenSegmentReader pass on the image itself (critical for viewfinder crops where outer LCD bezel is already cropped out)
    readSevenSegmentDirect(source, context).forEach { results += it }

    return results.toList()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal suspend fun readSevenSegmentDirect(source: CIImage, context: CIContext): List<String> {
    val extent = source.extent
    val srcW = extent.useContents { size.width }
    val srcH = extent.useContents { size.height }
    if (srcW <= 0.0 || srcH <= 0.0) return emptyList()

    val found = mutableSetOf<String>()
    val scale = minOf(1.0, 900.0 / maxOf(srcW, srcH))
    val scaled = if (scale < 1.0) {
        source.imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))
    } else {
        source
    }

    // Pass A: Full cropped image
    val fullCg = context.createCGImage(scaled, fromRect = scaled.extent)
    if (fullCg != null) {
        try {
            val w = CGImageGetWidth(fullCg).toInt()
            val h = CGImageGetHeight(fullCg).toInt()
            coroutineContext.ensureActive()
            extractSevenSegmentFromCgImage(fullCg, w, h)?.let { found += it }
        } finally {
            CGImageRelease(fullCg)
        }
    }

    // Pass B: Center horizontal band (if viewfinder crop has extra top/bottom padding around the LCD strip)
    val sExtent = scaled.extent
    val sX = sExtent.useContents { origin.x }
    val sY = sExtent.useContents { origin.y }
    val sW = sExtent.useContents { size.width }
    val sH = sExtent.useContents { size.height }
    if (sH > 120.0 && sW > 120.0) {
        val bandRect = CGRectMake(sX + sW * 0.06, sY + sH * 0.18, sW * 0.88, sH * 0.64)
        val bandCi = scaled.imageByCroppingToRect(bandRect)
        val bandCg = context.createCGImage(bandCi, fromRect = bandCi.extent)
        if (bandCg != null) {
            try {
                val w = CGImageGetWidth(bandCg).toInt()
                val h = CGImageGetHeight(bandCg).toInt()
                coroutineContext.ensureActive()
                extractSevenSegmentFromCgImage(bandCg, w, h)?.let { found += it }
            } finally {
                CGImageRelease(bandCg)
            }
        }
    }

    return found.toList()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun extractSevenSegmentFromCgImage(cgImage: platform.CoreGraphics.CGImageRef, width: Int, height: Int): String? {
    if (width !in 80..1200 || height !in 60..1200) return null
    val pixels = ByteArray(width * height)
    val colorSpace = CGColorSpaceCreateDeviceGray() ?: return null
    return try {
        pixels.usePinned { pinned ->
            val bitmap = CGBitmapContextCreate(
                pinned.addressOf(0),
                width.toULong(),
                height.toULong(),
                8u,
                width.toULong(),
                colorSpace,
                CGImageAlphaInfo.kCGImageAlphaNone.value,
            ) ?: return null
            try {
                CGContextDrawImage(bitmap, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)
            } finally {
                CGContextRelease(bitmap)
            }
        }
        SevenSegmentReader.read(pixels, width, height, 0.95)
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
}
