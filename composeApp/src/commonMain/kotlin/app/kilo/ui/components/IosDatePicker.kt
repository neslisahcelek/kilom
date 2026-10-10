package app.kilo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.platform.rememberHaptics
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.date_picker_cancel
import kilo.composeapp.generated.resources.date_picker_done
import kilo.composeapp.generated.resources.date_today
import kilo.composeapp.generated.resources.date_yesterday
import kilo.composeapp.generated.resources.month_1
import kilo.composeapp.generated.resources.month_10
import kilo.composeapp.generated.resources.month_11
import kilo.composeapp.generated.resources.month_12
import kilo.composeapp.generated.resources.month_2
import kilo.composeapp.generated.resources.month_3
import kilo.composeapp.generated.resources.month_4
import kilo.composeapp.generated.resources.month_5
import kilo.composeapp.generated.resources.month_6
import kilo.composeapp.generated.resources.month_7
import kilo.composeapp.generated.resources.month_8
import kilo.composeapp.generated.resources.month_9
import kilo.composeapp.generated.resources.month_short_1
import kilo.composeapp.generated.resources.month_short_10
import kilo.composeapp.generated.resources.month_short_11
import kilo.composeapp.generated.resources.month_short_12
import kilo.composeapp.generated.resources.month_short_2
import kilo.composeapp.generated.resources.month_short_3
import kilo.composeapp.generated.resources.month_short_4
import kilo.composeapp.generated.resources.month_short_5
import kilo.composeapp.generated.resources.month_short_6
import kilo.composeapp.generated.resources.month_short_7
import kilo.composeapp.generated.resources.month_short_8
import kilo.composeapp.generated.resources.month_short_9
import kilo.composeapp.generated.resources.weekday_fri
import kilo.composeapp.generated.resources.weekday_mon
import kilo.composeapp.generated.resources.weekday_sat
import kilo.composeapp.generated.resources.weekday_sun
import kilo.composeapp.generated.resources.weekday_thu
import kilo.composeapp.generated.resources.weekday_tue
import kilo.composeapp.generated.resources.weekday_wed
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/**
 * Modern Apple iOS Liquid Glass styled Calendar / DatePicker component.
 * Uses [GlassCard], [HazeState], compact chevrons, quick jump pills ("Bugün", "Dün"),
 * 7-column day grid with accent styling, and haptic feedback.
 */
