package app.kilo.ui.components

import androidx.compose.runtime.Composable
import app.kilo.domain.CyclePhase
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.cycle_avoid_artificial_sweeteners
import kilo.composeapp.generated.resources.cycle_avoid_caffeine
import kilo.composeapp.generated.resources.cycle_avoid_cold_heavy
import kilo.composeapp.generated.resources.cycle_avoid_excess_alcohol
import kilo.composeapp.generated.resources.cycle_avoid_excess_salt
import kilo.composeapp.generated.resources.cycle_avoid_excess_sugar
import kilo.composeapp.generated.resources.cycle_avoid_fried
import kilo.composeapp.generated.resources.cycle_avoid_heavy_caffeine
import kilo.composeapp.generated.resources.cycle_avoid_heavy_dairy
import kilo.composeapp.generated.resources.cycle_avoid_high_sodium
import kilo.composeapp.generated.resources.cycle_avoid_late_coffee
import kilo.composeapp.generated.resources.cycle_avoid_processed_meats
import kilo.composeapp.generated.resources.cycle_avoid_processed_snacks
import kilo.composeapp.generated.resources.cycle_avoid_refined_carbs
import kilo.composeapp.generated.resources.cycle_avoid_refined_sugar
import kilo.composeapp.generated.resources.cycle_avoid_salt
import kilo.composeapp.generated.resources.cycle_avoid_salty_snacks
import kilo.composeapp.generated.resources.cycle_avoid_starvation_diets
import kilo.composeapp.generated.resources.cycle_avoid_sugar_spikes
import kilo.composeapp.generated.resources.cycle_eat_antioxidant_berries
import kilo.composeapp.generated.resources.cycle_eat_artichokes_liver
import kilo.composeapp.generated.resources.cycle_eat_avocado_fats
import kilo.composeapp.generated.resources.cycle_eat_berries_antioxidants
import kilo.composeapp.generated.resources.cycle_eat_bone_broth
import kilo.composeapp.generated.resources.cycle_eat_chickpeas_b6
import kilo.composeapp.generated.resources.cycle_eat_cruciferous_broccoli
import kilo.composeapp.generated.resources.cycle_eat_dandelion_tea
import kilo.composeapp.generated.resources.cycle_eat_dark_chocolate
import kilo.composeapp.generated.resources.cycle_eat_fermented
import kilo.composeapp.generated.resources.cycle_eat_flaxseed
import kilo.composeapp.generated.resources.cycle_eat_ginger_tea
import kilo.composeapp.generated.resources.cycle_eat_iron_greens
import kilo.composeapp.generated.resources.cycle_eat_leafy_greens
import kilo.composeapp.generated.resources.cycle_eat_pumpkin_seeds
import kilo.composeapp.generated.resources.cycle_eat_quinoa
import kilo.composeapp.generated.resources.cycle_eat_salmon_protein
import kilo.composeapp.generated.resources.cycle_eat_spinach_magnesium
import kilo.composeapp.generated.resources.cycle_eat_sweet_potatoes
import kilo.composeapp.generated.resources.cycle_eat_tryptophan_banana
import kilo.composeapp.generated.resources.cycle_eat_walnuts
import kilo.composeapp.generated.resources.cycle_eat_warm_lemon_water
import kilo.composeapp.generated.resources.cycle_eat_zinc_eggs
import kilo.composeapp.generated.resources.cycle_follicular_early_body
import kilo.composeapp.generated.resources.cycle_follicular_early_hormones
import kilo.composeapp.generated.resources.cycle_follicular_early_title
import kilo.composeapp.generated.resources.cycle_follicular_late_body
import kilo.composeapp.generated.resources.cycle_follicular_late_hormones
import kilo.composeapp.generated.resources.cycle_follicular_late_title
import kilo.composeapp.generated.resources.cycle_luteal_early_body
import kilo.composeapp.generated.resources.cycle_luteal_early_hormones
import kilo.composeapp.generated.resources.cycle_luteal_early_title
import kilo.composeapp.generated.resources.cycle_luteal_late_body
import kilo.composeapp.generated.resources.cycle_luteal_late_hormones
import kilo.composeapp.generated.resources.cycle_luteal_late_title
import kilo.composeapp.generated.resources.cycle_luteal_mid_body
import kilo.composeapp.generated.resources.cycle_luteal_mid_hormones
import kilo.composeapp.generated.resources.cycle_luteal_mid_title
import kilo.composeapp.generated.resources.cycle_menstrual_early_body
import kilo.composeapp.generated.resources.cycle_menstrual_early_hormones
import kilo.composeapp.generated.resources.cycle_menstrual_early_title
import kilo.composeapp.generated.resources.cycle_menstrual_late_body
import kilo.composeapp.generated.resources.cycle_menstrual_late_hormones
import kilo.composeapp.generated.resources.cycle_menstrual_late_title
import kilo.composeapp.generated.resources.cycle_ovulation_body
import kilo.composeapp.generated.resources.cycle_ovulation_hormones
import kilo.composeapp.generated.resources.cycle_ovulation_title
import kilo.composeapp.generated.resources.cycle_phase_follicular
import kilo.composeapp.generated.resources.cycle_phase_luteal
import kilo.composeapp.generated.resources.cycle_phase_menstrual
import kilo.composeapp.generated.resources.cycle_phase_ovulation
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun cyclePhaseName(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> stringResource(Res.string.cycle_phase_menstrual)
        CyclePhase.FOLLICULAR -> stringResource(Res.string.cycle_phase_follicular)
        CyclePhase.OVULATION -> stringResource(Res.string.cycle_phase_ovulation)
        CyclePhase.LUTEAL -> stringResource(Res.string.cycle_phase_luteal)
    }
}

