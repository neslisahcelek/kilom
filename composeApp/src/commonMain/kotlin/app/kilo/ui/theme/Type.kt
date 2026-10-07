package app.kilo.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** iOS-like hierarchy. Default font family resolves to SF on iOS and Roboto on Android. */
data class KiloTypography(
    val hero: TextStyle,
    val largeTitle: TextStyle,
    val title: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val callout: TextStyle,
    val subhead: TextStyle,
    val caption: TextStyle,
    val input: TextStyle,
)

private val family = FontFamily.Default

val DefaultKiloTypography = KiloTypography(
    hero = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 72.sp, lineHeight = 76.sp, letterSpacing = (-0.03).em),
    largeTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 41.sp, letterSpacing = (-0.02).em),
    title = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    headline = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 22.sp),
    callout = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 21.sp),
    subhead = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    input = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 62.sp, letterSpacing = (-0.02).em),
)

val LocalKiloTypography = staticCompositionLocalOf { DefaultKiloTypography }
