package app.kilo.ocr

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate

actual class ScaleOcr {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readText(image: ByteArray): List<String> = withContext(Dispatchers.Default) {
        if (image.isEmpty()) return@withContext emptyList()
        val data = image.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = image.size.toULong())
        }
        val request = VNRecognizeTextRequest()
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        request.usesLanguageCorrection = false
        // VNImageRequestHandler(data:) honors EXIF orientation embedded in the data.
        val handler = VNImageRequestHandler(data = data, options = emptyMap<Any?, Any?>())
        val ok = handler.performRequests(listOf(request), error = null)
        if (!ok) return@withContext emptyList()
        val observations = (request.results ?: emptyList<Any?>()).filterIsInstance<VNRecognizedTextObservation>()
        // Vision bounding boxes are normalized with origin bottom-left: higher minY == higher on screen.
        observations
            .sortedByDescending { it.boundingBox.useContents { origin.y + size.height } }
            .mapNotNull { (it.topCandidates(1u).firstOrNull() as? platform.Vision.VNRecognizedText)?.string }
    }
}

