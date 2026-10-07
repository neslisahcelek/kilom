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
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate

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
        val request = VNRecognizeTextRequest()
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        request.usesLanguageCorrection = false
        // VNImageRequestHandler(data:) honors EXIF orientation embedded in the data.
        val handler = VNImageRequestHandler(data = data, options = emptyMap<Any?, Any?>())
        val ok = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            error.value = null
            val success = handler.performRequests(listOf(request), error = error.ptr)
            if (!success) {
                logOcr("ios vision_failed domain=${error.value?.domain} code=${error.value?.code}")
            }
            success
        }
        if (!ok) return@withContext emptyList()
        val observations = (request.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
        logOcr("ios vision_completed observations=${observations.size}")
        // Vision bounding boxes are normalized with origin bottom-left: higher minY == higher on screen.
        val lines = observations
            .sortedByDescending { it.boundingBox.useContents { origin.y + size.height } }
            .mapNotNull { (it.topCandidates(1u).firstOrNull() as? platform.Vision.VNRecognizedText)?.string }
        coroutineContext.ensureActive()
        if (WeightParser.pick(lines, WeightUnit.KG) != null || WeightParser.pick(lines, WeightUnit.LB) != null) {
            return@withContext lines
        }
        val digital = readDigitalScale(data)
        digital + lines
    }
}
