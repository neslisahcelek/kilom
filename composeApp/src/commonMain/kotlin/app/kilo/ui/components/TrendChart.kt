package app.kilo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.TrendChartData
import app.kilo.domain.TrendPoint
import app.kilo.domain.TrendRange
import app.kilo.domain.WeightEntry
import app.kilo.domain.WeightTag
import app.kilo.domain.WeightUnit
import app.kilo.domain.buildTrendChartData
import app.kilo.domain.formatTwoDecimals
import app.kilo.domain.formatWeight
import app.kilo.domain.formatWeightWithUnit
import app.kilo.domain.kgTo
import app.kilo.domain.label
import app.kilo.domain.round2
import app.kilo.platform.rememberHaptics
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.trend_avg
import kilo.composeapp.generated.resources.trend_empty_subtitle
import kilo.composeapp.generated.resources.trend_empty_title
import kilo.composeapp.generated.resources.trend_legend_weight
import kilo.composeapp.generated.resources.trend_max
import kilo.composeapp.generated.resources.trend_min
import kilo.composeapp.generated.resources.trend_moving_average
import kilo.composeapp.generated.resources.trend_range_30
import kilo.composeapp.generated.resources.trend_range_7
import kilo.composeapp.generated.resources.trend_range_90
import kilo.composeapp.generated.resources.trend_range_all
import kilo.composeapp.generated.resources.trend_title
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Interactive weight trend chart styled with liquid-glass aesthetic.
 *
 * Supports range selection, smooth cubic Bezier weight curves with transparent gradient fill,
 * secondary moving average curves, and interactive touch scrubbing with haptic feedback.
 */
@Composable
fun TrendChart(
    entries: List<WeightEntry>,
    unit: WeightUnit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
    initialRange: TrendRange = TrendRange.LAST_7,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant? = null,
) {
    var selectedRange by remember { mutableStateOf(initialRange) }

    val effectiveNow = remember(entries, now) {
        now ?: entries.maxOfOrNull { it.at } ?: Instant.fromEpochMilliseconds(0L)
    }

    val chartData = remember(entries, selectedRange, effectiveNow, timeZone) {
        buildTrendChartData(entries, selectedRange, effectiveNow, timeZone)
    }

    TrendChart(
        data = chartData,
        unit = unit,
        selectedRange = selectedRange,
        onRangeSelected = { selectedRange = it },
        hazeState = hazeState,
        modifier = modifier,
        timeZone = timeZone,
    )
}

@Composable
fun TrendChart(
    data: TrendChartData?,
    unit: WeightUnit,
    selectedRange: TrendRange,
    onRangeSelected: (TrendRange) -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val haptics = rememberHaptics()

    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    GlassCard(
        hazeState = hazeState,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            // Top Row: Chart Title & Range Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.trend_title),
                    style = typography.title,
                    color = colors.textPrimary,
                )

                RangeSelectorPills(
                    selectedRange = selectedRange,
                    onRangeSelected = { range ->
                        scrubIndex = null
                        onRangeSelected(range)
                    },
                )
            }

            Spacer(Modifier.height(14.dp))

            val points = data?.points.orEmpty()
            val hasEnoughData = points.size >= 2

            if (hasEnoughData) {
                // Header Display: Scrubbed point info or Overall Range stats
                val activePoint = scrubIndex?.let { points.getOrNull(it) }

                AnimatedContent(
                    targetState = activePoint,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "trend_header_anim",
                ) { scrubbed ->
                    if (scrubbed != null) {
                        ScrubbedPointHeader(
                            point = scrubbed,
                            unit = unit,
                            timeZone = timeZone,
                        )
                    } else {
                        RangeStatsHeader(
                            data = data!!,
                            unit = unit,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Interactive Canvas Chart Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                ) {
                    ChartCanvas(
                        data = data!!,
                        scrubIndex = scrubIndex,
                        onScrubIndexChange = { newIdx ->
                            if (newIdx != scrubIndex) {
                                scrubIndex = newIdx
                                if (newIdx != null) {
                                    haptics.light()
                                }
                            }
                        },
                    )
                }

                // Legend row (Weight line & Moving average if present)
                val hasMa = points.any { it.movingAverageKg != null }
                if (hasMa) {
                    Spacer(Modifier.height(8.dp))
                    ChartLegend()
                }
            } else {
                // Graceful empty / insufficient data state
                EmptyTrendPlaceholder()
            }
        }
    }
}

