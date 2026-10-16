package app.kilo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.data.HevyWorkoutSession
import app.kilo.data.MuscleCategory
import app.kilo.data.UserGender
import app.kilo.domain.CyclePhase
import app.kilo.domain.GeneralWorkoutAnalytics
import app.kilo.domain.RelativeStrengthProgress
import app.kilo.domain.StrengthTier
import app.kilo.domain.TierBounds
import app.kilo.domain.analyzeGeneralWorkouts
import app.kilo.domain.calculateRelativeStrength
import app.kilo.domain.resolveWorkoutCyclePhase
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.correlation_best_set_format
import kilo.composeapp.generated.resources.correlation_bw_ratio_format
import kilo.composeapp.generated.resources.correlation_empty_desc
import kilo.composeapp.generated.resources.correlation_est_1rm_format
import kilo.composeapp.generated.resources.correlation_no_data
import kilo.composeapp.generated.resources.correlation_set_in_phase_format
import kilo.composeapp.generated.resources.correlation_source_1rm_desc
import kilo.composeapp.generated.resources.correlation_source_1rm_title
import kilo.composeapp.generated.resources.correlation_source_doms_desc
import kilo.composeapp.generated.resources.correlation_source_doms_title
import kilo.composeapp.generated.resources.correlation_source_strength_desc
import kilo.composeapp.generated.resources.correlation_source_strength_title
import kilo.composeapp.generated.resources.correlation_source_volume_desc
import kilo.composeapp.generated.resources.correlation_source_volume_title
import kilo.composeapp.generated.resources.correlation_sources_header
import kilo.composeapp.generated.resources.correlation_sources_intro
import kilo.composeapp.generated.resources.correlation_tier_advanced
import kilo.composeapp.generated.resources.correlation_tier_beginner
import kilo.composeapp.generated.resources.correlation_tier_elite
import kilo.composeapp.generated.resources.correlation_tier_intermediate
import kilo.composeapp.generated.resources.cycle_back_action
import kilo.composeapp.generated.resources.cycle_phase_follicular
import kilo.composeapp.generated.resources.cycle_phase_luteal
import kilo.composeapp.generated.resources.cycle_phase_menstrual
import kilo.composeapp.generated.resources.cycle_phase_ovulation
import kilo.composeapp.generated.resources.hevy_benefit_doms
import kilo.composeapp.generated.resources.hevy_benefit_strength
import kilo.composeapp.generated.resources.hevy_how_to_export_hint
import kilo.composeapp.generated.resources.hevy_import_button
import kilo.composeapp.generated.resources.workout_doms_card_title
import kilo.composeapp.generated.resources.workout_doms_formula_desc
import kilo.composeapp.generated.resources.workout_doms_peak_session_format
import kilo.composeapp.generated.resources.workout_elite_reached_badge
import kilo.composeapp.generated.resources.workout_formula_card_title
import kilo.composeapp.generated.resources.workout_formula_epley_line
import kilo.composeapp.generated.resources.workout_formula_ratio_line
import kilo.composeapp.generated.resources.workout_formula_scale_line
import kilo.composeapp.generated.resources.workout_general_summary_title
import kilo.composeapp.generated.resources.workout_lift_calc_breakdown_format
import kilo.composeapp.generated.resources.workout_lift_no_record_hint
import kilo.composeapp.generated.resources.workout_metric_avg_tonnage
import kilo.composeapp.generated.resources.workout_metric_peak_tonnage
import kilo.composeapp.generated.resources.workout_metric_sessions
import kilo.composeapp.generated.resources.workout_metric_total_prs
import kilo.composeapp.generated.resources.workout_muscle_back_pull
import kilo.composeapp.generated.resources.workout_muscle_chest_push
import kilo.composeapp.generated.resources.workout_muscle_core
import kilo.composeapp.generated.resources.workout_muscle_dist_title
import kilo.composeapp.generated.resources.workout_muscle_legs_glutes
import kilo.composeapp.generated.resources.workout_muscle_sets_format
import kilo.composeapp.generated.resources.workout_muscle_shoulders_arms
import kilo.composeapp.generated.resources.workout_next_tier_progress_format
import kilo.composeapp.generated.resources.workout_pr_count_badge_format
import kilo.composeapp.generated.resources.workout_scale_header
import kilo.composeapp.generated.resources.workout_screen_subtitle
import kilo.composeapp.generated.resources.workout_screen_title
import kilo.composeapp.generated.resources.workout_standard_female
import kilo.composeapp.generated.resources.workout_standard_hint
import kilo.composeapp.generated.resources.workout_standard_male
import kilo.composeapp.generated.resources.workout_standard_selector_label
import kilo.composeapp.generated.resources.workout_tab_sources
import kilo.composeapp.generated.resources.workout_tab_strength
import kilo.composeapp.generated.resources.workout_tab_volume
import kilo.composeapp.generated.resources.workout_tier_bounds_kg_open_format
import kilo.composeapp.generated.resources.workout_tier_bounds_kg_range_format
import kilo.composeapp.generated.resources.workout_tier_bounds_open_format
import kilo.composeapp.generated.resources.workout_tier_bounds_range_format
import kilo.composeapp.generated.resources.workout_top_prs_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

