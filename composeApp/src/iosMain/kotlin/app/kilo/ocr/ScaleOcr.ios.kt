package app.kilo.ocr

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import app.kilo.domain.WeightUnit
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGImageRelease
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.kCIImageApplyOrientationProperty
import platform.CoreImage.createCGImage
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.math.PI

actual class ScaleOcr {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun readText(image: ByteArray): List<String> = withContext(Dispatchers.Default) {
        if (image.isEmpty()) {
            logOcr("ios empty_image")
            return@withContext emptyList()
        }
        logOcr("ios vision_started bytes=${image.size}")
        val data = image.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = image.size.toULong())
        }

        // Tier 1: Fast direct pass honoring EXIF orientation
        val t1Lines = runVisionOnData(data)
        coroutineContext.ensureActive()
        if (hasValidWeight(t1Lines)) {
            logOcr("ios vision_success tier=1 lines=${t1Lines.size}")
            return@withContext t1Lines
        }

        val baseImage = CIImage.imageWithData(data, options = mapOf(kCIImageApplyOrientationProperty to true))
            ?: return@withContext t1Lines

        // Tier 2: Multi-orientation pass (180° upside-down, then 90° & 270°)
        val orientations = listOf(3, 6, 8) // 3=180°, 6=90° CW, 8=270° CW
        for (orient in orientations) {
            coroutineContext.ensureActive()
            val rotated = baseImage.imageByApplyingOrientation(orient)
            val rotLines = runVisionOnCiImage(rotated)
            if (hasValidWeight(rotLines)) {
                logOcr("ios vision_success tier=2 orientation=$orient lines=${rotLines.size}")
                return@withContext rotLines + t1Lines
            }
        }

        // Tier 3: CoreImage Preprocessing (Inverted for white-on-black LED, Contrast-boosted for LCD)
        // 3A: Inverted polarity (for scales with white LED on black glass like Foto 4)
        coroutineContext.ensureActive()
        val inverted = baseImage.imageByApplyingFilter("CIColorInvert")
        val invLines = runVisionOnCiImage(inverted)
        if (hasValidWeight(invLines)) {
            logOcr("ios vision_success tier=3_invert lines=${invLines.size}")
            return@withContext invLines + t1Lines
        }

        // Also test inverted upside-down (Foto 4 upside-down)
        val inv180 = inverted.imageByApplyingOrientation(3)
        val inv180Lines = runVisionOnCiImage(inv180)
        if (hasValidWeight(inv180Lines)) {
            logOcr("ios vision_success tier=3_invert_180 lines=${inv180Lines.size}")
            return@withContext inv180Lines + t1Lines
        }

        // 3B: High-contrast LCD + grayscale (for faint 7-segment LCD digits like Foto 1, 2, 3)
        coroutineContext.ensureActive()
        val enhancedLcd = baseImage.imageByApplyingFilter(
            "CIColorControls",
            withInputParameters = mapOf("inputContrast" to 2.2, "inputSaturation" to 0.0)
        )
        val lcdLines = runVisionOnCiImage(enhancedLcd)
        if (hasValidWeight(lcdLines)) {
            logOcr("ios vision_success tier=3_lcd_contrast lines=${lcdLines.size}")
            return@withContext lcdLines + t1Lines
        }

        // Tier 4: Tilted angle passes (±35°, ±25° for photos taken at an angle while standing on scale like Foto 1 & 2)
        val angles = listOf(-35.0, 35.0, -25.0, 25.0)
        for (deg in angles) {
            coroutineContext.ensureActive()
            val rad = deg * PI / 180.0
            val tilted = baseImage.imageByApplyingTransform(CGAffineTransformMakeRotation(rad))
            val tiltLines = runVisionOnCiImage(tilted)
            if (hasValidWeight(tiltLines)) {
                logOcr("ios vision_success tier=4_tilted angle=$deg lines=${tiltLines.size}")
                return@withContext tiltLines + t1Lines
            }
        }

        // Tier 5: Perspective-corrected LCD screen fallback
        coroutineContext.ensureActive()
        val digital = readDigitalScale(data)
        if (hasValidWeight(digital)) {
            logOcr("ios vision_success tier=5_digital lines=${digital.size}")
            return@withContext digital + t1Lines
        }

        t1Lines
    }

    private fun hasValidWeight(lines: List<String>): Boolean =
        WeightParser.pick(lines, WeightUnit.KG) != null || WeightParser.pick(lines, WeightUnit.LB) != null

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun runVisionOnData(data: NSData): List<String> {
        val request = createTextRequest()
        val handler = VNImageRequestHandler(data = data, options = emptyMap<Any?, Any?>())
        return executeRequest(handler, request)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun runVisionOnCiImage(image: CIImage): List<String> {
        val context = CIContext(options = null)
        val cgImage = context.createCGImage(image, fromRect = image.extent) ?: return emptyList()
        return try {
            val request = createTextRequest()
            val handler = VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any?>())
            executeRequest(handler, request)
        } finally {
            CGImageRelease(cgImage)
        }
    }

    private fun createTextRequest(): VNRecognizeTextRequest {
        val request = VNRecognizeTextRequest()
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        request.usesLanguageCorrection = false
        request.recognitionLanguages = listOf("en-US")
        request.customWords = listOf("kg", "KG", "lb", "LB", "lbs", "LBS", "st", "ST")
        return request
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun executeRequest(handler: VNImageRequestHandler, request: VNRecognizeTextRequest): List<String> {
        val ok = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            error.value = null
            val success = handler.performRequests(listOf(request), error = error.ptr)
            if (!success) {
                logOcr("ios vision_failed domain=${error.value?.domain} code=${error.value?.code}")
            }
            success
        }
        if (!ok) return emptyList()
        val observations = (request.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
        val sorted = observations.sortedByDescending { it.boundingBox.useContents { origin.y + size.height } }
        // Include top candidates (up to 5) so alternative interpretations (e.g. 1 vs I, 0 vs O, 5 vs S) are evaluated by WeightParser
        return sorted.flatMap { observation ->
            observation.topCandidates(5u).mapNotNull { (it as? VNRecognizedText)?.string }
        }
    }
}
