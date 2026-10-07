package app.kilo.domain

sealed interface WeightInputResult {
    /** [kg] is the value to store (kg input rounded to 2 decimals; lb input converted exactly so lb round-trips). */
    data class Valid(val kg: Double) : WeightInputResult
    data class Invalid(val reason: Reason) : WeightInputResult

    enum class Reason { EMPTY, NOT_A_NUMBER, OUT_OF_RANGE }
}

private val numberRegex = Regex("^(\\d+([.]\\d*)?|[.]\\d+)$")

/** Parses user text ("72,5", "72.5", " 154 ") typed in [unit]; validates 20–500 kg. */
fun parseWeightInput(text: String, unit: WeightUnit): WeightInputResult {
    val t = text.trim().replace(',', '.')
    if (t.isEmpty()) return WeightInputResult.Invalid(WeightInputResult.Reason.EMPTY)
    if (!numberRegex.matches(t)) return WeightInputResult.Invalid(WeightInputResult.Reason.NOT_A_NUMBER)
    val v = t.toDoubleOrNull()
        ?: return WeightInputResult.Invalid(WeightInputResult.Reason.NOT_A_NUMBER)
    val entered = v.round2()
    val kg = when (unit) {
        WeightUnit.KG -> entered
        WeightUnit.LB -> entered.lbToKg()
    }
    // compare on the 2-decimal rounded kg to keep the boundary intuitive
    val check = kg.round2()
    if (check !in MIN_KG..MAX_KG) return WeightInputResult.Invalid(WeightInputResult.Reason.OUT_OF_RANGE)
    return WeightInputResult.Valid(kg)
}
