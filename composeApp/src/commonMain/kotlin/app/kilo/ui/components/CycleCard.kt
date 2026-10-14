package app.kilo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.CycleState
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.cycle_card_cta_button
import kilo.composeapp.generated.resources.cycle_card_cta_subtitle
import kilo.composeapp.generated.resources.cycle_card_cta_title
import kilo.composeapp.generated.resources.cycle_day_counter
import kilo.composeapp.generated.resources.cycle_next_period_in
import kilo.composeapp.generated.resources.cycle_open_sheet
import kilo.composeapp.generated.resources.cycle_water_retention_alert
import org.jetbrains.compose.resources.stringResource

@Composable
fun CycleCard(
    state: CycleState,
    hazeState: HazeState?,
    onSetupClick: () -> Unit,
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    GlassCard(
        hazeState = hazeState,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                if (state.isEnabled && state.currentDayInfo != null) {
                    onOpenSheet()
                } else {
                    onSetupClick()
                }
            },
        shape = RoundedCornerShape(24.dp),
    ) {
        if (!state.isEnabled || state.currentDayInfo == null) {
            // Setup / CTA state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌸", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.cycle_card_cta_title),
                            style = typography.headline,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.cycle_card_cta_subtitle),
                            style = typography.caption,
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.cycle_card_cta_button),
                        style = typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = colors.onAccent,
                    )
                }
            }
        } else {
            // Active Cycle Card
            val info = state.currentDayInfo
            val advice = info.advice
            val emoji = cyclePhaseEmoji(info.phase)
            val phaseName = cyclePhaseName(info.phase)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Header: Phase + Day in cycle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = phaseName,
                            style = typography.headline,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accent.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.cycle_day_counter, info.cycleDay, info.totalDays),
                            style = typography.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent,
                        )
                    }
                }

                // Water Retention Alert (if active)
                if (state.isWaterRetentionActive) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.positive.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "💧", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.cycle_water_retention_alert),
                            style = typography.caption,
                            fontWeight = FontWeight.Medium,
                            color = colors.positive,
                        )
                    }
                }

                // Workout Intensity Pill
                val workoutEmoji = workoutIntensityEmoji(advice.workoutIntensity)
                val workoutName = workoutIntensityName(advice.workoutIntensity)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.08f))
                        .border(1.dp, colors.accent.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = workoutEmoji, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$workoutName • ${resolveCycleString(advice.workoutTitleKey)}",
                        style = typography.caption.copy(fontSize = 11.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Title & Body Summary
                Text(
                    text = resolveCycleString(advice.titleKey),
                    style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = resolveCycleString(advice.bodySummaryKey),
                    style = typography.caption,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(14.dp))

                // Nutrition Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    advice.eatKeys.firstOrNull()?.let { eatKey ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.negative.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "🥗 ${resolveCycleString(eatKey)}",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.negative,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    advice.avoidKeys.firstOrNull()?.let { avoidKey ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.positive.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "🚫 ${resolveCycleString(avoidKey)}",
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.positive,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Footer: Days until next period + "Daily Guide >"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val daysUntil = state.daysUntilNextPeriod
                    if (daysUntil != null) {
                        Text(
                            text = stringResource(Res.string.cycle_next_period_in, daysUntil),
                            style = typography.caption,
                            color = colors.textSecondary.copy(alpha = 0.8f),
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Text(
                        text = "${stringResource(Res.string.cycle_open_sheet)} →",
                        style = typography.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            }
        }
    }
}
