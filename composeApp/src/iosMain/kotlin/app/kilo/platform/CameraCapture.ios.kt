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
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.hasTorch
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.createCGImage
import platform.CoreImage.kCIImageApplyOrientationProperty
import platform.Foundation.NSError
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIView
import platform.darwin.NSObject
import app.kilo.ocr.logOcr
import kotlin.coroutines.resume

import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.exposurePointOfInterestSupported
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.focusPointOfInterestSupported
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.setExposureTargetBias
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGPointMake
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.dispatch_queue_create

actual val isCustomCameraSupported: Boolean = true

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class CameraController {
    val session = AVCaptureSession()
    val photoOutput = AVCapturePhotoOutput()
    private val videoDataOutput = AVCaptureVideoDataOutput()
    private val ciContext = CIContext(options = null)
    private var videoDevice: AVCaptureDevice? = null
    var previewLayer: AVCaptureVideoPreviewLayer? = null
    private val sessionQueue = NSOperationQueue().apply { maxConcurrentOperationCount = 1 }
    private val videoFrameQueue = dispatch_queue_create("app.kilo.camera.videoframes", null)
    private var activeCaptureDelegate: NSObject? = null
    private var videoSampleDelegate: NSObject? = null
    private var pendingFrameRequest: Pair<ViewfinderRect, (ByteArray?) -> Unit>? = null

    var permissionStatusState by mutableStateOf(CameraPermissionStatus.CHECKING)
        private set

    actual val permissionStatus: CameraPermissionStatus get() = permissionStatusState

    var isTorchActiveState by mutableStateOf(false)
        private set

    actual val isTorchActive: Boolean get() = isTorchActiveState

    var isLowGlareModeState by mutableStateOf(false)
        private set

    actual val isLowGlareMode: Boolean get() = isLowGlareModeState

    init {
        checkPermissionAndSetup()
    }

    private fun checkPermissionAndSetup() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device == null) {
            permissionStatusState = CameraPermissionStatus.NOT_SUPPORTED
            return
        }
        videoDevice = device

        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                permissionStatusState = CameraPermissionStatus.GRANTED
                startSession()
            }
            AVAuthorizationStatusNotDetermined -> {
                permissionStatusState = CameraPermissionStatus.CHECKING
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted: Boolean ->
                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        if (granted) {
                            permissionStatusState = CameraPermissionStatus.GRANTED
                            startSession()
                        } else {
                            permissionStatusState = CameraPermissionStatus.DENIED
                        }
                    }
                }
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                permissionStatusState = CameraPermissionStatus.DENIED
            }
            else -> {
                permissionStatusState = CameraPermissionStatus.DENIED
            }
        }
    }

    private fun startSession() {
        val dev = videoDevice ?: return
        sessionQueue.addOperationWithBlock {
            if (!session.isRunning()) {
                session.beginConfiguration()
                session.sessionPreset = AVCaptureSessionPresetPhoto
                val input = AVCaptureDeviceInput.deviceInputWithDevice(dev, error = null)
                if (input != null && session.canAddInput(input)) {
                    session.addInput(input)
                }
                if (session.canAddOutput(photoOutput)) {
                    session.addOutput(photoOutput)
                }

                videoDataOutput.alwaysDiscardsLateVideoFrames = true
                val sampleDelegate = object : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
                    override fun captureOutput(
                        output: AVCaptureOutput,
                        didOutputSampleBuffer: CMSampleBufferRef?,
                        fromConnection: AVCaptureConnection
                    ) {
                        val req = pendingFrameRequest ?: return
                        pendingFrameRequest = null
                        val sampleBuffer = didOutputSampleBuffer ?: run {
                            req.second(null)
                            return
                        }
                        val imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) ?: run {
                            req.second(null)
                            return
                        }
                        val rawCi = CIImage.imageWithCVPixelBuffer(imageBuffer)
                        // Ensure portrait orientation if connection wasn't already rotated
                        val ci = if (fromConnection.videoOrientation == AVCaptureVideoOrientationPortrait) {
                            rawCi
                        } else {
                            rawCi.imageByApplyingOrientation(6)
                        }
                        val bytes = cropCiImageToJpeg(ci, req.first)
                        req.second(bytes)
                    }
                }
                videoSampleDelegate = sampleDelegate
                videoDataOutput.setSampleBufferDelegate(sampleDelegate, queue = videoFrameQueue)
                if (session.canAddOutput(videoDataOutput)) {
                    session.addOutput(videoDataOutput)
                    val conn = videoDataOutput.connectionWithMediaType(AVMediaTypeVideo)
                    if (conn != null && conn.supportsVideoOrientation) {
                        conn.videoOrientation = AVCaptureVideoOrientationPortrait
                    }
                }

                session.commitConfiguration()
                configureDeviceFocusAndExposure(dev, lowGlare = isLowGlareModeState)
                session.startRunning()
            }
        }
    }

    private fun configureDeviceFocusAndExposure(dev: AVCaptureDevice, lowGlare: Boolean) {
        try {
            if (dev.lockForConfiguration(null)) {
                val targetPoint = CGPointMake(0.5, 0.42)
                if (dev.focusPointOfInterestSupported && dev.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
                    dev.focusPointOfInterest = targetPoint
                    dev.focusMode = AVCaptureFocusModeContinuousAutoFocus
                }
                if (dev.exposurePointOfInterestSupported && dev.isExposureModeSupported(AVCaptureExposureModeContinuousAutoExposure)) {
                    dev.exposurePointOfInterest = targetPoint
                    dev.exposureMode = AVCaptureExposureModeContinuousAutoExposure
                }
                // Slight negative bias stops bright LED segments on dark glass scales from blooming into 8s
                val desiredBias = if (lowGlare) -1.15f else -0.35f
                val clampedBias = desiredBias.coerceIn(dev.minExposureTargetBias, dev.maxExposureTargetBias)
                dev.setExposureTargetBias(clampedBias, completionHandler = null)
                dev.unlockForConfiguration()
            }
        } catch (_: Exception) {}
    }

    actual fun openSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }

    actual fun refreshPermission() {
        if (permissionStatusState != CameraPermissionStatus.GRANTED) {
            checkPermissionAndSetup()
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

    actual fun toggleLowGlareMode() {
        val dev = videoDevice ?: return
        val next = !isLowGlareModeState
        isLowGlareModeState = next
        configureDeviceFocusAndExposure(dev, lowGlare = next)
    }

    actual suspend fun sampleLiveFrame(viewfinder: ViewfinderRect): ByteArray? = withContext(Dispatchers.Default) {
        if (!session.isRunning()) return@withContext null
        suspendCancellableCoroutine { continuation ->
            pendingFrameRequest = viewfinder to { bytes ->
                if (continuation.isActive) {
                    continuation.resume(bytes)
                }
            }
            continuation.invokeOnCancellation {
                pendingFrameRequest = null
            }
        }
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
                    activeCaptureDelegate = null
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

                    // Crop to viewfinder rectangle with generous safe margin
                    val croppedBytes = cropViewfinderImage(data, viewfinder)
                    continuation.resume(croppedBytes)
                }
            }
            activeCaptureDelegate = delegate
            continuation.invokeOnCancellation {
                activeCaptureDelegate = null
            }
            photoOutput.capturePhotoWithSettings(settings, delegate = delegate)
        }
    }

    private fun cropViewfinderImage(data: platform.Foundation.NSData, vf: ViewfinderRect): ByteArray? {
        val ci = CIImage.imageWithData(data, options = mapOf(kCIImageApplyOrientationProperty to true)) ?: return null
        return cropCiImageToJpeg(ci, vf)
    }

    private fun cropCiImageToJpeg(ci: CIImage, vf: ViewfinderRect): ByteArray? {
        val extent = ci.extent
        val originX = extent.useContents { origin.x }
        val originY = extent.useContents { origin.y }
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

        // Add generous padding (25% horizontal, 35% vertical) so outer LCD bezels and digits are never clipped
        val padX = (rawImgX2 - rawImgX1) * 0.25
        val padY = (rawImgY2 - rawImgY1) * 0.35
        val left = (rawImgX1 - padX).coerceIn(0.0, imgW)
        val right = (rawImgX2 + padX).coerceIn(0.0, imgW)
        val top = (rawImgY1 - padY).coerceIn(0.0, imgH)
        val bottom = (rawImgY2 + padY).coerceIn(0.0, imgH)

        // CoreImage coordinate system has origin at bottom-left + extent origin offset
        val cropX = originX + left
        val cropY = originY + (imgH - bottom)
        val cropW = (right - left).coerceAtLeast(1.0)
        val cropH = (bottom - top).coerceAtLeast(1.0)

        val croppedCi = ci.imageByCroppingToRect(CGRectMake(cropX, cropY, cropW, cropH))
        val cg = ciContext.createCGImage(croppedCi, fromRect = croppedCi.extent) ?: return null
        return try {
            val uiImage = UIImage.imageWithCGImage(cg)
            val jpeg = UIImageJPEGRepresentation(uiImage, 0.92) ?: return null

            val length = jpeg.length.toInt()
            val bytes = ByteArray(length)
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), jpeg.bytes, jpeg.length)
            }
            bytes
        } finally {
            CGImageRelease(cg)
        }
    }

    actual fun release() {
        activeCaptureDelegate = null
        pendingFrameRequest = null
        videoDataOutput.setSampleBufferDelegate(null, queue = null)
        videoSampleDelegate = null
        if (isTorchActiveState) {
            videoDevice?.let { dev ->
                if (dev.hasTorch && dev.lockForConfiguration(null)) {
                    dev.torchMode = AVCaptureTorchModeOff
                    dev.unlockForConfiguration()
                }
            }
        }
        sessionQueue.addOperationWithBlock {
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
private class CameraPreviewUIView(
    frame: CValue<CGRect> = CGRectZero.readValue(),
) : UIView(frame = frame) {
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer?.let { layer ->
            layer.frame = bounds
            val conn = layer.connection
            if (conn != null && conn.supportsVideoOrientation) {
                conn.videoOrientation = AVCaptureVideoOrientationPortrait
            }
        }
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            val view = CameraPreviewUIView()
            view.backgroundColor = UIColor.blackColor
            val layer = AVCaptureVideoPreviewLayer(session = controller.session)
            layer.videoGravity = AVLayerVideoGravityResizeAspectFill
            val conn = layer.connection
            if (conn != null && conn.supportsVideoOrientation) {
                conn.videoOrientation = AVCaptureVideoOrientationPortrait
            }
            view.layer.addSublayer(layer)
            view.previewLayer = layer
            controller.previewLayer = layer
            view
        },
        modifier = modifier,
        update = { view ->
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            view.previewLayer?.let { layer ->
                layer.frame = view.bounds
                val conn = layer.connection
                if (conn != null && conn.supportsVideoOrientation) {
                    conn.videoOrientation = AVCaptureVideoOrientationPortrait
                }
            }
            CATransaction.commit()
        },
        onResize = { view, _ ->
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            view.previewLayer?.let { layer ->
                layer.frame = view.bounds
                val conn = layer.connection
                if (conn != null && conn.supportsVideoOrientation) {
                    conn.videoOrientation = AVCaptureVideoOrientationPortrait
                }
            }
            CATransaction.commit()
        }
    )
}

