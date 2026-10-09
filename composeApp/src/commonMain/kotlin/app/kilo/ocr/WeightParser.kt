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
    // Matches 3-4 digit integers without decimals when dot is dropped on LCD/LED displays (e.g. 491, 689, 5075, 5100)
    private val integerRegex = Regex("(?:^|[^\\d.,])(\\d{3,4})(?:$|[^\\d.,])")
    private val tokenRegex = Regex("^([0-9OoIlSsBbZzDU|!\\[\\].,]+)(.*)$")
    private val penaltyRegex = Regex("(?i)(bmi|%|fat|water|muscle|bone|kcal|bmr|batt|tare)")
    private val peripheralRegex = Regex("(?i)(?:\\b\\d{1,2}\\s*°[cCfF]\\b|\\bmax\\.?\\s*\\d+\\s*(?:kg|lb)?\\b|\\bd\\s*=\\s*\\d+\\s*(?:g|kg|oz|lb)?\\b)")

    private data class Candidate(val value: Double, val score: Double)

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

        for (rawLine in norm) {
            // Strip known peripheral specifications like ambient temperature (15°C) and capacity (Max 180kg)
            val line = peripheralRegex.replace(rawLine, " ")
            val hasPenalty = penaltyRegex.containsMatchIn(line)
            val penalty = if (hasPenalty) 5.0 else 0.0

            val candidates = mutableListOf<Candidate>()

            // 1. Explicit decimal candidates (highest priority: base score 3.0)
            for (m in candidateRegex.findAll(line)) {
                val before = line.getOrNull(m.range.first - 1)
                val after = line.getOrNull(m.range.last + 1)
                if (before != null && (before.isDigit() || before == '.' || before == ',')) continue
                if (after != null && after.isDigit()) continue

                val rawValue = (m.groupValues[1] + "." + m.groupValues[2]).toDouble()
                candidates += Candidate(rawValue.round2(), 3.0 - penalty)
            }

            // 2. Implicit decimal candidates when dot was dropped by OCR on 7-segment/LED displays (base score 1.5)
            for (m in integerRegex.findAll(line)) {
                val digits = m.groupValues[1]
                val implicitValue = recoverImplicitDecimal(digits, preferred)
                if (implicitValue != null) {
                    candidates += Candidate(implicitValue, 1.5 - penalty)
                }
            }

            for (cand in candidates) {
                val value = cand.value
                val kg = if (preferred == WeightUnit.KG) value else value.lbToKg()
                if (kg < MIN_KG || kg > MAX_KG) continue

                if (cand.score > bestScore) {
                    bestScore = cand.score
                    best = ParsedWeight(value, preferred)
                }
            }
        }
        return best
    }

    private fun recoverImplicitDecimal(digits: String, preferred: WeightUnit): Double? {
        if (digits.length == 3) {
            // e.g. 491 -> 49.1, 689 -> 68.9
            val v = (digits.take(2) + "." + digits.takeLast(1)).toDouble().round2()
            val kg = if (preferred == WeightUnit.KG) v else v.lbToKg()
            return if (kg in MIN_KG..MAX_KG) v else null
        }
        if (digits.length == 4) {
            val prefix = digits.take(2).toIntOrNull() ?: return null
            if (prefix in 20..99) {
                // e.g. 5075 -> 50.75, 5100 -> 51.00, 6890 -> 68.90
                val v = (digits.take(2) + "." + digits.takeLast(2)).toDouble().round2()
                val kg = if (preferred == WeightUnit.KG) v else v.lbToKg()
                return if (kg in MIN_KG..MAX_KG) v else null
            } else if (prefix in 10..19) {
                // e.g. 1205 -> 120.5, 1452 -> 145.2
                val v = (digits.take(3) + "." + digits.takeLast(1)).toDouble().round2()
                val kg = if (preferred == WeightUnit.KG) v else v.lbToKg()
                return if (kg in MIN_KG..MAX_KG) v else null
            }
        }
        return null
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
