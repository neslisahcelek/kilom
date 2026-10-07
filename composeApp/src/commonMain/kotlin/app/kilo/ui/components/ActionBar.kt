package app.kilo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.WeightUnit
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.action_camera
import kilo.composeapp.generated.resources.action_gallery
import kilo.composeapp.generated.resources.action_manual
import kilo.composeapp.generated.resources.unit_kg
import kilo.composeapp.generated.resources.unit_lb
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionBar(
    selectedUnit: WeightUnit,
    onUnitChange: (WeightUnit) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onManualClick: () -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Main action buttons wrapped in glass
        GlassCard(
            hazeState = hazeState,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Camera
                ActionButton(
                    icon = { CameraIcon(tint = colors.accent) },
                    label = stringResource(Res.string.action_camera),
                    onClick = onCameraClick,
                )

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.glassBorder),
                )

                // Gallery
                ActionButton(
                    icon = { GalleryIcon(tint = colors.accent) },
                    label = stringResource(Res.string.action_gallery),
                    onClick = onGalleryClick,
                )

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.glassBorder),
                )

                // Manual Entry
                ActionButton(
                    icon = { ManualIcon(tint = colors.accent) },
                    label = stringResource(Res.string.action_manual),
                    onClick = onManualClick,
                )
            }
        }

        // Unit segmented toggle (kg / lb)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            GlassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UnitSegmentItem(
                        title = stringResource(Res.string.unit_kg),
                        selected = selectedUnit == WeightUnit.KG,
                        onClick = { onUnitChange(WeightUnit.KG) },
                    )
                    UnitSegmentItem(
                        title = stringResource(Res.string.unit_lb),
                        selected = selectedUnit == WeightUnit.LB,
                        onClick = { onUnitChange(WeightUnit.LB) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            style = typography.caption,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun UnitSegmentItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val bgColor by animateColorAsState(
        if (selected) colors.accent else Color.Transparent,
        label = "unit_segment_bg",
    )
    val textColor by animateColorAsState(
        if (selected) colors.onAccent else colors.textSecondary,
        label = "unit_segment_text",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bgColor)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = typography.callout,
            color = textColor,
        )
    }
}

@Composable
private fun CameraIcon(tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Camera body
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, h * 0.22f),
            size = Size(w, h * 0.72f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 1.8.dp.toPx()),
        )
        // Camera lens
        drawCircle(
            color = tint,
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.58f),
            style = Stroke(width = 1.8.dp.toPx()),
        )
        // Top flash / shutter bump
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.32f, h * 0.08f),
            size = Size(w * 0.36f, h * 0.16f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx()),
        )
    }
}

@Composable
private fun GalleryIcon(tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Outer image frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 1.8.dp.toPx()),
        )
        // Sun / Moon circle
        drawCircle(
            color = tint,
            radius = w * 0.12f,
            center = Offset(w * 0.32f, h * 0.32f),
        )
        // Mountain line
        drawLine(
            color = tint,
            start = Offset(w * 0.1f, h * 0.82f),
            end = Offset(w * 0.45f, h * 0.50f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.45f, h * 0.50f),
            end = Offset(w * 0.65f, h * 0.68f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.65f, h * 0.68f),
            end = Offset(w * 0.90f, h * 0.44f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ManualIcon(tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Pencil tip & shaft
        drawLine(
            color = tint,
            start = Offset(w * 0.15f, h * 0.85f),
            end = Offset(w * 0.80f, h * 0.20f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.65f, h * 0.05f),
            end = Offset(w * 0.95f, h * 0.35f),
            strokeWidth = 2.0.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // Bottom baseline
        drawLine(
            color = tint,
            start = Offset(w * 0.05f, h * 0.95f),
            end = Offset(w * 0.45f, h * 0.95f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
