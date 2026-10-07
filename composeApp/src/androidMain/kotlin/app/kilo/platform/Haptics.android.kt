package app.kilo.platform

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import app.kilo.ocr.ScaleOcr

actual class Haptics internal constructor(private val view: View?) {
    actual fun success() {
        perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS)
    }

    actual fun scanComplete() {
        perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.KEYBOARD_TAP)
    }

    actual fun light() {
        perform(HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun perform(constant: Int) {
        view?.performHapticFeedback(constant)
    }
}

@Composable
actual fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

@Composable
actual fun rememberScaleOcr(): ScaleOcr = remember { ScaleOcr() }
