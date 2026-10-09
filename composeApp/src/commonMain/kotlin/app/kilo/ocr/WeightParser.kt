package app.kilo.ocr

import app.kilo.domain.MAX_KG
import app.kilo.domain.MIN_KG
import app.kilo.domain.WeightUnit
import app.kilo.domain.lbToKg
import app.kilo.domain.round2

/** [value] is expressed in [unit] (as shown on the scale), 2 decimals. */
data class ParsedWeight(val value: Double, val unit: WeightUnit)

object WeightParser {
    // Matches numbers with 1 or 2 decimals (e.g., 74.5 or 74.50)
    private val candidateRegex = Regex("(\\d{2,3})[.,](\\d{1,2})")
    private val tokenRegex = Regex("^([0-9OoIlSsBbZzDU|!\\[\\].,]+)(.*)$")
    private val penaltyRegex = Regex("(?i)(bmi|%|fat|water|muscle|bone|kcal|bmr|max\\.?|d\\s*=\\s*\\d|°c|°f|temp|celsius|batt|tare)")

    /**
     * Picks the most likely weight from OCR [lines]. Unlabeled values are assumed to be
     * in [preferred]. Returns null if no plausible (20–500 kg) candidate exists.
     * Scale unit labels (kg/lb) are not used to override [preferred] as the user selects
     * the unit prior to scanning.
     */
    fun pick(lines: List<String>, preferred: WeightUnit): ParsedWeight? {
        val norm = lines.map { normalize(it) }
        var best: ParsedWeight? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for ((i, line) in norm.withIndex()) {
            for (m in candidateRegex.findAll(line)) {
                val before = line.getOrNull(m.range.first - 1)
                val after = line.getOrNull(m.range.last + 1)
                if (before != null && (before.isDigit() || before == '.' || before == ',')) continue
                if (after != null && after.isDigit()) continue

                val rawValue = (m.groupValues[1] + "." + m.groupValues[2]).toDouble()
                val value = rawValue.round2()
                var score = 1.0

                if (penaltyRegex.containsMatchIn(line)) score -= 5.0

                val kg = if (preferred == WeightUnit.KG) value else value.lbToKg()
                if (kg < MIN_KG || kg > MAX_KG) continue

                if (score > bestScore) {
                    bestScore = score
                    best = ParsedWeight(value, preferred)
                }
            }
        }
        return best
    }

    /** Fixes 7-segment character misreads and cleans spaced decimals inside numeric tokens. */
    internal fun normalize(line: String): String {
        // Strip leading minus, dash, tilde or tare symbols right before numeric tokens (e.g. "- 49.1" or "-49.1")
        var cleaned = line.replace(Regex("(?:^|[\\s(\\[])[-~–—]+\\s*([0-9OoIlSsBbZzDU])"), " $1")
        // Collapse spaces around decimal separators between digits/digit-substitutes (e.g. "49 . 1" -> "49.1", "50. 20" -> "50.20")
        cleaned = Regex("([0-9OoIlSsBbZzDU]+)\\s*[.,]\\s*([0-9OoIlSsBbZzDU]+)").replace(cleaned) {
            "${it.groupValues[1]}.${it.groupValues[2]}"
        }

        val sb = StringBuilder()
        var i = 0
        // preserve original whitespace; process tokens
        while (i < cleaned.length) {
            if (cleaned[i].isWhitespace()) { sb.append(cleaned[i]); i++; continue }
            var j = i
            while (j < cleaned.length && !cleaned[j].isWhitespace()) j++
            val token = cleaned.substring(i, j)
            val m = tokenRegex.find(token)
            if (m != null && m.groupValues[1].any { it.isDigit() }) {
                val fixed = m.groupValues[1].map {
                    when (it) {
                        'O', 'o', 'D', 'U' -> '0'
                        'I', 'l', '|', '!', '[', ']' -> '1'
                        'Z', 'z' -> '2'
                        'S', 's' -> '5'
                        'b' -> '6'
                        'B' -> '8'
                        'q' -> '9'
                        else -> it
                    }
                }.joinToString("")
                sb.append(fixed).append(m.groupValues[2])
            } else sb.append(token)
            i = j
        }
        return sb.toString()
    }
}