/**
 * Full-screen Liquid Glass Strength & Workout Analytics view (`WorkoutAnalyticsScreen`).
 * Completely decoupled from menstrual cycle tracking so both male and female athletes
 * can analyze relative strength (1RM / BW), normative tier bounds (min-max xBW and kg),
 * muscle group volume distribution, DOMS fluid shield thresholds, and peer-reviewed methodology.
 */
@Composable
fun WorkoutAnalyticsScreen(
    isOpen: Boolean = true,
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double = 0.0,
    userGender: UserGender = UserGender.UNSPECIFIED,
    onSelectGender: (UserGender) -> Unit = {},
    cycleStartEpochDays: Long? = null,
    cycleLength: Int = 28,
    onImportHevyCsv: (String) -> Unit = {},
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val scope = rememberCoroutineScope()

    val hevyPicker = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("csv")),
    ) { platformFile: PlatformFile? ->
        if (platformFile != null) {
            scope.launch {
                try {
                    val text = platformFile.readString()
                    if (text.isNotBlank()) {
                        onImportHevyCsv(text)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var isMaleSelected by remember(userGender) {
        mutableStateOf(userGender == UserGender.MALE)
    }

    val generalAnalytics = remember(workouts) {
        analyzeGeneralWorkouts(workouts)
    }

    val compoundLifts = remember(workouts, currentWeightKg, isMaleSelected, cycleStartEpochDays, cycleLength) {
        computeCompoundLiftsWithNorms(
            workouts = workouts,
            currentWeightKg = currentWeightKg,
            isMale = isMaleSelected,
            cycleStartEpochDays = cycleStartEpochDays,
            cycleLength = cycleLength,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Ambient background gradient blobs for liquid glass refraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (hazeState != null) Modifier.hazeSource(hazeState) else Modifier),
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = 180.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(colors.blobA.copy(alpha = 0.45f))
                    .blur(70.dp),
            )
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = (-100).dp, y = 350.dp)
                    .clip(CircleShape)
                    .background(colors.blobB.copy(alpha = 0.35f))
                    .blur(80.dp),
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar / Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val backDesc = stringResource(Res.string.cycle_back_action)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = backDesc,
                            onClick = onDismiss,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.workout_screen_title),
                        style = typography.title,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(Res.string.workout_screen_subtitle),
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Quick CSV Upload Action in Header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.14f))
                        .border(0.5.dp, colors.accent.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = { hevyPicker.launch() },
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "📥 CSV",
                        style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        color = colors.accent,
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(4.dp))

                // 3-Tab Segmented Selector (1RM & Scales | Volume & DOMS | Science & Math)
                WorkoutAnalyticsTabSelector(
                    selectedIndex = selectedTabIndex,
                    onSelect = { selectedTabIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(18.dp))

                // If no workouts uploaded yet, show compact CSV Upload CTA Banner above the tabs
                if (workouts.isEmpty() && selectedTabIndex != 2) {
                    EmptyWorkoutImportBanner(
                        onImportClick = { hevyPicker.launch() },
                    )
                    Spacer(Modifier.height(16.dp))
                }

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WorkoutAnalyticsTabContent",
                ) { tab ->
                    when (tab) {
                        0 -> RelativeStrengthAndScalesTab(
                            lifts = compoundLifts,
                            currentWeightKg = currentWeightKg,
                            isMaleStandard = isMaleSelected,
                            onSelectMaleStandard = { male ->
                                isMaleSelected = male
                                onSelectGender(if (male) UserGender.MALE else UserGender.FEMALE)
                            },
                        )
                        1 -> UniversalVolumeAndDomsTab(
                            analytics = generalAnalytics,
                        )
                        2 -> MethodologyAndSourcesTab()
                    }
                }
            }
        }
    }
}

