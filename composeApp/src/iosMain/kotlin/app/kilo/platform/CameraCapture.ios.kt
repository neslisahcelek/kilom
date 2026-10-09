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
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.exposureMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.hasTorch
import platform.AVFoundation.torchMode
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
import app.kilo.ocr.logOcr
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
            try {
                if (device.lockForConfiguration(null)) {
                    if (device.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
                        device.focusMode = AVCaptureFocusModeContinuousAutoFocus
                    }
                    if (device.isExposureModeSupported(AVCaptureExposureModeContinuousAutoExposure)) {
                        device.exposureMode = AVCaptureExposureModeContinuousAutoExposure
                    }
                    device.unlockForConfiguration()
                }
            } catch (e: Exception) {
                logOcr("ios camera_config_failed exception=${e::class.simpleName}")
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
                        logOcr("ios capture_failed domain=${error.domain} code=${error.code}")
                        continuation.resume(null)
                        return
                    }
                    val data = didFinishProcessingPhoto.fileDataRepresentation()
                    if (data == null) {
                        logOcr("ios capture_null_data")
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

        // Calculate aspect-fill transformation between image and viewfinder screen
        val layerBounds = previewLayer?.bounds
        val screenW = layerBounds?.useContents { size.width } ?: 0.0
        val screenH = layerBounds?.useContents { size.height } ?: 0.0

        val (rawImgX1, rawImgY1, rawImgX2, rawImgY2) = if (screenW > 0.0 && screenH > 0.0) {
            val scale = maxOf(screenW / imgW, screenH / imgH)
            val scaledW = imgW * scale
            val scaledH = imgH * scale
            val offsetX = (scaledW - screenW) / 2.0
            val offsetY = (scaledH - screenH) / 2.0

            val imgX1 = (vf.left * screenW + offsetX) / scale
            val imgY1 = (vf.top * screenH + offsetY) / scale
            val imgX2 = (vf.right * screenW + offsetX) / scale
            val imgY2 = (vf.bottom * screenH + offsetY) / scale
            listOf(imgX1, imgY1, imgX2, imgY2)
        } else {
            listOf(vf.left.toDouble() * imgW, vf.top.toDouble() * imgH, vf.right.toDouble() * imgW, vf.bottom.toDouble() * imgH)
        }

        // Add 15% padding so digits aren't clipped on edges
        val padX = (rawImgX2 - rawImgX1) * 0.15
        val padY = (rawImgY2 - rawImgY1) * 0.15
        val left = (rawImgX1 - padX).coerceIn(0.0, imgW)
        val right = (rawImgX2 + padX).coerceIn(0.0, imgW)
        val top = (rawImgY1 - padY).coerceIn(0.0, imgH)
        val bottom = (rawImgY2 + padY).coerceIn(0.0, imgH)

        // CoreImage coordinate system has origin at bottom-left
        val cropX = left
        val cropY = imgH - bottom
        val cropW = right - left
        val cropH = bottom - top

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
