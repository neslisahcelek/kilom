package app.kilo.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeightExportImportTest {
    private val utc = TimeZone.UTC

    @Test
    fun csvExportProducesExpectedHeaderAndFormat() {
        val entry1 = WeightEntry(
            id = "1",
            kg = 75.0,
            at = Instant.parse("2024-01-10T08:05:09Z"),
            tag = "morning_fasted",
        )
        val entry2 = WeightEntry(
            id = "2",
            kg = 74.25,
            at = Instant.parse("2024-01-09T18:30:00Z"),
            tag = null,
        )

        val csv = exportToCsv(listOf(entry1, entry2), utc)
        val lines = csv.lines()

        assertEquals("date,time,weight_kg,tag", lines[0])
        assertEquals("2024-01-10,08:05:09,75.00,morning_fasted", lines[1])
        assertEquals("2024-01-09,18:30:00,74.25,", lines[2])
    }

    @Test
    fun csvExportEmptyListReturnsOnlyHeader() {
        val csv = exportToCsv(emptyList(), utc)
        assertEquals("date,time,weight_kg,tag", csv.trim())
    }

    @Test
    fun csvRoundTripWithMultipleEntries() {
        val original = listOf(
            WeightEntry("1", 82.5, Instant.parse("2024-03-01T07:15:30Z"), "morning_fasted"),
            WeightEntry("2", 81.8, Instant.parse("2024-03-02T09:00:00Z"), null),
            WeightEntry("3", 83.0, Instant.parse("2024-03-03T21:45:12Z"), "post_workout"),
        )

        val csv = exportToCsv(original, utc)
        val imported = importFromCsv(csv, utc)

        assertEquals(original.size, imported.size)
        for (i in original.indices) {
            assertEquals(original[i].kg, imported[i].kg)
            assertEquals(original[i].at, imported[i].at)
            assertEquals(original[i].tag, imported[i].tag)
            assertTrue(imported[i].id.isNotBlank())
        }
    }

    @Test
    fun csvImportBlankOrEmptyReturnsEmptyList() {
        assertTrue(importFromCsv("", utc).isEmpty())
        assertTrue(importFromCsv("   \n\n  ", utc).isEmpty())
    }

    @Test
    fun csvImportSkipsCorruptLinesSafely() {
        val csvContent = """
            date,time,weight_kg,tag
            corrupt_header_without_commas
            2024-01-10,08:00:00,70.0,good_entry_1
            2024-99-99,08:00:00,70.0,bad_date
            2024-01-11,99:99:99,70.0,bad_time
            2024-01-12,08:00:00,not_a_weight,bad_weight
            2024-01-13,only_two_columns
            2024-01-14,08:00:00,71.50,good_entry_2
            "unclosed_quote,08:00:00,70.0
        """.trimIndent()

        val imported = importFromCsv(csvContent, utc)
        assertEquals(2, imported.size)
        assertEquals("good_entry_1", imported[0].tag)
        assertEquals(70.0, imported[0].kg)
        assertEquals("good_entry_2", imported[1].tag)
        assertEquals(71.5, imported[1].kg)
    }

    @Test
    fun csvImportValidatesWeightRange() {
        val csvContent = """
            date,time,weight_kg,tag
            2024-01-10,08:00:00,19.9,too_light
            2024-01-10,08:00:01,20.0,min_boundary
            2024-01-10,08:00:02,75.0,normal
            2024-01-10,08:00:03,500.0,max_boundary
            2024-01-10,08:00:04,500.1,too_heavy
            2024-01-10,08:00:05,-10.0,negative
        """.trimIndent()

        val imported = importFromCsv(csvContent, utc)
        assertEquals(3, imported.size)
        assertEquals(20.0, imported[0].kg)
        assertEquals(75.0, imported[1].kg)
        assertEquals(500.0, imported[2].kg)
    }

    @Test
    fun csvImportHandlesSpecialFormats() {
        val csvContent = """
            date,time,weight_kg,tag
            2024-01-10 , 08:30:00 , 75.50 , morning_fasted 
            2024-01-11,09:15,"74,20","tag with comma, and quotes"
            2024-01-12,8:05,73.0,short_time
        """.trimIndent()

        val imported = importFromCsv(csvContent, utc)
        assertEquals(3, imported.size)

        assertEquals(75.5, imported[0].kg)
        assertEquals("morning_fasted", imported[0].tag)

        assertEquals(74.2, imported[1].kg)
        assertEquals("tag with comma, and quotes", imported[1].tag)

        assertEquals(73.0, imported[2].kg)
        assertEquals(Instant.parse("2024-01-12T08:05:00Z"), imported[2].at)
    }

    @Test
    fun jsonExportProducesValidVersionedEnvelope() {
        val entry = WeightEntry("e1", 72.4, Instant.parse("2024-05-01T10:00:00Z"), "fasted")
        val json = exportToJson(listOf(entry), exportedAtMillis = 1714557600000L)

        assertTrue(json.contains("\"version\": 1"))
        assertTrue(json.contains("\"exportedAtMillis\": 1714557600000"))
        assertTrue(json.contains("\"kg\": 72.4"))
        assertTrue(json.contains("\"tag\": \"fasted\""))
    }

    @Test
    fun jsonRoundTripWithMultipleEntries() {
        val original = listOf(
            WeightEntry("id-1", 65.5, Instant.parse("2024-02-14T06:30:00Z"), "morning_fasted"),
            WeightEntry("id-2", 66.0, Instant.parse("2024-02-15T07:00:00Z"), null),
        )

        val json = exportToJson(original)
        val imported = importFromJson(json)

        assertEquals(original.size, imported.size)
        for (i in original.indices) {
            assertEquals(original[i].id, imported[i].id)
            assertEquals(original[i].kg, imported[i].kg)
            assertEquals(original[i].at, imported[i].at)
            assertEquals(original[i].tag, imported[i].tag)
        }
    }

    @Test
    fun jsonImportSafelyHandlesCorruptAndBlank() {
        assertTrue(importFromJson("").isEmpty())
        assertTrue(importFromJson("   ").isEmpty())
        assertTrue(importFromJson("{ not valid json }").isEmpty())
        assertTrue(importFromJson("[]").isEmpty())
    }

    @Test
    fun jsonImportIgnoresUnknownFieldsAndMissingTags() {
        val jsonWithUnknownFields = """
            {
                "version": 1,
                "exportedAtMillis": 1700000000000,
                "extraField": "kilom_backup",
                "entries": [
                    {
                        "id": "known_id",
                        "kg": 80.0,
                        "atMillis": 1700000000000,
                        "tag": null,
                        "device": "scale_ble",
                        "battery": 95
                    },
                    {
                        "kg": 81.5,
                        "atMillis": 1700086400000
                    }
                ]
            }
        """.trimIndent()

        val imported = importFromJson(jsonWithUnknownFields)
        assertEquals(2, imported.size)

        assertEquals("known_id", imported[0].id)
        assertEquals(80.0, imported[0].kg)
        assertNull(imported[0].tag)

        assertTrue(imported[1].id.isNotBlank())
        assertEquals(81.5, imported[1].kg)
        assertNull(imported[1].tag)
    }

    @Test
    fun jsonImportValidatesWeightRange() {
        val json = """
            {
                "version": 1,
                "exportedAtMillis": 1700000000000,
                "entries": [
                    { "kg": 15.0, "atMillis": 1700000000000 },
                    { "kg": 20.0, "atMillis": 1700000000000 },
                    { "kg": 350.0, "atMillis": 1700000000000 },
                    { "kg": 500.0, "atMillis": 1700000000000 },
                    { "kg": 505.0, "atMillis": 1700000000000 }
                ]
            }
        """.trimIndent()

        val imported = importFromJson(json)
        assertEquals(3, imported.size)
        assertEquals(20.0, imported[0].kg)
        assertEquals(350.0, imported[1].kg)
        assertEquals(500.0, imported[2].kg)
    }

    @Test
    fun jsonImportBareArrayFallback() {
        val jsonArray = """
            [
                { "id": "arr1", "kg": 68.5, "atMillis": 1700000000000, "tag": "post_workout" }
            ]
        """.trimIndent()

        val imported = importFromJson(jsonArray)
        assertEquals(1, imported.size)
        assertEquals("arr1", imported[0].id)
        assertEquals(68.5, imported[0].kg)
        assertEquals("post_workout", imported[0].tag)
    }

    @Test
    fun mergeEntriesWithEmptyLists() {
        val e1 = WeightEntry("1", 70.0, Instant.parse("2024-01-01T08:00:00Z"))

        assertTrue(mergeEntries(emptyList(), emptyList()).isEmpty())
        assertEquals(listOf(e1), mergeEntries(listOf(e1), emptyList()))
        assertEquals(listOf(e1), mergeEntries(emptyList(), listOf(e1)))
    }

    @Test
    fun mergeDeduplicatesByExactId() {
        val existing = listOf(
            WeightEntry("id_1", 70.0, Instant.parse("2024-01-01T08:00:00Z"), "fasted")
        )
        val imported = listOf(
            WeightEntry("id_1", 70.0, Instant.parse("2024-01-01T08:00:00Z"), "fasted")
        )

        val merged = mergeEntries(existing, imported)
        assertEquals(1, merged.size)
        assertEquals("id_1", merged[0].id)
        assertEquals(70.0, merged[0].kg)
        assertEquals("fasted", merged[0].tag)
    }

    @Test
    fun mergeDeduplicatesWithin1000msAndKeepsExistingId() {
        val t0 = Instant.parse("2024-01-01T08:00:00.000Z")
        val tWithin500 = Instant.fromEpochMilliseconds(t0.toEpochMilliseconds() + 500)

        val existing = listOf(
            WeightEntry("existing_id", 72.0, t0, "morning_fasted")
        )
        val imported = listOf(
            WeightEntry("csv_generated_id", 72.0, tWithin500, null)
        )

        val merged = mergeEntries(existing, imported)
        assertEquals(1, merged.size)
        assertEquals("existing_id", merged[0].id)
        assertEquals(t0, merged[0].at)
        assertEquals(72.0, merged[0].kg)
        // Null tag in imported does not overwrite existing tag
        assertEquals("morning_fasted", merged[0].tag)
    }

    @Test
    fun mergeDeduplicatesAtExact1000msBoundary() {
        val t0 = Instant.parse("2024-01-01T08:00:00.000Z")
        val tAt1000 = Instant.fromEpochMilliseconds(t0.toEpochMilliseconds() + 1000)

        val existing = listOf(WeightEntry("e1", 70.0, t0))
        val imported = listOf(WeightEntry("i1", 70.0, tAt1000))

        val merged = mergeEntries(existing, imported)
        assertEquals(1, merged.size)
        assertEquals("e1", merged[0].id)
    }

    @Test
    fun mergePreservesEntriesSeparatedByMoreThan1000ms() {
        val t0 = Instant.parse("2024-01-01T08:00:00.000Z")
        val tAt1001 = Instant.fromEpochMilliseconds(t0.toEpochMilliseconds() + 1001)

        val existing = listOf(WeightEntry("e1", 70.0, t0))
        val imported = listOf(WeightEntry("i1", 70.0, tAt1001))

        val merged = mergeEntries(existing, imported)
        assertEquals(2, merged.size)
        assertEquals(tAt1001, merged[0].at)
        assertEquals(t0, merged[1].at)
    }

    @Test
    fun mergeUpdatesExistingDataWhenImportedHasUpdatedValues() {
        val t0 = Instant.parse("2024-01-01T08:00:00.000Z")
        val existing = listOf(WeightEntry("e1", 70.0, t0, null))
        val imported = listOf(WeightEntry("e1", 71.5, t0, "post_workout"))

        val merged = mergeEntries(existing, imported)
        assertEquals(1, merged.size)
        assertEquals("e1", merged[0].id)
        assertEquals(71.5, merged[0].kg)
        assertEquals("post_workout", merged[0].tag)
    }

    @Test
    fun mergeSelfDeduplicatesImportedList() {
        val t0 = Instant.parse("2024-01-01T08:00:00.000Z")
        val t200 = Instant.fromEpochMilliseconds(t0.toEpochMilliseconds() + 200)

        val imported = listOf(
            WeightEntry("i1", 75.0, t0, "fasted"),
            WeightEntry("i2", 75.0, t200, "fasted")
        )

        val merged = mergeEntries(emptyList(), imported)
        assertEquals(1, merged.size)
        assertEquals("i1", merged[0].id)
    }

    @Test
    fun mergeResultIsAlwaysSortedDescendingByAt() {
        val e1 = WeightEntry("1", 70.0, Instant.parse("2024-01-01T08:00:00Z"))
        val e2 = WeightEntry("2", 71.0, Instant.parse("2024-01-05T08:00:00Z"))
        val e3 = WeightEntry("3", 72.0, Instant.parse("2024-01-03T08:00:00Z"))

        val merged = mergeEntries(listOf(e1), listOf(e2, e3))
        assertEquals(3, merged.size)
        assertEquals("2", merged[0].id)
        assertEquals("3", merged[1].id)
        assertEquals("1", merged[2].id)
    }
}
