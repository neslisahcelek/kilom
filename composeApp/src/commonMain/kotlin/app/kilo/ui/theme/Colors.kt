package app.kilo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** All UI colors live here. */
data class KiloColors(
    val isDark: Boolean,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val blobA: Color,
    val blobB: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glassTint: Color,
    val glassBorder: Color,
    val glassSpecular: Color,
    val accent: Color,
    val onAccent: Color,
    val positive: Color,
    val negative: Color,
    val neutral: Color,
    val danger: Color,
    val scrim: Color,
    val sheet: Color,
) {
    val backgroundBrush: Brush get() = Brush.verticalGradient(listOf(backgroundTop, backgroundBottom))
}

val LightKiloColors = KiloColors(
    isDark = false,
    backgroundTop = Color(0xFFE8F0FF),
    backgroundBottom = Color(0xFFF6EEFF),
    blobA = Color(0xFF7AA7FF),
    blobB = Color(0xFFFFA8D0),
    textPrimary = Color(0xFF111418),
    textSecondary = Color(0xFF5B6270),
    glassTint = Color(0xB3FFFFFF),
    glassBorder = Color(0x66FFFFFF),
    glassSpecular = Color(0xCCFFFFFF),
    accent = Color(0xFF0A84FF),
    onAccent = Color(0xFFFFFFFF),
    positive = Color(0xFFE5484D),
    negative = Color(0xFF30A46C),
    neutral = Color(0xFF8A8F98),
    danger = Color(0xFFE5484D),
    scrim = Color(0x66000000),
    sheet = Color(0xE6FFFFFF),
)

val DarkKiloColors = KiloColors(
    isDark = true,
    backgroundTop = Color(0xFF0B1020),
    backgroundBottom = Color(0xFF1A1030),
    blobA = Color(0xFF2B5BD7),
    blobB = Color(0xFF9B2F7A),
    textPrimary = Color(0xFFF5F6F8),
    textSecondary = Color(0xFFA0A7B5),
    glassTint = Color(0x33FFFFFF),
    glassBorder = Color(0x33FFFFFF),
    glassSpecular = Color(0x40FFFFFF),
    accent = Color(0xFF0A84FF),
    onAccent = Color(0xFFFFFFFF),
    positive = Color(0xFFFF6369),
    negative = Color(0xFF3DD68C),
    neutral = Color(0xFF9AA0AA),
    danger = Color(0xFFFF6369),
    scrim = Color(0x99000000),
    sheet = Color(0xE61C1C24),
)

val LocalKiloColors = staticCompositionLocalOf { LightKiloColors }

object KiloTheme {
    val colors: KiloColors
        @Composable @ReadOnlyComposable get() = LocalKiloColors.current
    val type: KiloTypography
        @Composable @ReadOnlyComposable get() = LocalKiloTypography.current
}

@Composable
fun KiloTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalKiloColors provides if (darkTheme) DarkKiloColors else LightKiloColors,
        LocalKiloTypography provides DefaultKiloTypography,
        content = content,
    )
}
