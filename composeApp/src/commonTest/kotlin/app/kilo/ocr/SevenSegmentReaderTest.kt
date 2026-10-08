package app.kilo.ocr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SevenSegmentReaderTest {
    private val width = 600
    private val height = 200

    private fun display(decimal: Boolean = true, damaged: Boolean = false, value: String = "50.75"): ByteArray {
        val pixels = ByteArray(width * height) { 200.toByte() }
        val digitPatterns = listOf("1110111", "0010010", "1011101", "1011011", "0111010", "1101011", "1101111", "1010010", "1111111", "1111011")
        val decimalAfter = value.indexOf('.')
        val patterns = value.filter { it != '.' }.map { digitPatterns[it.digitToInt()] }
        val starts = patterns.indices.map { 40 + it * 75 + if (it >= decimalAfter) 15 else 0 }
        val regions = arrayOf(
            doubleArrayOf(.15, .0, .85, .12), doubleArrayOf(.05, .15, .25, .45),
            doubleArrayOf(.75, .15, .95, .45), doubleArrayOf(.15, .44, .85, .56),
            doubleArrayOf(.05, .58, .25, .88), doubleArrayOf(.75, .58, .95, .88),
            doubleArrayOf(.15, .9, .85, 1.0),
        )
        for ((digit, pattern) in patterns.withIndex()) for ((segment, region) in regions.withIndex()) {
            if (pattern[segment] != '1' || (damaged && digit == 0 && segment == 0)) continue
            for (y in 15 + (region[1] * 160).toInt() until 15 + (region[3] * 160).toInt()) {
                for (x in starts[digit] + (region[0] * 60).toInt() until starts[digit] + (region[2] * 60).toInt()) {
                    pixels[y * width + x] = 35
                }
            }
        }
        if (decimal) for (y in 164..175) for (x in (40 + decimalAfter * 75 - 7)..(40 + decimalAfter * 75 + 4)) pixels[y * width + x] = 35
        return pixels
    }

    @Test fun recognizesExplicitDecimal() =
        assertEquals("50.75", SevenSegmentReader.read(display(), width, height, .95))

    @Test fun preservesLeadingOne() =
        assertEquals("170.75", SevenSegmentReader.read(display(value = "170.75"), width, height, .95))

    @Test fun recognizesOneInsideWeight() =
        assertEquals("51.75", SevenSegmentReader.read(display(value = "51.75"), width, height, .95))

    @Test fun recognizesEveryDigit() {
        for (digit in '0'..'9') {
            val value = "50.${digit}5"
            assertEquals(value, SevenSegmentReader.read(display(value = value), width, height, .95), value)
        }
    }

    @Test fun refusesShortUnknownDigitInsteadOfDroppingIt() {
        val pixels = display(value = "170.75")
        // Shorten only the upper stroke of the leading 1: retain an unresolved candidate.
        for (y in 39 until 68) for (x in 85 until 97) pixels[y * width + x] = 200.toByte()
        assertNull(SevenSegmentReader.read(pixels, width, height, .95))
    }

    @Test fun refusesMissingDecimal() = assertNull(SevenSegmentReader.read(display(false), width, height, .95))

    @Test fun refusesDamagedDigit() = assertNull(SevenSegmentReader.read(display(damaged = true), width, height, .95))

    @Test fun requiresUnitToRightOfWeight() = assertNull(SevenSegmentReader.read(display(), width, height, .4))

    @Test fun refusesBlankScreen() = assertNull(SevenSegmentReader.read(ByteArray(width * height) { 200.toByte() }, width, height, .95))

    @Test fun refusesInvalidBuffer() = assertNull(SevenSegmentReader.read(byteArrayOf(), width, height, .95))

    @Test fun recognizesBrightLedOnDarkBackground() {
        val darkPixels = display(value = "50.75").map { (255 - (it.toInt() and 255)).toByte() }.toByteArray()
        assertEquals("50.75", SevenSegmentReader.read(darkPixels, width, height, .95))
    }
}
