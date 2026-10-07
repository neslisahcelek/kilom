package app.kilo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.domain.WeightUnit
import app.kilo.domain.format
import app.kilo.domain.formatWeight
import app.kilo.domain.label
import app.kilo.domain.valueIn
import app.kilo.ui.HeroState
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.delta_vs_previous
import kilo.composeapp.generated.resources.hero_empty_subtitle
import kilo.composeapp.generated.resources.hero_empty_title
import kilo.composeapp.generated.resources.hero_since_days
import kilo.composeapp.generated.resources.hero_since_today
import kilo.composeapp.generated.resources.hero_since_yesterday
import org.jetbrains.compose.resources.stringResource

@Composable
fun HeroCard(
    hero: HeroState?,
    unit: WeightUnit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    GlassCard(
        hazeState = hazeState,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
    ) {
        if (hero == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "⚖",
                        fontSize = 28.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.hero_empty_title),
                    style = typography.title,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.hero_empty_subtitle),
                    style = typography.subhead,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Days since badge
                val sinceText = when (hero.daysSince) {
                    0 -> stringResource(Res.string.hero_since_today)
                    1 -> stringResource(Res.string.hero_since_yesterday)
                    else -> stringResource(Res.string.hero_since_days, hero.daysSince)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.textSecondary.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = sinceText,
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Large weight value with animated number
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    val weightFormatted = formatWeight(hero.latest.entry.kg, unit)
                    AnimatedContent(
                        targetState = weightFormatted,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "hero_weight_anim",
                    ) { targetWeight ->
                        Text(
                            text = targetWeight,
                            style = typography.hero,
                            color = colors.textPrimary,
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = unit.label,
                        style = typography.title,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                // Delta badge (if previous entry exists)
                val delta = hero.latest.delta
                if (delta != null) {
                    Spacer(Modifier.height(12.dp))
                    val deltaVal = delta.valueIn(unit)
                    val deltaColor = when {
                        deltaVal > 0 -> colors.positive
                        deltaVal < 0 -> colors.negative
                        else -> colors.neutral
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(deltaColor.copy(alpha = 0.14f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = delta.format(unit),
                                style = typography.callout,
                                color = deltaColor,
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = stringResource(Res.string.delta_vs_previous),
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
