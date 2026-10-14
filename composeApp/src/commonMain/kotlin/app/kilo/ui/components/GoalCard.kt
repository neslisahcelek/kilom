package app.kilo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.GoalProgress
import app.kilo.domain.WeightUnit
import app.kilo.domain.formatWeight
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.goal_eta
import kilo.composeapp.generated.resources.goal_progress_title
import kilo.composeapp.generated.resources.goal_reached
import kilo.composeapp.generated.resources.goal_remaining
import kilo.composeapp.generated.resources.goal_set_cta
import kilo.composeapp.generated.resources.goal_title
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun GoalCard(
    goal: GoalProgress?,
    unit: WeightUnit,
    hazeState: HazeState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    GlassCard(
        hazeState = hazeState,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
    ) {
        if (goal == null) {
            // Empty / CTA state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🎯", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.goal_title),
                            style = typography.headline,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.goal_set_cta),
                        style = typography.callout,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        } else {
            // Active Goal Progress
            val animatedFraction by animateFloatAsState(
                targetValue = goal.fraction.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 600),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎯", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.goal_progress_title),
                            style = typography.caption,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Text(
                        text = formatWeight(goal.goalKg, unit),
                        style = typography.callout,
                        color = colors.accent,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Big Remaining or Reached text
                val remainingFormatted = formatWeight(goal.remainingKg, unit)
                Text(
                    text = if (goal.reached) {
                        stringResource(Res.string.goal_reached)
                    } else {
                        stringResource(Res.string.goal_remaining, remainingFormatted)
                    },
                    style = typography.largeTitle,
                    color = if (goal.reached) colors.negative else colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(14.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.glassBorder),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(colors.accent, colors.blobA)
                                )
                            ),
                    )
                }

                // Estimated date if available
                if (goal.estimatedDate != null && !goal.reached) {
                    Spacer(Modifier.height(10.dp))
                    val localDate = goal.estimatedDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val dateStr = "${localDate.dayOfMonth} ${monthName(localDate.monthNumber)}"
                    Text(
                        text = stringResource(Res.string.goal_eta, dateStr),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}
