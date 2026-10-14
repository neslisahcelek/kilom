package app.kilo.domain

import app.kilo.platform.HealthWeightSample
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthImportTest {

    @Test
    fun emptySamplesReturnsExisting() {
        val existing = listOf(WeightEntry.create(75.0, Instant.fromEpochSeconds(1000)))
        val result = mergeHealthSamples(existing, emptyList())
        assertEquals(existing, result)
    }

    @Test
    fun outOfRangeSamplesAreDiscarded() {
        val existing = emptyList<WeightEntry>()
        val samples = listOf(
            HealthWeightSample(15.0, Instant.fromEpochSeconds(1000), "source"), // < 20 kg
            HealthWeightSample(550.0, Instant.fromEpochSeconds(2000), "source"), // > 500 kg
            HealthWeightSample(72.5, Instant.fromEpochSeconds(3000), "source"), // Valid
        )
        val result = mergeHealthSamples(existing, samples)
        assertEquals(1, result.size)
        assertEquals(72.5, result.first().kg)
    }

    @Test
    fun duplicatesWithExistingAreFiltered() {
        val existing = listOf(
            WeightEntry.create(80.0, Instant.fromEpochSeconds(1000))
        )
        val samples = listOf(
            // Duplicate (same time, same weight)
            HealthWeightSample(80.04, Instant.fromEpochSeconds(1020), "source"),
            // Non-duplicate (different time)
            HealthWeightSample(80.0, Instant.fromEpochSeconds(5000), "source"),
        )
        val result = mergeHealthSamples(existing, samples)
        assertEquals(2, result.size)
        assertEquals(Instant.fromEpochSeconds(5000), result[0].at)
        assertEquals(Instant.fromEpochSeconds(1000), result[1].at)
    }

    @Test
    fun duplicatesAmongSamplesAreFiltered() {
        val samples = listOf(
            HealthWeightSample(70.0, Instant.fromEpochSeconds(1000), "source"),
            HealthWeightSample(70.02, Instant.fromEpochSeconds(1030), "source"), // duplicate within 30s
        )
        val result = mergeHealthSamples(emptyList(), samples)
        assertEquals(1, result.size)
        assertEquals(70.0, result.first().kg)
    }

    @Test
    fun roundsWeightToOneDecimalPlace() {
        val samples = listOf(
            HealthWeightSample(75.367, Instant.fromEpochSeconds(1000), "source")
        )
        val result = mergeHealthSamples(emptyList(), samples)
        assertEquals(75.4, result.first().kg)
    }

    @Test
    fun resultsAreSortedDescendingByTime() {
        val samples = listOf(
            HealthWeightSample(70.0, Instant.fromEpochSeconds(1000), "source"),
            HealthWeightSample(71.0, Instant.fromEpochSeconds(3000), "source"),
            HealthWeightSample(72.0, Instant.fromEpochSeconds(2000), "source"),
        )
        val result = mergeHealthSamples(emptyList(), samples)
        assertEquals(listOf(71.0, 72.0, 70.0), result.map { it.kg })
    }
}
