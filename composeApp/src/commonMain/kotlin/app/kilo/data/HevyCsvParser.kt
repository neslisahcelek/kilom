package app.kilo.data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round

object HevyCsvParser {

    /**
     * Parses a Hevy workouts CSV export into a list of [HevyWorkoutSession]s,
     * sorted by [HevyWorkoutSession.dateEpochDays] descending (then by start time descending).
     */
    fun parse(
        csvContent: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): List<HevyWorkoutSession> {
        if (csvContent.isBlank()) return emptyList()

        val rows = parseCsvRows(csvContent)
        if (rows.isEmpty()) return emptyList()

        // Detect header
        var headerIndex = -1
        var columnMap = emptyMap<String, Int>()

        for (i in rows.indices) {
            val candidate = rows[i]
            val mapped = candidate.mapIndexed { idx, col ->
                normalizeHeader(col) to idx
            }.toMap()

            if (mapped.containsKey("start_time") || mapped.containsKey("exercise_title") || mapped.containsKey("title")) {
                headerIndex = i
                columnMap = mapped
                break
            }
        }

        val dataRows = if (headerIndex >= 0) {
            rows.subList(headerIndex + 1, rows.size)
        } else {
            rows
        }

        // Builders for grouping sets by workout session
        // Key: canonical session identifier (e.g. parsed start timestamp + title)
        val sessionsMap = LinkedHashMap<String, WorkoutSessionBuilder>()

        for (row in dataRows) {
            if (row.all { it.isBlank() }) continue

            fun getCol(key: String, defaultIdx: Int): String {
                val idx = columnMap[key] ?: defaultIdx
                return row.getOrNull(idx)?.trim().orEmpty()
            }

            val title = getCol("title", 0).ifBlank { "Workout" }
            val startTimeStr = getCol("start_time", 1)
            val endTimeStr = getCol("end_time", 2)
            val exerciseTitle = getCol("exercise_title", 4)
            val setIndexStr = getCol("set_index", 7)
            val setTypeStr = getCol("set_type", 8)
            val weightStr = getCol("weight_kg", 9)
            val repsStr = getCol("reps", 10)
            val durationSecStr = getCol("duration_seconds", 12)
            val rpeStr = getCol("rpe", 13)

            val parsedStart = parseHevyDateTime(startTimeStr, timeZone)
            val parsedEnd = parseHevyDateTime(endTimeStr, timeZone)

            val startEpochMs = parsedStart?.epochMs ?: 0L
            val dateEpochDays = parsedStart?.epochDays ?: (startEpochMs / 86_400_000L)

            // Session grouping key
            val sessionKey = if (startEpochMs > 0L) {
                "${startEpochMs}_$title"
            } else {
                "${startTimeStr}_$title"
            }

            val sessionBuilder = sessionsMap.getOrPut(sessionKey) {
                val endEpochMs = parsedEnd?.epochMs ?: startEpochMs
                val durationMinutes = if (endEpochMs > startEpochMs) {
                    ((endEpochMs - startEpochMs) / 60_000L).toInt()
                } else {
                    0
                }

                // Deterministic ID
                val cleanTitle = title.filter { it.isLetterOrDigit() }.take(20)
                val id = "hevy_${startEpochMs}_${cleanTitle}".ifBlank { "hevy_${startEpochMs}" }

                WorkoutSessionBuilder(
                    id = id,
                    title = title,
                    dateEpochDays = dateEpochDays,
                    startTimeEpochMs = startEpochMs,
                    endTimeEpochMs = endEpochMs,
                    initialDurationMinutes = durationMinutes,
                )
            }

            // Update end time / duration if row has a more accurate end time
            if (parsedEnd != null && parsedEnd.epochMs > sessionBuilder.endTimeEpochMs) {
                sessionBuilder.endTimeEpochMs = parsedEnd.epochMs
                if (sessionBuilder.startTimeEpochMs > 0L) {
                    sessionBuilder.initialDurationMinutes =
                        ((sessionBuilder.endTimeEpochMs - sessionBuilder.startTimeEpochMs) / 60_000L).toInt()
                }
            }

            val durationSec = durationSecStr.toIntOrNull() ?: 0
            sessionBuilder.accumulatedDurationSeconds += durationSec

            // If there's no exercise title, skip adding a set
            if (exerciseTitle.isBlank()) continue

            val weightKg = weightStr.replace(',', '.').toDoubleOrNull() ?: 0.0
            val reps = repsStr.toIntOrNull() ?: 0
            val rpe = rpeStr.replace(',', '.').toDoubleOrNull()
            val setIndex = setIndexStr.toIntOrNull() ?: (sessionBuilder.getExerciseSetCount(exerciseTitle) + 1)
            val setType = setTypeStr.ifBlank { "normal" }

            val set = HevySet(
                index = setIndex,
                type = setType,
                weightKg = weightKg,
                reps = reps,
                rpe = rpe,
            )

            sessionBuilder.addSet(exerciseTitle, set)
        }

        return sessionsMap.values
            .filter { it.hasExercises() }
            .map { it.build() }
            .sortedWith(
                compareByDescending<HevyWorkoutSession> { it.dateEpochDays }
                    .thenByDescending { it.startTimeEpochMs }
            )
    }