@Composable
private fun RangeSelectorPills(
    selectedRange: TrendRange,
    onRangeSelected: (TrendRange) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RangePill(
            label = stringResource(Res.string.trend_range_7),
            selected = selectedRange == TrendRange.LAST_7,
            onClick = { onRangeSelected(TrendRange.LAST_7) },
        )
        RangePill(
            label = stringResource(Res.string.trend_range_30),
            selected = selectedRange == TrendRange.DAYS_30,
            onClick = { onRangeSelected(TrendRange.DAYS_30) },
        )
        RangePill(
            label = stringResource(Res.string.trend_range_90),
            selected = selectedRange == TrendRange.DAYS_90,
            onClick = { onRangeSelected(TrendRange.DAYS_90) },
        )
        RangePill(
            label = stringResource(Res.string.trend_range_all),
            selected = selectedRange == TrendRange.ALL,
            onClick = { onRangeSelected(TrendRange.ALL) },
        )
    }
}

@Composable
private fun RangePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.glassTint,
        label = "pill_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.onAccent else colors.textSecondary,
        label = "pill_text",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.glassBorder,
        label = "pill_border",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = typography.caption.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
            ),
            color = textColor,
        )
    }
}

@Composable
private fun RangeStatsHeader(
    data: TrendChartData,
    unit: WeightUnit,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val first = data.points.first().kg
    val last = data.points.last().kg
    val deltaKg = (last - first).round2()
    val deltaInUnit = deltaKg.kgTo(unit)

    val deltaColor = when {
        deltaKg > 0.0 -> colors.positive
        deltaKg < 0.0 -> colors.negative
        else -> colors.neutral
    }
    val deltaFormatted = if (deltaInUnit > 0.0) "+${formatTwoDecimals(deltaInUnit)}" else formatTwoDecimals(deltaInUnit)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(deltaColor.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "$deltaFormatted ${unit.label}",
                    style = typography.callout.copy(fontWeight = FontWeight.SemiBold),
                    color = deltaColor,
                )
            }
        }

        // Min / Avg / Max Quick Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatLabel(
                label = stringResource(Res.string.trend_min),
                value = formatWeight(data.minKg, unit),
            )
            StatLabel(
                label = stringResource(Res.string.trend_avg),
                value = formatWeight(data.averageKg, unit),
            )
            StatLabel(
                label = stringResource(Res.string.trend_max),
                value = formatWeight(data.maxKg, unit),
            )
        }
    }
}

