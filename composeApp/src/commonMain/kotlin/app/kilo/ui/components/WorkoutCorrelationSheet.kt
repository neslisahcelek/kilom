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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.data.HevyWorkoutSession
import app.kilo.data.MuscleCategory
import app.kilo.domain.CyclePhase
import app.kilo.domain.CycleWorkoutCorrelation
import app.kilo.domain.PhaseWorkoutSummary
import app.kilo.domain.RelativeStrengthProgress
import app.kilo.domain.StrengthTier
import app.kilo.domain.WorkoutSessionSummary
import app.kilo.domain.analyzeCycleWorkoutCorrelation
import app.kilo.domain.calculateRelativeStrength
import app.kilo.domain.resolveWorkoutCyclePhase
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.correlation_avg_volume_format
import kilo.composeapp.generated.resources.correlation_best_set_format
import kilo.composeapp.generated.resources.correlation_bw_ratio_explained_format
import kilo.composeapp.generated.resources.correlation_bw_ratio_format
import kilo.composeapp.generated.resources.correlation_empty_desc
import kilo.composeapp.generated.resources.correlation_est_1rm_format
import kilo.composeapp.generated.resources.correlation_luteal_physiology_fallback
import kilo.composeapp.generated.resources.correlation_luteal_rpe_card_title
import kilo.composeapp.generated.resources.correlation_luteal_rpe_normal
import kilo.composeapp.generated.resources.correlation_luteal_volume_fallback_format
import kilo.composeapp.generated.resources.correlation_no_compound_lifts
import kilo.composeapp.generated.resources.correlation_no_data
import kilo.composeapp.generated.resources.correlation_no_rpe_hint
import kilo.composeapp.generated.resources.correlation_peak_volume_format
import kilo.composeapp.generated.resources.correlation_phase_desc_follicular
import kilo.composeapp.generated.resources.correlation_phase_desc_luteal
import kilo.composeapp.generated.resources.correlation_phase_desc_menstrual
import kilo.composeapp.generated.resources.correlation_phase_desc_ovulation
import kilo.composeapp.generated.resources.correlation_prs_anabolic_insight_format
import kilo.composeapp.generated.resources.correlation_prs_anabolic_title
import kilo.composeapp.generated.resources.correlation_prs_card_title
import kilo.composeapp.generated.resources.correlation_prs_general_insight
import kilo.composeapp.generated.resources.correlation_prs_share_format
import kilo.composeapp.generated.resources.correlation_relative_strength_title
import kilo.composeapp.generated.resources.correlation_rpe_insight_format
import kilo.composeapp.generated.resources.correlation_set_in_phase_format
import kilo.composeapp.generated.resources.correlation_strength_edema_note
import kilo.composeapp.generated.resources.correlation_strength_guide_body
import kilo.composeapp.generated.resources.correlation_strength_guide_title
import kilo.composeapp.generated.resources.correlation_subtitle
import kilo.composeapp.generated.resources.correlation_tab_prs
import kilo.composeapp.generated.resources.correlation_tab_strength
import kilo.composeapp.generated.resources.correlation_tab_volume
import kilo.composeapp.generated.resources.correlation_tier_advanced
import kilo.composeapp.generated.resources.correlation_tier_beginner
import kilo.composeapp.generated.resources.correlation_tier_elite
import kilo.composeapp.generated.resources.correlation_tier_intermediate
import kilo.composeapp.generated.resources.correlation_title
import kilo.composeapp.generated.resources.correlation_total_prs_format
import kilo.composeapp.generated.resources.correlation_total_volume_format
import kilo.composeapp.generated.resources.correlation_volume_anabolic_higher_format
import kilo.composeapp.generated.resources.correlation_volume_general_insight_format
import kilo.composeapp.generated.resources.correlation_volume_insight_title
import kilo.composeapp.generated.resources.correlation_volume_luteal_higher_format
import kilo.composeapp.generated.resources.correlation_workouts_count_format
import kilo.composeapp.generated.resources.cycle_phase_follicular
import kilo.composeapp.generated.resources.cycle_phase_luteal
import kilo.composeapp.generated.resources.cycle_phase_menstrual
import kilo.composeapp.generated.resources.cycle_phase_ovulation
import kilo.composeapp.generated.resources.hevy_benefit_cycle
import kilo.composeapp.generated.resources.hevy_benefit_doms
import kilo.composeapp.generated.resources.hevy_benefit_strength
import kilo.composeapp.generated.resources.hevy_how_to_export_hint
import kilo.composeapp.generated.resources.hevy_import_button
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCorrelationSheet(
    isOpen: Boolean,
    workouts: List<HevyWorkoutSession>,
    cycleStartEpochDays: Long?,
    cycleLength: Int = 28,
    currentWeightKg: Double = 0.0,
    onImportHevyCsv: (String) -> Unit = {},
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val scope = rememberCoroutineScope()

    val hevyPicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("csv")),
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

    // Convert HevyWorkoutSessions to WorkoutSessionSummary
    val summaries = remember(workouts) {
        workouts.map { session ->
            val allRpes = session.exercises.flatMap { it.sets }.mapNotNull { it.rpe }
            val avgRpe = if (allRpes.isNotEmpty()) allRpes.average() else null

            val isLegsOrBack = session.exercises.any {
                it.category == MuscleCategory.LEGS_GLUTES || it.category == MuscleCategory.BACK_PULL
            }

            WorkoutSessionSummary(
                id = session.id,
                title = session.title,
                dateEpochDays = session.dateEpochDays,
                totalVolumeKg = session.totalVolumeKg,
                isLegsOrBack = isLegsOrBack,
                exerciseNames = session.exercises.map { it.title },
                maxWeights = session.exercises.associate { it.title to it.maxWeightKg },
                avgRpe = avgRpe,
            )
        }
    }

    // Compute cycle workout correlation
    val correlation: CycleWorkoutCorrelation? = remember(summaries, cycleStartEpochDays, cycleLength) {
        if (summaries.isNotEmpty() && cycleStartEpochDays != null) {
            analyzeCycleWorkoutCorrelation(
                workouts = summaries,
                cycleStartEpochDays = cycleStartEpochDays,
                cycleLength = cycleLength,
            )
        } else {
            null
        }
    }

    // Compute top lifts & relative strength with cycle phase context
    val topLifts = remember(workouts, currentWeightKg, cycleStartEpochDays, cycleLength) {
        computeTopLifts(
            workouts = workouts,
            currentWeightKg = currentWeightKg,
            cycleStartEpochDays = cycleStartEpochDays,
            cycleLength = cycleLength,
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.sheet,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.glassBorder)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Text(
                text = stringResource(Res.string.correlation_title),
                style = typography.title,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.correlation_subtitle),
                style = typography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(18.dp))

            if (workouts.isEmpty()) {
                // Empty state with benefits and direct CSV upload button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(20.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏋️", fontSize = 36.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(Res.string.correlation_no_data),
                            style = typography.headline,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(Res.string.correlation_empty_desc),
                            style = typography.caption,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(16.dp))

                        // 3 Benefits list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.glassTint)
                                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            listOf(
                                Res.string.hevy_benefit_doms,
                                Res.string.hevy_benefit_cycle,
                                Res.string.hevy_benefit_strength,
                            ).forEach { benefitRes ->
                                Text(
                                    text = stringResource(benefitRes),
                                    style = typography.caption.copy(lineHeight = 17.sp),
                                    color = colors.textPrimary,
                                )
                            }

                            Text(
                                text = stringResource(Res.string.hevy_how_to_export_hint),
                                style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                color = colors.textSecondary,
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Direct CSV Import Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.accent.copy(alpha = 0.16f))
                                .border(0.5.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .clickable(role = Role.Button) { hevyPicker.launch() }
                                .padding(vertical = 13.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.hevy_import_button),
                                style = typography.subhead,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                            )
                        }
                    }
                }
            } else {
                // Tab Selector
                CorrelationTabSelector(
                    selectedIndex = selectedTabIndex,
                    onSelect = { selectedTabIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(18.dp))

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "CorrelationTabContent",
                ) { tab ->
                    when (tab) {
                        0 -> VolumeTab(correlation = correlation)
                        1 -> PrsAndRpeTab(correlation = correlation)
                        2 -> RelativeStrengthTab(topLifts = topLifts, currentWeightKg = currentWeightKg)
                    }
                }
            }
        }
    }
}

