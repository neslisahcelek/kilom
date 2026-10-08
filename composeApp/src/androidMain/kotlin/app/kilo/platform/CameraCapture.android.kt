package app.kilo.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

actual val isCustomCameraSupported: Boolean = false

actual class CameraController {
    var isTorchActiveState by mutableStateOf(false)
        private set

    actual val isTorchActive: Boolean get() = isTorchActiveState

    actual fun toggleTorch() {
        isTorchActiveState = !isTorchActiveState
    }

    actual suspend fun capture(viewfinder: ViewfinderRect): ByteArray? {
        // Fallback stub on Android if custom camera is requested
        return null
    }

    actual fun release() {}
}

@Composable
actual fun rememberCameraController(): CameraController {
    val controller = remember { CameraController() }
    DisposableEffect(Unit) {
        onDispose {
            controller.release()
        }
    }
    return controller
}

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    Box(modifier = modifier.background(Color.Black))
}
