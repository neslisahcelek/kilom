package app.kilo.platform

import androidx.compose.runtime.Composable
import app.kilo.ocr.ScaleOcr

/** Platform haptic feedback. */
expect class Haptics {
    fun success()
    fun scanComplete()
    fun light()
}

@Composable
expect fun rememberHaptics(): Haptics

@Composable
expect fun rememberScaleOcr(): ScaleOcr
