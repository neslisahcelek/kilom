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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.Milestone
import app.kilo.domain.MilestoneType
import app.kilo.domain.StreakInfo
import app.kilo.domain.WeightUnit
import app.kilo.domain.formatWeight
import app.kilo.domain.label
import app.kilo.ui.theme.KiloTheme
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.milestone_goal_reached
import kilo.composeapp.generated.resources.milestone_loss_1
import kilo.composeapp.generated.resources.milestone_loss_10
import kilo.composeapp.generated.resources.milestone_loss_3
import kilo.composeapp.generated.resources.milestone_loss_5
import kilo.composeapp.generated.resources.milestone_streak_14
import kilo.composeapp.generated.resources.milestone_streak_3
import kilo.composeapp.generated.resources.milestone_streak_30
import kilo.composeapp.generated.resources.milestone_streak_7
import kilo.composeapp.generated.resources.share_action_button
import kilo.composeapp.generated.resources.share_card_badge_on_device
import kilo.composeapp.generated.resources.share_card_streak_badge
import kilo.composeapp.generated.resources.share_card_subtitle
import kilo.composeapp.generated.resources.share_hide_weight_subtitle
import kilo.composeapp.generated.resources.share_hide_weight_title
import kilo.composeapp.generated.resources.share_sheet_title
import kilo.composeapp.generated.resources.share_text_with_weight
import kilo.composeapp.generated.resources.share_text_private
import kilo.composeapp.generated.resources.streak_current_days
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardSheet(
    isOpen: Boolean,
    streak: StreakInfo?,
    latestKg: Double?,
    unit: WeightUnit,
    onShare: (text: String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (!isOpen) return

    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    var hideWeight by remember { mutableStateOf(false) }

    val activeMilestone = streak?.activeMilestones?.firstOrNull()

    // Formatted texts for share
    val shareWithWeightText = stringResource(
        Res.string.share_text_with_weight,
        latestKg?.let { "${formatWeight(it, unit)} ${unit.label}" } ?: "",
        streak?.currentStreak ?: 1,
    )
    val sharePrivateText = stringResource(
        Res.string.share_text_private,
        streak?.currentStreak ?: 1,
    )

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
            Text(
                text = stringResource(Res.string.share_sheet_title),
                style = typography.largeTitle,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(20.dp))

            // Liquid Glass Achievement Card Preview
            AchievementCardPreview(
                streak = streak,
                activeMilestone = activeMilestone,
                latestKg = latestKg,
                unit = unit,
                hideWeight = hideWeight,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            // Privacy Toggle: Hide Exact Weight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.glassTint)
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(Res.string.share_hide_weight_title),
                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.share_hide_weight_subtitle),
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                    Switch(
                        checked = hideWeight,
                        onCheckedChange = { hideWeight = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.glassTint,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Share Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent)
                    .clickable {
                        val finalShareText = if (hideWeight || latestKg == null) {
                            sharePrivateText
                        } else {
                            shareWithWeightText
                        }
                        onShare(finalShareText)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.share_action_button),
                    style = typography.headline,
                    color = colors.onAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AchievementCardPreview(
    streak: StreakInfo?,
    activeMilestone: Milestone?,
    latestKg: Double?,
    unit: WeightUnit,
    hideWeight: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val gradient = Brush.verticalGradient(
        colors = listOf(
            colors.blobA.copy(alpha = 0.28f),
            colors.blobB.copy(alpha = 0.22f),
            colors.glassTint.copy(alpha = 0.85f),
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(1.5.dp, colors.glassSpecular.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .padding(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // App branding top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "⚖️  Kilom",
                    style = typography.headline,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.glassBorder.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.share_card_badge_on_device),
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Big achievement emoji
            val emoji = activeMilestone?.emoji ?: if ((streak?.currentStreak ?: 0) > 0) "🔥" else "✨"
            Text(
                text = emoji,
                fontSize = 44.sp,
            )

            Spacer(Modifier.height(8.dp))

            // Milestone or Streak headline
            val titleText = if (activeMilestone != null) {
                milestoneTitle(activeMilestone.type)
            } else {
                stringResource(Res.string.streak_current_days, streak?.currentStreak ?: 1)
            }
            Text(
                text = titleText,
                style = typography.title,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            // Body: Exact weight or Private streak badge
            if (!hideWeight && latestKg != null) {
                Text(
                    text = "${formatWeight(latestKg, unit)} ${unit.label}",
                    style = typography.hero.copy(fontSize = 40.sp, lineHeight = 44.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.streak_current_days, streak?.currentStreak ?: 1),
                    style = typography.callout,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.share_card_streak_badge, streak?.currentStreak ?: 1),
                        style = typography.headline,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Card footer
            Text(
                text = stringResource(Res.string.share_card_subtitle),
                style = typography.caption,
                color = colors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun milestoneTitle(type: MilestoneType): String {
    return when (type) {
        MilestoneType.STREAK_3_DAYS -> stringResource(Res.string.milestone_streak_3)
        MilestoneType.STREAK_7_DAYS -> stringResource(Res.string.milestone_streak_7)
        MilestoneType.STREAK_14_DAYS -> stringResource(Res.string.milestone_streak_14)
        MilestoneType.STREAK_30_DAYS -> stringResource(Res.string.milestone_streak_30)
        MilestoneType.WEIGHT_LOSS_1KG -> stringResource(Res.string.milestone_loss_1)
        MilestoneType.WEIGHT_LOSS_3KG -> stringResource(Res.string.milestone_loss_3)
        MilestoneType.WEIGHT_LOSS_5KG -> stringResource(Res.string.milestone_loss_5)
        MilestoneType.WEIGHT_LOSS_10KG -> stringResource(Res.string.milestone_loss_10)
        MilestoneType.GOAL_REACHED -> stringResource(Res.string.milestone_goal_reached)
    }
}
