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

/** Camera permission and availability lifecycle status. */
enum class CameraPermissionStatus {
    CHECKING,
    GRANTED,
    DENIED,
    NOT_SUPPORTED,
}

/** Whether the platform supports the in-app live viewfinder camera. */
expect val isCustomCameraSupported: Boolean

/** Platform camera controller for live viewfinder preview and ROI capture. */
expect class CameraController {
    val permissionStatus: CameraPermissionStatus
    val isTorchActive: Boolean
    fun toggleTorch()
    suspend fun capture(viewfinder: ViewfinderRect): ByteArray?
    fun release()
    fun openSettings()
    fun refreshPermission()
}

@Composable
expect fun rememberCameraController(): CameraController

@Composable
expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)
