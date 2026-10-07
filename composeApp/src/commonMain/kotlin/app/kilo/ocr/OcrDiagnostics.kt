package app.kilo.ocr

/** Local diagnostics only. Never include image bytes, recognized text, or weight values. */
internal fun logOcr(message: String) {
    println("[KiloOCR] $message")
}

internal fun logOcrLines(scanId: Long, lines: List<String>) {
    logOcr("scan=$scanId recognition_completed lines=${lines.size}")
    lines.forEachIndexed { index, line ->
        val normalized = WeightParser.normalize(line)
        val decimals = Regex("\\d{2,3}[.,]\\d{1,2}").findAll(normalized).count()
        val hasUnit = Regex("(?i)(kg|lbs?)").containsMatchIn(normalized)
        logOcr(
            "scan=$scanId line=$index chars=${line.length} " +
                "digits=${normalized.count { it.isDigit() }} " +
                "decimal_patterns=$decimals unit_label=$hasUnit",
        )
    }
}
