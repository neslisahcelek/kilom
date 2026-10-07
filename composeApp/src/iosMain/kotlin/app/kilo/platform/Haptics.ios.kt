package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.kilo.ocr.ScaleOcr
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

@OptIn(ExperimentalForeignApi::class)
actual class Haptics {
    actual fun success() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    actual fun scanComplete() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
    }

    actual fun light() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
    }
}

@Composable
actual fun rememberHaptics(): Haptics = remember { Haptics() }

@Composable
actual fun rememberScaleOcr(): ScaleOcr = remember { ScaleOcr() }