/**
 * Backward-compatible alias for [WorkoutAnalyticsScreen].
 */
@Composable
fun WorkoutCorrelationSheet(
    isOpen: Boolean,
    workouts: List<HevyWorkoutSession>,
    cycleStartEpochDays: Long? = null,
    cycleLength: Int = 28,
    currentWeightKg: Double = 0.0,
    userGender: UserGender = UserGender.UNSPECIFIED,
    onSelectGender: (UserGender) -> Unit = {},
    onImportHevyCsv: (String) -> Unit = {},
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
) {
    WorkoutAnalyticsScreen(
        isOpen = isOpen,
        workouts = workouts,
        currentWeightKg = currentWeightKg,
        userGender = userGender,
        onSelectGender = onSelectGender,
        cycleStartEpochDays = cycleStartEpochDays,
        cycleLength = cycleLength,
        onImportHevyCsv = onImportHevyCsv,
        onDismiss = onDismiss,
        hazeState = hazeState,
    )
}

@Composable
private fun WorkoutAnalyticsTabSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val tabs = listOf(
        stringResource(Res.string.workout_tab_strength),
        stringResource(Res.string.workout_tab_volume),
        stringResource(Res.string.workout_tab_sources),
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.glassTint)
            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.accent else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 9.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = typography.caption.copy(fontSize = 11.5.sp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onAccent else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyWorkoutImportBanner(
    onImportClick: () -> Unit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.glassTint)
            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🏋️", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.correlation_no_data),
                style = typography.headline,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.correlation_empty_desc),
                style = typography.caption.copy(lineHeight = 17.sp),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.glassTint)
                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Res.string.hevy_benefit_strength,
                    Res.string.hevy_benefit_doms,
                ).forEach { benefitRes ->
                    Text(
                        text = stringResource(benefitRes),
                        style = typography.caption.copy(lineHeight = 16.5.sp),
                        color = colors.textPrimary,
                    )
                }

                Text(
                    text = stringResource(Res.string.hevy_how_to_export_hint),
                    style = typography.caption.copy(fontSize = 11.sp, lineHeight = 15.5.sp),
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent)
                    .clickable(role = Role.Button, onClick = onImportClick)
                    .padding(vertical = 13.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.hevy_import_button),
                    style = typography.subhead,
                    fontWeight = FontWeight.Bold,
                    color = colors.onAccent,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelativeStrengthAndScalesTab(
    lifts: List<RelativeStrengthProgress>,
    currentWeightKg: Double,
    isMaleStandard: Boolean,
    onSelectMaleStandard: (Boolean) -> Unit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Sex-Stratified Normative Standard Selector (Female vs Male)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassTint)
                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.workout_standard_selector_label),
                    style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.workout_standard_hint),
                    style = typography.caption.copy(fontSize = 11.5.sp),
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.glassBorder.copy(alpha = 0.25f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Female Standard Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isMaleStandard) colors.accent else Color.Transparent)
                            .clickable(role = Role.RadioButton) { onSelectMaleStandard(false) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.workout_standard_female),
                            style = typography.caption.copy(fontSize = 12.sp),
                            fontWeight = if (!isMaleStandard) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isMaleStandard) colors.onAccent else colors.textSecondary,
                        )
                    }

                    // Male Standard Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMaleStandard) colors.accent else Color.Transparent)
                            .clickable(role = Role.RadioButton) { onSelectMaleStandard(true) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.workout_standard_male),
                            style = typography.caption.copy(fontSize = 12.sp),
                            fontWeight = if (isMaleStandard) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMaleStandard) colors.onAccent else colors.textSecondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Explicit Mathematical Formula Card (Epley 1RM & Relative Strength)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.09f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.32f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📐", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.workout_formula_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Formula Lines
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.workout_formula_epley_line),
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = colors.accent,
                    )
                    Text(
                        text = stringResource(Res.string.workout_formula_ratio_line),
                        style = typography.caption.copy(fontSize = 11.5.sp),
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.workout_formula_scale_line),
                        style = typography.caption.copy(fontSize = 11.sp, lineHeight = 15.5.sp),
                        color = colors.textSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. Compound Lift Cards with 4-Tier Visual Normative Scales (Min-Max xBW & kg)
        lifts.forEach { lift ->
            CompoundLiftNormativeCard(
                lift = lift,
                currentWeightKg = currentWeightKg,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompoundLiftNormativeCard(
    lift: RelativeStrengthProgress,
    currentWeightKg: Double,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val hasLiftRecord = lift.estimated1RmKg > 0.0
    val formatted1Rm = formatOneDecimal(lift.estimated1RmKg)
    val formattedBestWeight = formatOneDecimal(lift.bestWeightKg)
    val formattedBw = formatOneDecimal(currentWeightKg)
    val ratioFormatted = formatTwoDecimals(lift.relativeRatio)
    val tierLabel = strengthTierDisplayName(lift.tier)
    val tierColor = strengthTierColor(lift.tier)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.glassTint)
            .border(
                0.5.dp,
                if (hasLiftRecord) tierColor.copy(alpha = 0.45f) else colors.glassBorder,
                RoundedCornerShape(18.dp),
            )
            .padding(16.dp),
    ) {
        Column {
            // Top Row: Exercise Title + Tier Badge + xBW Ratio Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lift.exerciseName,
                        style = typography.headline.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )

                    if (hasLiftRecord) {
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (currentWeightKg > 0.0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tierColor.copy(alpha = 0.15f))
                                        .border(0.5.dp, tierColor.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = tierLabel,
                                        style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = tierColor,
                                    )
                                }
                            }

                            lift.achievedInPhase?.let { phase ->
                                val phaseColor = phaseColorFor(phase)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(phaseColor.copy(alpha = 0.14f))
                                        .border(0.5.dp, phaseColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = stringResource(
                                            Res.string.correlation_set_in_phase_format,
                                            phaseDisplayName(phase),
                                        ),
                                        style = typography.caption.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                        color = phaseColor,
                                    )
                                }
                            }
                        }
                    }
                }

                if (hasLiftRecord && currentWeightKg > 0.0) {
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accent.copy(alpha = 0.15f))
                            .border(0.5.dp, colors.accent.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.correlation_bw_ratio_format, ratioFormatted),
                            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = colors.accent,
                        )
                    }
                }
            }

            if (hasLiftRecord) {
                Spacer(Modifier.height(10.dp))

                // Best Set & Est 1RM summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.correlation_est_1rm_format, formatted1Rm),
                        style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    if (lift.bestWeightKg > 0.0 && lift.bestReps > 0) {
                        Text(
                            text = stringResource(
                                Res.string.correlation_best_set_format,
                                formattedBestWeight,
                                lift.bestReps,
                            ),
                            style = typography.caption.copy(fontSize = 11.5.sp),
                            color = colors.textSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Explicit Step-by-Step Calculation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.glassBorder.copy(alpha = 0.22f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    val breakdownText = stringResource(
                        Res.string.workout_lift_calc_breakdown_format,
                        formattedBestWeight,
                        lift.bestReps,
                        formatted1Rm,
                        formattedBw,
                        ratioFormatted,
                    )
                    Text(
                        text = breakdownText,
                        style = typography.caption.copy(fontSize = 11.sp, lineHeight = 15.5.sp),
                        color = colors.textSecondary,
                    )
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.workout_lift_no_record_hint),
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(12.dp))

            // 4-Tier Visual Normative Scale Header
            Text(
                text = stringResource(Res.string.workout_scale_header),
                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary,
            )

            Spacer(Modifier.height(8.dp))

            // 4-Column Normative Scale Grid (Beginner | Intermediate | Advanced | Elite)
            val boundsList = lift.norms.tierBounds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                boundsList.forEach { bound ->
                    val isCurrentTier = hasLiftRecord && currentWeightKg > 0.0 && lift.tier == bound.tier
                    val cellColor = strengthTierColor(bound.tier)
                    NormativeTierCell(
                        bound = bound,
                        currentWeightKg = currentWeightKg,
                        isCurrentTier = isCurrentTier,
                        cellColor = cellColor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Next Tier Progress or Elite Reached Badge
            if (hasLiftRecord && currentWeightKg > 0.0) {
                Spacer(Modifier.height(10.dp))
                val nextTier = lift.nextTier
                val nextTargetRatio = lift.nextTierTargetRatio
                val nextDeltaKg = lift.nextTierDeltaKg

                if (nextTier != null && nextTargetRatio != null && nextDeltaKg != null) {
                    Text(
                        text = stringResource(
                            Res.string.workout_next_tier_progress_format,
                            strengthTierDisplayName(nextTier),
                            formatTwoDecimals(nextTargetRatio),
                            formatOneDecimal(nextDeltaKg),
                        ),
                        style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                        color = colors.accent,
                    )
                } else if (lift.tier == StrengthTier.ELITE) {
                    val eliteRatioStr = formatTwoDecimals(lift.norms.eliteMin)
                    Text(
                        text = stringResource(Res.string.workout_elite_reached_badge, eliteRatioStr),
                        style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                        color = colors.positive,
                    )
                }
            }
        }
    }
}

@Composable
private fun NormativeTierCell(
    bound: TierBounds,
    currentWeightKg: Double,
    isCurrentTier: Boolean,
    cellColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val minStr = formatTwoDecimals(bound.minRatio)
    val maxRatio = bound.maxRatio
    val ratioRangeText = if (maxRatio != null) {
        stringResource(Res.string.workout_tier_bounds_range_format, minStr, formatTwoDecimals(maxRatio))
    } else {
        stringResource(Res.string.workout_tier_bounds_open_format, minStr)
    }

    val kgRangeText = if (currentWeightKg > 0.0) {
        val minKg = formatOneDecimal(bound.minRatio * currentWeightKg)
        if (maxRatio != null) {
            val maxKg = formatOneDecimal(maxRatio * currentWeightKg)
            stringResource(Res.string.workout_tier_bounds_kg_range_format, minKg, maxKg)
        } else {
            stringResource(Res.string.workout_tier_bounds_kg_open_format, minKg)
        }
    } else {
        null
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCurrentTier) cellColor.copy(alpha = 0.18f)
                else colors.glassBorder.copy(alpha = 0.18f)
            )
            .border(
                if (isCurrentTier) 1.dp else 0.5.dp,
                if (isCurrentTier) cellColor else colors.glassBorder.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = strengthTierDisplayName(bound.tier),
                style = typography.caption.copy(fontSize = 10.sp),
                fontWeight = if (isCurrentTier) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentTier) cellColor else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = ratioRangeText,
                style = typography.caption.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                color = if (isCurrentTier) colors.textPrimary else colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            if (kgRangeText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = kgRangeText,
                    style = typography.caption.copy(fontSize = 9.sp),
                    color = if (isCurrentTier) cellColor else colors.textSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun UniversalVolumeAndDomsTab(
    analytics: GeneralWorkoutAnalytics,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Universal Session & Tonnage Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.glassTint)
                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.workout_general_summary_title),
                    style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricPillBox(
                        label = stringResource(Res.string.workout_metric_sessions),
                        value = "${analytics.sessionCount}",
                        modifier = Modifier.weight(1f),
                    )
                    MetricPillBox(
                        label = stringResource(Res.string.workout_metric_avg_tonnage),
                        value = "${formatVolumeInt(analytics.avgSessionVolumeKg)} kg",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricPillBox(
                        label = stringResource(Res.string.workout_metric_peak_tonnage),
                        value = "${formatVolumeInt(analytics.maxSessionVolumeKg)} kg",
                        modifier = Modifier.weight(1f),
                    )
                    MetricPillBox(
                        label = stringResource(Res.string.workout_metric_total_prs),
                        value = "${analytics.totalPrCount}",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Muscle Group Set & Volume Distribution Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.glassTint)
                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.workout_muscle_dist_title),
                    style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )

                Spacer(Modifier.height(12.dp))

                analytics.muscleShares.forEach { share ->
                    val fraction = (share.sharePercent / 100f).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = muscleCategoryDisplayName(share.category),
                                style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                                color = colors.textPrimary,
                            )
                            Text(
                                text = stringResource(
                                    Res.string.workout_muscle_sets_format,
                                    share.setCount,
                                    "${share.sharePercent}%",
                                    formatVolumeInt(share.totalVolumeKg),
                                ),
                                style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = colors.accent,
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.glassBorder.copy(alpha = 0.35f)),
                        ) {
                            if (fraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(colors.accent, colors.accent.copy(alpha = 0.75f))
                                            )
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 3. DOMS / EIMD Fluid Shield Rule & High-Trauma Session Counter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.accent.copy(alpha = 0.09f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.32f), RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.workout_doms_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.workout_doms_formula_desc),
                    style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.5.sp),
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val peakKgStr = formatVolumeInt(analytics.maxLegsOrBackSessionVolumeKg)
                    Text(
                        text = stringResource(
                            Res.string.workout_doms_peak_session_format,
                            peakKgStr,
                        ),
                        style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                }
            }
        }

        // 4. All-Time Exercise PRs Card
        if (analytics.topExercisePrs.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.glassTint)
                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.workout_top_prs_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )

                    Spacer(Modifier.height(10.dp))

                    analytics.topExercisePrs.forEachIndexed { idx, pr ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .height(0.5.dp)
                                    .background(colors.glassBorder.copy(alpha = 0.45f)),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = pr.exerciseName,
                                    style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.correlation_best_set_format,
                                        formatOneDecimal(pr.maxWeightKg),
                                        pr.bestReps,
                                    ),
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = colors.textSecondary,
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accent.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = stringResource(Res.string.workout_pr_count_badge_format, pr.prCount),
                                        style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = colors.accent,
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(
                                        Res.string.correlation_est_1rm_format,
                                        formatOneDecimal(pr.estimated1RmKg),
                                    ),
                                    style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPillBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.glassBorder.copy(alpha = 0.2f))
            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                text = label,
                style = typography.caption.copy(fontSize = 10.5.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                style = typography.subhead.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = colors.textPrimary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MethodologyAndSourcesTab() {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📚 ${stringResource(Res.string.correlation_sources_header)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.correlation_sources_intro),
            style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = colors.textSecondary,
        )

        Spacer(Modifier.height(14.dp))

        ScientificCitationCard(
            title = stringResource(Res.string.correlation_source_1rm_title),
            body = stringResource(Res.string.correlation_source_1rm_desc),
        )
        Spacer(Modifier.height(10.dp))

        ScientificCitationCard(
            title = stringResource(Res.string.correlation_source_strength_title),
            body = stringResource(Res.string.correlation_source_strength_desc),
        )
        Spacer(Modifier.height(10.dp))

        ScientificCitationCard(
            title = stringResource(Res.string.correlation_source_doms_title),
            body = stringResource(Res.string.correlation_source_doms_desc),
        )
        Spacer(Modifier.height(10.dp))

        ScientificCitationCard(
            title = stringResource(Res.string.correlation_source_volume_title),
            body = stringResource(Res.string.correlation_source_volume_desc),
        )
    }
}

