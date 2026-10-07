package app.kilo.ocr

import app.kilo.domain.WeightUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeightParserTest {
    private fun pick(vararg lines: String, unit: WeightUnit = WeightUnit.KG) =
        WeightParser.pick(lines.toList(), unit)

    @Test fun simpleValue() = assertEquals(ParsedWeight(72.5, WeightUnit.KG), pick("72.5"))

    @Test fun commaDecimal() = assertEquals(ParsedWeight(72.5, WeightUnit.KG), pick("Weight", "72,5 kg"))

    @Test fun attachedLabel() = assertEquals(ParsedWeight(70.5, WeightUnit.KG), pick("70.5kg"))

    @Test fun upperCaseLabel() = assertEquals(ParsedWeight(70.5, WeightUnit.KG), pick("70.5 KG"))

    @Test fun poundsLabelOverridesPreferred() =
        assertEquals(ParsedWeight(154.3, WeightUnit.LB), pick("154.3 lb", unit = WeightUnit.KG))

    @Test fun lbsLabel() = assertEquals(ParsedWeight(154.3, WeightUnit.LB), pick("154.3 lbs"))

    @Test fun labelOnNextLine() = assertEquals(ParsedWeight(70.5, WeightUnit.KG), pick("70.5", "kg"))

    @Test fun lbLabelOnNextLine() = assertEquals(ParsedWeight(165.2, WeightUnit.LB), pick("165.2", "lb"))

    @Test fun letterOFixedToZero() = assertEquals(ParsedWeight(70.5, WeightUnit.KG), pick("7O.5", "kg"))

    @Test fun lowerLFixedToOne() = assertEquals(ParsedWeight(165.2, WeightUnit.LB), pick("l65.2", "lb"))

    @Test fun letterOAndLWithAttachedLabel() = assertEquals(ParsedWeight(101.0, WeightUnit.KG), pick("lO1.0kg"))

    @Test fun unlabeledUsesPreferredUnit() {
        assertEquals(ParsedWeight(165.4, WeightUnit.LB), pick("165.4", unit = WeightUnit.LB))
        assertEquals(ParsedWeight(165.4, WeightUnit.KG), pick("165.4", unit = WeightUnit.KG))
    }

    @Test fun noiseIsIgnored() {
        assertEquals(
            ParsedWeight(71.8, WeightUnit.KG),
            pick("12:30", "2024.01.15", "BMI 22.5", "71.8 kg", "Body fat 18.2%"),
        )
    }

    @Test fun labeledBeatsUnlabeled() =
        assertEquals(ParsedWeight(70.2, WeightUnit.KG), pick("88.8", "70.2 kg"))

    @Test fun outOfRangeKgDiscarded() {
        assertNull(pick("15.5 kg"))
        assertNull(pick("888.8"))
    }

    @Test fun outOfRangeLbDiscarded() = assertNull(pick("30.0 lb"))

    @Test fun validLbWouldBeInvalidKgIsKeptWithLbLabel() =
        assertEquals(ParsedWeight(300.5, WeightUnit.LB), pick("300.5 lb"))

    @Test fun tooManyDigitsRejected() = assertNull(pick("1234.5"))

    @Test fun noDecimalIgnored() = assertNull(pick("72 kg"))

    @Test fun emptyAndGarbage() {
        assertNull(pick())
        assertNull(pick(""))
        assertNull(pick("hello world", "---", "|||"))
    }

    @Test fun firstWinsOnTie() = assertEquals(ParsedWeight(70.1, WeightUnit.KG), pick("70.1", "69.9"))
}
