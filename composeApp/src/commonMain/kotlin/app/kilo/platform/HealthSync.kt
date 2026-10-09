package app.kilo.platform

import androidx.compose.runtime.Composable
import app.kilo.domain.WeightEntry
import kotlinx.datetime.Instant

/**
 * Platform health data synchronization interface (Apple Health on iOS, Health Connect on Android).
 * Strictly on-device and opt-in by the user.
 */
interface HealthSync {
    /** True if this platform and device support health tracking integration. */
    val isSupported: Boolean

    /**
     * Requests authorization from the user to write weight samples.
     * Returns true if authorization was successfully requested/granted.
     */
    suspend fun requestAuthorization(): Boolean

    /**
     * Saves a single weight entry (in kg) to the platform health store.
     * Returns true on success.
     */
    suspend fun writeWeight(kg: Double, timestamp: Instant): Boolean

    /**
     * Batch writes weight entries to the platform health store.
     * Returns the count of successfully written entries.
     */
    suspend fun writeWeights(entries: List<WeightEntry>): Int
}

/**
 * No-op implementation used when health sync is unsupported, disabled, or in tests.
 */
object NoOpHealthSync : HealthSync {
    override val isSupported: Boolean = false
    override suspend fun requestAuthorization(): Boolean = false
    override suspend fun writeWeight(kg: Double, timestamp: Instant): Boolean = false
    override suspend fun writeWeights(entries: List<WeightEntry>): Int = 0
}

@Composable
expect fun rememberHealthSync(): HealthSync