@Composable
private fun ScientificCitationCard(
    title: String,
    body: String,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.glassTint)
            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = title,
                style = typography.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.5.sp),
                color = colors.textSecondary,
            )
        }
    }
}

private fun computeCompoundLiftsWithNorms(
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double,
    isMale: Boolean,
    cycleStartEpochDays: Long?,
    cycleLength: Int,
): List<RelativeStrengthProgress> {
    val targetLiftKeywords = listOf(
        "Squat" to listOf("squat"),
        "Deadlift" to listOf("deadlift", "rdl"),
        "Bench Press" to listOf("bench press", "chest press"),
        "Overhead Press" to listOf("overhead press", "military press", "ohp", "shoulder press"),
        "Barbell Row" to listOf("barbell row", "bent over row", "pendlay row"),
        "Hip Thrust" to listOf("hip thrust"),
    )

    val logged = mutableListOf<RelativeStrengthProgress>()
    val unlogged = mutableListOf<RelativeStrengthProgress>()

    for ((label, keywords) in targetLiftKeywords) {
        var best1Rm = 0.0
        var bestWeight = 0.0
        var bestReps = 0
        var bestEpochDays: Long? = null

        for (workout in workouts) {
            for (exercise in workout.exercises) {
                val lowerTitle = exercise.title.lowercase()
                val matches = keywords.any { lowerTitle.contains(it) }
                if (matches) {
                    if (exercise.estimated1RmKg > best1Rm) {
                        best1Rm = exercise.estimated1RmKg
                        val bestSet = exercise.sets.maxByOrNull { set ->
                            if (set.reps <= 0 || set.weightKg <= 0.0) {
                                0.0
                            } else if (set.reps == 1) {
                                set.weightKg
                            } else {
                                set.weightKg * (1.0 + set.reps / 30.0)
                            }
                        } ?: exercise.sets.maxByOrNull { it.weightKg }
                        bestWeight = bestSet?.weightKg ?: exercise.maxWeightKg
                        bestReps = bestSet?.reps ?: 1
                        bestEpochDays = workout.dateEpochDays
                    }
                }
            }
        }

        if (best1Rm > 0.0) {
            val achievedPhase = if (cycleStartEpochDays != null && bestEpochDays != null) {
                resolveWorkoutCyclePhase(
                    workoutEpochDays = bestEpochDays,
                    cycleStartEpochDays = cycleStartEpochDays,
                    cycleLength = cycleLength,
                )
            } else {
                null
            }

            logged.add(
                calculateRelativeStrength(
                    bodyWeightKg = currentWeightKg,
                    exerciseName = label,
                    weightLiftedKg = bestWeight,
                    reps = bestReps,
                    achievedInPhase = achievedPhase,
                    isMale = isMale,
                )
            )
        } else {
            unlogged.add(
                calculateRelativeStrength(
                    bodyWeightKg = currentWeightKg,
                    exerciseName = label,
                    weightLiftedKg = 0.0,
                    reps = 0,
                    achievedInPhase = null,
                    isMale = isMale,
                )
            )
        }
    }

    return logged + unlogged
}

