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
    actual val permissionStatus: CameraPermissionStatus = CameraPermissionStatus.NOT_SUPPORTED

    var isTorchActiveState by mutableStateOf(false)
        private set

    actual val isTorchActive: Boolean get() = isTorchActiveState

    var isLowGlareModeState by mutableStateOf(false)
        private set

    actual val isLowGlareMode: Boolean get() = isLowGlareModeState

    actual fun toggleTorch() {
        isTorchActiveState = !isTorchActiveState
    }

    actual fun toggleLowGlareMode() {
        isLowGlareModeState = !isLowGlareModeState
    }

    actual suspend fun capture(viewfinder: ViewfinderRect): ByteArray? {
        // Fallback stub on Android if custom camera is requested
        return null
    }

    actual suspend fun sampleLiveFrame(viewfinder: ViewfinderRect): ByteArray? {
        return null
    }

    actual fun release() {}

    actual fun openSettings() {}

    actual fun refreshPermission() {}
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
