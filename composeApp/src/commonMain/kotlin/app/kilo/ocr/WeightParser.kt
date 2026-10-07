package app.kilo.ocr

import app.kilo.domain.MAX_KG
import app.kilo.domain.MIN_KG
import app.kilo.domain.WeightUnit
import app.kilo.domain.lbToKg
import app.kilo.domain.round1

/** [value] is expressed in [unit] (as shown on the scale), 1 decimal. */
data class ParsedWeight(val value: Double, val unit: WeightUnit)

object WeightParser {
    // Matches numbers with 1 or 2 decimals (e.g., 74.5 or 74.50)
    private val candidateRegex = Regex("(\\d{2,3})[.,](\\d{1,2})")
    private val labelRegex = Regex("(?i)(kg|lbs?)")
    private val tokenRegex = Regex("^([0-9OoIl|.,]+)(.*)$")
    private val penaltyRegex = Regex("(?i)(bmi|%|fat|water|muscle|bone|kcal|bmr)")

    private data class Label(val start: Int, val end: Int, val unit: WeightUnit)

    /**
     * Picks the most likely weight from OCR [lines]. Unlabeled values are assumed to be
     * in [preferred]. Returns null if no plausible (20–500 kg) candidate exists.
     */
    fun pick(lines: List<String>, preferred: WeightUnit): ParsedWeight? {
        val norm = lines.map { normalize(it) }
        val labels = norm.map { findLabels(it) }
        var best: ParsedWeight? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for ((i, line) in norm.withIndex()) {
            for (m in candidateRegex.findAll(line)) {
                val before = line.getOrNull(m.range.first - 1)
                val after = line.getOrNull(m.range.last + 1)
                if (before != null && (before.isDigit() || before == '.' || before == ',')) continue
                if (after != null && after.isDigit()) continue

                val rawValue = (m.groupValues[1] + "." + m.groupValues[2]).toDouble()
                val value = rawValue.round1()
                var score = 1.0
                var unit = preferred

                val here = labels[i]
                val following = here.firstOrNull { it.start >= m.range.last + 1 && line.substring(m.range.last + 1, it.start).isBlank() }
                when {
                    following != null -> { score += 3; unit = following.unit }
                    here.isNotEmpty() -> { score += 2; unit = here.minBy { kotlin.math.abs(it.start - m.range.first) }.unit }
                    else -> {
                        val adj = listOfNotNull(labels.getOrNull(i - 1), labels.getOrNull(i + 1)).flatten()
                        if (adj.isNotEmpty()) { score += 1; unit = adj.first().unit }
                    }
                }
                if (penaltyRegex.containsMatchIn(line)) score -= 2

                val kg = if (unit == WeightUnit.KG) value else value.lbToKg()
                if (kg < MIN_KG || kg > MAX_KG) continue

                if (score > bestScore) {
                    bestScore = score
                    best = ParsedWeight(value, unit)
                }
            }
        }
        return best
    }

    private fun findLabels(line: String): List<Label> = labelRegex.findAll(line).mapNotNull { m ->
        val b = line.getOrNull(m.range.first - 1)
        val a = line.getOrNull(m.range.last + 1)
        if ((b != null && b.isLetter()) || (a != null && a.isLetter())) null
        else Label(m.range.first, m.range.last + 1, if (m.value.startsWith("k", true)) WeightUnit.KG else WeightUnit.LB)
    }.toList()

    /** Fixes O->0, l/I/|->1 inside numeric-looking token prefixes (e.g. "7O.5kg" -> "70.5kg"). */
    internal fun normalize(line: String): String {
        val sb = StringBuilder()
        var i = 0
        // preserve original whitespace; process tokens
        while (i < line.length) {
            if (line[i].isWhitespace()) { sb.append(line[i]); i++; continue }
            var j = i
            while (j < line.length && !line[j].isWhitespace()) j++
            val token = line.substring(i, j)
            val m = tokenRegex.find(token)
            if (m != null && m.groupValues[1].any { it.isDigit() }) {
                val fixed = m.groupValues[1].map {
                    when (it) { 'O', 'o' -> '0'; 'I', 'l', '|' -> '1'; else -> it }
                }.joinToString("")
                sb.append(fixed).append(m.groupValues[2])
            } else sb.append(token)
            i = j
        }
        return sb.toString()
    }
}
