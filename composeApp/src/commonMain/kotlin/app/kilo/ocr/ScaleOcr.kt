package app.kilo.ocr

/** Platform-native text recognition. Returns recognized text lines (top to bottom). */
expect class ScaleOcr {
    suspend fun readText(image: ByteArray): List<String>
}
