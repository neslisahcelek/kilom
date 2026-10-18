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
import platform.CoreGraphics.CGAffineTransformMakeScale
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
import kotlin.time.TimeSource

actual class ScaleOcr {
    private val ciContext = CIContext(options = null)

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun readText(image: ByteArray): List<String> = withContext(Dispatchers.Default) {
        if (image.isEmpty()) {
            logOcr("ios empty_image")
            return@withContext emptyList()
        }
        val totalStart = TimeSource.Monotonic.markNow()
        logOcr("ios vision_started bytes=${image.size}")
        val data = image.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = image.size.toULong())
        }

        // Tier 1: Fast direct pass honoring EXIF orientation
        val t1Start = TimeSource.Monotonic.markNow()
        val t1Lines = runVisionOnData(data)
        val t1Ms = t1Start.elapsedNow().inWholeMilliseconds
        val t1Strong = hasStrongExplicitConsensus(t1Lines)
        logOcr("ios pass=tier1_direct ms=$t1Ms candidates=${t1Lines.size} strong=$t1Strong")
        if (t1Strong) {
            logOcr("ios vision_completed tier=tier1_direct_strong total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
            return@withContext t1Lines
        }

        val rawCiImage = CIImage.imageWithData(data, options = mapOf(kCIImageApplyOrientationProperty to true))
            ?: return@withContext t1Lines

        // Downscale to max dimension 2000 px for fast processing and minimal memory footprint
        val largestDim = rawCiImage.extent.useContents { maxOf(size.width, size.height) }
        val baseImage = if (largestDim > 2000.0) {
            val scale = 2000.0 / largestDim
            rawCiImage.imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))
        } else {
            rawCiImage
        }

        val ensembleLines = mutableListOf<String>()
        ensembleLines.addAll(t1Lines)

        suspend fun runPass(name: String, block: suspend () -> List<String>): List<String> {
            coroutineContext.ensureActive()
            val start = TimeSource.Monotonic.markNow()
            val lines = block()
            val ms = start.elapsedNow().inWholeMilliseconds
            val valid = hasValidWeight(lines)
            logOcr("ios pass=$name ms=$ms candidates=${lines.size} valid=$valid")
            return lines
        }

        // Tier 2: Upright Multi-Pass Ensemble (LCD Contrast + LED Invert + Direct 7-Segment)
        // Running these together prevents an early false-positive (e.g. "22.4" instead of "72.4") from short-circuiting the true reading.
        val enhancedLcd = baseImage.imageByApplyingFilter(
            "CIColorControls",
            withInputParameters = mapOf("inputContrast" to 2.4, "inputSaturation" to 0.0)
        )
        ensembleLines += runPass("tier2_lcd_contrast") { runVisionOnCiImage(enhancedLcd) }

        val ledThresholded = baseImage
            .imageByApplyingFilter("CIColorControls", withInputParameters = mapOf("inputContrast" to 2.6, "inputBrightness" to -0.18, "inputSaturation" to 0.0))
            .imageByApplyingFilter("CIColorInvert")
        ensembleLines += runPass("tier2_led_contrast") { runVisionOnCiImage(ledThresholded) }

        ensembleLines += runPass("tier2_direct_7seg") { readSevenSegmentDirect(baseImage, ciContext) }

        if (hasExplicitDecimalWeight(ensembleLines)) {
            logOcr("ios vision_completed tier=tier2_ensemble total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
            return@withContext ensembleLines
        }

        // Tier 2B: Pure invert + Morphological passes for disjointed LED / faint LCD segments
        val inverted = baseImage.imageByApplyingFilter("CIColorInvert")
        ensembleLines += runPass("tier2_invert") { runVisionOnCiImage(inverted) }

        val ledMorphed = baseImage
            .imageByApplyingFilter("CIColorControls", withInputParameters = mapOf("inputContrast" to 2.8, "inputBrightness" to -0.12, "inputSaturation" to 0.0))
            .imageByApplyingFilter("CIMorphologyMaximum", withInputParameters = mapOf("inputRadius" to 3.0))
            .imageByApplyingFilter("CIColorInvert")
        ensembleLines += runPass("tier2_led_morphology") { runVisionOnCiImage(ledMorphed) }

        val lcdMorphed = enhancedLcd
            .imageByApplyingFilter("CIColorInvert")
            .imageByApplyingFilter("CIMorphologyMaximum", withInputParameters = mapOf("inputRadius" to 2.0))
            .imageByApplyingFilter("CIColorInvert")
        ensembleLines += runPass("tier2_lcd_morphology") { runVisionOnCiImage(lcdMorphed) }

        if (hasExplicitDecimalWeight(ensembleLines)) {
            logOcr("ios vision_completed tier=tier2_morphology total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
            return@withContext ensembleLines
        }

        // Tier 3: Perspective-corrected LCD screen rectangle detection & SevenSegment fallback
        ensembleLines += runPass("tier3_screen_rectangles") { readDigitalScale(baseImage, ciContext) }
        if (hasValidWeight(ensembleLines)) {
            logOcr("ios vision_completed tier=tier3_screen_rectangles total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
            return@withContext ensembleLines
        }

        // Tier 4: Multi-orientation pass (180° upside-down, then 90° & 270°)
        val orientations = listOf(3 to "180", 6 to "90_cw", 8 to "270_cw")
        for ((orient, label) in orientations) {
            val rotated = baseImage.imageByApplyingOrientation(orient)
            val rotLines = runPass("tier4_rot_$label") { runVisionOnCiImage(rotated) }
            if (hasValidWeight(rotLines)) {
                logOcr("ios vision_completed tier=tier4_rot_$label total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
                return@withContext rotLines + ensembleLines
            }
            if (orient == 3) {
                val lcd180 = runPass("tier4_lcd_180") { runVisionOnCiImage(enhancedLcd.imageByApplyingOrientation(3)) }
                if (hasValidWeight(lcd180)) return@withContext lcd180 + ensembleLines
                val inv180 = runPass("tier4_invert_180") { runVisionOnCiImage(inverted.imageByApplyingOrientation(3)) }
                if (hasValidWeight(inv180)) return@withContext inv180 + ensembleLines
                val led180 = runPass("tier4_led_morph_180") { runVisionOnCiImage(ledMorphed.imageByApplyingOrientation(3)) }
                if (hasValidWeight(led180)) return@withContext led180 + ensembleLines
            }
        }

        // Tier 5: Tilted angle passes (±25°, ±35°)
        val angles = listOf(-25.0, 25.0, -35.0, 35.0)
        for (deg in angles) {
            val rad = deg * PI / 180.0
            val tilted = baseImage.imageByApplyingTransform(CGAffineTransformMakeRotation(rad))
            val tiltLines = runPass("tier5_tilt_$deg") { runVisionOnCiImage(tilted) }
            if (hasValidWeight(tiltLines)) {
                logOcr("ios vision_completed tier=tier5_tilt_$deg total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
                return@withContext tiltLines + ensembleLines
            }
        }

        logOcr("ios vision_exhausted total_ms=${totalStart.elapsedNow().inWholeMilliseconds}")
        ensembleLines
    }

    private val explicitDecimalCheckRegex = Regex("\\b\\d{2,3}[.,]\\d{1,2}\\b")

    private fun hasExplicitDecimalWeight(lines: List<String>): Boolean =
        lines.any { explicitDecimalCheckRegex.containsMatchIn(WeightParser.normalize(it)) } && hasValidWeight(lines)

    private fun hasStrongExplicitConsensus(lines: List<String>): Boolean {
        if (!hasExplicitDecimalWeight(lines)) return false
        val matches = lines.mapNotNull { line ->
            val norm = WeightParser.normalize(line)
            explicitDecimalCheckRegex.find(norm)?.value?.replace(',', '.')?.toDoubleOrNull()
        }
        if (matches.size < 2) return false
        val counts = matches.groupingBy { (it * 10).toInt() }.eachCount()
        return (counts.values.maxOrNull() ?: 0) >= 2
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
        val cgImage = ciContext.createCGImage(image, fromRect = image.extent) ?: return emptyList()
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
        request.minimumTextHeight = 0.01f
        request.revision = 3u
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
