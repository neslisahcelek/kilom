package app.kilo.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.WeightUnit
import app.kilo.domain.label
import app.kilo.ui.UiError
import app.kilo.ui.theme.KiloTheme
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.error_empty
import kilo.composeapp.generated.resources.error_not_a_number
import kilo.composeapp.generated.resources.error_out_of_range
import kilo.composeapp.generated.resources.goal_clear
import kilo.composeapp.generated.resources.goal_input_hint
import kilo.composeapp.generated.resources.goal_save
import kilo.composeapp.generated.resources.goal_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSheet(
    isOpen: Boolean,
    goalInput: String,
    unit: WeightUnit,
    hasExistingGoal: Boolean,
    error: UiError?,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.goal_title),
                style = typography.largeTitle,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(18.dp))

            // Number input field
            OutlinedTextField(
                value = goalInput,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = "70.0",
                        color = colors.textSecondary.copy(alpha = 0.5f),
                        style = typography.hero.copy(fontSize = 36.sp),
                    )
                },
                trailingIcon = {
                    Text(
                        text = unit.label,
                        style = typography.title,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = typography.hero.copy(fontSize = 36.sp, textAlign = TextAlign.Center),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.glassBorder,
                    errorBorderColor = colors.positive,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            if (error != null) {
                Spacer(Modifier.height(6.dp))
                val errorMsg = when (error) {
                    UiError.EMPTY -> stringResource(Res.string.error_empty)
                    UiError.NOT_A_NUMBER -> stringResource(Res.string.error_not_a_number)
                    UiError.OUT_OF_RANGE -> stringResource(Res.string.error_out_of_range)
                    else -> stringResource(Res.string.error_out_of_range)
                }
                Text(
                    text = errorMsg,
                    style = typography.caption,
                    color = colors.positive,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent)
                    .clickable { onSave() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.goal_save),
                    style = typography.headline,
                    color = colors.onAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (hasExistingGoal) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.goal_clear),
                    style = typography.callout,
                    color = colors.positive,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClear() }
                        .padding(8.dp),
                )
            }
        }
    }
}
