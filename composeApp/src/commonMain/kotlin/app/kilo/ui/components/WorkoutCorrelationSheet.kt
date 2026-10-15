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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.kilo.domain.WorkoutSessionSummary
import app.kilo.domain.analyzeCycleWorkoutCorrelation
import app.kilo.domain.calculateRelativeStrength
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.correlation_avg_volume_format
import kilo.composeapp.generated.resources.correlation_bw_ratio_format
import kilo.composeapp.generated.resources.correlation_empty_desc
import kilo.composeapp.generated.resources.correlation_est_1rm_format
import kilo.composeapp.generated.resources.correlation_luteal_rpe_card_title
import kilo.composeapp.generated.resources.correlation_luteal_rpe_higher
import kilo.composeapp.generated.resources.correlation_luteal_rpe_normal
import kilo.composeapp.generated.resources.correlation_no_compound_lifts
import kilo.composeapp.generated.resources.correlation_no_data
import kilo.composeapp.generated.resources.correlation_no_rpe_hint
import kilo.composeapp.generated.resources.correlation_peak_volume_format
import kilo.composeapp.generated.resources.correlation_phase_format
import kilo.composeapp.generated.resources.correlation_prs_card_title
import kilo.composeapp.generated.resources.correlation_prs_format
import kilo.composeapp.generated.resources.correlation_relative_strength_title
import kilo.composeapp.generated.resources.correlation_rpe_insight_format
import kilo.composeapp.generated.resources.correlation_subtitle
import kilo.composeapp.generated.resources.correlation_tab_prs
import kilo.composeapp.generated.resources.correlation_tab_strength
import kilo.composeapp.generated.resources.correlation_tab_volume
import kilo.composeapp.generated.resources.correlation_title
import kilo.composeapp.generated.resources.correlation_total_prs_format
import kilo.composeapp.generated.resources.correlation_workouts_count_format
import kilo.composeapp.generated.resources.cycle_phase_follicular
import kilo.composeapp.generated.resources.cycle_phase_luteal
import kilo.composeapp.generated.resources.cycle_phase_menstrual
import kilo.composeapp.generated.resources.cycle_phase_ovulation
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCorrelationSheet(
    isOpen: Boolean,
    workouts: List<HevyWorkoutSession>,
    cycleStartEpochDays: Long?,
    cycleLength: Int = 28,
    currentWeightKg: Double = 0.0,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type

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

    // Compute top lifts & relative strength
    val topLifts = remember(workouts, currentWeightKg) {
        computeTopLifts(workouts, currentWeightKg)
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
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.glassTint)
                        .border(1.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                        .padding(24.dp),
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
            .clip(RoundedCornerShape(14.dp))
            .background(colors.glassTint)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.accent else colors.glassTint.copy(alpha = 0f))
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = typography.caption.copy(fontSize = 11.5.sp),
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

    val maxVolume = correlation.phaseSummaries.maxOfOrNull { it.totalVolumeKg }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = Modifier.fillMaxWidth()) {
        // Peak volume banner
        correlation.peakVolumePhase?.let { peakPhase ->
            val peakName = phaseDisplayName(peakPhase)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent.copy(alpha = 0.12f))
                    .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "🏆", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.correlation_peak_volume_format, peakName),
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.accent,
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // 4 Phase Volume Cards
        correlation.phaseSummaries.forEach { summary ->
            PhaseVolumeRow(
                summary = summary,
                maxVolume = maxVolume,
                isPeak = summary.phase == correlation.peakVolumePhase && summary.totalVolumeKg > 0.0,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PhaseVolumeRow(
    summary: PhaseWorkoutSummary,
    maxVolume: Double,
    isPeak: Boolean,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val phaseName = phaseDisplayName(summary.phase)
    val fraction = (summary.totalVolumeKg / maxVolume).toFloat().coerceIn(0f, 1f)
    val volumeFormatted = formatVolumeInt(summary.totalVolumeKg)

    val phaseColor = when (summary.phase) {
        CyclePhase.MENSTRUAL -> colors.danger
        CyclePhase.FOLLICULAR -> colors.accent
        CyclePhase.OVULATION -> colors.positive
        CyclePhase.LUTEAL -> colors.neutral
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.glassTint)
            .border(
                1.dp,
                if (isPeak) colors.accent.copy(alpha = 0.4f) else colors.glassBorder,
                RoundedCornerShape(14.dp),
            )
            .padding(14.dp),
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(phaseColor),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = phaseName,
                        style = typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                    )
                }

                Text(
                    text = "$volumeFormatted kg",
                    style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = if (isPeak) colors.accent else colors.textPrimary,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Visual Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.glassBorder.copy(alpha = 0.3f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(phaseColor),
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.correlation_workouts_count_format, summary.workoutCount),
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = colors.textSecondary,
                )
                if (summary.workoutCount > 0) {
                    val avgVol = formatVolumeInt(summary.avgVolumePerWorkoutKg)
                    Text(
                        text = stringResource(Res.string.correlation_avg_volume_format, avgVol),
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textSecondary,
                    )
                }
            }
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
        // PR distribution summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassTint)
                .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = phaseName,
                            style = typography.caption.copy(fontSize = 12.sp),
                            color = colors.textSecondary,
                            modifier = Modifier.width(110.dp),
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.glassBorder.copy(alpha = 0.3f)),
                        ) {
                            if (fraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.accent),
                                )
                            }
                        }

                        Text(
                            text = "$prCount",
                            style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Luteal RPE Shift Card
        val rpeDiff = correlation.lutealRpeDifference
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassTint)
                .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.correlation_luteal_rpe_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                val insightText = if (rpeDiff != null && rpeDiff > 0.0) {
                    stringResource(Res.string.correlation_rpe_insight_format, rpeDiff)
                } else if (rpeDiff != null) {
                    stringResource(Res.string.correlation_luteal_rpe_normal)
                } else {
                    stringResource(Res.string.correlation_no_rpe_hint)
                }

                Text(
                    text = insightText,
                    style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun RelativeStrengthTab(
    topLifts: List<RelativeStrengthProgress>,
    currentWeightKg: Double,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.glassTint)
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.glassTint)
                        .border(1.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = lift.exerciseName,
                                style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            val rounded1Rm = round(lift.estimated1RmKg * 10.0) / 10.0
                            Text(
                                text = stringResource(Res.string.correlation_est_1rm_format, rounded1Rm),
                                style = typography.caption,
                                color = colors.textSecondary,
                            )
                        }

                        if (currentWeightKg > 0.0) {
                            val ratioFormatted = round(lift.relativeRatio * 100.0) / 100.0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accent.copy(alpha = 0.15f))
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
                }
            }
        }
    }
}

private fun computeTopLifts(
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double,
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

        for (workout in workouts) {
            for (exercise in workout.exercises) {
                val lowerTitle = exercise.title.lowercase()
                val matches = keywords.any { lowerTitle.contains(it) }
                if (matches) {
                    if (exercise.estimated1RmKg > best1Rm) {
                        best1Rm = exercise.estimated1RmKg
                        val bestSet = exercise.sets.maxByOrNull { it.weightKg }
                        bestWeight = bestSet?.weightKg ?: exercise.maxWeightKg
                        bestReps = bestSet?.reps ?: 1
                    }
                }
            }
        }

        if (best1Rm > 0.0) {
            results.add(
                calculateRelativeStrength(
                    bodyWeightKg = currentWeightKg,
                    exerciseName = label,
                    weightLiftedKg = bestWeight,
                    reps = bestReps,
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

private fun formatVolumeInt(volume: Double): String {
    return if (volume >= 1000.0) {
        val thousands = volume / 1000.0
        val rounded = round(thousands * 10.0) / 10.0
        "${rounded}k"
    } else {
        "${volume.toInt()}"
    }
}
