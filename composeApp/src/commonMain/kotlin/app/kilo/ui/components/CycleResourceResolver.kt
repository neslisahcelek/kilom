package app.kilo.ui.components

import androidx.compose.runtime.Composable
import app.kilo.domain.CyclePhase
import app.kilo.domain.WorkoutIntensity
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.workout_intensity_rest_recovery
import kilo.composeapp.generated.resources.workout_intensity_moderate_steady
import kilo.composeapp.generated.resources.workout_intensity_high_intensity
import kilo.composeapp.generated.resources.workout_intensity_peak_power
import kilo.composeapp.generated.resources.workout_menstrual_early_title
import kilo.composeapp.generated.resources.workout_menstrual_early_desc
import kilo.composeapp.generated.resources.workout_menstrual_late_title
import kilo.composeapp.generated.resources.workout_menstrual_late_desc
import kilo.composeapp.generated.resources.workout_follicular_early_title
import kilo.composeapp.generated.resources.workout_follicular_early_desc
import kilo.composeapp.generated.resources.workout_follicular_late_title
import kilo.composeapp.generated.resources.workout_follicular_late_desc
import kilo.composeapp.generated.resources.workout_ovulation_title
import kilo.composeapp.generated.resources.workout_ovulation_desc
import kilo.composeapp.generated.resources.workout_luteal_early_title
import kilo.composeapp.generated.resources.workout_luteal_early_desc
import kilo.composeapp.generated.resources.workout_luteal_mid_title
import kilo.composeapp.generated.resources.workout_luteal_mid_desc
import kilo.composeapp.generated.resources.workout_luteal_late_title
import kilo.composeapp.generated.resources.workout_luteal_late_desc
import kilo.composeapp.generated.resources.workout_focus_light_upper
import kilo.composeapp.generated.resources.workout_focus_mobility
import kilo.composeapp.generated.resources.workout_focus_pelvic_warmup
import kilo.composeapp.generated.resources.workout_focus_moderate_compound
import kilo.composeapp.generated.resources.workout_focus_hypertrophy_8_12
import kilo.composeapp.generated.resources.workout_focus_dynamic_warmup
import kilo.composeapp.generated.resources.workout_focus_progressive_overload
import kilo.composeapp.generated.resources.workout_focus_volume_sets
import kilo.composeapp.generated.resources.workout_focus_drop_sets
import kilo.composeapp.generated.resources.workout_focus_heavy_compounds
import kilo.composeapp.generated.resources.workout_focus_pr_attempts
import kilo.composeapp.generated.resources.workout_focus_strength_3_5
import kilo.composeapp.generated.resources.workout_focus_explosive_power
import kilo.composeapp.generated.resources.workout_focus_hip_thrust
import kilo.composeapp.generated.resources.workout_focus_controlled_eccentric
import kilo.composeapp.generated.resources.workout_focus_superset
import kilo.composeapp.generated.resources.workout_focus_isolation_exercises
import kilo.composeapp.generated.resources.workout_focus_controlled_tempo
import kilo.composeapp.generated.resources.workout_focus_machines_cables
import kilo.composeapp.generated.resources.workout_focus_high_reps_12_15
import kilo.composeapp.generated.resources.workout_focus_joint_friendly
import kilo.composeapp.generated.resources.workout_focus_deload_half_weight
import kilo.composeapp.generated.resources.workout_focus_form_refinement
import kilo.composeapp.generated.resources.workout_focus_mobility_stretch
import kilo.composeapp.generated.resources.workout_caution_pelvic_pressure
import kilo.composeapp.generated.resources.workout_caution_acl_ligament
import kilo.composeapp.generated.resources.workout_caution_thermoregulation
import kilo.composeapp.generated.resources.workout_caution_rpe_elevation
import kilo.composeapp.generated.resources.workout_caution_catabolism_protein
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
internal fun workoutIntensityName(intensity: WorkoutIntensity): String {
    return when (intensity) {
        WorkoutIntensity.REST_RECOVERY -> stringResource(Res.string.workout_intensity_rest_recovery)
        WorkoutIntensity.MODERATE_STEADY -> stringResource(Res.string.workout_intensity_moderate_steady)
        WorkoutIntensity.HIGH_INTENSITY -> stringResource(Res.string.workout_intensity_high_intensity)
        WorkoutIntensity.PEAK_POWER -> stringResource(Res.string.workout_intensity_peak_power)
    }
}