@Composable
fun IosDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val todayLocalDate = remember(timeZone) {
        Clock.System.now().toLocalDateTime(timeZone).date
    }
    val todayEpochDays = remember(todayLocalDate) {
        todayLocalDate.toEpochDays()
    }
    val yesterdayLocalDate = remember(todayEpochDays) {
        LocalDate.fromEpochDays(todayEpochDays - 1)
    }

    val initialDate = remember(selectedDateMillis, todayLocalDate) {
        selectedDateMillis?.let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
        } ?: todayLocalDate
    }

    var displayedYear by remember(initialDate) { mutableStateOf(initialDate.year) }
    var displayedMonth by remember(initialDate) { mutableStateOf(initialDate.monthNumber) }
    var tempSelectedDate by remember(initialDate) { mutableStateOf(initialDate) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click */ },
                ),
        ) {
            GlassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header: Month & Year + Left/Right compact chevron buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${localizedMonthName(displayedMonth)} $displayedYear",
                            style = typography.title.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Previous month chevron
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.glassTint.copy(alpha = 0.65f))
                                    .border(0.5.dp, colors.glassBorder, CircleShape)
                                    .clickable {
                                        if (displayedMonth == 1) {
                                            displayedMonth = 12
                                            displayedYear -= 1
                                        } else {
                                            displayedMonth -= 1
                                        }
                                        haptics.light()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "‹",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }

                            // Next month chevron (disabled if at or past current month)
                            val isCurrentMonthOrFuture = displayedYear > todayLocalDate.year ||
                                (displayedYear == todayLocalDate.year && displayedMonth >= todayLocalDate.monthNumber)

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCurrentMonthOrFuture) colors.glassTint.copy(alpha = 0.25f)
                                        else colors.glassTint.copy(alpha = 0.65f)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isCurrentMonthOrFuture) colors.glassBorder.copy(alpha = 0.3f) else colors.glassBorder,
                                        CircleShape,
                                    )
                                    .clickable(enabled = !isCurrentMonthOrFuture) {
                                        if (displayedMonth == 12) {
                                            displayedMonth = 1
                                            displayedYear += 1
                                        } else {
                                            displayedMonth += 1
                                        }
                                        haptics.light()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "›",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentMonthOrFuture) colors.textSecondary.copy(alpha = 0.3f) else colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Quick Jump Pills: "Bugün" (Today) & "Dün" (Yesterday)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val isSelectedToday = tempSelectedDate == todayLocalDate
                        val isSelectedYesterday = tempSelectedDate == yesterdayLocalDate

                        // Today Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (isSelectedToday) colors.accent.copy(alpha = 0.18f)
                                    else colors.glassTint.copy(alpha = 0.6f),
                                )
                                .border(
                                    1.dp,
                                    if (isSelectedToday) colors.accent else colors.glassBorder,
                                    RoundedCornerShape(999.dp),
                                )
                                .clickable {
                                    tempSelectedDate = todayLocalDate
                                    displayedYear = todayLocalDate.year
                                    displayedMonth = todayLocalDate.monthNumber
                                    haptics.light()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.date_today),
                                style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelectedToday) colors.accent else colors.textPrimary,
                            )
                        }

                        // Yesterday Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (isSelectedYesterday) colors.accent.copy(alpha = 0.18f)
                                    else colors.glassTint.copy(alpha = 0.6f),
                                )
                                .border(
                                    1.dp,
                                    if (isSelectedYesterday) colors.accent else colors.glassBorder,
                                    RoundedCornerShape(999.dp),
                                )
                                .clickable {
                                    tempSelectedDate = yesterdayLocalDate
                                    displayedYear = yesterdayLocalDate.year
                                    displayedMonth = yesterdayLocalDate.monthNumber
                                    haptics.light()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.date_yesterday),
                                style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelectedYesterday) colors.accent else colors.textPrimary,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Weekdays Header (7 columns: Mon..Sun / Pt..Pz)
                    val weekdays = listOf(
                        Res.string.weekday_mon,
                        Res.string.weekday_tue,
                        Res.string.weekday_wed,
                        Res.string.weekday_thu,
                        Res.string.weekday_fri,
                        Res.string.weekday_sat,
                        Res.string.weekday_sun,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        weekdays.forEach { resId ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(resId),
                                    style = typography.caption.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                    ),
                                    color = colors.textSecondary.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Days Grid (7 columns)
                    val firstDay = remember(displayedYear, displayedMonth) {
                        LocalDate(displayedYear, displayedMonth, 1)
                    }
                    val startOffset = remember(firstDay) {
                        firstDay.dayOfWeek.isoDayNumber - 1
                    }
                    val daysCount = remember(displayedYear, displayedMonth) {
                        daysInMonth(displayedYear, displayedMonth)
                    }
                    val totalCells = startOffset + daysCount
                    val numRows = (totalCells + 6) / 7

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (row in 0 until numRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    if (cellIndex < startOffset || cellIndex >= totalCells) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val dayNum = cellIndex - startOffset + 1
                                        val cellDate = LocalDate(displayedYear, displayedMonth, dayNum)
                                        val isSelected = cellDate == tempSelectedDate
                                        val isToday = cellDate == todayLocalDate
                                        val isFuture = cellDate.toEpochDays() > todayEpochDays

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            val cellModifier = when {
                                                isFuture -> Modifier.padding(2.dp)
                                                isSelected -> Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.accent)
                                                    .clickable {
                                                        tempSelectedDate = cellDate
                                                        haptics.light()
                                                    }
                                                isToday -> Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, colors.accent.copy(alpha = 0.6f), CircleShape)
                                                    .clickable {
                                                        tempSelectedDate = cellDate
                                                        haptics.light()
                                                    }
                                                else -> Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        tempSelectedDate = cellDate
                                                        haptics.light()
                                                    }
                                            }

                                            Box(
                                                modifier = cellModifier,
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                ) {
                                                    Text(
                                                        text = dayNum.toString(),
                                                        style = typography.body.copy(
                                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 15.sp,
                                                        ),
                                                        color = when {
                                                            isSelected -> colors.onAccent
                                                            isFuture -> colors.textSecondary.copy(alpha = 0.25f)
                                                            isToday -> colors.accent
                                                            else -> colors.textPrimary
                                                        },
                                                    )

                                                    if (isToday && !isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(top = 1.dp)
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(colors.accent),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Bottom actions: "Vazgeç" (Cancel) & "Seç / Bitti" (Done)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.date_picker_cancel),
                            style = typography.body,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.accent)
                                .clickable {
                                    val millis = tempSelectedDate
                                        .atTime(0, 0)
                                        .toInstant(TimeZone.UTC)
                                        .toEpochMilliseconds()
                                    onDateSelected(millis)
                                    onDismiss()
                                }
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.date_picker_done),
                                style = typography.headline.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.onAccent,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns the number of days in the specified month of [year].
 */
internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
    else -> 30
}

/**
 * Localized full month name.
 */
@Composable
internal fun localizedMonthName(monthNumber: Int): String {
    val res = when (monthNumber) {
        1 -> Res.string.month_1
        2 -> Res.string.month_2
        3 -> Res.string.month_3
        4 -> Res.string.month_4
        5 -> Res.string.month_5
        6 -> Res.string.month_6
        7 -> Res.string.month_7
        8 -> Res.string.month_8
        9 -> Res.string.month_9
        10 -> Res.string.month_10
        11 -> Res.string.month_11
        12 -> Res.string.month_12
        else -> Res.string.month_1
    }
    return stringResource(res)
}

/**
 * Localized short month name.
 */
@Composable
internal fun localizedMonthShortName(monthNumber: Int): String {
    val res = when (monthNumber) {
        1 -> Res.string.month_short_1
        2 -> Res.string.month_short_2
        3 -> Res.string.month_short_3
        4 -> Res.string.month_short_4
        5 -> Res.string.month_short_5
        6 -> Res.string.month_short_6
        7 -> Res.string.month_short_7
        8 -> Res.string.month_short_8
        9 -> Res.string.month_short_9
        10 -> Res.string.month_short_10
        11 -> Res.string.month_short_11
        12 -> Res.string.month_short_12
        else -> Res.string.month_short_1
    }
    return stringResource(res)
}
