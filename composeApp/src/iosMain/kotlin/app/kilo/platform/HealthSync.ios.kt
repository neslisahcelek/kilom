package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.kilo.domain.WeightEntry
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import platform.Foundation.NSCompoundPredicate
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.numberWithInt
import platform.HealthKit.HKHealthStore
import platform.HealthKit.HKMetadataKeySyncIdentifier
import platform.HealthKit.HKMetadataKeySyncVersion
import platform.HealthKit.HKObjectQueryNoLimit
import platform.HealthKit.HKObjectType
import platform.HealthKit.HKQuantity
import platform.HealthKit.HKQuantitySample
import platform.HealthKit.HKQuantityType
import platform.HealthKit.HKQuantityTypeIdentifierBodyMass
import platform.HealthKit.HKQuery
import platform.HealthKit.HKQueryOptionNone
import platform.HealthKit.HKSampleQuery
import platform.HealthKit.HKSampleSortIdentifierEndDate
import platform.HealthKit.HKSource
import platform.HealthKit.HKUnit
import platform.HealthKit.HKWorkout
import platform.HealthKit.HKWorkoutActivityTypeCoreTraining
import platform.HealthKit.HKWorkoutActivityTypeCrossTraining
import platform.HealthKit.HKWorkoutActivityTypeFunctionalStrengthTraining
import platform.HealthKit.HKWorkoutActivityTypeHighIntensityIntervalTraining
import platform.HealthKit.HKWorkoutActivityTypeTraditionalStrengthTraining
import platform.HealthKit.predicateForObjectsFromSource
import platform.HealthKit.predicateForSamplesWithStartDate
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * iOS implementation of [HealthSync] leveraging Apple HealthKit with deduplication support.
 */
