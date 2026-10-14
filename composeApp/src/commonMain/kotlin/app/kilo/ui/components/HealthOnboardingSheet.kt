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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.ui.theme.KiloTheme
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.health_onboarding_body
import kilo.composeapp.generated.resources.health_onboarding_connect
import kilo.composeapp.generated.resources.health_onboarding_import_option
import kilo.composeapp.generated.resources.health_onboarding_later
import kilo.composeapp.generated.resources.health_onboarding_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthOnboardingSheet(
    isOpen: Boolean,
    onConnect: (importPast: Boolean) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    var importPast by remember { mutableStateOf(true) }

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
            // Heart Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf<Color>(
                                colors.negative.copy(alpha = 0.25f),
                                colors.negative.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "❤️",
                    fontSize = 28.sp,
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = stringResource(Res.string.health_onboarding_title),
                style = typography.largeTitle,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.health_onboarding_body),
                style = typography.subhead,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(20.dp))

            // Import Past Checkbox row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.glassTint)
                    .clickable { importPast = !importPast }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = importPast,
                    onCheckedChange = { importPast = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        uncheckedColor = colors.textSecondary,
                        checkmarkColor = colors.onAccent,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.health_onboarding_import_option),
                    style = typography.callout,
                    color = colors.textPrimary,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Connect button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent)
                    .clickable { onConnect(importPast) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.health_onboarding_connect),
                    style = typography.headline,
                    color = colors.onAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Later button
            Text(
                text = stringResource(Res.string.health_onboarding_later),
                style = typography.callout,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(8.dp),
            )
        }
    }
}
