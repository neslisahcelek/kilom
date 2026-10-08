package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.createCGImage
import platform.CoreImage.kCIImageApplyOrientationProperty
import platform.Foundation.NSError
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIView
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual val isCustomCameraSupported: Boolean = true

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class CameraController {
    val session = AVCaptureSession()
    val photoOutput = AVCapturePhotoOutput()
    private val ciContext = CIContext(options = null)
    private var videoDevice: AVCaptureDevice? = null
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    var isTorchActiveState by mutableStateOf(false)
        private set

    actual val isTorchActive: Boolean get() = isTorchActiveState

    init {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        videoDevice = device
        if (device != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null)
            if (input != null && session.canAddInput(input)) {
                session.addInput(input)
            }
        }
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
        }
        NSOperationQueue().addOperationWithBlock {
            session.startRunning()
        }
    }

    actual fun toggleTorch() {
        val dev = videoDevice ?: return
        if (!dev.hasTorch) return
        try {
            if (dev.lockForConfiguration(null)) {
                val next = !isTorchActiveState
                dev.torchMode = if (next) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                dev.unlockForConfiguration()
                isTorchActiveState = next
            }
        } catch (_: Exception) {}
    }

    actual suspend fun capture(viewfinder: ViewfinderRect): ByteArray? = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            val settings = AVCapturePhotoSettings.photoSettings()
            val delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
                override fun captureOutput(
                    output: AVCapturePhotoOutput,
                    didFinishProcessingPhoto: AVCapturePhoto,
                    error: NSError?
                ) {
                    if (error != null) {
                        continuation.resume(null)
                        return
                    }
                    val data = didFinishProcessingPhoto.fileDataRepresentation()
                    if (data == null) {
                        continuation.resume(null)
                        return
                    }

                    // Crop to viewfinder rectangle with safe margin
                    val croppedBytes = cropViewfinderImage(data, viewfinder)
                    continuation.resume(croppedBytes)
                }
            }
            photoOutput.capturePhotoWithSettings(settings, delegate = delegate)
        }
    }

    private fun cropViewfinderImage(data: platform.Foundation.NSData, vf: ViewfinderRect): ByteArray? {
        val ci = CIImage.imageWithData(data, options = mapOf(kCIImageApplyOrientationProperty to true)) ?: return null
        val extent = ci.extent
        val imgW = extent.useContents { size.width }
        val imgH = extent.useContents { size.height }
        if (imgW <= 0.0 || imgH <= 0.0) return null

        // Add 8% padding around viewfinder so digits aren't clipped on edges
        val padX = (vf.right - vf.left) * 0.08f
        val padY = (vf.bottom - vf.top) * 0.08f
        val left = (vf.left - padX).coerceIn(0f, 1f)
        val right = (vf.right + padX).coerceIn(0f, 1f)
        val top = (vf.top - padY).coerceIn(0f, 1f)
        val bottom = (vf.bottom + padY).coerceIn(0f, 1f)

        // CoreImage coordinate system has origin at bottom-left
        val cropX = left * imgW
        val cropY = (1.0 - bottom) * imgH
        val cropW = (right - left) * imgW
        val cropH = (bottom - top) * imgH

        val croppedCi = ci.imageByCroppingToRect(CGRectMake(cropX, cropY, cropW, cropH))
        val cg = ciContext.createCGImage(croppedCi, fromRect = croppedCi.extent) ?: return null
        val uiImage = UIImage.imageWithCGImage(cg)
        val jpeg = UIImageJPEGRepresentation(uiImage, 0.92) ?: return null

        val length = jpeg.length.toInt()
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), jpeg.bytes, jpeg.length)
        }
        return bytes
    }

    actual fun release() {
        if (isTorchActiveState) {
            videoDevice?.let { dev ->
                if (dev.hasTorch && dev.lockForConfiguration(null)) {
                    dev.torchMode = AVCaptureTorchModeOff
                    dev.unlockForConfiguration()
                }
            }
        }
        NSOperationQueue().addOperationWithBlock {
            if (session.isRunning()) {
                session.stopRunning()
            }
        }
    }
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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            val view = UIView()
            view.backgroundColor = UIColor.blackColor
            val layer = AVCaptureVideoPreviewLayer(session = controller.session)
            layer.videoGravity = AVLayerVideoGravityResizeAspectFill
            layer.frame = view.bounds
            view.layer.addSublayer(layer)
            controller.previewLayer = layer
            view
        },
        modifier = modifier,
        onResize = { view, rect ->
            controller.previewLayer?.frame = rect
        }
    )
}
