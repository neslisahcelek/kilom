package app.kilo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.WeightTag
import app.kilo.domain.WeightUnit
import app.kilo.domain.label
import app.kilo.ui.SheetState
import app.kilo.ui.UiError
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.date_picker_cancel
import kilo.composeapp.generated.resources.date_picker_confirm
import kilo.composeapp.generated.resources.date_picker_title
import kilo.composeapp.generated.resources.date_today
import kilo.composeapp.generated.resources.date_yesterday
import kilo.composeapp.generated.resources.error_empty
import kilo.composeapp.generated.resources.error_not_a_number
import kilo.composeapp.generated.resources.error_ocr_failed
import kilo.composeapp.generated.resources.error_ocr_no_result
import kilo.composeapp.generated.resources.error_out_of_range
import kilo.composeapp.generated.resources.sheet_cancel
import kilo.composeapp.generated.resources.sheet_edit_title
import kilo.composeapp.generated.resources.sheet_ocr_prefilled
import kilo.composeapp.generated.resources.sheet_placeholder
import kilo.composeapp.generated.resources.sheet_save
import kilo.composeapp.generated.resources.sheet_scanning
import kilo.composeapp.generated.resources.sheet_title
import kilo.composeapp.generated.resources.tag_section_title
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    sheet: SheetState,
    unit: WeightUnit,
    onInputChange: (String) -> Unit,
    onDateSelected: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    selectedTag: WeightTag? = sheet.selectedTag,
    onTagSelected: (WeightTag?) -> Unit = {},
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val focusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(sheet.scanning) {
        if (!sheet.scanning) {
            focusRequester.requestFocus()
        }
    }

    // Modal Scrim + Bottom Sheet Layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Prevent clicking inside sheet from triggering scrim dismissal
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume */ },
                )
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            GlassCard(
                hazeState = hazeState,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Grabber handle
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(colors.textSecondary.copy(alpha = 0.3f)),
                    )

                    Spacer(Modifier.height(16.dp))

                    // Header row: Cancel, Title, Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.sheet_cancel),
                            style = typography.body,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                        )

                        Text(
                            text = stringResource(if (sheet.isEditing) Res.string.sheet_edit_title else Res.string.sheet_title),
                            style = typography.headline,
                            color = colors.textPrimary,
                        )

                        Text(
                            text = stringResource(Res.string.sheet_save),
                            style = typography.headline,
                            color = if (sheet.scanning) colors.textSecondary else colors.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !sheet.scanning, onClick = onSave)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    if (sheet.scanning) {
                        // Scanning progress indicator
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(
                                color = colors.accent,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp),
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.sheet_scanning),
                                style = typography.body,
                                color = colors.textSecondary,
                            )
                        }
                    } else {
                        // Date Pill (tap to choose date)
                        val todayEpochDays = Clock.System.now().toLocalDateTime(timeZone).date.toEpochDays()
                        val selectedLocalDate = sheet.selectedDateMillis?.let {
                            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        }
                        val isToday = selectedLocalDate == null || selectedLocalDate.toEpochDays() == todayEpochDays
                        val isYesterday = selectedLocalDate != null && selectedLocalDate.toEpochDays() == todayEpochDays - 1

                        val dateLabel = when {
                            isToday -> stringResource(Res.string.date_today)
                            isYesterday -> stringResource(Res.string.date_yesterday)
                            else -> "${selectedLocalDate.dayOfMonth} ${monthName(selectedLocalDate.monthNumber)} ${selectedLocalDate.year}"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.glassTint.copy(alpha = 0.65f))
                                .border(1.dp, colors.glassBorder, RoundedCornerShape(999.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "📅",
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = dateLabel,
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (!isToday) colors.accent else colors.textSecondary,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Tags section
                        Text(
                            text = stringResource(Res.string.tag_section_title),
                            style = typography.caption,
                            color = colors.textSecondary.copy(alpha = 0.8f),
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WeightTag.entries.forEach { tag ->
                                val isSelected = selectedTag == tag
                                val chipBgColor by animateColorAsState(
                                    targetValue = if (isSelected) colors.accent else colors.glassTint.copy(alpha = 0.65f),
                                    label = "tag_chip_bg",
                                )
                                val chipTextColor by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                    label = "tag_chip_text",
                                )
                                val chipBorderColor by animateColorAsState(
                                    targetValue = if (isSelected) colors.accent else colors.glassBorder,
                                    label = "tag_chip_border",
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(chipBgColor)
                                        .border(1.dp, chipBorderColor, RoundedCornerShape(999.dp))
                                        .clickable {
                                            val nextTag = if (isSelected) null else tag
                                            onTagSelected(nextTag)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(tag.labelRes()),
                                        style = typography.caption.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        ),
                                        color = chipTextColor,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // Numeric input area
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            BasicTextField(
                                value = sheet.input,
                                onValueChange = onInputChange,
                                textStyle = typography.input.copy(
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.End,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { onSave() },
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .width(IntrinsicSize.Min)
                                    .widthIn(min = 180.dp, max = 230.dp),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterEnd) {
                                        if (sheet.input.isEmpty()) {
                                            Text(
                                                text = stringResource(Res.string.sheet_placeholder),
                                                style = typography.input,
                                                color = colors.textSecondary.copy(alpha = 0.35f),
                                                textAlign = TextAlign.End,
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = unit.label,
                                style = typography.title,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }

                        // Notices / Error message
                        val errorMessage = when (sheet.error) {
                            UiError.EMPTY -> stringResource(Res.string.error_empty)
                            UiError.NOT_A_NUMBER -> stringResource(Res.string.error_not_a_number)
                            UiError.OUT_OF_RANGE -> stringResource(Res.string.error_out_of_range)
                            else -> null
                        }

                        val noticeMessage = when (sheet.notice) {
                            UiError.OCR_NO_RESULT -> stringResource(Res.string.error_ocr_no_result)
                            UiError.OCR_FAILED -> stringResource(Res.string.error_ocr_failed)
                            else -> if (sheet.isPrefilledFromOcr && sheet.input.isNotEmpty() && sheet.error == null) {
                                stringResource(Res.string.sheet_ocr_prefilled)
                            } else null
                        }

                        Spacer(Modifier.height(18.dp))

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                style = typography.caption,
                                color = colors.danger,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        } else if (noticeMessage != null) {
                            Text(
                                text = noticeMessage,
                                style = typography.caption,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        } else {
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        // DatePicker represents calendar dates as UTC midnight, regardless of the device zone.
        val todayUtcMillis = remember(timeZone) {
            val localToday = Clock.System.now().toLocalDateTime(timeZone).date
            localToday.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = sheet.selectedDateMillis ?: todayUtcMillis,
            selectableDates = remember(todayUtcMillis) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= todayUtcMillis
                    }
                }
            },
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.date_picker_confirm),
                        color = colors.accent,
                        style = typography.headline,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = stringResource(Res.string.date_picker_cancel),
                        color = colors.textSecondary,
                        style = typography.body,
                    )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = colors.backgroundTop,
            ),
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = stringResource(Res.string.date_picker_title),
                        style = typography.headline,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
            )
        }
    }
}
