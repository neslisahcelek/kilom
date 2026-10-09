package app.kilo.ui.components

import app.kilo.domain.WeightTag
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.tag_heavy_meal
import kilo.composeapp.generated.resources.tag_morning_fasted
import kilo.composeapp.generated.resources.tag_post_workout
import kilo.composeapp.generated.resources.tag_water_retention
import org.jetbrains.compose.resources.StringResource

fun WeightTag.labelRes(): StringResource = when (this) {
    WeightTag.MORNING_FASTED -> Res.string.tag_morning_fasted
    WeightTag.POST_WORKOUT -> Res.string.tag_post_workout
    WeightTag.HEAVY_MEAL -> Res.string.tag_heavy_meal
    WeightTag.WATER_RETENTION -> Res.string.tag_water_retention
}
