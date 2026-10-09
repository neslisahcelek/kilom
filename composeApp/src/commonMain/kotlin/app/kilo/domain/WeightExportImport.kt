package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.math.abs

/**
 * Versioned envelope for JSON weight exports.
 */
@Serializable
data class ExportEnvelope(
    val version: Int = 1,
    val exportedAtMillis: Long,
    val entries: List<ExportEntry>,
)

/**
 * Individual entry representation in JSON export/import.
 */
@Serializable
data class ExportEntry(
    val id: String? = null,
    val kg: Double,
    val atMillis: Long,
    val tag: String? = null,
)

private val exportJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    coerceInputValues = true
}

private const val CSV_HEADER = "date,time,weight_kg,tag"

/**
 * Exports weight entries into standard CSV format.
 * Header: date,time,weight_kg,tag
 * Formats date (yyyy-MM-dd), time (HH:mm:ss), weight (2 decimals), and optional tag.
 */
fun exportToCsv(entries: List<WeightEntry>, tz: TimeZone): String {
    val rows = entries.map { entry ->
        val dt = entry.at.toLocalDateTime(tz)
        val dateStr = dt.date.toString()
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        val second = dt.second.toString().padStart(2, '0')
        val timeStr = "$hour:$minute:$second"
        val weightStr = formatTwoDecimals(entry.kg)
        val tagStr = entry.tag?.let { escapeCsv(it) } ?: ""
        "$dateStr,$timeStr,$weightStr,$tagStr"
    }
    return (listOf(CSV_HEADER) + rows).joinToString("\n")
}

/**
 * Imports weight entries from CSV text.
 * Safely ignores header, malformed lines, out-of-range weights (20.0..500.0 kg).
 */
fun importFromCsv(csvText: String, tz: TimeZone): List<WeightEntry> {
    if (csvText.isBlank()) return emptyList()

    val lines = csvText.lines()
    val result = mutableListOf<WeightEntry>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue

        val tokens = parseCsvLine(trimmed)
        if (tokens.size < 3) continue

        // Skip header lines
        val firstCol = tokens[0].lowercase()
        if (firstCol == "date" || firstCol.startsWith("date")) continue

        val date = try {
            LocalDate.parse(tokens[0])
        } catch (_: Exception) {
            continue
        }

        val timeParts = tokens[1].split(':')
        if (timeParts.size < 2) continue
        val hour = timeParts[0].toIntOrNull() ?: continue
        val minute = timeParts[1].toIntOrNull() ?: continue
        val second = if (timeParts.size >= 3) timeParts[2].toIntOrNull() ?: continue else 0
        if (hour !in 0..23 || minute !in 0..59 || second !in 0..59) continue
        val localTime = LocalTime(hour, minute, second)

        val instant = try {
            date.atTime(localTime).toInstant(tz)
        } catch (_: Exception) {
            continue
        }

        val weightStr = tokens[2].replace(',', '.')
        val weight = weightStr.toDoubleOrNull() ?: continue
        if (weight < MIN_KG || weight > MAX_KG || weight.isNaN() || weight.isInfinite()) {
            continue
        }

        val tag = tokens.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() }

        result.add(
            WeightEntry.create(
                kg = weight.round2(),
                at = instant,
                tag = tag,
            )
        )
    }

    return result
}

/**
 * Exports weight entries into a versioned JSON envelope.
 */
fun exportToJson(
    entries: List<WeightEntry>,
    exportedAtMillis: Long = entries.firstOrNull()?.at?.toEpochMilliseconds() ?: 0L,
): String {
    val envelope = ExportEnvelope(
        version = 1,
        exportedAtMillis = exportedAtMillis,
        entries = entries.map {
            ExportEntry(
                id = it.id,
                kg = it.kg.round2(),
                atMillis = it.at.toEpochMilliseconds(),
                tag = it.tag,
            )
        }
    )
    return exportJson.encodeToString(ExportEnvelope.serializer(), envelope)
}

/**
 * Imports weight entries from JSON text.
 * Safely decodes envelope (or raw array fallback), validates weight range (20..500 kg),
 * and handles missing tags and unknown fields gracefully.
 */
fun importFromJson(jsonText: String): List<WeightEntry> {
    if (jsonText.isBlank()) return emptyList()

    val rawEntries: List<ExportEntry> = try {
        exportJson.decodeFromString(ExportEnvelope.serializer(), jsonText).entries
    } catch (_: Exception) {
        try {
            exportJson.decodeFromString(ListSerializer(ExportEntry.serializer()), jsonText)
        } catch (_: Exception) {
            return emptyList()
        }
    }

    val result = mutableListOf<WeightEntry>()
    for (entry in rawEntries) {
        if (entry.kg < MIN_KG || entry.kg > MAX_KG || entry.kg.isNaN() || entry.kg.isInfinite()) {
            continue
        }
        val at = try {
            Instant.fromEpochMilliseconds(entry.atMillis)
        } catch (_: Exception) {
            continue
        }
        val id = entry.id?.takeIf { it.isNotBlank() } ?: WeightEntry.create(entry.kg, at, entry.tag).id
        val tag = entry.tag?.trim()?.takeIf { it.isNotEmpty() }
        result.add(
            WeightEntry(
                id = id,
                kg = entry.kg.round2(),
                at = at,
                tag = tag,
            )
        )
    }

    return result
}

/**
 * Merges existing and imported weight entries.
 * Deduplicates by ID or timestamp (matching entries within 1000ms),
 * updates existing entries when matched, and returns the list sorted descending by `at`.
 */
fun mergeEntries(existing: List<WeightEntry>, imported: List<WeightEntry>): List<WeightEntry> {
    val result = mutableListOf<WeightEntry>()

    for (entry in existing) {
        val matchIndex = result.indexOfFirst {
            it.id == entry.id || abs(it.at.toEpochMilliseconds() - entry.at.toEpochMilliseconds()) <= 1000L
        }
        if (matchIndex >= 0) {
            val current = result[matchIndex]
            result[matchIndex] = current.copy(
                kg = entry.kg,
                tag = entry.tag ?: current.tag,
            )
        } else {
            result.add(entry)
        }
    }

    for (entry in imported) {
        val matchIndex = result.indexOfFirst {
            it.id == entry.id || abs(it.at.toEpochMilliseconds() - entry.at.toEpochMilliseconds()) <= 1000L
        }
        if (matchIndex >= 0) {
            val current = result[matchIndex]
            result[matchIndex] = current.copy(
                kg = entry.kg,
                tag = entry.tag ?: current.tag,
            )
        } else {
            result.add(entry)
        }
    }

    return result.sortedWith(compareByDescending<WeightEntry> { it.at }.thenByDescending { it.id })
}

private fun escapeCsv(value: String): String {
    return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}

private fun parseCsvLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        if (c == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                sb.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (c == ',' && !inQuotes) {
            tokens.add(sb.toString().trim())
            sb.clear()
        } else {
            sb.append(c)
        }
        i++
    }
    if (inQuotes) return emptyList()
    tokens.add(sb.toString().trim())
    return tokens
}
