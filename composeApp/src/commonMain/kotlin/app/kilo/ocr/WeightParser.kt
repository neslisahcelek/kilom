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
    // Matches any 1-3 digit number with decimal for 7-segment first-digit repair (e.g. 12.4 when actual is 72.4)
    private val repairableDecimalRegex = Regex("(\\d{1,3})[.,](\\d{1,2})")
    // Matches 3-4 digit integers without decimals when dot is dropped on LCD/LED displays (e.g. 491, 689, 5075, 5100)
    private val integerRegex = Regex("(?:^|[^\\d.,:])(\\d{3,4})(?:$|[^\\d.,:])")
    private val tokenRegex = Regex("^([0-9OoQDUGgIlSsBbZzqAT|!\\[\\]/\\\\.,·•]+)(.*)$")
    private val penaltyRegex = Regex("(?i)(bmi|%|fat|water|muscle|bone|kcal|bmr|batt|tare)")
    private val unitRegex = Regex("(?i)\\b(kg|kgs|lb|lbs)\\b|(?:\\d)(kg|kgs|lb|lbs)\\b")
    private val peripheralRegex = Regex("(?i)(?:\\b\\d{1,2}\\s*°[cCfF]\\b|\\bmax\\.?\\s*\\d+\\s*(?:kg|lb)?\\b|\\bd\\s*=\\s*\\d+\\s*(?:g|kg|oz|lb)?\\b)")

    // Classic 7-segment single-digit confusion map (differing by 1-2 segments due to glare/PWM flicker)
    private val sevenSegmentConfusions: Map<Char, List<Char>> = mapOf(
        '0' to listOf('8', '6', '9'),
        '1' to listOf('7', '4'),
        '2' to listOf('7', '3', '8'),
        '3' to listOf('8', '9', '5', '2'),
        '4' to listOf('9', '1', '8'),
        '5' to listOf('6', '8', '9', '3'),
        '6' to listOf('8', '5', '0'),
        '7' to listOf('1', '2', '0', '3'),
        '8' to listOf('0', '6', '9', '3', '5', '2'),
        '9' to listOf('8', '5', '3', '0', '4'),
    )

    private data class Candidate(val value: Double, val score: Double, val order: Int)

    /**
     * Picks the most likely weight from OCR [lines]. Unlabeled values are assumed to be
     * in [preferred]. Returns null if no plausible (20–500 kg) candidate exists.
     *
     * When [referenceKg] (the user's latest recorded weight in kg) is provided, candidates
     * close to [referenceKg] receive a plausibility bonus and single-digit 7-segment misreads
     * on the leading digit (e.g. reading `22.4` or `12.4` instead of `72.4`) are repaired.
     */
    fun pick(
        lines: List<String>,
        preferred: WeightUnit,
        referenceKg: Double? = null,
    ): ParsedWeight? {
        val norm = lines.map { normalize(it) }
        val rawCandidates = mutableListOf<Candidate>()
        var orderCounter = 0

        for ((lineIndex, rawLine) in norm.withIndex()) {
            // Strip known peripheral specifications like ambient temperature (15°C) and capacity (Max 180kg)
            val line = peripheralRegex.replace(rawLine, " ")
            val hasPenalty = penaltyRegex.containsMatchIn(line)
            val penalty = if (hasPenalty) 5.0 else 0.0

            val nextLine = norm.getOrNull(lineIndex + 1)?.let { peripheralRegex.replace(it, " ") }.orEmpty()
            val hasUnitBonus = unitRegex.containsMatchIn(line) || unitRegex.containsMatchIn(nextLine.trim())
            val unitBonus = if (hasUnitBonus && !hasPenalty) 0.35 else 0.0

            // 1. Explicit decimal candidates (highest priority: base score 3.0)
            for (m in candidateRegex.findAll(line)) {
                val before = line.getOrNull(m.range.first - 1)
                val after = line.getOrNull(m.range.last + 1)
                if (before != null && (before.isDigit() || before == '.' || before == ',' || before == ':')) continue
                if (after != null && (after.isDigit() || after == ':')) continue

                val rawValue = (m.groupValues[1] + "." + m.groupValues[2]).toDouble()
                rawCandidates += Candidate(rawValue.round2(), 3.0 + unitBonus - penalty, orderCounter++)
            }

            // 2. Implicit decimal candidates when dot was dropped by OCR on 7-segment/LED displays (base score 1.5)
            for (m in integerRegex.findAll(line)) {
                val digits = m.groupValues[1]
                val implicitValue = recoverImplicitDecimal(digits, preferred)
                if (implicitValue != null) {
                    rawCandidates += Candidate(implicitValue, 1.5 + unitBonus - penalty, orderCounter++)
                }
            }

            // 3. Single-digit 7-segment leading-digit confusion repair when referenceKg is known
            if (referenceKg != null && referenceKg in MIN_KG..MAX_KG && !hasPenalty) {
                for (m in repairableDecimalRegex.findAll(line)) {
                    val before = line.getOrNull(m.range.first - 1)
                    val after = line.getOrNull(m.range.last + 1)
                    if (before != null && (before.isDigit() || before == '.' || before == ',' || before == ':')) continue
                    if (after != null && (after.isDigit() || after == ':')) continue

                    val intPart = m.groupValues[1]
                    val decPart = m.groupValues[2]
                    val originalVal = "$intPart.$decPart".toDoubleOrNull()?.round2() ?: continue
                    val originalKg = if (preferred == WeightUnit.KG) originalVal else originalVal.lbToKg()

                    // Only attempt leading-digit repair if the raw reading is implausibly far (> 9 kg) from referenceKg
                    if (kotlin.math.abs(originalKg - referenceKg) > 9.0) {
                        for (repairedVal in generateLeadingDigitRepairs(intPart, decPart)) {
                            val repairedKg = if (preferred == WeightUnit.KG) repairedVal else repairedVal.lbToKg()
                            if (repairedKg in MIN_KG..MAX_KG && kotlin.math.abs(repairedKg - referenceKg) <= 2.0) {
                                rawCandidates += Candidate(repairedVal, 2.6 + unitBonus, orderCounter++)
                            }
                        }
                    }
                }
            }
        }

        val validCandidates = rawCandidates.filter { cand ->
            val kg = if (preferred == WeightUnit.KG) cand.value else cand.value.lbToKg()
            kg in MIN_KG..MAX_KG
        }
        if (validCandidates.isEmpty()) return null

        // Count how many times each rounded 0.1 value appears across passes (multi-pass consensus)
        val frequencyByTenths = mutableMapOf<Int, Int>()
        for (cand in validCandidates) {
            val key = kotlin.math.round(cand.value * 10.0).toInt()
            frequencyByTenths[key] = (frequencyByTenths[key] ?: 0) + 1
        }
        val maxFrequency = frequencyByTenths.values.maxOrNull() ?: 1

        var best: ParsedWeight? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for (cand in validCandidates) {
            val value = cand.value
            val kg = if (preferred == WeightUnit.KG) value else value.lbToKg()

            // Only apply consensus bonus if at least one candidate appeared multiple times across passes
            val key = kotlin.math.round(value * 10.0).toInt()
            val freq = frequencyByTenths[key] ?: 1
            val consensusBonus = if (maxFrequency > 1 && freq > 1) {
                minOf(2.4, (freq - 1) * 0.8)
            } else {
                0.0
            }

            val referenceBonus = if (referenceKg != null && referenceKg in MIN_KG..MAX_KG) {
                val diffKg = kotlin.math.abs(kg - referenceKg)
                when {
                    diffKg <= 2.0 -> 4.0
                    diffKg <= 5.0 -> 3.0
                    diffKg <= 10.0 -> 2.0
                    diffKg <= 20.0 -> 1.0
                    diffKg > 35.0 -> -1.5
                    else -> 0.0
                }
            } else {
                0.0
            }

            val totalScore = cand.score + consensusBonus + referenceBonus
            if (totalScore > bestScore) {
                bestScore = totalScore
                best = ParsedWeight(value, preferred)
            }
        }
        return best
    }

    private fun generateLeadingDigitRepairs(intPart: String, decPart: String): List<Double> {
        if (intPart.isEmpty()) return emptyList()
        val results = mutableListOf<Double>()
        val firstChar = intPart[0]
        val rest = intPart.substring(1)
        val replacements = sevenSegmentConfusions[firstChar].orEmpty()
        for (rep in replacements) {
            val candidateStr = "$rep$rest.$decPart"
            candidateStr.toDoubleOrNull()?.round2()?.let { results += it }
        }
        // Also handle dropped leading '1' on 3-digit weights (e.g. "20.5" when referenceKg is 120.5)
        if (intPart.length == 2) {
            "1$intPart.$decPart".toDoubleOrNull()?.round2()?.let { results += it }
        }
        return results
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
        var cleaned = line.replace(Regex("(?:^|[\\s(\\[])[-~–—]+\\s*([0-9OoQDUGgIlSsBbZzqAT])"), " $1")
        // Collapse spaces around decimal separators (including middle dots · and •) between digits/digit-substitutes
        cleaned = Regex("([0-9OoQDUGgIlSsBbZzqAT]+)\\s*[.,·•]\\s*([0-9OoQDUGgIlSsBbZzqAT]+)").replace(cleaned) {
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
                        'O', 'o', 'Q', 'D', 'U' -> '0'
                        'I', 'l', '|', '!', '[', ']', '/', '\\' -> '1'
                        'Z', 'z' -> '2'
                        'A' -> '4'
                        'S', 's' -> '5'
                        'b', 'G' -> '6'
                        'T' -> '7'
                        'B' -> '8'
                        'q', 'g' -> '9'
                        '·', '•' -> '.'
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