@Composable
private fun muscleCategoryDisplayName(category: MuscleCategory): String {
    return when (category) {
        MuscleCategory.LEGS_GLUTES -> stringResource(Res.string.workout_muscle_legs_glutes)
        MuscleCategory.BACK_PULL -> stringResource(Res.string.workout_muscle_back_pull)
        MuscleCategory.CHEST_PUSH -> stringResource(Res.string.workout_muscle_chest_push)
        MuscleCategory.SHOULDERS_ARMS -> stringResource(Res.string.workout_muscle_shoulders_arms)
        MuscleCategory.CORE_FULL_BODY -> stringResource(Res.string.workout_muscle_core)
        MuscleCategory.OTHER -> stringResource(Res.string.workout_muscle_core)
    }
}

@Composable
private fun phaseDisplayName(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> stringResource(Res.string.cycle_phase_menstrual)
        CyclePhase.FOLLICULAR -> stringResource(Res.string.cycle_phase_follicular)
        CyclePhase.OVULATION -> stringResource(Res.string.cycle_phase_ovulation)
        CyclePhase.LUTEAL -> stringResource(Res.string.cycle_phase_luteal)
    }
}

@Composable
private fun phaseColorFor(phase: CyclePhase): Color {
    val colors = KiloTheme.colors
    return when (phase) {
        CyclePhase.MENSTRUAL -> colors.danger
        CyclePhase.FOLLICULAR -> colors.accent
        CyclePhase.OVULATION -> colors.positive
        CyclePhase.LUTEAL -> colors.neutral
    }
}