internal fun cyclePhaseEmoji(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> "🌸"
        CyclePhase.FOLLICULAR -> "🌱"
        CyclePhase.OVULATION -> "✨"
        CyclePhase.LUTEAL -> "🌙"
    }
}

@Composable
internal fun resolveCycleString(key: String): String {
    return when (key) {
        // Micro-period titles
        "cycle_menstrual_early_title" -> stringResource(Res.string.cycle_menstrual_early_title)
        "cycle_menstrual_late_title" -> stringResource(Res.string.cycle_menstrual_late_title)
        "cycle_follicular_early_title" -> stringResource(Res.string.cycle_follicular_early_title)
        "cycle_follicular_late_title" -> stringResource(Res.string.cycle_follicular_late_title)
        "cycle_ovulation_title" -> stringResource(Res.string.cycle_ovulation_title)
        "cycle_luteal_early_title" -> stringResource(Res.string.cycle_luteal_early_title)
        "cycle_luteal_mid_title" -> stringResource(Res.string.cycle_luteal_mid_title)
        "cycle_luteal_late_title" -> stringResource(Res.string.cycle_luteal_late_title)

        // Micro-period bodies
        "cycle_menstrual_early_body" -> stringResource(Res.string.cycle_menstrual_early_body)
        "cycle_menstrual_late_body" -> stringResource(Res.string.cycle_menstrual_late_body)
        "cycle_follicular_early_body" -> stringResource(Res.string.cycle_follicular_early_body)
        "cycle_follicular_late_body" -> stringResource(Res.string.cycle_follicular_late_body)
        "cycle_ovulation_body" -> stringResource(Res.string.cycle_ovulation_body)
        "cycle_luteal_early_body" -> stringResource(Res.string.cycle_luteal_early_body)
        "cycle_luteal_mid_body" -> stringResource(Res.string.cycle_luteal_mid_body)
        "cycle_luteal_late_body" -> stringResource(Res.string.cycle_luteal_late_body)

        // Micro-period hormones
        "cycle_menstrual_early_hormones" -> stringResource(Res.string.cycle_menstrual_early_hormones)
        "cycle_menstrual_late_hormones" -> stringResource(Res.string.cycle_menstrual_late_hormones)
        "cycle_follicular_early_hormones" -> stringResource(Res.string.cycle_follicular_early_hormones)
        "cycle_follicular_late_hormones" -> stringResource(Res.string.cycle_follicular_late_hormones)
        "cycle_ovulation_hormones" -> stringResource(Res.string.cycle_ovulation_hormones)
        "cycle_luteal_early_hormones" -> stringResource(Res.string.cycle_luteal_early_hormones)
        "cycle_luteal_mid_hormones" -> stringResource(Res.string.cycle_luteal_mid_hormones)
        "cycle_luteal_late_hormones" -> stringResource(Res.string.cycle_luteal_late_hormones)

        // Eat keys
        "cycle_eat_iron_greens" -> stringResource(Res.string.cycle_eat_iron_greens)
        "cycle_eat_bone_broth" -> stringResource(Res.string.cycle_eat_bone_broth)
        "cycle_eat_ginger_tea" -> stringResource(Res.string.cycle_eat_ginger_tea)
        "cycle_eat_zinc_eggs" -> stringResource(Res.string.cycle_eat_zinc_eggs)
        "cycle_eat_pumpkin_seeds" -> stringResource(Res.string.cycle_eat_pumpkin_seeds)
        "cycle_eat_fermented" -> stringResource(Res.string.cycle_eat_fermented)
        "cycle_eat_salmon_protein" -> stringResource(Res.string.cycle_eat_salmon_protein)
        "cycle_eat_berries_antioxidants" -> stringResource(Res.string.cycle_eat_berries_antioxidants)
        "cycle_eat_quinoa" -> stringResource(Res.string.cycle_eat_quinoa)
        "cycle_eat_cruciferous_broccoli" -> stringResource(Res.string.cycle_eat_cruciferous_broccoli)
        "cycle_eat_avocado_fats" -> stringResource(Res.string.cycle_eat_avocado_fats)
        "cycle_eat_flaxseed" -> stringResource(Res.string.cycle_eat_flaxseed)
        "cycle_eat_leafy_greens" -> stringResource(Res.string.cycle_eat_leafy_greens)
        "cycle_eat_artichokes_liver" -> stringResource(Res.string.cycle_eat_artichokes_liver)
        "cycle_eat_antioxidant_berries" -> stringResource(Res.string.cycle_eat_antioxidant_berries)
        "cycle_eat_sweet_potatoes" -> stringResource(Res.string.cycle_eat_sweet_potatoes)
        "cycle_eat_walnuts" -> stringResource(Res.string.cycle_eat_walnuts)
        "cycle_eat_chickpeas_b6" -> stringResource(Res.string.cycle_eat_chickpeas_b6)
        "cycle_eat_dark_chocolate" -> stringResource(Res.string.cycle_eat_dark_chocolate)
        "cycle_eat_spinach_magnesium" -> stringResource(Res.string.cycle_eat_spinach_magnesium)
        "cycle_eat_tryptophan_banana" -> stringResource(Res.string.cycle_eat_tryptophan_banana)
        "cycle_eat_dandelion_tea" -> stringResource(Res.string.cycle_eat_dandelion_tea)
        "cycle_eat_warm_lemon_water" -> stringResource(Res.string.cycle_eat_warm_lemon_water)

        // Avoid keys
        "cycle_avoid_salt" -> stringResource(Res.string.cycle_avoid_salt)
        "cycle_avoid_cold_heavy" -> stringResource(Res.string.cycle_avoid_cold_heavy)
        "cycle_avoid_caffeine" -> stringResource(Res.string.cycle_avoid_caffeine)
        "cycle_avoid_processed_meats" -> stringResource(Res.string.cycle_avoid_processed_meats)
        "cycle_avoid_excess_sugar" -> stringResource(Res.string.cycle_avoid_excess_sugar)
        "cycle_avoid_heavy_dairy" -> stringResource(Res.string.cycle_avoid_heavy_dairy)
        "cycle_avoid_fried" -> stringResource(Res.string.cycle_avoid_fried)
        "cycle_avoid_refined_carbs" -> stringResource(Res.string.cycle_avoid_refined_carbs)
        "cycle_avoid_artificial_sweeteners" -> stringResource(Res.string.cycle_avoid_artificial_sweeteners)
        "cycle_avoid_excess_alcohol" -> stringResource(Res.string.cycle_avoid_excess_alcohol)
        "cycle_avoid_processed_snacks" -> stringResource(Res.string.cycle_avoid_processed_snacks)
        "cycle_avoid_starvation_diets" -> stringResource(Res.string.cycle_avoid_starvation_diets)
        "cycle_avoid_heavy_caffeine" -> stringResource(Res.string.cycle_avoid_heavy_caffeine)
        "cycle_avoid_high_sodium" -> stringResource(Res.string.cycle_avoid_high_sodium)
        "cycle_avoid_salty_snacks" -> stringResource(Res.string.cycle_avoid_salty_snacks)
        "cycle_avoid_sugar_spikes" -> stringResource(Res.string.cycle_avoid_sugar_spikes)
        "cycle_avoid_excess_salt" -> stringResource(Res.string.cycle_avoid_excess_salt)
        "cycle_avoid_refined_sugar" -> stringResource(Res.string.cycle_avoid_refined_sugar)
        "cycle_avoid_late_coffee" -> stringResource(Res.string.cycle_avoid_late_coffee)

        else -> key
    }
}
