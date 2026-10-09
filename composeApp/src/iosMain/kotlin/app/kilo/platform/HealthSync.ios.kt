package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.kilo.domain.WeightEntry
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.HealthKit.HKHealthStore
import platform.HealthKit.HKQuantity
import platform.HealthKit.HKQuantitySample
import platform.HealthKit.HKQuantityType
import platform.HealthKit.HKQuantityTypeIdentifierBodyMass
import platform.HealthKit.HKUnit
import kotlin.coroutines.resume

/**
 * iOS implementation of [HealthSync] leveraging Apple HealthKit.
 */
@OptIn(ExperimentalForeignApi::class)
class IosHealthSync : HealthSync {
    private val healthStore: HKHealthStore? = if (HKHealthStore.isHealthDataAvailable()) HKHealthStore() else null
    private val bodyMassType: HKQuantityType? = HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierBodyMass)

    override val isSupported: Boolean
        get() = healthStore != null && bodyMassType != null

    override suspend fun requestAuthorization(): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        return suspendCancellableCoroutine { cont ->
            val shareTypes = setOf(type)
            store.requestAuthorizationToShareTypes(
                typesToShare = shareTypes,
                readTypes = null,
            ) { success, error ->
                if (cont.isActive) {
                    cont.resume(success && error == null)
                }
            }
        }
    }

    override suspend fun writeWeight(kg: Double, timestamp: Instant): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        val unit = HKUnit.unitFromString("kg")
        val quantity = HKQuantity.quantityWithUnit(unit = unit, doubleValue = kg)
        val date = timestamp.toNSDate()
        val sample = HKQuantitySample.quantitySampleWithType(
            quantityType = type,
            quantity = quantity,
            startDate = date,
            endDate = date,
        )

        return suspendCancellableCoroutine { cont ->
            store.saveObject(sample) { success, error ->
                if (cont.isActive) {
                    cont.resume(success && error == null)
                }
            }
        }
    }

    override suspend fun writeWeights(entries: List<WeightEntry>): Int {
        val store = healthStore ?: return 0
        val type = bodyMassType ?: return 0
        if (entries.isEmpty()) return 0

        val unit = HKUnit.unitFromString("kg")
        val samples = entries.map { entry ->
            val quantity = HKQuantity.quantityWithUnit(unit = unit, doubleValue = entry.kg)
            val date = entry.at.toNSDate()
            HKQuantitySample.quantitySampleWithType(
                quantityType = type,
                quantity = quantity,
                startDate = date,
                endDate = date,
            )
        }

        return suspendCancellableCoroutine { cont ->
            store.saveObjects(samples) { success, error ->
                if (cont.isActive) {
                    cont.resume(if (success && error == null) samples.size else 0)
                }
            }
        }
    }

    private fun Instant.toNSDate(): NSDate {
        val seconds = epochSeconds.toDouble() + (nanosecondsOfSecond.toDouble() / 1_000_000_000.0)
        return NSDate.dateWithTimeIntervalSince1970(seconds)
    }
}

@Composable
actual fun rememberHealthSync(): HealthSync = remember { IosHealthSync() }
