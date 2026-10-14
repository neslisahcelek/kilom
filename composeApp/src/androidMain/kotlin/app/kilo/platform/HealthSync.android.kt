package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.kilo.domain.WeightEntry
import kotlinx.datetime.Instant

/**
 * Android implementation of [HealthSync].
 * Health Connect integration will gracefully report unsupported until configured.
 */
class AndroidHealthSync : HealthSync {
    override val isSupported: Boolean = false
    override val provider: HealthProvider get() = HealthProvider.HEALTH_CONNECT

    override suspend fun requestAuthorization(): Boolean = false

    override suspend fun writeWeight(kg: Double, timestamp: Instant, syncId: String?): Boolean = false

    override suspend fun writeWeights(entries: List<WeightEntry>): Int = 0

    override suspend fun readWeights(since: Instant?): List<HealthWeightSample> = emptyList()
}

@Composable
actual fun rememberHealthSync(): HealthSync = remember { AndroidHealthSync() }