internal fun workoutIntensityEmoji(intensity: WorkoutIntensity): String {
    return when (intensity) {
        WorkoutIntensity.REST_RECOVERY -> "🧘"
        WorkoutIntensity.MODERATE_STEADY -> "🏃"
        WorkoutIntensity.HIGH_INTENSITY -> "🏋️"
        WorkoutIntensity.PEAK_POWER -> "⚡"
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

        // Workout titles
        "workout_menstrual_early_title" -> stringResource(Res.string.workout_menstrual_early_title)
        "workout_menstrual_late_title" -> stringResource(Res.string.workout_menstrual_late_title)
        "workout_follicular_early_title" -> stringResource(Res.string.workout_follicular_early_title)
        "workout_follicular_late_title" -> stringResource(Res.string.workout_follicular_late_title)
        "workout_ovulation_title" -> stringResource(Res.string.workout_ovulation_title)
        "workout_luteal_early_title" -> stringResource(Res.string.workout_luteal_early_title)
        "workout_luteal_mid_title" -> stringResource(Res.string.workout_luteal_mid_title)
        "workout_luteal_late_title" -> stringResource(Res.string.workout_luteal_late_title)

        // Workout descriptions
        "workout_menstrual_early_desc" -> stringResource(Res.string.workout_menstrual_early_desc)
        "workout_menstrual_late_desc" -> stringResource(Res.string.workout_menstrual_late_desc)
        "workout_follicular_early_desc" -> stringResource(Res.string.workout_follicular_early_desc)
        "workout_follicular_late_desc" -> stringResource(Res.string.workout_follicular_late_desc)
        "workout_ovulation_desc" -> stringResource(Res.string.workout_ovulation_desc)
        "workout_luteal_early_desc" -> stringResource(Res.string.workout_luteal_early_desc)
        "workout_luteal_mid_desc" -> stringResource(Res.string.workout_luteal_mid_desc)
        "workout_luteal_late_desc" -> stringResource(Res.string.workout_luteal_late_desc)

        // Workout focus chips
        "workout_focus_light_upper" -> stringResource(Res.string.workout_focus_light_upper)
        "workout_focus_mobility" -> stringResource(Res.string.workout_focus_mobility)
        "workout_focus_pelvic_warmup" -> stringResource(Res.string.workout_focus_pelvic_warmup)
        "workout_focus_moderate_compound" -> stringResource(Res.string.workout_focus_moderate_compound)
        "workout_focus_hypertrophy_8_12" -> stringResource(Res.string.workout_focus_hypertrophy_8_12)
        "workout_focus_dynamic_warmup" -> stringResource(Res.string.workout_focus_dynamic_warmup)
        "workout_focus_progressive_overload" -> stringResource(Res.string.workout_focus_progressive_overload)
        "workout_focus_volume_sets" -> stringResource(Res.string.workout_focus_volume_sets)
        "workout_focus_drop_sets" -> stringResource(Res.string.workout_focus_drop_sets)
        "workout_focus_heavy_compounds" -> stringResource(Res.string.workout_focus_heavy_compounds)
        "workout_focus_pr_attempts" -> stringResource(Res.string.workout_focus_pr_attempts)
        "workout_focus_strength_3_5" -> stringResource(Res.string.workout_focus_strength_3_5)
        "workout_focus_explosive_power" -> stringResource(Res.string.workout_focus_explosive_power)
        "workout_focus_hip_thrust" -> stringResource(Res.string.workout_focus_hip_thrust)
        "workout_focus_controlled_eccentric" -> stringResource(Res.string.workout_focus_controlled_eccentric)
        "workout_focus_superset" -> stringResource(Res.string.workout_focus_superset)
        "workout_focus_isolation_exercises" -> stringResource(Res.string.workout_focus_isolation_exercises)
        "workout_focus_controlled_tempo" -> stringResource(Res.string.workout_focus_controlled_tempo)
        "workout_focus_machines_cables" -> stringResource(Res.string.workout_focus_machines_cables)
        "workout_focus_high_reps_12_15" -> stringResource(Res.string.workout_focus_high_reps_12_15)
        "workout_focus_joint_friendly" -> stringResource(Res.string.workout_focus_joint_friendly)
        "workout_focus_deload_half_weight" -> stringResource(Res.string.workout_focus_deload_half_weight)
        "workout_focus_form_refinement" -> stringResource(Res.string.workout_focus_form_refinement)
        "workout_focus_mobility_stretch" -> stringResource(Res.string.workout_focus_mobility_stretch)

        // Workout cautions
        "workout_caution_pelvic_pressure" -> stringResource(Res.string.workout_caution_pelvic_pressure)
        "workout_caution_acl_ligament" -> stringResource(Res.string.workout_caution_acl_ligament)
        "workout_caution_thermoregulation" -> stringResource(Res.string.workout_caution_thermoregulation)
        "workout_caution_rpe_elevation" -> stringResource(Res.string.workout_caution_rpe_elevation)
        "workout_caution_catabolism_protein" -> stringResource(Res.string.workout_caution_catabolism_protein)

        else -> key
    }
}
