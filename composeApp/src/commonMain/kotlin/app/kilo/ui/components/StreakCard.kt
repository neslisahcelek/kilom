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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.DayStatus
import app.kilo.domain.StreakInfo
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.streak_best_days
import kilo.composeapp.generated.resources.streak_current_days
import kilo.composeapp.generated.resources.streak_day_fri
import kilo.composeapp.generated.resources.streak_day_mon
import kilo.composeapp.generated.resources.streak_day_sat
import kilo.composeapp.generated.resources.streak_day_sun
import kilo.composeapp.generated.resources.streak_day_thu
import kilo.composeapp.generated.resources.streak_day_tue
import kilo.composeapp.generated.resources.streak_day_wed
import kilo.composeapp.generated.resources.streak_risk_warning
import kilo.composeapp.generated.resources.streak_share_action
import kilo.composeapp.generated.resources.streak_title
import kilo.composeapp.generated.resources.streak_today_done
import kilo.composeapp.generated.resources.streak_total_count
import org.jetbrains.compose.resources.stringResource

@Composable
fun StreakCard(
    streak: StreakInfo,
    hazeState: HazeState?,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    GlassCard(
        hazeState = hazeState,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Header: Streak badge, counter and Share button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (streak.currentStreak > 0) colors.accent.copy(alpha = 0.15f)
                                else colors.neutral.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (streak.currentStreak > 0) "🔥" else "✨",
                            fontSize = 18.sp,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val title = if (streak.currentStreak > 0) {
                            stringResource(Res.string.streak_current_days, streak.currentStreak)
                        } else {
                            stringResource(Res.string.streak_title)
                        }
                        Text(
                            text = title,
                            style = typography.headline,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val subtitle = if (streak.bestStreak > 0) {
                            stringResource(Res.string.streak_best_days, streak.bestStreak)
                        } else {
                            stringResource(Res.string.streak_total_count, streak.totalWeighIns)
                        }
                        Text(
                            text = subtitle,
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                }

                // Share Button (subtle liquid-glass pill)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .clickable { onShareClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.streak_share_action),
                        style = typography.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4-Week Consistency Matrix (M T W T F S S)
            ConsistencyMatrix(
                recentDays = streak.recentDays,
                modifier = Modifier.fillMaxWidth(),
            )

            // Status notice: At-risk warning or today logged
            if (streak.isStreakAtRisk) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.positive.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "⚡", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.streak_risk_warning),
                        style = typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = colors.positive,
                    )
                }
            } else if (streak.isTodayLogged) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.negative.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "✓", fontSize = 13.sp, color = colors.negative, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.streak_today_done),
                        style = typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = colors.negative,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsistencyMatrix(
    recentDays: List<DayStatus>,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val dayHeaders = listOf(
        stringResource(Res.string.streak_day_mon),
        stringResource(Res.string.streak_day_tue),
        stringResource(Res.string.streak_day_wed),
        stringResource(Res.string.streak_day_thu),
        stringResource(Res.string.streak_day_fri),
        stringResource(Res.string.streak_day_sat),
        stringResource(Res.string.streak_day_sun),
    )

    Column(modifier = modifier) {
        // Headers row: M T W T F S S
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            dayHeaders.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = typography.caption,
                    color = colors.textSecondary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 4 rows of 7 days
        val weeks = recentDays.chunked(7)
        weeks.forEachIndexed { weekIndex, weekDays ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                weekDays.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        DayDot(day = day)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDot(day: DayStatus) {
    val colors = KiloTheme.colors

    val dotSize = 13.dp
    val shape = RoundedCornerShape(4.dp)

    when {
        day.isToday -> {
            Box(
                modifier = Modifier
                    .size(dotSize + 2.dp)
                    .clip(shape)
                    .border(1.5.dp, colors.accent, shape)
                    .background(
                        if (day.hasWeighIn) colors.accent
                        else colors.accent.copy(alpha = 0.15f)
                    ),
            )
        }
        day.isFuture -> {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(shape)
                    .background(colors.neutral.copy(alpha = 0.08f)),
            )
        }
        day.hasWeighIn -> {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(shape)
                    .background(colors.accent.copy(alpha = 0.85f)),
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(shape)
                    .background(colors.textSecondary.copy(alpha = 0.12f)),
            )
        }
    }
}
