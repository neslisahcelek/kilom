package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Normalized rectangle coordinates [0..1] defining the viewfinder crop area. */
data class ViewfinderRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/** Whether the platform supports the in-app live viewfinder camera. */
expect val isCustomCameraSupported: Boolean

/** Platform camera controller for live viewfinder preview and ROI capture. */
expect class CameraController {
    val isTorchActive: Boolean
    fun toggleTorch()
    suspend fun capture(viewfinder: ViewfinderRect): ByteArray?
    fun release()
}

@Composable
expect fun rememberCameraController(): CameraController

@Composable
expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)
