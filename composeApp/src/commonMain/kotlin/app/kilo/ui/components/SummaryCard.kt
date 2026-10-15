package app.kilo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.PeriodSummary
import app.kilo.domain.SummaryPeriod
import app.kilo.domain.WeightUnit
import app.kilo.domain.formatWeight
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.summary_average
import kilo.composeapp.generated.resources.summary_change
import kilo.composeapp.generated.resources.summary_empty
import kilo.composeapp.generated.resources.summary_month
import kilo.composeapp.generated.resources.summary_period_month
import kilo.composeapp.generated.resources.summary_period_week
import kilo.composeapp.generated.resources.summary_range
import kilo.composeapp.generated.resources.summary_title
import kilo.composeapp.generated.resources.summary_vs_previous
import kilo.composeapp.generated.resources.summary_week
import org.jetbrains.compose.resources.stringResource

@Composable
fun SummaryCard(
    summary: PeriodSummary?,
    period: SummaryPeriod,
    unit: WeightUnit,
    hazeState: HazeState?,
    onSelectPeriod: (SummaryPeriod) -> Unit,
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
            // Header Row: Title + Period Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.summary_title),
                    style = typography.headline,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )

                // Period Selector (7d vs 30d)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.glassBorder.copy(alpha = 0.25f))
                        .border(0.5.dp, colors.glassBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp),
                ) {
                    val periods = listOf(SummaryPeriod.WEEK to Res.string.summary_week, SummaryPeriod.MONTH to Res.string.summary_month)
                    periods.forEach { (p, resId) ->
                        val isSelected = period == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.glassTint else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { onSelectPeriod(p) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(resId),
                                style = typography.caption,
                                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = summary,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "summary_card_content",
            ) { s ->
                if (s == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.summary_empty),
                            style = typography.subhead,
                            color = colors.textSecondary,
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            // Average
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.summary_average),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = formatWeight(s.averageKg, unit),
                                    style = typography.headline,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Range (min - max)
                            Column(
                                modifier = Modifier.weight(1.3f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(Res.string.summary_range),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${formatWeight(s.minKg, unit)} - ${formatWeight(s.maxKg, unit)}",
                                    style = typography.headline.copy(fontSize = 15.5.sp),
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }

                            // Change
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Text(
                                    text = stringResource(Res.string.summary_change),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                                Spacer(Modifier.height(4.dp))
                                val change = s.changeKg
                                if (change != null) {
                                    val prefix = if (change > 0) "+" else ""
                                    val changeText = "$prefix${formatWeight(change, unit)}"
                                    val changeColor = when {
                                        change < 0 -> colors.negative // loss is green
                                        change > 0 -> colors.positive // gain is orange
                                        else -> colors.textSecondary
                                    }
                                    Text(
                                        text = changeText,
                                        style = typography.headline,
                                        color = changeColor,
                                        fontWeight = FontWeight.Bold,
                                    )
                                } else {
                                    Text(
                                        text = "—",
                                        style = typography.headline,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        // Comparison vs previous period if available
                        if (s.previousAverageKg != null) {
                            Spacer(Modifier.height(14.dp))
                            val periodName = if (period == SummaryPeriod.WEEK) {
                                stringResource(Res.string.summary_period_week)
                            } else {
                                stringResource(Res.string.summary_period_month)
                            }
                            val diff = s.averageKg - s.previousAverageKg
                            val diffPrefix = if (diff > 0) "+" else ""
                            val diffFormatted = "$diffPrefix${formatWeight(diff, unit)}"
                            val diffColor = if (diff <= 0) colors.negative else colors.positive

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(diffColor.copy(alpha = 0.12f))
                                    .border(0.5.dp, diffColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "${stringResource(Res.string.summary_vs_previous, periodName)}: $diffFormatted",
                                    style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = diffColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
