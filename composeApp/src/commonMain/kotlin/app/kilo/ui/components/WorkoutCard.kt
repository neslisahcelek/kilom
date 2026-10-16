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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.data.HevyWorkoutSession
import app.kilo.data.UserGender
import app.kilo.domain.StrengthTier
import app.kilo.domain.analyzeGeneralWorkouts
import app.kilo.domain.calculateRelativeStrength
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.correlation_tier_advanced
import kilo.composeapp.generated.resources.correlation_tier_beginner
import kilo.composeapp.generated.resources.correlation_tier_elite
import kilo.composeapp.generated.resources.correlation_tier_intermediate
import kilo.composeapp.generated.resources.hevy_import_button
import kilo.composeapp.generated.resources.workout_card_active_sessions_format
import kilo.composeapp.generated.resources.workout_card_empty_subtitle
import kilo.composeapp.generated.resources.workout_card_open_cta
import kilo.composeapp.generated.resources.workout_card_preview_cta
import kilo.composeapp.generated.resources.workout_card_title
import kilo.composeapp.generated.resources.workout_card_top_lift_format
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun WorkoutCard(
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double,
    userGender: UserGender,
    hazeState: HazeState?,
    onImportHevyCsv: (String) -> Unit,
    onOpenScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val scope = rememberCoroutineScope()

    val isMaleStandard = userGender == UserGender.MALE

    val csvPicker = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("csv")),
    ) { platformFile: PlatformFile? ->
        if (platformFile != null) {
            scope.launch {
                try {
                    val text = platformFile.readString()
                    if (text.isNotBlank()) {
                        onImportHevyCsv(text)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val generalAnalytics = remember(workouts) {
        analyzeGeneralWorkouts(workouts)
    }

    val topLift = remember(workouts, currentWeightKg, isMaleStandard) {
        findBestRelativeLift(
            workouts = workouts,
            currentWeightKg = currentWeightKg,
            isMale = isMaleStandard,
        )
    }

    GlassCard(
        hazeState = hazeState,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onOpenScreen),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Header Row
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
                        Text(text = "🏋️", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(Res.string.workout_card_title),
                        style = typography.headline,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (workouts.isNotEmpty()) {
                    // Quick CSV update icon button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.glassTint)
                            .border(0.5.dp, colors.glassBorder, CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClick = { csvPicker.launch() },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "↑",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                        )
                    }
                }
            }

            if (workouts.isEmpty()) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(Res.string.workout_card_empty_subtitle),
                    style = typography.caption.copy(lineHeight = 16.5.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // Primary CSV Import Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent.copy(alpha = 0.16f))
                        .border(0.5.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { csvPicker.launch() }
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.hevy_import_button),
                        style = typography.caption.copy(fontSize = 12.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Secondary: Open Scales & Methodology Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.workout_card_preview_cta),
                        style = typography.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            } else {
                Spacer(Modifier.height(10.dp))

                // Session & Average Volume Summary
                val avgStr = formatCardVolume(generalAnalytics.avgSessionVolumeKg)
                Text(
                    text = stringResource(
                        Res.string.workout_card_active_sessions_format,
                        generalAnalytics.sessionCount,
                        avgStr,
                    ),
                    style = typography.caption.copy(fontSize = 12.5.sp),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                )

                // Top Relative Strength Lift Badge
                if (topLift != null && currentWeightKg > 0.0) {
                    Spacer(Modifier.height(10.dp))
                    val ratioStr = formatCardTwoDecimals(topLift.relativeRatio)
                    val tierLabel = when (topLift.tier) {
                        StrengthTier.BEGINNER -> stringResource(Res.string.correlation_tier_beginner)
                        StrengthTier.INTERMEDIATE -> stringResource(Res.string.correlation_tier_intermediate)
                        StrengthTier.ADVANCED -> stringResource(Res.string.correlation_tier_advanced)
                        StrengthTier.ELITE -> stringResource(Res.string.correlation_tier_elite)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accent.copy(alpha = 0.10f))
                            .border(0.5.dp, colors.accent.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "💪", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                Res.string.workout_card_top_lift_format,
                                topLift.exerciseName,
                                ratioStr,
                                tierLabel,
                            ),
                            style = typography.caption.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.workout_card_open_cta),
                        style = typography.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            }
        }
    }
}

private fun findBestRelativeLift(
    workouts: List<HevyWorkoutSession>,
    currentWeightKg: Double,
    isMale: Boolean,
) = if (workouts.isEmpty() || currentWeightKg <= 0.0) {
    null
} else {
    val targetLiftKeywords = listOf(
        "Squat" to listOf("squat"),
        "Deadlift" to listOf("deadlift", "rdl"),
        "Bench Press" to listOf("bench press", "chest press"),
        "Overhead Press" to listOf("overhead press", "military press", "ohp"),
        "Barbell Row" to listOf("row", "pull"),
        "Hip Thrust" to listOf("hip thrust"),
    )

    targetLiftKeywords.mapNotNull { (label, keywords) ->
        var best1Rm = 0.0
        var bestWeight = 0.0
        var bestReps = 0
        for (workout in workouts) {
            for (exercise in workout.exercises) {
                val lower = exercise.title.lowercase()
                if (keywords.any { lower.contains(it) } && exercise.estimated1RmKg > best1Rm) {
                    best1Rm = exercise.estimated1RmKg
                    val bestSet = exercise.sets.maxByOrNull { s ->
                        if (s.reps <= 0 || s.weightKg <= 0.0) 0.0
                        else if (s.reps == 1) s.weightKg
                        else s.weightKg * (1.0 + s.reps / 30.0)
                    }
                    bestWeight = bestSet?.weightKg ?: exercise.maxWeightKg
                    bestReps = bestSet?.reps ?: 1
                }
            }
        }
        if (best1Rm > 0.0) {
            calculateRelativeStrength(
                bodyWeightKg = currentWeightKg,
                exerciseName = label,
                weightLiftedKg = bestWeight,
                reps = bestReps,
                isMale = isMale,
            )
        } else {
            null
        }
    }.maxByOrNull { it.relativeRatio }
}

private fun formatCardVolume(volume: Double): String {
    return if (volume >= 1000.0) {
        val thousands = volume / 1000.0
        val tenths = round(thousands * 10.0).toInt()
        "${tenths / 10}.${abs(tenths % 10)}k"
    } else {
        "${round(volume).toInt()}"
    }
}

private fun formatCardTwoDecimals(value: Double): String {
    val hundredths = round(value * 100.0).toInt()
    val whole = abs(hundredths) / 100
    val frac = abs(hundredths) % 100
    val fracPadded = if (frac < 10) "0$frac" else "$frac"
    return "$whole.$fracPadded"
}
