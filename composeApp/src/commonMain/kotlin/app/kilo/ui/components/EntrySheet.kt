package app.kilo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.WeightUnit
import app.kilo.domain.label
import app.kilo.ui.SheetState
import app.kilo.ui.UiError
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.error_empty
import kilo.composeapp.generated.resources.error_not_a_number
import kilo.composeapp.generated.resources.error_ocr_failed
import kilo.composeapp.generated.resources.error_ocr_no_result
import kilo.composeapp.generated.resources.error_out_of_range
import kilo.composeapp.generated.resources.sheet_cancel
import kilo.composeapp.generated.resources.sheet_ocr_prefilled
import kilo.composeapp.generated.resources.sheet_placeholder
import kilo.composeapp.generated.resources.sheet_save
import kilo.composeapp.generated.resources.sheet_scanning
import kilo.composeapp.generated.resources.sheet_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun EntrySheet(
    sheet: SheetState,
    unit: WeightUnit,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val focusRequester = remember { FocusRequester() }

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
                            text = stringResource(Res.string.sheet_title),
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

                    Spacer(Modifier.height(28.dp))

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
                                    .width(180.dp),
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
}
