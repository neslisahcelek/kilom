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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.CycleDayInfo
import app.kilo.domain.CyclePhase
import app.kilo.domain.CycleState
import app.kilo.domain.computeCycleDayInfo
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.cycle_back_action
import kilo.composeapp.generated.resources.cycle_day_counter
import kilo.composeapp.generated.resources.cycle_medical_disclaimer
import kilo.composeapp.generated.resources.cycle_remove_cta
import kilo.composeapp.generated.resources.cycle_section_avoid
import kilo.composeapp.generated.resources.cycle_section_body
import kilo.composeapp.generated.resources.cycle_section_eat
import kilo.composeapp.generated.resources.cycle_section_hormones
import kilo.composeapp.generated.resources.cycle_sheet_title
import kilo.composeapp.generated.resources.cycle_sources_title
import kilo.composeapp.generated.resources.cycle_source_acog_desc
import kilo.composeapp.generated.resources.cycle_source_acog_title
import kilo.composeapp.generated.resources.cycle_source_pubmed_desc
import kilo.composeapp.generated.resources.cycle_source_pubmed_title
import kilo.composeapp.generated.resources.cycle_source_sims_desc
import kilo.composeapp.generated.resources.cycle_source_sims_title
import kilo.composeapp.generated.resources.cycle_tab_body
import kilo.composeapp.generated.resources.cycle_tab_nutrition
import kilo.composeapp.generated.resources.cycle_tab_workout
import kilo.composeapp.generated.resources.cycle_tab_sources
import kilo.composeapp.generated.resources.cycle_workout_focus_section
import kilo.composeapp.generated.resources.cycle_workout_physiology_section
import kilo.composeapp.generated.resources.cycle_workout_strategy_title
import kilo.composeapp.generated.resources.cycle_update_period_cta
import kilo.composeapp.generated.resources.cycle_water_retention_alert
import kilo.composeapp.generated.resources.cycle_date_picker_prompt
import kilo.composeapp.generated.resources.cycle_last_period_date
import kilo.composeapp.generated.resources.cycle_length_title
import kilo.composeapp.generated.resources.cycle_length_desc
import kilo.composeapp.generated.resources.cycle_length_days_format
import kilo.composeapp.generated.resources.correlation_title
import kilo.composeapp.generated.resources.correlation_open_button
import kilo.composeapp.generated.resources.cycle_correlation_empty_cta
import androidx.compose.ui.semantics.Role
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun CycleScreen(
    isOpen: Boolean = true,
    state: CycleState,
    hasWorkouts: Boolean = false,
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
                            hasWorkouts = hasWorkouts,
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
    hasWorkouts: Boolean = false,
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
        hasWorkouts = hasWorkouts,
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
    hasWorkouts: Boolean = false,
    onOpenCorrelation: () -> Unit = {},
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val advice = info.advice

    Column(modifier = Modifier.fillMaxWidth()) {
        // Always show Cycle & Workout correlation entry point (prompts import if empty)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.accent.copy(alpha = 0.12f))
                .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onOpenCorrelation)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "📊", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.correlation_title),
                            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                if (hasWorkouts) {
                                    Res.string.correlation_open_button
                                } else {
                                    Res.string.cycle_correlation_empty_cta
                                }
                            ),
                            style = typography.caption.copy(fontSize = 11.5.sp),
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Text(
                    text = "›",
                    style = typography.title.copy(fontWeight = FontWeight.Light),
                    color = colors.accent,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Intensity Banner
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
            title = stringResource(Res.string.cycle_source_acog_title),
            desc = stringResource(Res.string.cycle_source_acog_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_sims_title),
            desc = stringResource(Res.string.cycle_source_sims_desc),
        )
        Spacer(Modifier.height(10.dp))

        SourceItem(
            title = stringResource(Res.string.cycle_source_pubmed_title),
            desc = stringResource(Res.string.cycle_source_pubmed_desc),
        )
    }
}

@Composable
private fun SourceItem(title: String, desc: String) {
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
            Spacer(Modifier.height(4.dp))
            Text(
                text = desc,
                style = typography.caption,
                color = colors.textSecondary,
                lineHeight = 16.sp,
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
