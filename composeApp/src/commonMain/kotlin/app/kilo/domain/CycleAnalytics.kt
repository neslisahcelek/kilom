package app.kilo.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Four primary phases of the female infradian cycle. */
enum class CyclePhase {
    MENSTRUAL,   // Days 1 to periodDuration
    FOLLICULAR,  // Maturing follicle, rising estrogen
    OVULATION,   // LH surge, peak vitality
    LUTEAL,      // Progesterone dominant, fluid retention, PMS
}

enum class EnergyLevel {
    LOW_REST,
    RISING,
    PEAK,
    DECLINING,
}

/** Evidence-based nutritional and biological guidance for a specific cycle day. */
data class CycleDayAdvice(
    val phase: CyclePhase,
    val titleKey: String,
    val bodySummaryKey: String,
    val hormoneInsightKey: String,
    val waterRetentionRisk: Boolean,
    val energyLevel: EnergyLevel,
    val eatKeys: List<String>,
    val avoidKeys: List<String>,
)

/** Aggregated status of a specific calendar day in relation to the cycle. */
data class CycleDayInfo(
    val date: LocalDate,
    val cycleDay: Int, // 1..cycleLength
    val totalDays: Int,
    val phase: CyclePhase,
    val phaseDay: Int,
    val isToday: Boolean,
    val isPeriodDay: Boolean,
    val advice: CycleDayAdvice,
)

/** Current reactive cycle state for the dashboard. */
data class CycleState(
    val isEnabled: Boolean,
    val lastPeriodDate: LocalDate? = null,
    val cycleLength: Int = 28,
    val periodDuration: Int = 5,
    val currentDayInfo: CycleDayInfo? = null,
    val daysUntilNextPeriod: Int? = null,
    val isWaterRetentionActive: Boolean = false,
)

/**
 * Computes reactive [CycleState] based on stored settings, [now] and [tz].
 * Pure Kotlin, 100% on-device and deterministic.
 */
fun computeCycleState(
    enabled: Boolean,
    lastPeriodDateIso: String?,
    cycleLength: Int,
    periodDuration: Int,
    now: Instant,
    tz: TimeZone,
): CycleState {
    if (!enabled || lastPeriodDateIso.isNullOrBlank()) {
        return CycleState(
            isEnabled = false,
            cycleLength = cycleLength,
            periodDuration = periodDuration,
        )
    }

    val lastPeriod = try {
        LocalDate.parse(lastPeriodDateIso)
    } catch (_: Exception) {
        return CycleState(
            isEnabled = false,
            cycleLength = cycleLength,
            periodDuration = periodDuration,
        )
    }

    val today = now.toLocalDateTime(tz).date
    val todayInfo = computeCycleDayInfo(
        lastPeriodDate = lastPeriod,
        targetDate = today,
        cycleLength = cycleLength,
        periodDuration = periodDuration,
        today = today,
    )

    val currentCycleDay = todayInfo.cycleDay
    val daysUntilNext = (cycleLength - currentCycleDay + 1).coerceAtLeast(0)

    return CycleState(
        isEnabled = true,
        lastPeriodDate = lastPeriod,
        cycleLength = cycleLength,
        periodDuration = periodDuration,
        currentDayInfo = todayInfo,
        daysUntilNextPeriod = daysUntilNext,
        isWaterRetentionActive = todayInfo.advice.waterRetentionRisk,
    )
}

/**
 * Calculates cycle details for any [targetDate] relative to [lastPeriodDate].
 */
fun computeCycleDayInfo(
    lastPeriodDate: LocalDate,
    targetDate: LocalDate,
    cycleLength: Int,
    periodDuration: Int,
    today: LocalDate,
): CycleDayInfo {
    val daysDiff = lastPeriodDate.daysUntil(targetDate)
    // Handle past and future cycles using positive modulo
    val dayInCycle = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1

    val ovulationDay = (cycleLength - 14).coerceAtLeast(periodDuration + 2)
    val ovulationStart = (ovulationDay - 1).coerceAtLeast(periodDuration + 1)
    val ovulationEnd = (ovulationDay + 1).coerceAtMost(cycleLength - 2)

    val (phase, phaseDay) = when {
        dayInCycle <= periodDuration -> {
            CyclePhase.MENSTRUAL to dayInCycle
        }
        dayInCycle < ovulationStart -> {
            CyclePhase.FOLLICULAR to (dayInCycle - periodDuration)
        }
        dayInCycle <= ovulationEnd -> {
            CyclePhase.OVULATION to (dayInCycle - ovulationStart + 1)
        }
        else -> {
            CyclePhase.LUTEAL to (dayInCycle - ovulationEnd)
        }
    }

    val advice = resolveCycleAdvice(
        dayInCycle = dayInCycle,
        cycleLength = cycleLength,
        phase = phase,
        periodDuration = periodDuration,
    )

    return CycleDayInfo(
        date = targetDate,
        cycleDay = dayInCycle,
        totalDays = cycleLength,
        phase = phase,
        phaseDay = phaseDay,
        isToday = (targetDate == today),
        isPeriodDay = (dayInCycle <= periodDuration),
        advice = advice,
    )
}

/**
 * Maps day in cycle to clinical, functional-medicine nutrition and body insights.
 * Referencing ACOG, Dr. Stacy Sims (ROAR), and Cleveland Clinic literature.
 */