@OptIn(ExperimentalForeignApi::class)
class IosHealthSync : HealthSync {
    private val healthStore: HKHealthStore? = if (HKHealthStore.isHealthDataAvailable()) HKHealthStore() else null
    private val bodyMassType: HKQuantityType? = HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierBodyMass)
    private val workoutType = HKObjectType.workoutType()

    override val isSupported: Boolean
        get() = healthStore != null && bodyMassType != null

    override val provider: HealthProvider
        get() = HealthProvider.APPLE_HEALTH

    override suspend fun requestAuthorization(): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        return suspendCancellableCoroutine { cont ->
            val shareTypes = setOf(type)
            val readTypes = setOf(type, workoutType)
            store.requestAuthorizationToShareTypes(
                typesToShare = shareTypes,
                readTypes = readTypes,
            ) { success, error ->
                if (cont.isActive) {
                    cont.resume(success && error == null)
                }
            }
        }
    }

    private suspend fun deleteSamplesForDate(date: NSDate): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        return suspendCancellableCoroutine { cont ->
            val datePredicate = HKQuery.predicateForSamplesWithStartDate(
                startDate = date,
                endDate = date,
                options = HKQueryOptionNone,
            )
            val sourcePredicate = HKQuery.predicateForObjectsFromSource(HKSource.defaultSource())
            val compoundPredicate = NSCompoundPredicate.andPredicateWithSubpredicates(
                listOf<NSPredicate>(datePredicate, sourcePredicate)
            )
            store.deleteObjectsOfType(
                objectType = type,
                predicate = compoundPredicate,
            ) { success, _, _ ->
                if (cont.isActive) {
                    cont.resume(success)
                }
            }
        }
    }

    private suspend fun deleteAllSamplesFromApp(): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        return suspendCancellableCoroutine { cont ->
            val sourcePredicate = HKQuery.predicateForObjectsFromSource(HKSource.defaultSource())
            store.deleteObjectsOfType(
                objectType = type,
                predicate = sourcePredicate,
            ) { success, _, _ ->
                if (cont.isActive) {
                    cont.resume(success)
                }
            }
        }
    }

    override suspend fun writeWeight(kg: Double, timestamp: Instant, syncId: String?): Boolean {
        val store = healthStore ?: return false
        val type = bodyMassType ?: return false

        val date = timestamp.toNSDate()
        // Deduplication: Remove any previous sample created by this app for this timestamp
        deleteSamplesForDate(date)

        val unit = HKUnit.unitFromString("kg")
        val quantity = HKQuantity.quantityWithUnit(unit = unit, doubleValue = kg)
        val cleanSyncId = syncId ?: "app.kilo.weight_${timestamp.toEpochMilliseconds()}"
        val metadata = mapOf<Any?, Any>(
            HKMetadataKeySyncIdentifier to cleanSyncId,
            HKMetadataKeySyncVersion to NSNumber.numberWithInt(1),
        )
        val sample = HKQuantitySample.quantitySampleWithType(
            quantityType = type,
            quantity = quantity,
            startDate = date,
            endDate = date,
            metadata = metadata,
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

        // Clean up previously exported samples from Kilom to eliminate existing duplicates
        deleteAllSamplesFromApp()

        val unit = HKUnit.unitFromString("kg")
        val uniqueEntries = entries.distinctBy { it.at.toEpochMilliseconds() }
        val samples = uniqueEntries.map { entry ->
            val quantity = HKQuantity.quantityWithUnit(unit = unit, doubleValue = entry.kg)
            val date = entry.at.toNSDate()
            val metadata = mapOf<Any?, Any>(
                HKMetadataKeySyncIdentifier to "app.kilo.weight_${entry.id}",
                HKMetadataKeySyncVersion to NSNumber.numberWithInt(1),
            )
            HKQuantitySample.quantitySampleWithType(
                quantityType = type,
                quantity = quantity,
                startDate = date,
                endDate = date,
                metadata = metadata,
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

    override suspend fun readWeights(since: Instant?): List<HealthWeightSample> {
        val store = healthStore ?: return emptyList()
        val type = bodyMassType ?: return emptyList()

        return suspendCancellableCoroutine { cont ->
            // Exclude samples written by Kilom itself to avoid circular imports.
            val ownSource = HKQuery.predicateForObjectsFromSource(HKSource.defaultSource())
            val notOwn = NSCompoundPredicate.notPredicateWithSubpredicate(ownSource)
            val predicate: NSPredicate = if (since != null) {
                val datePredicate = HKQuery.predicateForSamplesWithStartDate(
                    startDate = since.toNSDate(),
                    endDate = null,
                    options = HKQueryOptionNone,
                )
                NSCompoundPredicate.andPredicateWithSubpredicates(listOf<NSPredicate>(datePredicate, notOwn))
            } else {
                notOwn
            }
            val sort = NSSortDescriptor(key = HKSampleSortIdentifierEndDate, ascending = false)
            val unit = HKUnit.unitFromString("kg")
            val query = HKSampleQuery(
                sampleType = type,
                predicate = predicate,
                limit = HKObjectQueryNoLimit,
                sortDescriptors = listOf(sort),
            ) { _, results, error ->
                val samples = if (error != null) {
                    emptyList()
                } else {
                    results.orEmpty().mapNotNull { item ->
                        val sample = item as? HKQuantitySample ?: return@mapNotNull null
                        val seconds = sample.endDate.timeIntervalSince1970
                        HealthWeightSample(
                            kg = sample.quantity.doubleValueForUnit(unit),
                            at = Instant.fromEpochMilliseconds((seconds * 1000.0).toLong()),
                            sourceId = sample.sourceRevision.source.bundleIdentifier,
                        )
                    }
                }
                if (cont.isActive) {
                    cont.resume(samples)
                }
            }
            store.executeQuery(query)
        }
    }

    override suspend fun readWorkouts(since: Instant?): List<HealthWorkoutSample> {
        val store = healthStore ?: return emptyList()

        return suspendCancellableCoroutine { cont ->
            val predicate: NSPredicate? = if (since != null) {
                HKQuery.predicateForSamplesWithStartDate(
                    startDate = since.toNSDate(),
                    endDate = null,
                    options = HKQueryOptionNone,
                )
            } else {
                null
            }
            val sort = NSSortDescriptor(key = HKSampleSortIdentifierEndDate, ascending = false)
            val query = HKSampleQuery(
                sampleType = workoutType,
                predicate = predicate,
                limit = 250u,
                sortDescriptors = listOf(sort),
            ) { _, results, error ->
                val samples = if (error != null) {
                    emptyList()
                } else {
                    results.orEmpty().mapNotNull { item ->
                        val workout = item as? HKWorkout ?: return@mapNotNull null
                        val bundleId = workout.sourceRevision.source.bundleIdentifier.lowercase()
                        val sourceName = workout.sourceRevision.source.name
                        val activityType = workout.workoutActivityType

                        val isHevySource = bundleId.contains("hevy") || sourceName.lowercase().contains("hevy")
                        val isStrengthActivity = activityType == HKWorkoutActivityTypeTraditionalStrengthTraining ||
                            activityType == HKWorkoutActivityTypeFunctionalStrengthTraining ||
                            activityType == HKWorkoutActivityTypeCrossTraining ||
                            activityType == HKWorkoutActivityTypeCoreTraining ||
                            activityType == HKWorkoutActivityTypeHighIntensityIntervalTraining

                        if (!isHevySource && !isStrengthActivity) return@mapNotNull null

                        val startMs = (workout.startDate.timeIntervalSince1970 * 1000.0).toLong()
                        val endMs = (workout.endDate.timeIntervalSince1970 * 1000.0).toLong()
                        val durationMin = (workout.duration / 60.0).roundToInt().coerceAtLeast(1)

                        val cleanSource = sourceName.trim().ifBlank { "Apple Health" }
                        val title = when {
                            isHevySource -> "Hevy · Apple Health"
                            cleanSource.equals("Apple Health", ignoreCase = true) -> "Apple Health"
                            else -> "$cleanSource · Apple Health"
                        }

                        HealthWorkoutSample(
                            id = "health_workout_$startMs",
                            title = title,
                            startInstant = Instant.fromEpochMilliseconds(startMs),
                            endInstant = Instant.fromEpochMilliseconds(endMs),
                            durationMinutes = durationMin,
                            sourceId = workout.sourceRevision.source.bundleIdentifier,
                        )
                    }
                }
                if (cont.isActive) {
                    cont.resume(samples)
                }
            }
            store.executeQuery(query)
        }
    }

    private fun Instant.toNSDate(): NSDate {
        val seconds = epochSeconds.toDouble() + (nanosecondsOfSecond.toDouble() / 1_000_000_000.0)
        return NSDate.dateWithTimeIntervalSince1970(seconds)
    }
}

@Composable
actual fun rememberHealthSync(): HealthSync = remember { IosHealthSync() }
