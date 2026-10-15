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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import kilo.composeapp.generated.resources.cycle_card_dismiss
import kilo.composeapp.generated.resources.cycle_card_dismiss_desc
import kilo.composeapp.generated.resources.cycle_day_counter
import kilo.composeapp.generated.resources.cycle_gender_female_btn
import kilo.composeapp.generated.resources.cycle_gender_male_btn
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
    onDismiss: (() -> Unit)? = null,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    GlassCard(
        hazeState = hazeState,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (state.isEnabled && state.currentDayInfo != null) {
                    Modifier.clickable(role = Role.Button, onClick = onOpenSheet)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(24.dp),
    ) {
        if (!state.isEnabled || state.currentDayInfo == null) {
            // Setup / CTA state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "🌸", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.cycle_card_cta_title),
                            style = typography.headline,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    if (onDismiss != null) {
                        val dismissDesc = stringResource(Res.string.cycle_card_dismiss_desc)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.glassTint)
                                .border(0.5.dp, colors.glassBorder, CircleShape)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = dismissDesc,
                                    onClick = onDismiss,
                                )
                                .semantics {
                                    role = Role.Button
                                    contentDescription = dismissDesc
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(Res.string.cycle_card_cta_subtitle),
                    style = typography.caption.copy(lineHeight = 16.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // Direct & crystal clear choices: Female (Start) or Male (Hide)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Male / Hide Option
                    if (onDismiss != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.glassTint)
                                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(14.dp))
                                .clickable(role = Role.Button, onClick = onDismiss)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.cycle_gender_male_btn),
                                style = typography.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Female / Start Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.accent)
                            .clickable(role = Role.Button, onClick = onSetupClick)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.cycle_gender_female_btn),
                            style = typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = phaseName,
                            style = typography.headline,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        if (onDismiss != null) {
                            Spacer(Modifier.width(8.dp))
                            val dismissDesc = stringResource(Res.string.cycle_card_dismiss_desc)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colors.glassTint)
                                    .border(0.5.dp, colors.glassBorder, CircleShape)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = dismissDesc,
                                        onClick = onDismiss,
                                    )
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = dismissDesc
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
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
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Workout Intensity Pill
                val workoutEmoji = workoutIntensityEmoji(advice.workoutIntensity)
                val workoutName = workoutIntensityName(advice.workoutIntensity)
                val workoutTitle = resolveCycleString(advice.workoutTitleKey)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.08f))
                        .border(1.dp, colors.accent.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = workoutEmoji, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (workoutTitle.isNotBlank()) "$workoutName • $workoutTitle" else workoutName,
                        style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                        modifier = Modifier.weight(1f),
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
                    style = typography.caption.copy(lineHeight = 16.5.sp),
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(14.dp))

                // Nutrition Quick Items (full width to accommodate complete descriptions)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    advice.eatKeys.firstOrNull()?.let { eatKey ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.negative.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "🥗", fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = resolveCycleString(eatKey),
                                style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                                color = colors.negative,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    advice.avoidKeys.firstOrNull()?.let { avoidKey ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.positive.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "🚫", fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = resolveCycleString(avoidKey),
                                style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                                color = colors.positive,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
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
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Spacer(Modifier.width(8.dp))

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