@Composable
private fun strengthTierDisplayName(tier: StrengthTier): String {
    return when (tier) {
        StrengthTier.BEGINNER -> stringResource(Res.string.correlation_tier_beginner)
        StrengthTier.INTERMEDIATE -> stringResource(Res.string.correlation_tier_intermediate)
        StrengthTier.ADVANCED -> stringResource(Res.string.correlation_tier_advanced)
        StrengthTier.ELITE -> stringResource(Res.string.correlation_tier_elite)
    }
}

@Composable
private fun strengthTierColor(tier: StrengthTier): Color {
    val colors = KiloTheme.colors
    return when (tier) {
        StrengthTier.BEGINNER -> colors.neutral
        StrengthTier.INTERMEDIATE -> colors.accent
        StrengthTier.ADVANCED -> colors.positive
        StrengthTier.ELITE -> colors.positive
    }
}

private fun formatVolumeInt(volume: Double): String {
    return if (volume >= 1000.0) {
        val thousands = volume / 1000.0
        "${formatOneDecimal(thousands)}k"
    } else {
        "${round(volume).toInt()}"
    }
}

private fun formatOneDecimal(value: Double): String {
    val tenthsTotal = round(value * 10.0).toInt()
    val sign = if (tenthsTotal < 0) "-" else ""
    val absTenths = abs(tenthsTotal)
    val whole = absTenths / 10
    val frac = absTenths % 10
    return "$sign$whole.$frac"
}

private fun formatTwoDecimals(value: Double): String {
    val hundredthsTotal = round(value * 100.0).toInt()
    val sign = if (hundredthsTotal < 0) "-" else ""
    val absHundredths = abs(hundredthsTotal)
    val whole = absHundredths / 100
    val frac = absHundredths % 100
    val fracPadded = if (frac < 10) "0$frac" else "$frac"
    return "$sign$whole.$fracPadded"
}
