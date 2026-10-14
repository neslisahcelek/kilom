package app.kilo.platform

import androidx.compose.runtime.Composable
import app.kilo.domain.WeightEntry
import kotlinx.datetime.Instant

enum class HealthProvider { APPLE_HEALTH, HEALTH_CONNECT, NONE }

/**
 * Platform health data synchronization interface (Apple Health on iOS, Health Connect on Android).
 * Strictly on-device and opt-in by the user.
 */
interface HealthSync {
    /** True if this platform and device support health tracking integration. */
    val isSupported: Boolean

    /** Identifies the underlying health ecosystem. */
    val provider: HealthProvider get() = HealthProvider.NONE

    /**
     * Requests authorization from the user to write weight samples.
     * Returns true if authorization was successfully requested/granted.
     */
    suspend fun requestAuthorization(): Boolean

    /**
     * Saves a single weight entry (in kg) to the platform health store.
     * [syncId] is an optional unique identifier used for deduplication.
     * Returns true on success.
     */
    suspend fun writeWeight(kg: Double, timestamp: Instant, syncId: String? = null): Boolean

    /**
     * Batch writes weight entries to the platform health store.
     * Automatically deduplicates samples.
     * Returns the count of successfully written entries.
     */
    suspend fun writeWeights(entries: List<WeightEntry>): Int

    /** Reads body-mass samples (kg) from the health store, newest first. Own app samples are excluded. */
    suspend fun readWeights(since: Instant? = null): List<HealthWeightSample>
}

/** A body-mass sample read from the platform health store. Weight is always in kg. */
data class HealthWeightSample(val kg: Double, val at: Instant, val sourceId: String)

/**
 * No-op implementation used when health sync is unsupported, disabled, or in tests.
 */
object NoOpHealthSync : HealthSync {
    override val isSupported: Boolean = false
    override suspend fun requestAuthorization(): Boolean = false
    override suspend fun writeWeight(kg: Double, timestamp: Instant, syncId: String?): Boolean = false
    override suspend fun writeWeights(entries: List<WeightEntry>): Int = 0
    override suspend fun readWeights(since: Instant?): List<HealthWeightSample> = emptyList()
}

@Composable
expect fun rememberHealthSync(): HealthSync
