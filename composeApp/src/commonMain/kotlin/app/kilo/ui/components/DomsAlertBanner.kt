package app.kilo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.DomsAssessment
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.doms_alert_badge
import kilo.composeapp.generated.resources.doms_alert_desc_format
import kilo.composeapp.generated.resources.doms_alert_dismiss
import kilo.composeapp.generated.resources.doms_alert_title
import kilo.composeapp.generated.resources.doms_default_muscle_group
import org.jetbrains.compose.resources.stringResource

/**
 * Liquid-glass banner displayed when recent heavy compound resistance training explains
 * an acute scale weight elevation due to exercise-induced muscle damage (EIMD) and fluid retention.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DomsAlertBanner(
    assessment: DomsAssessment,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
) {
    if (!assessment.hasTrauma) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val volumeFormatted = if (assessment.recentVolumeKg >= 1000.0) {
        val thousands = assessment.recentVolumeKg / 1000.0
        val rounded = kotlin.math.round(thousands * 10.0) / 10.0
        "${rounded}k"
    } else {
        "${assessment.recentVolumeKg.toInt()}"
    }

    val targetGroup = assessment.targetMuscleGroup ?: stringResource(Res.string.doms_default_muscle_group)

    GlassCard(
        hazeState = hazeState,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        colors.accent.copy(alpha = 0.55f),
                        colors.positive.copy(alpha = 0.35f),
                    )
                ),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // Header: Badge + Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "⚡", fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.doms_alert_badge),
                        style = typography.caption.copy(fontSize = 11.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                    )
                }

                // Dismiss pill button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.glassTint)
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.doms_alert_dismiss),
                        style = typography.caption.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = stringResource(Res.string.doms_alert_title),
                style = typography.subhead,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )

            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                text = stringResource(
                    Res.string.doms_alert_desc_format,
                    targetGroup,
                    volumeFormatted,
                    assessment.expectedFluidWeightKg,
                ),
                style = typography.caption.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                color = colors.textSecondary,
            )

            // Exercise chips if available
            if (assessment.exerciseNames.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    assessment.exerciseNames.take(4).forEach { name ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.accent.copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = name,
                                style = typography.caption.copy(fontSize = 10.5.sp),
                                color = colors.accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
