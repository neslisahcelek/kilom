package app.kilo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Frosted glass surface: haze blur of [hazeState]'s source, tint, 0.5dp border and a top specular highlight.
 * Pass `null` for [hazeState] to render without blur (previews).
 */
@Composable
fun GlassCard(
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit,
) {
    val c = KiloTheme.colors
    Box(
        modifier
            .clip(shape)
            .then(
                if (hazeState != null) Modifier.hazeEffect(state = hazeState) {
                    blurRadius = 28.dp
                    backgroundColor = c.backgroundTop
                    tints = listOf(HazeTint(c.glassTint))
                } else Modifier.background(c.glassTint)
            )
            .border(BorderStroke(0.5.dp, c.glassBorder), shape),
    ) {
        // top specular highlight
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(c.glassSpecular.copy(alpha = c.glassSpecular.alpha * 0.45f), Color.Transparent)))
        )
        content()
    }
}