    /**
     * Categorizes an exercise based on keywords in its title.
     */
    fun inferMuscleCategory(exerciseTitle: String): MuscleCategory {
        val lower = exerciseTitle.lowercase()
        val words = lower.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }.toSet()

        // 0. Explicit Core exercises containing "leg" or "knee" (e.g. "Hanging Leg Raise")
        if (lower.contains("leg raise") || lower.contains("knee raise")) {
            return MuscleCategory.CORE_FULL_BODY
        }

        // 1. LEGS_GLUTES (Squats, deadlifts, hip thrust, lunges, leg work)
        if (lower.contains("squat") ||
            lower.contains("hip thrust") ||
            lower.contains("deadlift") ||
            lower.contains("rdl") ||
            lower.contains("lunge") ||
            lower.contains("glute") ||
            lower.contains("hamstring") ||
            words.contains("leg") || words.contains("legs") ||
            words.contains("quad") || words.contains("quads") ||
            words.contains("calf") || words.contains("calves") ||
            lower.contains("adductor") || lower.contains("abductor") ||
            lower.contains("step up") || lower.contains("split squat")
        ) {
            return MuscleCategory.LEGS_GLUTES
        }

        // 2. SHOULDERS_ARMS (Shoulders, OHP, lateral raises, biceps, triceps)
        // Evaluated before CHEST_PUSH so overhead press / shoulder press map here
        if (lower.contains("shoulder") ||
            lower.contains("overhead") ||
            lower.contains("military press") ||
            words.contains("ohp") ||
            lower.contains("lateral raise") ||
            lower.contains("front raise") ||
            lower.contains("rear delt") ||
            words.contains("delt") || words.contains("delts") ||
            lower.contains("bicep") ||
            words.contains("curl") || words.contains("curls") ||
            lower.contains("tricep") ||
            lower.contains("skull crusher") || lower.contains("skullcrusher") ||
            words.contains("dip") || words.contains("dips") ||
            words.contains("arm") || words.contains("arms")
        ) {
            return MuscleCategory.SHOULDERS_ARMS
        }

        // 3. BACK_PULL (Rows, pulldowns, pull ups, lats, chin ups)
        if (words.contains("row") || words.contains("rows") ||
            words.contains("pull") || words.contains("pulls") ||
            lower.contains("pulldown") || lower.contains("pull down") ||
            lower.contains("pull up") || lower.contains("pull-up") ||
            lower.contains("chin up") || lower.contains("chin-up") ||
            words.contains("lat") || words.contains("lats") ||
            lower.contains("shrug") || lower.contains("pullover")
        ) {
            return MuscleCategory.BACK_PULL
        }

        // 4. CHEST_PUSH (Bench, chest press, push ups, flies, pecs)
        if (lower.contains("bench") ||
            lower.contains("chest") ||
            lower.contains("push up") || lower.contains("push-up") || lower.contains("pushup") ||
            words.contains("press") || words.contains("push") ||
            lower.contains("fly") || lower.contains("flye") ||
            words.contains("pec") || words.contains("pecs")
        ) {
            return MuscleCategory.CHEST_PUSH
        }

        // 5. CORE_FULL_BODY (Plank, crunch, abs, core, clean, snatch, burpee)
        if (lower.contains("plank") ||
            lower.contains("crunch") ||
            lower.contains("sit up") || lower.contains("sit-up") || lower.contains("situp") ||
            words.contains("ab") || words.contains("abs") ||
            words.contains("core") ||
            lower.contains("clean") ||
            lower.contains("snatch") ||
            lower.contains("burpee") ||
            lower.contains("kettlebell")
        ) {
            return MuscleCategory.CORE_FULL_BODY
        }