@Composable
private fun CorrelationTabSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val tabs = listOf(
        stringResource(Res.string.correlation_tab_volume),
        stringResource(Res.string.correlation_tab_prs),
        stringResource(Res.string.correlation_tab_strength),
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
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = typography.caption.copy(fontSize = 12.sp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onAccent else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun VolumeTab(correlation: CycleWorkoutCorrelation?) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    if (correlation == null) return

    val maxAvgVolume = correlation.phaseSummaries
        .maxOfOrNull { it.avgVolumePerWorkoutKg }
        ?.coerceAtLeast(1.0) ?: 1.0

    val activePeakPhase = correlation.peakAvgVolumePhase ?: correlation.peakVolumePhase

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hormonal Volume Insight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.10f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🧬", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.correlation_volume_insight_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                val diffPercent = correlation.anabolicVsLutealVolumeDiffPercent
                val insightBody = when {
                    diffPercent != null && diffPercent > 0 -> {
                        stringResource(Res.string.correlation_volume_anabolic_higher_format, diffPercent)
                    }
                    diffPercent != null && diffPercent < 0 -> {
                        stringResource(Res.string.correlation_volume_luteal_higher_format, abs(diffPercent))
                    }
                    activePeakPhase != null -> {
                        val peakSummary = correlation.phaseSummaries.firstOrNull { it.phase == activePeakPhase }
                        val avgStr = formatVolumeInt(peakSummary?.avgVolumePerWorkoutKg ?: 0.0)
                        stringResource(
                            Res.string.correlation_volume_general_insight_format,
                            phaseDisplayName(activePeakPhase),
                            avgStr,
                        )
                    }
                    else -> stringResource(Res.string.correlation_subtitle)
                }

                Text(
                    text = insightBody,
                    style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Peak average volume badge
        activePeakPhase?.let { peakPhase ->
            val peakName = phaseDisplayName(peakPhase)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.glassTint)
                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "🏆", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.correlation_peak_volume_format, peakName),
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                    color = colors.accent,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // 4 Phase Volume Cards
        correlation.phaseSummaries.forEach { summary ->
            PhaseVolumeRow(
                summary = summary,
                maxAvgVolume = maxAvgVolume,
                isPeak = summary.phase == activePeakPhase && summary.avgVolumePerWorkoutKg > 0.0,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PhaseVolumeRow(
    summary: PhaseWorkoutSummary,
    maxAvgVolume: Double,
    isPeak: Boolean,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val phaseName = phaseDisplayName(summary.phase)
    val fraction = (summary.avgVolumePerWorkoutKg / maxAvgVolume).toFloat().coerceIn(0f, 1f)
    val avgFormatted = formatVolumeInt(summary.avgVolumePerWorkoutKg)
    val totalFormatted = formatVolumeInt(summary.totalVolumeKg)

    val phaseColor = phaseColorFor(summary.phase)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.glassTint)
            .border(
                0.5.dp,
                if (isPeak) colors.accent.copy(alpha = 0.45f) else colors.glassBorder,
                RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(phaseColor),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = phaseName,
                        style = typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                    )
                }

                Text(
                    text = stringResource(Res.string.correlation_avg_volume_format, avgFormatted),
                    style = typography.subhead.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                    color = if (isPeak) colors.accent else colors.textPrimary,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Visual Progress Bar (scaled by session average volume)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.glassBorder.copy(alpha = 0.35f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isPeak) {
                                Brush.horizontalGradient(
                                    listOf(phaseColor, phaseColor.copy(alpha = 0.85f))
                                )
                            } else {
                                SolidColor(phaseColor)
                            }
                        ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.correlation_workouts_count_format, summary.workoutCount),
                    style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                    color = colors.textSecondary,
                )
                if (summary.workoutCount > 0) {
                    Text(
                        text = stringResource(Res.string.correlation_total_volume_format, totalFormatted),
                        style = typography.caption.copy(fontSize = 11.5.sp),
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = phaseVolumeDescription(summary.phase),
                style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                color = colors.textSecondary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun PrsAndRpeTab(correlation: CycleWorkoutCorrelation?) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    if (correlation == null) return

    val totalPrs = correlation.prDistribution.values.sum()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Anabolic PR Window Insight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.10f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "⚡", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.correlation_prs_anabolic_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                val prInsightText = if (totalPrs > 0) {
                    stringResource(
                        Res.string.correlation_prs_anabolic_insight_format,
                        correlation.anabolicPrPercentage,
                    )
                } else {
                    stringResource(Res.string.correlation_prs_general_insight)
                }

                Text(
                    text = prInsightText,
                    style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // PR distribution summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassTint)
                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.correlation_prs_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.correlation_total_prs_format, totalPrs),
                        style = typography.headline.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                }

                Spacer(Modifier.height(12.dp))

                CyclePhase.entries.forEach { phase ->
                    val prCount = correlation.prDistribution[phase] ?: 0
                    val phaseName = phaseDisplayName(phase)
                    val fraction = if (totalPrs > 0) prCount.toFloat() / totalPrs else 0f
                    val sharePct = if (totalPrs > 0) round((prCount.toDouble() / totalPrs) * 100.0).toInt() else 0
                    val dotColor = phaseColorFor(phase)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(122.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = phaseName,
                                style = typography.caption.copy(fontSize = 12.sp),
                                color = colors.textSecondary,
                                maxLines = 1,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.glassBorder.copy(alpha = 0.35f)),
                        ) {
                            if (fraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(dotColor),
                                )
                            }
                        }

                        Text(
                            text = stringResource(Res.string.correlation_prs_share_format, prCount, sharePct),
                            style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(78.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Luteal RPE & Fatigue Shift Card
        val rpeDiff = correlation.lutealRpeDifference
        val volumeDiff = correlation.anabolicVsLutealVolumeDiffPercent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassTint)
                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.correlation_luteal_rpe_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(10.dp))

                val insightText = when {
                    rpeDiff != null && rpeDiff > 0.0 -> {
                        stringResource(
                            Res.string.correlation_rpe_insight_format,
                            formatSignedOneDecimal(rpeDiff),
                        )
                    }
                    rpeDiff != null -> {
                        stringResource(Res.string.correlation_luteal_rpe_normal)
                    }
                    volumeDiff != null -> {
                        val shiftStr = if (volumeDiff > 0) "-$volumeDiff" else "+${abs(volumeDiff)}"
                        stringResource(Res.string.correlation_luteal_volume_fallback_format, shiftStr)
                    }
                    else -> {
                        stringResource(Res.string.correlation_luteal_physiology_fallback) +
                            "\n\n" +
                            stringResource(Res.string.correlation_no_rpe_hint)
                    }
                }

                Text(
                    text = insightText,
                    style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelativeStrengthTab(
    topLifts: List<RelativeStrengthProgress>,
    currentWeightKg: Double,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
        // Educational Guide Card explaining 1RM, Relative Strength & Cycle Edema
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.10f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "💡", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.correlation_strength_guide_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.correlation_strength_guide_body),
                    style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.5.sp),
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.correlation_strength_edema_note),
                        style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.5.sp),
                        color = colors.textSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.correlation_relative_strength_title),
            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(10.dp))

        if (topLifts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.glassTint)
                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.correlation_no_compound_lifts),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        } else {
            topLifts.forEach { lift ->
                val formatted1Rm = formatOneDecimal(lift.estimated1RmKg)
                val formattedBestWeight = formatOneDecimal(lift.bestWeightKg)
                val ratioFormatted = formatTwoDecimals(lift.relativeRatio)
                val tierLabel = strengthTierDisplayName(lift.tier)
                val tierColor = strengthTierColor(lift.tier)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lift.exerciseName,
                                    style = typography.body.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                )

                                Spacer(Modifier.height(6.dp))

                                // Badges: Strength Tier + Achieved Cycle Phase
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (currentWeightKg > 0.0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(tierColor.copy(alpha = 0.14f))
                                                .border(0.5.dp, tierColor.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                text = tierLabel,
                                                style = typography.caption.copy(
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
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
                                                style = typography.caption.copy(
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = phaseColor,
                                            )
                                        }
                                    }
                                }
                            }

                            if (currentWeightKg > 0.0) {
                                Spacer(Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accent.copy(alpha = 0.15f))
                                            .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.correlation_bw_ratio_format, ratioFormatted),
                                            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                                            color = colors.accent,
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(
                                            Res.string.correlation_bw_ratio_explained_format,
                                            ratioFormatted,
                                        ),
                                        style = typography.caption.copy(fontSize = 11.sp),
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Divider line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(colors.glassBorder.copy(alpha = 0.5f)),
                        )

                        Spacer(Modifier.height(8.dp))

                        // Bottom stats: Est. 1RM and Actual Best Set
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.correlation_est_1rm_format, formatted1Rm),
                                style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
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
                    }
                }
            }
        }
    }
}

