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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.kilo.domain.CycleDayInfo
import app.kilo.domain.CyclePhase
import app.kilo.domain.CycleState
import app.kilo.domain.CycleWorkoutCorrelation
import app.kilo.domain.PhaseWorkoutSummary
import app.kilo.domain.WorkoutSessionSummary
import app.kilo.domain.analyzeCycleWorkoutCorrelation
import app.kilo.domain.computeCycleDayInfo
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.correlation_avg_volume_format
import kilo.composeapp.generated.resources.correlation_luteal_physiology_fallback
import kilo.composeapp.generated.resources.correlation_luteal_rpe_card_title
import kilo.composeapp.generated.resources.correlation_luteal_rpe_normal
import kilo.composeapp.generated.resources.correlation_luteal_volume_fallback_format
import kilo.composeapp.generated.resources.correlation_no_rpe_hint
import kilo.composeapp.generated.resources.correlation_open_button
import kilo.composeapp.generated.resources.correlation_peak_volume_format
import kilo.composeapp.generated.resources.correlation_phase_desc_follicular
import kilo.composeapp.generated.resources.correlation_phase_desc_luteal
import kilo.composeapp.generated.resources.correlation_phase_desc_menstrual
import app.kilo.domain.PrDistributionPattern
import kilo.composeapp.generated.resources.correlation_phase_desc_ovulation
import kilo.composeapp.generated.resources.correlation_prs_anabolic_insight_format
import kilo.composeapp.generated.resources.correlation_prs_anabolic_title
import kilo.composeapp.generated.resources.correlation_prs_card_title
import kilo.composeapp.generated.resources.correlation_prs_general_insight
import kilo.composeapp.generated.resources.correlation_prs_pattern_anabolic_dominant
import kilo.composeapp.generated.resources.correlation_prs_pattern_balanced
import kilo.composeapp.generated.resources.correlation_prs_pattern_follicular_dominant
import kilo.composeapp.generated.resources.correlation_prs_pattern_follicular_luteal
import kilo.composeapp.generated.resources.correlation_prs_pattern_luteal_dominant
import kilo.composeapp.generated.resources.correlation_prs_pattern_menstrual_dominant
import kilo.composeapp.generated.resources.correlation_prs_pattern_ovulation_dominant
import kilo.composeapp.generated.resources.correlation_prs_share_format
import kilo.composeapp.generated.resources.correlation_rpe_insight_format
import kilo.composeapp.generated.resources.correlation_subtitle
import kilo.composeapp.generated.resources.correlation_total_prs_format
import kilo.composeapp.generated.resources.correlation_total_volume_format
import kilo.composeapp.generated.resources.correlation_volume_anabolic_higher_format
import kilo.composeapp.generated.resources.correlation_volume_general_insight_format
import kilo.composeapp.generated.resources.correlation_volume_insight_title
import kilo.composeapp.generated.resources.correlation_volume_luteal_higher_format
import kilo.composeapp.generated.resources.correlation_workouts_count_format
import kilo.composeapp.generated.resources.cycle_back_action
import kilo.composeapp.generated.resources.cycle_correlation_empty_cta
import kilo.composeapp.generated.resources.cycle_date_picker_prompt
import kilo.composeapp.generated.resources.cycle_day_counter
import kilo.composeapp.generated.resources.cycle_inline_correlation_header
import kilo.composeapp.generated.resources.cycle_inline_correlation_subtitle
import kilo.composeapp.generated.resources.cycle_last_period_date
import kilo.composeapp.generated.resources.cycle_length_days_format
import kilo.composeapp.generated.resources.cycle_length_desc
import kilo.composeapp.generated.resources.cycle_length_title
import kilo.composeapp.generated.resources.cycle_medical_disclaimer
import kilo.composeapp.generated.resources.cycle_remove_cta
import kilo.composeapp.generated.resources.cycle_section_avoid
import kilo.composeapp.generated.resources.cycle_section_body
import kilo.composeapp.generated.resources.cycle_section_eat
import kilo.composeapp.generated.resources.cycle_section_hormones
import kilo.composeapp.generated.resources.cycle_sheet_title
import kilo.composeapp.generated.resources.cycle_source_acog_desc
import kilo.composeapp.generated.resources.cycle_source_acog_title
import kilo.composeapp.generated.resources.cycle_source_baker_solomon_desc
import kilo.composeapp.generated.resources.cycle_source_baker_solomon_title
import kilo.composeapp.generated.resources.cycle_source_herzberg_acog_desc
import kilo.composeapp.generated.resources.cycle_source_herzberg_acog_title
import kilo.composeapp.generated.resources.cycle_source_mcnulty_desc
import kilo.composeapp.generated.resources.cycle_source_mcnulty_title
import kilo.composeapp.generated.resources.cycle_source_sims_desc
import kilo.composeapp.generated.resources.cycle_source_sims_title
import kilo.composeapp.generated.resources.cycle_source_stachenfeld_desc
import kilo.composeapp.generated.resources.cycle_source_stachenfeld_title
import kilo.composeapp.generated.resources.cycle_sources_title
import kilo.composeapp.generated.resources.cycle_tab_body
import kilo.composeapp.generated.resources.cycle_tab_nutrition
import kilo.composeapp.generated.resources.cycle_tab_sources
import kilo.composeapp.generated.resources.cycle_tab_workout
import kilo.composeapp.generated.resources.cycle_update_period_cta
import kilo.composeapp.generated.resources.cycle_water_retention_alert
import kilo.composeapp.generated.resources.cycle_workout_focus_section
import kilo.composeapp.generated.resources.cycle_workout_physiology_section
import kilo.composeapp.generated.resources.cycle_workout_strategy_title
import kilo.composeapp.generated.resources.hevy_import_button
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun CycleScreen(
    isOpen: Boolean = true,
    state: CycleState,
    workouts: List<HevyWorkoutSession> = emptyList(),
    hasWorkouts: Boolean = workouts.isNotEmpty(),
    onImportHevyCsv: (String) -> Unit = {},
    onOpenCorrelation: () -> Unit = {},
    onUpdateCycleLength: (Int) -> Unit = {},
    onUpdateLastPeriodDate: (LocalDate) -> Unit,
    onDisableCycle: () -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
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
    var isDatePickerOpen by remember { mutableStateOf(false) }

    val today = remember(timeZone) {
        val now = kotlin.time.Clock.System.now()
        now.toLocalDateTime(timeZone).date
    }

    val lastPeriod = state.lastPeriodDate ?: today
    val cycleLength = state.cycleLength.coerceIn(21, 45)
    val periodDuration = state.periodDuration.coerceIn(2, 10)

    var selectedDayOffset by remember(state.currentDayInfo) {
        mutableStateOf(((state.currentDayInfo?.cycleDay ?: 1) - 1).coerceIn(0, cycleLength - 1))
    }

    val currentCycleDays = remember(lastPeriod, cycleLength, periodDuration, today) {
        (0 until cycleLength).map { offset ->
            val target = lastPeriod.plus(offset, DateTimeUnit.DAY)
            computeCycleDayInfo(
                lastPeriodDate = lastPeriod,
                targetDate = target,
                cycleLength = cycleLength,
                periodDuration = periodDuration,
                today = today,
            )
        }
    }

    val activeDayInfo = currentCycleDays.getOrNull(selectedDayOffset) ?: state.currentDayInfo ?: currentCycleDays.first()

    // Compute inline cycle-workout correlation directly inside CycleScreen
    val cycleCorrelation: CycleWorkoutCorrelation? = remember(workouts, lastPeriod, cycleLength) {
        if (workouts.isEmpty()) {
            null
        } else {
            val summaries = workouts.map { session ->
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
            analyzeCycleWorkoutCorrelation(
                workouts = summaries,
                cycleStartEpochDays = lastPeriod.toEpochDays().toLong(),
                cycleLength = cycleLength,
            )
        }
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

                Text(
                    text = stringResource(Res.string.cycle_sheet_title),
                    style = typography.title,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(4.dp))

                // Cycle Length Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.glassTint)
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(Res.string.cycle_length_title),
                            style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp),
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.cycle_length_desc),
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.textSecondary,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.glassBorder.copy(alpha = 0.25f))
                            .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(colors.glassTint)
                                .clickable(role = Role.Button) {
                                    val newLength = (cycleLength - 1).coerceIn(21, 45)
                                    onUpdateCycleLength(newLength)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "−", style = typography.subhead.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        }

                        Text(
                            text = stringResource(Res.string.cycle_length_days_format, cycleLength),
                            style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                            color = colors.accent,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(colors.glassTint)
                                .clickable(role = Role.Button) {
                                    val newLength = (cycleLength + 1).coerceIn(21, 45)
                                    onUpdateCycleLength(newLength)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "+", style = typography.subhead.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 28-Day (or cycleLength-day) Cycle Strip Selector
                CycleDaySelectorStrip(
                    days = currentCycleDays,
                    selectedOffset = selectedDayOffset,
                    onSelectOffset = { selectedDayOffset = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(18.dp))

                // Segmented Tab Selector (Beden | Beslenme | Ağırlık | Kaynaklar)
                CycleTabSelector(
                    selectedIndex = selectedTabIndex,
                    onSelect = { selectedTabIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(20.dp))

                // Tab Content
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "CycleTabContent",
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> BodyAndHormoneTab(info = activeDayInfo)
                        1 -> NutritionTab(info = activeDayInfo)
                        2 -> WorkoutTab(
                            info = activeDayInfo,
                            correlation = cycleCorrelation,
                            hasWorkouts = hasWorkouts || workouts.isNotEmpty(),
                            onImportCsvClick = { hevyPicker.launch() },
                            onOpenCorrelation = onOpenCorrelation,
                        )
                        3 -> SourcesTab()
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Bottom Actions: Primary Update Date & Secondary Disable Guide
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Primary Action: Update Last Period Date
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accent.copy(alpha = 0.15f))
                            .border(0.5.dp, colors.accent.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(Res.string.cycle_update_period_cta),
                                onClick = { isDatePickerOpen = true },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "📅", fontSize = 15.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.cycle_update_period_cta),
                                style = typography.headline.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent,
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Secondary Destructive Action: Turn Off Cycle Guide
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.danger.copy(alpha = 0.08f))
                            .border(0.5.dp, colors.danger.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(Res.string.cycle_remove_cta),
                                onClick = {
                                    onDisableCycle()
                                    onDismiss()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.cycle_remove_cta),
                            style = typography.caption.copy(fontSize = 12.5.sp),
                            fontWeight = FontWeight.Medium,
                            color = colors.danger,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Modal Date Picker for selecting last period date
        if (isDatePickerOpen) {
            val lastMillis = remember(lastPeriod, timeZone) {
                lastPeriod.atTime(12, 0).toInstant(timeZone).toEpochMilliseconds()
            }
            IosDatePicker(
                selectedDateMillis = lastMillis,
                title = stringResource(Res.string.cycle_last_period_date),
                subtitle = stringResource(Res.string.cycle_date_picker_prompt),
                onDateSelected = { millis ->
                    val instant = Instant.fromEpochMilliseconds(millis)
                    val newDate = instant.toLocalDateTime(TimeZone.UTC).date
                    onUpdateLastPeriodDate(newDate)
                    isDatePickerOpen = false
                },
                onDismiss = { isDatePickerOpen = false },
                hazeState = hazeState,
                timeZone = timeZone,
            )
        }
    }
}

/**
 * Backward-compatible alias for [CycleScreen].
 */
@Composable
fun CycleSheet(
    isOpen: Boolean,
    state: CycleState,
    workouts: List<HevyWorkoutSession> = emptyList(),
    hasWorkouts: Boolean = workouts.isNotEmpty(),
    onImportHevyCsv: (String) -> Unit = {},
    onOpenCorrelation: () -> Unit = {},
    onUpdateCycleLength: (Int) -> Unit = {},
    onUpdateLastPeriodDate: (LocalDate) -> Unit,
    onDisableCycle: () -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    modifier: Modifier = Modifier,
) {
    CycleScreen(
        isOpen = isOpen,
        state = state,
        workouts = workouts,
        hasWorkouts = hasWorkouts,
        onImportHevyCsv = onImportHevyCsv,
        onOpenCorrelation = onOpenCorrelation,
        onUpdateCycleLength = onUpdateCycleLength,
        onUpdateLastPeriodDate = onUpdateLastPeriodDate,
        onDisableCycle = onDisableCycle,
        onDismiss = onDismiss,
        hazeState = hazeState,
        timeZone = timeZone,
        modifier = modifier,
    )
}

@Composable
private fun CycleDaySelectorStrip(
    days: List<CycleDayInfo>,
    selectedOffset: Int,
    onSelectOffset: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val listState = rememberLazyListState()

    LaunchedEffect(selectedOffset) {
        val targetIndex = (selectedOffset - 3).coerceAtLeast(0)
        listState.animateScrollToItem(targetIndex)
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(days.size) { index ->
            val dayInfo = days[index]
            val isSelected = index == selectedOffset
            val phaseColor = when (dayInfo.phase) {
                CyclePhase.MENSTRUAL -> colors.danger
                CyclePhase.FOLLICULAR -> colors.accent
                CyclePhase.OVULATION -> colors.positive
                CyclePhase.LUTEAL -> colors.neutral
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) colors.accent
                        else colors.glassTint
                    )
                    .border(
                        1.dp,
                        if (isSelected) colors.glassSpecular else colors.glassBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(role = Role.Tab) { onSelectOffset(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${dayInfo.cycleDay}",
                        style = typography.headline.copy(fontSize = 15.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.onAccent else colors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.onAccent else phaseColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleTabSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val tabs = listOf(
        stringResource(Res.string.cycle_tab_body),
        stringResource(Res.string.cycle_tab_nutrition),
        stringResource(Res.string.cycle_tab_workout),
        stringResource(Res.string.cycle_tab_sources),
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
                    .padding(vertical = 9.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = typography.caption.copy(fontSize = 11.5.sp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onAccent else colors.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BodyAndHormoneTab(info: CycleDayInfo) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val advice = info.advice

    Column(modifier = Modifier.fillMaxWidth()) {
        // Phase Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = cyclePhaseEmoji(info.phase), fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = cyclePhaseName(info.phase),
                        style = typography.headline,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.cycle_day_counter, info.cycleDay, info.totalDays),
                        style = typography.caption,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Water retention warning if applicable
        if (advice.waterRetentionRisk) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.positive.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "💧", fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(Res.string.cycle_water_retention_alert),
                    style = typography.caption,
                    fontWeight = FontWeight.Medium,
                    color = colors.positive,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // What's Happening in Your Body
        InfoCard(
            title = stringResource(Res.string.cycle_section_body),
            headline = resolveCycleString(advice.titleKey),
            body = resolveCycleString(advice.bodySummaryKey),
            emoji = "🧬",
        )

        Spacer(Modifier.height(12.dp))

        // Hormonal State
        InfoCard(
            title = stringResource(Res.string.cycle_section_hormones),
            headline = null,
            body = resolveCycleString(advice.hormoneInsightKey),
            emoji = "🔬",
        )

        Spacer(Modifier.height(12.dp))

        // Medical Disclaimer
        Text(
            text = stringResource(Res.string.cycle_medical_disclaimer),
            style = typography.caption.copy(fontSize = 11.sp),
            color = colors.textSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun NutritionTab(info: CycleDayInfo) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val advice = info.advice

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section: Eat
        Text(
            text = "🥗 ${stringResource(Res.string.cycle_section_eat)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        advice.eatKeys.forEach { key ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.negative.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "✓", color = colors.negative, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = resolveCycleString(key),
                    style = typography.subhead,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Section: Avoid
        Text(
            text = "🚫 ${stringResource(Res.string.cycle_section_avoid)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        advice.avoidKeys.forEach { key ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.positive.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "✕", color = colors.positive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = resolveCycleString(key),
                    style = typography.subhead,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

@Composable
private fun WorkoutTab(
    info: CycleDayInfo,
    correlation: CycleWorkoutCorrelation?,
    hasWorkouts: Boolean = false,
    onImportCsvClick: () -> Unit = {},
    onOpenCorrelation: () -> Unit = {},
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val advice = info.advice

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Daily Hormonal Intensity Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = workoutIntensityEmoji(advice.workoutIntensity), fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = workoutIntensityName(advice.workoutIntensity),
                        style = typography.headline,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.cycle_workout_strategy_title),
                        style = typography.caption,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Workout Caution (if any)
        advice.workoutCautionKey?.let { cautionKey ->
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.danger.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "⚠️", fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = resolveCycleString(cautionKey),
                    style = typography.caption,
                    fontWeight = FontWeight.Medium,
                    color = colors.danger,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Lifting Strategy Card
        InfoCard(
            title = stringResource(Res.string.cycle_workout_strategy_title),
            headline = resolveCycleString(advice.workoutTitleKey),
            body = resolveCycleString(advice.workoutDescKey),
            emoji = "🏋️",
        )

        Spacer(Modifier.height(14.dp))

        // Target Focus & Exercises
        Text(
            text = "🎯 ${stringResource(Res.string.cycle_workout_focus_section)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        advice.workoutFocusKeys.forEach { focusKey ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accent.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "⚡", fontSize = 13.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = resolveCycleString(focusKey),
                    style = typography.subhead,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Biomechanics & Muscle Insight Card
        InfoCard(
            title = stringResource(Res.string.cycle_workout_physiology_section),
            headline = null,
            body = resolveCycleString(advice.hormoneInsightKey),
            emoji = "🔬",
        )

        Spacer(Modifier.height(22.dp))

        // 2. Embedded Cycle & Workout Correlation Section (Self-Contained inside CycleScreen)
        Text(
            text = "📊 ${stringResource(Res.string.cycle_inline_correlation_header)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.cycle_inline_correlation_subtitle),
            style = typography.caption.copy(fontSize = 12.sp, lineHeight = 16.5.sp),
            color = colors.textSecondary,
        )

        Spacer(Modifier.height(12.dp))

        if (correlation == null || !hasWorkouts) {
            // Empty state inside CycleScreen WorkoutTab with direct CSV upload
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.glassTint)
                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.cycle_correlation_empty_cta),
                        style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 17.5.sp),
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accent)
                            .clickable(role = Role.Button, onClick = onImportCsvClick)
                            .padding(vertical = 11.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.hevy_import_button),
                            style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                            color = colors.onAccent,
                        )
                    }
                }
            }
        } else {
            InlineCycleCorrelationContent(
                correlation = correlation,
                onOpenUniversalStrength = onOpenCorrelation,
            )
        }
    }
}

@Composable
private fun InlineCycleCorrelationContent(
    correlation: CycleWorkoutCorrelation,
    onOpenUniversalStrength: () -> Unit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val maxAvgVolume = correlation.phaseSummaries
        .maxOfOrNull { it.avgVolumePerWorkoutKg }
        ?.coerceAtLeast(1.0) ?: 1.0
    val activePeakPhase = correlation.peakAvgVolumePhase ?: correlation.peakVolumePhase
    val totalPrs = correlation.prDistribution.values.sum()

    Column(modifier = Modifier.fillMaxWidth()) {
        // A. Hormonal Volume Insight Card
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
                    Text(text = "🧬", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
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
                        stringResource(Res.string.correlation_volume_anabolic_higher_format, "${diffPercent}%")
                    }
                    diffPercent != null && diffPercent < 0 -> {
                        stringResource(Res.string.correlation_volume_luteal_higher_format, "${abs(diffPercent)}%")
                    }
                    activePeakPhase != null -> {
                        val peakSummary = correlation.phaseSummaries.firstOrNull { it.phase == activePeakPhase }
                        val avgStr = formatCycleVolumeInt(peakSummary?.avgVolumePerWorkoutKg ?: 0.0)
                        stringResource(
                            Res.string.correlation_volume_general_insight_format,
                            cyclePhaseName(activePeakPhase),
                            avgStr,
                        )
                    }
                    else -> stringResource(Res.string.correlation_subtitle)
                }

                Text(
                    text = insightBody,
                    style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.5.sp),
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        activePeakPhase?.let { peakPhase ->
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
                    text = stringResource(Res.string.correlation_peak_volume_format, cyclePhaseName(peakPhase)),
                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                    color = colors.accent,
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // 4 Phase Volume Cards
        correlation.phaseSummaries.forEach { summary ->
            CyclePhaseVolumeCard(
                summary = summary,
                maxAvgVolume = maxAvgVolume,
                isPeak = summary.phase == activePeakPhase && summary.avgVolumePerWorkoutKg > 0.0,
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(4.dp))

        // B. Anabolic PR Window & PR Distribution Card
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
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                }

                Spacer(Modifier.height(6.dp))

                val prInsightText = when (correlation.prPattern) {
                    PrDistributionPattern.EMPTY -> {
                        stringResource(Res.string.correlation_prs_general_insight)
                    }
                    PrDistributionPattern.FOLLICULAR_LUTEAL_DUAL -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_follicular_luteal,
                            "${correlation.peakPrPercentage}%",
                            "${correlation.secondPeakPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.LUTEAL_DOMINANT -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_luteal_dominant,
                            "${correlation.peakPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.ANABOLIC_DOMINANT -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_anabolic_dominant,
                            "${correlation.anabolicPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.FOLLICULAR_DOMINANT -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_follicular_dominant,
                            "${correlation.peakPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.OVULATION_DOMINANT -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_ovulation_dominant,
                            "${correlation.peakPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.MENSTRUAL_DOMINANT -> {
                        stringResource(
                            Res.string.correlation_prs_pattern_menstrual_dominant,
                            "${correlation.peakPrPercentage}%",
                        )
                    }
                    PrDistributionPattern.BALANCED -> {
                        val topPhaseName = correlation.peakPrPhase?.let { cyclePhaseName(it) } ?: ""
                        stringResource(
                            Res.string.correlation_prs_pattern_balanced,
                            topPhaseName,
                            "${correlation.peakPrPercentage}%",
                        )
                    }
                }

                Text(
                    text = prInsightText,
                    style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.5.sp),
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(10.dp))

                CyclePhase.entries.forEach { phase ->
                    val prCount = correlation.prDistribution[phase] ?: 0
                    val phaseName = cyclePhaseName(phase)
                    val fraction = if (totalPrs > 0) prCount.toFloat() / totalPrs else 0f
                    val sharePct = if (totalPrs > 0) round((prCount.toDouble() / totalPrs) * 100.0).toInt() else 0
                    val dotColor = cyclePhaseColor(phase)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(118.dp),
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
                                style = typography.caption.copy(fontSize = 11.5.sp),
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
                            text = stringResource(Res.string.correlation_prs_share_format, prCount, "${sharePct}%"),
                            style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(74.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // C. Luteal RPE & Thermoregulation Card
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
                    Text(text = "🔥", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.correlation_luteal_rpe_card_title),
                        style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                val insightText = when {
                    rpeDiff != null && rpeDiff > 0.0 -> {
                        stringResource(
                            Res.string.correlation_rpe_insight_format,
                            formatCycleSignedOneDecimal(rpeDiff),
                        )
                    }
                    rpeDiff != null -> {
                        stringResource(Res.string.correlation_luteal_rpe_normal)
                    }
                    volumeDiff != null -> {
                        val shiftStr = if (volumeDiff > 0) "-${volumeDiff}%" else "+${abs(volumeDiff)}%"
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
                    style = typography.caption.copy(fontSize = 12.sp, lineHeight = 17.5.sp),
                    color = colors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Link to Universal 1RM & Normative Scales Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.accent.copy(alpha = 0.14f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onOpenUniversalStrength)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${stringResource(Res.string.correlation_open_button)} →",
                style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                color = colors.accent,
            )
        }
    }
}

@Composable
private fun CyclePhaseVolumeCard(
    summary: PhaseWorkoutSummary,
    maxAvgVolume: Double,
    isPeak: Boolean,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val phaseName = cyclePhaseName(summary.phase)
    val fraction = (summary.avgVolumePerWorkoutKg / maxAvgVolume).toFloat().coerceIn(0f, 1f)
    val avgFormatted = formatCycleVolumeInt(summary.avgVolumePerWorkoutKg)
    val totalFormatted = formatCycleVolumeInt(summary.totalVolumeKg)
    val phaseColor = cyclePhaseColor(summary.phase)

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
                            .size(9.dp)
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
                    text = stringResource(Res.string.correlation_avg_volume_format, avgFormatted),
                    style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                    color = if (isPeak) colors.accent else colors.textPrimary,
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.glassBorder.copy(alpha = 0.35f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isPeak) {
                                Brush.horizontalGradient(listOf(phaseColor, phaseColor.copy(alpha = 0.85f)))
                            } else {
                                SolidColor(phaseColor)
                            }
                        ),
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.correlation_workouts_count_format, summary.workoutCount),
                    style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = colors.textSecondary,
                )
                if (summary.workoutCount > 0) {
                    Text(
                        text = stringResource(Res.string.correlation_total_volume_format, totalFormatted),
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = cyclePhaseVolumeDescription(summary.phase),
                style = typography.caption.copy(fontSize = 11.sp, lineHeight = 15.5.sp),
                color = colors.textSecondary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun SourcesTab() {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📚 ${stringResource(Res.string.cycle_sources_title)}",
            style = typography.headline,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(12.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_mcnulty_title),
            desc = stringResource(Res.string.cycle_source_mcnulty_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_stachenfeld_title),
            desc = stringResource(Res.string.cycle_source_stachenfeld_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_baker_solomon_title),
            desc = stringResource(Res.string.cycle_source_baker_solomon_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_herzberg_acog_title),
            desc = stringResource(Res.string.cycle_source_herzberg_acog_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_acog_title),
            desc = stringResource(Res.string.cycle_source_acog_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_sims_title),
            desc = stringResource(Res.string.cycle_source_sims_desc),
        )
    }
}

@Composable
private fun SourceItem(
    title: String,
    authors: String? = null,
    desc: String,
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
            if (!authors.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = authors,
                    style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.accent,
                    lineHeight = 15.5.sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = desc,
                style = typography.caption,
                color = colors.textSecondary,
                lineHeight = 16.5.sp,
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, headline: String?, body: String, emoji: String) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = typography.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent,
                )
            }
            if (headline != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = headline,
                    style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                style = typography.caption.copy(fontSize = 13.sp),
                color = colors.textSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun cyclePhaseVolumeDescription(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> stringResource(Res.string.correlation_phase_desc_menstrual)
        CyclePhase.FOLLICULAR -> stringResource(Res.string.correlation_phase_desc_follicular)
        CyclePhase.OVULATION -> stringResource(Res.string.correlation_phase_desc_ovulation)
        CyclePhase.LUTEAL -> stringResource(Res.string.correlation_phase_desc_luteal)
    }
}

@Composable
private fun cyclePhaseColor(phase: CyclePhase): Color {
    val colors = KiloTheme.colors
    return when (phase) {
        CyclePhase.MENSTRUAL -> colors.danger
        CyclePhase.FOLLICULAR -> colors.accent
        CyclePhase.OVULATION -> colors.positive
        CyclePhase.LUTEAL -> colors.neutral
    }
}

private fun formatCycleVolumeInt(volume: Double): String {
    return if (volume >= 1000.0) {
        val thousands = volume / 1000.0
        "${formatCycleOneDecimal(thousands)}k"
    } else {
        "${round(volume).toInt()}"
    }
}

private fun formatCycleOneDecimal(value: Double): String {
    val tenthsTotal = round(value * 10.0).toInt()
    val sign = if (tenthsTotal < 0) "-" else ""
    val absTenths = abs(tenthsTotal)
    val whole = absTenths / 10
    val frac = absTenths % 10
    return "$sign$whole.$frac"
}

private fun formatCycleSignedOneDecimal(value: Double): String {
    val formatted = formatCycleOneDecimal(value)
    return if (value > 0.0 && !formatted.startsWith("+")) "+$formatted" else formatted
}