        return MuscleCategory.OTHER
    }

    internal fun isWorkingSet(type: String?): Boolean {
        if (type.isNullOrBlank()) return true
        val lower = type.trim().lowercase()
        if (lower.contains("warmup") || lower == "w") return false
        return true
    }

    private class WorkoutSessionBuilder(
        val id: String,
        val title: String,
        val dateEpochDays: Long,
        val startTimeEpochMs: Long,
        var endTimeEpochMs: Long,
        var initialDurationMinutes: Int,
    ) {
        var accumulatedDurationSeconds: Int = 0
        private val exerciseSets = LinkedHashMap<String, MutableList<HevySet>>()

        fun getExerciseSetCount(exerciseTitle: String): Int =
            exerciseSets[exerciseTitle]?.size ?: 0

        fun hasExercises(): Boolean = exerciseSets.isNotEmpty()

        fun addSet(exerciseTitle: String, set: HevySet) {
            exerciseSets.getOrPut(exerciseTitle) { mutableListOf() }.add(set)
        }

        fun build(): HevyWorkoutSession {
            val exercises = exerciseSets.map { (exerciseTitle, sets) ->
                val category = inferMuscleCategory(exerciseTitle)

                // Volume: sum of (weightKg * reps) for normal / drop / working sets
                val workingSets = sets.filter { isWorkingSet(it.type) && it.weightKg > 0.0 && it.reps > 0 }
                val totalVolume = workingSets.sumOf { it.weightKg * it.reps }.round2()

                val maxWeight = sets.filter { it.weightKg > 0.0 }.maxOfOrNull { it.weightKg }?.round2() ?: 0.0

                // Estimated 1RM = maxWeight * (1 + reps/30.0) based on set with maxWeight
                val candidateSets = if (workingSets.isNotEmpty()) workingSets else sets.filter { it.weightKg > 0.0 && it.reps > 0 }
                val bestSetAtMaxWeight = candidateSets.filter { it.weightKg == maxWeight }.maxByOrNull { it.reps }

                val estimated1Rm = if (bestSetAtMaxWeight != null && maxWeight > 0.0 && bestSetAtMaxWeight.reps > 0) {
                    if (bestSetAtMaxWeight.reps <= 1) {
                        maxWeight
                    } else {
                        (maxWeight * (1.0 + bestSetAtMaxWeight.reps / 30.0)).round2()
                    }
                } else {
                    maxWeight
                }

                HevyExercise(
                    title = exerciseTitle,
                    category = category,
                    sets = sets.sortedBy { it.index },
                    totalVolumeKg = totalVolume,
                    maxWeightKg = maxWeight,
                    estimated1RmKg = estimated1Rm,
                )
            }

            val workoutTotalVolume = exercises.sumOf { it.totalVolumeKg }.round2()
            val finalDuration = if (initialDurationMinutes > 0) {
                initialDurationMinutes
            } else if (accumulatedDurationSeconds > 0) {
                accumulatedDurationSeconds / 60
            } else {
                0
            }

            return HevyWorkoutSession(
                id = id,
                title = title,
                dateEpochDays = dateEpochDays,
                startTimeEpochMs = startTimeEpochMs,
                endTimeEpochMs = endTimeEpochMs,
                durationMinutes = finalDuration,
                totalVolumeKg = workoutTotalVolume,
                exercises = exercises,
            )
        }
    }

    private fun normalizeHeader(header: String): String {
        return header.trim()
            .lowercase()
            .replace("\"", "")
            .replace(" ", "_")
            .replace("-", "_")
    }

    /**
     * Pure Kotlin CSV row parser with quote and multiline tolerance.
     */
    private fun parseCsvRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentField = StringBuilder()
        val currentRow = mutableListOf<String>()
        var inQuotes = false
        var i = 0
        val len = csv.length

        while (i < len) {
            val c = csv[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && csv[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        inQuotes = true
                    }
                    ',' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                    }
                    '\r' -> {
                        if (i + 1 < len && csv[i + 1] == '\n') {
                            i++
                        }
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotEmpty() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.clear()
                        if (currentRow.any { it.isNotEmpty() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotEmpty() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }
}

internal data class ParsedDateTime(
    val epochDays: Long,
    val epochMs: Long,
)

/**
 * Parses timestamps in various common formats:
 * - ISO-8601 (e.g. 2024-05-10T17:30:00Z)
 * - yyyy-MM-dd HH:mm:ss or yyyy-MM-dd HH:mm
 * - dd MMM yyyy, HH:mm (e.g. 10 May 2024, 17:30)
 * - Raw epoch millis or seconds
 */
internal fun parseHevyDateTime(raw: String?, timeZone: TimeZone = TimeZone.currentSystemDefault()): ParsedDateTime? {
    if (raw.isNullOrBlank()) return null
    val s = raw.trim()

    // 1. ISO instant parse if contains 'Z' or offset
    if (s.contains('Z') || (s.contains('+') && s.contains(':'))) {
        try {
            val instant = Instant.parse(s)
            val ldt = instant.toLocalDateTime(timeZone)
            return ParsedDateTime(ldt.date.toEpochDays().toLong(), instant.toEpochMilliseconds())
        } catch (_: Exception) {}
    }

    // 2. yyyy-MM-dd HH:mm:ss or yyyy-MM-ddTHH:mm:ss
    val isoMatch = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})(?:[ T](\d{1,2}):(\d{1,2})(?::(\d{1,2}))?(?:\.\d+)?)?(?:\s*(?:Z|[+\-]\d{2}:?\d{2}))?$""").matchEntire(s)
    if (isoMatch != null) {
        try {
            val year = isoMatch.groupValues[1].toInt()
            val month = isoMatch.groupValues[2].toInt()
            val day = isoMatch.groupValues[3].toInt()
            val hour = isoMatch.groupValues[4].toIntOrNull() ?: 0
            val min = isoMatch.groupValues[5].toIntOrNull() ?: 0
            val sec = isoMatch.groupValues[6].toIntOrNull() ?: 0

            val date = LocalDate(year, month, day)
            val ldt = LocalDateTime(year, month, day, hour, min, sec)
            val instant = ldt.toInstant(timeZone)
            return ParsedDateTime(date.toEpochDays().toLong(), instant.toEpochMilliseconds())
        } catch (_: Exception) {}
    }

    // 3. "10 May 2024, 17:30" or "10 May 2024 17:30:00"
    val monthNameMatch = Regex("""^(\d{1,2})\s+([A-Za-z]{3,9})\s+(\d{4})(?:,\s*|\s+)(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?$""").matchEntire(s)
    if (monthNameMatch != null) {
        try {
            val day = monthNameMatch.groupValues[1].toInt()
            val monthStr = monthNameMatch.groupValues[2].lowercase().take(3)
            val year = monthNameMatch.groupValues[3].toInt()
            val hour = monthNameMatch.groupValues[4].toInt()
            val min = monthNameMatch.groupValues[5].toInt()
            val sec = monthNameMatch.groupValues[6].toIntOrNull() ?: 0

            val monthNumber = when (monthStr) {
                "jan" -> 1
                "feb" -> 2
                "mar" -> 3
                "apr" -> 4
                "may" -> 5
                "jun" -> 6
                "jul" -> 7
                "aug" -> 8
                "sep" -> 9
                "oct" -> 10
                "nov" -> 11
                "dec" -> 12
                else -> null
            }
            if (monthNumber != null) {
                val date = LocalDate(year, monthNumber, day)
                val ldt = LocalDateTime(year, monthNumber, day, hour, min, sec)
                val instant = ldt.toInstant(timeZone)
                return ParsedDateTime(date.toEpochDays().toLong(), instant.toEpochMilliseconds())
            }
        } catch (_: Exception) {}
    }

    // 4. Raw epoch millis or seconds
    val numeric = s.toLongOrNull()
    if (numeric != null) {
        val ms = if (numeric > 100_000_000_000L) numeric else numeric * 1000L
        val instant = Instant.fromEpochMilliseconds(ms)
        val ldt = instant.toLocalDateTime(timeZone)
        return ParsedDateTime(ldt.date.toEpochDays().toLong(), ms)
    }

    return null
}

private fun Double.round2(): Double {
    val r = round(this * 100.0) / 100.0
    return if (r == 0.0) 0.0 else r
}
