package app.kilo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.HistoryItem
import app.kilo.domain.WeightUnit
import app.kilo.domain.format
import app.kilo.domain.formatWeight
import app.kilo.domain.label
import app.kilo.domain.valueIn
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.history_delete
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRow(
    item: HistoryItem,
    unit: WeightUnit,
    timeZone: TimeZone,
    onDelete: (String) -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item.entry.id)
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                if (isSwiping) colors.danger else colors.danger.copy(alpha = 0.85f),
                label = "delete_bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "🗑",
                        fontSize = 18.sp,
                    )
                    Text(
                        text = stringResource(Res.string.history_delete),
                        style = typography.callout.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
            }
        },
        content = {
            GlassCard(
                hazeState = hazeState,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Date & time
                    val ldt = item.entry.at.toLocalDateTime(timeZone)
                    val dateFormatted = "${ldt.dayOfMonth} ${monthName(ldt.monthNumber)} ${ldt.year}"
                    val timeFormatted = "${ldt.hour.toString().padStart(2, '0')}:${ldt.minute.toString().padStart(2, '0')}"

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = dateFormatted,
                            style = typography.headline,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = timeFormatted,
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }

                    // Weight & Delta
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${formatWeight(item.entry.kg, unit)} ${unit.label}",
                            style = typography.title.copy(fontSize = 20.sp),
                            color = colors.textPrimary,
                        )

                        if (item.delta != null) {
                            Spacer(Modifier.width(10.dp))
                            val deltaVal = item.delta.valueIn(unit)
                            val deltaColor = when {
                                deltaVal > 0 -> colors.positive
                                deltaVal < 0 -> colors.negative
                                else -> colors.neutral
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(deltaColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = item.delta.format(unit),
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = deltaColor,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

internal fun monthName(monthNumber: Int): String = when (monthNumber) {
    1 -> "Oca/Jan"
    2 -> "Şub/Feb"
    3 -> "Mar"
    4 -> "Nis/Apr"
    5 -> "May"
    6 -> "Haz/Jun"
    7 -> "Tem/Jul"
    8 -> "Ağu/Aug"
    9 -> "Eyl/Sep"
    10 -> "Eki/Oct"
    11 -> "Kas/Nov"
    12 -> "Ara/Dec"
    else -> ""
}