@Composable
private fun StatLabel(
    label: String,
    value: String,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label:",
            style = typography.caption.copy(fontSize = 11.sp),
            color = colors.textSecondary,
        )
        Text(
            text = value,
            style = typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun ScrubbedPointHeader(
    point: TrendPoint,
    unit: WeightUnit,
    timeZone: TimeZone,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val ldt = point.instant.toLocalDateTime(timeZone)
    val dateText = "${ldt.dayOfMonth} ${monthName(ldt.monthNumber)}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = dateText,
                style = typography.caption,
                color = colors.textSecondary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = formatWeightWithUnit(point.kg, unit),
                    style = typography.title.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )

                point.tag?.let { tagId ->
                    val tagEnum = WeightTag.fromId(tagId)
                    val tagText = tagEnum?.let { stringResource(it.labelRes()) } ?: tagId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accent.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = tagText,
                            style = typography.caption.copy(fontSize = 10.sp),
                            color = colors.accent,
                        )
                    }
                }
            }
        }

        point.movingAverageKg?.let { ma ->
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.trend_moving_average),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                Text(
                    text = formatWeightWithUnit(ma, unit),
                    style = typography.callout.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ChartCanvas(
    data: TrendChartData,
    scrubIndex: Int?,
    onScrubIndexChange: (Int?) -> Unit,
) {
    val colors = KiloTheme.colors
    val points = data.points

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(points) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val x = down.position.x
                    val padX = 16.dp.toPx()
                    val chartW = (size.width - padX * 2).coerceAtLeast(1f)
                    val fraction = ((x - padX) / chartW).coerceIn(0f, 1f)
                    val idx = (fraction * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
                    onScrubIndexChange(idx)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            onScrubIndexChange(null)
                            break
                        }
                        change.consume()
                        val currX = change.position.x
                        val currFraction = ((currX - padX) / chartW).coerceIn(0f, 1f)
                        val currIdx = (currFraction * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
                        onScrubIndexChange(currIdx)
                    }
                }
            },
    ) {
        val w = size.width
        val h = size.height
        val padX = 16.dp.toPx()
        val padTop = 18.dp.toPx()
        val padBottom = 22.dp.toPx()
        val chartW = w - padX * 2
        val chartH = h - padTop - padBottom

        // Compute Y Bounds with Moving Average included
        val maValues = points.mapNotNull { it.movingAverageKg }
        val allValues = points.map { it.kg } + maValues
        val rawMin = allValues.minOrNull() ?: data.minKg
        val rawMax = allValues.maxOrNull() ?: data.maxKg

        val (yMin, yMax) = if (abs(rawMax - rawMin) < 0.001) {
            (rawMin - 1.0) to (rawMax + 1.0)
        } else {
            val span = rawMax - rawMin
            (rawMin - span * 0.15) to (rawMax + span * 0.15)
        }
        val yRange = (yMax - yMin).toFloat()

        fun mapX(index: Int): Float =
            if (points.size <= 1) padX + chartW / 2f
            else padX + index.toFloat() * (chartW / (points.size - 1))

        fun mapY(kg: Double): Float =
            padTop + (1f - ((kg - yMin).toFloat() / yRange)) * chartH

        // 1. Subtle horizontal dashed grid lines
        val gridLines = 3
        for (i in 0 until gridLines) {
            val gridY = padTop + i * (chartH / (gridLines - 1))
            drawLine(
                color = colors.glassBorder.copy(alpha = 0.25f),
                start = Offset(padX, gridY),
                end = Offset(padX + chartW, gridY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }

        // 2. Moving Average Curve (Subtle dashed line)
        val maEntries = points.mapIndexedNotNull { i, p ->
            p.movingAverageKg?.let { ma -> Offset(mapX(i), mapY(ma)) }
        }

        if (maEntries.size >= 2) {
            val maPath = Path()
            maPath.moveTo(maEntries[0].x, maEntries[0].y)
            for (i in 0 until maEntries.size - 1) {
                val p0 = maEntries[i]
                val p1 = maEntries[i + 1]
                val cx1 = p0.x + (p1.x - p0.x) / 2f
                val cy1 = p0.y
                val cx2 = p0.x + (p1.x - p0.x) / 2f
                val cy2 = p1.y
                maPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
            }

            drawPath(
                path = maPath,
                color = colors.textSecondary.copy(alpha = 0.65f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        // 3. Main Weight Curve (Smooth Bezier) & Gradient Area Fill
        val weightCoords = points.mapIndexed { i, p -> Offset(mapX(i), mapY(p.kg)) }

        val linePath = Path()
        val fillPath = Path()

        val firstPt = weightCoords.first()
        val lastPt = weightCoords.last()
        val chartBottom = padTop + chartH

        linePath.moveTo(firstPt.x, firstPt.y)
        fillPath.moveTo(firstPt.x, chartBottom)
        fillPath.lineTo(firstPt.x, firstPt.y)

        for (i in 0 until weightCoords.size - 1) {
            val p0 = weightCoords[i]
            val p1 = weightCoords[i + 1]
            val cx1 = p0.x + (p1.x - p0.x) / 2f
            val cy1 = p0.y
            val cx2 = p0.x + (p1.x - p0.x) / 2f
            val cy2 = p1.y
            linePath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
            fillPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
        }

        fillPath.lineTo(lastPt.x, chartBottom)
        fillPath.close()

        // Gradient Fill fading to transparent
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.accent.copy(alpha = 0.30f),
                    colors.accent.copy(alpha = 0.01f),
                ),
                startY = padTop,
                endY = chartBottom,
            ),
        )

        // Main Weight Bezier Stroke
        drawPath(
            path = linePath,
            color = colors.accent,
            style = Stroke(
                width = 3.2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // 4. Data Point Dots (when dataset is concise)
        if (points.size <= 15) {
            for (pt in weightCoords) {
                drawCircle(
                    color = colors.accent,
                    radius = 3.dp.toPx(),
                    center = pt,
                )
                drawCircle(
                    color = colors.backgroundTop,
                    radius = 1.5.dp.toPx(),
                    center = pt,
                )
            }
        }

        // 5. Active Scrubbing Indicator
        if (scrubIndex != null && scrubIndex in weightCoords.indices) {
            val activePt = weightCoords[scrubIndex]

            // Vertical indicator line
            drawLine(
                color = colors.accent.copy(alpha = 0.40f),
                start = Offset(activePt.x, padTop),
                end = Offset(activePt.x, chartBottom),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )

            // Outer glow ring
            drawCircle(
                color = colors.accent.copy(alpha = 0.22f),
                radius = 11.dp.toPx(),
                center = activePt,
            )
            // Accent ring
            drawCircle(
                color = colors.accent,
                radius = 6.dp.toPx(),
                center = activePt,
            )
            // Crisp center dot
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = activePt,
            )

            // If MA exists at active point, show indicator on MA curve
            val activeMa = points[scrubIndex].movingAverageKg
            if (activeMa != null) {
                val maPt = Offset(activePt.x, mapY(activeMa))
                drawCircle(
                    color = colors.textSecondary,
                    radius = 4.5.dp.toPx(),
                    center = maPt,
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = maPt,
                )
            }
        }
    }
}

@Composable
private fun ChartLegend() {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(colors.accent),
            )
            Text(
                text = stringResource(Res.string.trend_legend_weight),
                style = typography.caption.copy(fontSize = 11.sp),
                color = colors.textSecondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Canvas(modifier = Modifier.size(width = 16.dp, height = 3.dp)) {
                drawLine(
                    color = colors.textSecondary.copy(alpha = 0.70f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
                )
            }
            Text(
                text = stringResource(Res.string.trend_moving_average),
                style = typography.caption.copy(fontSize = 11.sp),
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun EmptyTrendPlaceholder() {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Aesthetic dashed mini chart placeholder
        Canvas(modifier = Modifier.size(width = 120.dp, height = 44.dp)) {
            val w = size.width
            val h = size.height

            val placeholderPath = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(w * 0.35f, h * 0.75f, w * 0.45f, h * 0.25f, w, h * 0.30f)
            }

            drawPath(
                path = placeholderPath,
                color = colors.glassBorder.copy(alpha = 0.50f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    cap = StrokeCap.Round,
                ),
            )

            drawCircle(
                color = colors.accent.copy(alpha = 0.40f),
                radius = 4.dp.toPx(),
                center = Offset(w, h * 0.30f),
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = stringResource(Res.string.trend_empty_title),
            style = typography.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.trend_empty_subtitle),
            style = typography.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