private fun computeTopLifts(
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double,
    cycleStartEpochDays: Long?,
    cycleLength: Int,
): List<RelativeStrengthProgress> {
    val targetLiftKeywords = listOf(
        "Squat" to listOf("squat"),
        "Deadlift" to listOf("deadlift", "rdl"),
        "Hip Thrust" to listOf("hip thrust"),
        "Bench Press" to listOf("bench press", "chest press"),
        "Overhead Press" to listOf("overhead press", "military press", "ohp"),
    )

    val results = mutableListOf<RelativeStrengthProgress>()

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

            results.add(
                calculateRelativeStrength(
                    bodyWeightKg = currentWeightKg,
                    exerciseName = label,
                    weightLiftedKg = bestWeight,
                    reps = bestReps,
                    achievedInPhase = achievedPhase,
                )
            )
        }
    }

    return results
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
private fun phaseVolumeDescription(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> stringResource(Res.string.correlation_phase_desc_menstrual)
        CyclePhase.FOLLICULAR -> stringResource(Res.string.correlation_phase_desc_follicular)
        CyclePhase.OVULATION -> stringResource(Res.string.correlation_phase_desc_ovulation)
        CyclePhase.LUTEAL -> stringResource(Res.string.correlation_phase_desc_luteal)
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

private fun formatSignedOneDecimal(value: Double): String {
    val formatted = formatOneDecimal(value)
    return if (value > 0.0 && !formatted.startsWith("+")) "+$formatted" else formatted
}
