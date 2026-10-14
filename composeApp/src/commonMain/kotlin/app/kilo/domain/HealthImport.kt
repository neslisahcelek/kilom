package app.kilo.domain

import app.kilo.platform.HealthWeightSample
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Merges imported health store weight samples into [existing] entries.
 * - Filters samples strictly to the valid 20.0..500.0 kg range.
 * - Rounds weight to 1 decimal place.
 * - Skips samples that match an existing entry within ±60 seconds and ±0.05 kg.
 * - Deduplicates incoming samples amongst themselves within the same window.
 * - Preserves existing entries intact.
 * - Returns the unified list sorted by timestamp descending.
 */
fun mergeHealthSamples(
    existing: List<WeightEntry>,
    samples: List<HealthWeightSample>,
): List<WeightEntry> {
    if (samples.isEmpty()) return existing

    val validSamples = samples.filter { it.kg in MIN_KG..MAX_KG }
    if (validSamples.isEmpty()) return existing

    // Sort samples descending to process newest first
    val sortedSamples = validSamples.sortedByDescending { it.at.epochSeconds }

    val newEntries = mutableListOf<WeightEntry>()

    for (sample in sortedSamples) {
        val roundedKg = (sample.kg * 10.0).roundToInt() / 10.0

        // Check duplicate with existing entries
        val existsInExisting = existing.any { entry ->
            abs(entry.at.epochSeconds - sample.at.epochSeconds) <= 60 &&
                abs(entry.kg - roundedKg) < 0.05
        }
        if (existsInExisting) continue

        // Check duplicate with already accepted new entries
        val existsInNew = newEntries.any { entry ->
            abs(entry.at.epochSeconds - sample.at.epochSeconds) <= 60 &&
                abs(entry.kg - roundedKg) < 0.05
        }
        if (existsInNew) continue

        newEntries.add(WeightEntry.create(roundedKg, sample.at))
    }

    return (existing + newEntries).sortedWith(
        compareByDescending<WeightEntry> { it.at }.thenByDescending { it.id }
    )
}