private fun resolveCycleAdvice(
    dayInCycle: Int,
    cycleLength: Int,
    phase: CyclePhase,
    periodDuration: Int,
): CycleDayAdvice {
    val daysUntilEnd = cycleLength - dayInCycle

    return when {
        // Menstrual Phase: Days 1-2 (Cramps, Peak Bleeding, Low Energy)
        phase == CyclePhase.MENSTRUAL && dayInCycle <= 2 -> CycleDayAdvice(
            phase = CyclePhase.MENSTRUAL,
            titleKey = "cycle_menstrual_early_title",
            bodySummaryKey = "cycle_menstrual_early_body",
            hormoneInsightKey = "cycle_menstrual_early_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.LOW_REST,
            eatKeys = listOf("cycle_eat_iron_greens", "cycle_eat_bone_broth", "cycle_eat_ginger_tea"),
            avoidKeys = listOf("cycle_avoid_salt", "cycle_avoid_cold_heavy", "cycle_avoid_caffeine"),
        )

        // Menstrual Phase: Days 3 to periodDuration (Cellular renewal, fluid shedding)
        phase == CyclePhase.MENSTRUAL -> CycleDayAdvice(
            phase = CyclePhase.MENSTRUAL,
            titleKey = "cycle_menstrual_late_title",
            bodySummaryKey = "cycle_menstrual_late_body",
            hormoneInsightKey = "cycle_menstrual_late_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.RISING,
            eatKeys = listOf("cycle_eat_zinc_eggs", "cycle_eat_pumpkin_seeds", "cycle_eat_fermented"),
            avoidKeys = listOf("cycle_avoid_processed_meats", "cycle_avoid_excess_sugar"),
        )

        // Follicular Phase: Early (Days 6-9 in standard cycle)
        phase == CyclePhase.FOLLICULAR && dayInCycle <= (periodDuration + 4) -> CycleDayAdvice(
            phase = CyclePhase.FOLLICULAR,
            titleKey = "cycle_follicular_early_title",
            bodySummaryKey = "cycle_follicular_early_body",
            hormoneInsightKey = "cycle_follicular_early_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.RISING,
            eatKeys = listOf("cycle_eat_salmon_protein", "cycle_eat_berries_antioxidants", "cycle_eat_quinoa"),
            avoidKeys = listOf("cycle_avoid_heavy_dairy", "cycle_avoid_fried"),
        )

        // Follicular Phase: Late / Pre-Ovulation (Days 10-13)
        phase == CyclePhase.FOLLICULAR -> CycleDayAdvice(
            phase = CyclePhase.FOLLICULAR,
            titleKey = "cycle_follicular_late_title",
            bodySummaryKey = "cycle_follicular_late_body",
            hormoneInsightKey = "cycle_follicular_late_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.PEAK,
            eatKeys = listOf("cycle_eat_cruciferous_broccoli", "cycle_eat_avocado_fats", "cycle_eat_flaxseed"),
            avoidKeys = listOf("cycle_avoid_refined_carbs", "cycle_avoid_artificial_sweeteners"),
        )

        // Ovulation Phase (Days 14-16)
        phase == CyclePhase.OVULATION -> CycleDayAdvice(
            phase = CyclePhase.OVULATION,
            titleKey = "cycle_ovulation_title",
            bodySummaryKey = "cycle_ovulation_body",
            hormoneInsightKey = "cycle_ovulation_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.PEAK,
            eatKeys = listOf("cycle_eat_leafy_greens", "cycle_eat_artichokes_liver", "cycle_eat_antioxidant_berries"),
            avoidKeys = listOf("cycle_avoid_excess_alcohol", "cycle_avoid_processed_snacks"),
        )

        // Luteal Phase: Early (Days 17-20, Progesterone rising, BMR +150 kcal)
        phase == CyclePhase.LUTEAL && daysUntilEnd > 7 -> CycleDayAdvice(
            phase = CyclePhase.LUTEAL,
            titleKey = "cycle_luteal_early_title",
            bodySummaryKey = "cycle_luteal_early_body",
            hormoneInsightKey = "cycle_luteal_early_hormones",
            waterRetentionRisk = false,
            energyLevel = EnergyLevel.DECLINING,
            eatKeys = listOf("cycle_eat_sweet_potatoes", "cycle_eat_walnuts", "cycle_eat_chickpeas_b6"),
            avoidKeys = listOf("cycle_avoid_starvation_diets", "cycle_avoid_heavy_caffeine"),
        )

        // Luteal Phase: Mid (Days 21-24, Aldosterone elevation, Fluid Retention starts)
        phase == CyclePhase.LUTEAL && daysUntilEnd > 3 -> CycleDayAdvice(
            phase = CyclePhase.LUTEAL,
            titleKey = "cycle_luteal_mid_title",
            bodySummaryKey = "cycle_luteal_mid_body",
            hormoneInsightKey = "cycle_luteal_mid_hormones",
            waterRetentionRisk = true, // ⚠️ Alert: Water retention
            energyLevel = EnergyLevel.DECLINING,
            eatKeys = listOf("cycle_eat_dark_chocolate", "cycle_eat_pumpkin_seeds", "cycle_eat_spinach_magnesium"),
            avoidKeys = listOf("cycle_avoid_high_sodium", "cycle_avoid_salty_snacks", "cycle_avoid_sugar_spikes"),
        )

        // Luteal Phase: Late PMS (Days 25-28, Maximum fluid retention +1.5-2.5 kg, Serotonin drop)
        else -> CycleDayAdvice(
            phase = CyclePhase.LUTEAL,
            titleKey = "cycle_luteal_late_title",
            bodySummaryKey = "cycle_luteal_late_body",
            hormoneInsightKey = "cycle_luteal_late_hormones",
            waterRetentionRisk = true, // ⚠️ Peak water retention
            energyLevel = EnergyLevel.LOW_REST,
            eatKeys = listOf("cycle_eat_tryptophan_banana", "cycle_eat_dandelion_tea", "cycle_eat_warm_lemon_water"),
            avoidKeys = listOf("cycle_avoid_excess_salt", "cycle_avoid_refined_sugar", "cycle_avoid_late_coffee"),
        )
    }
}
