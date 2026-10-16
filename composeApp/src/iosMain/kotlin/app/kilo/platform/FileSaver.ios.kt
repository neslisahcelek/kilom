package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

/**
 * iOS implementation of [FileSaver].
 * Writes file to NSTemporaryDirectory and presents UIActivityViewController,
 * allowing the user to "Save to Files" (Dosyalara Kaydet), AirDrop, or share.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosFileSaver : FileSaver {
    override fun saveFile(suggestedName: String, extension: String, data: String) {
        val fileName = "$suggestedName.$extension"
        val filePath = "${NSTemporaryDirectory().trimEnd('/')}/$fileName"

        val bytes = data.encodeToByteArray()
        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        val success = NSFileManager.defaultManager.createFileAtPath(
            path = filePath,
            contents = nsData,
            attributes = null,
        )
        if (!success) return

        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null,
        )

        val root = findKeyWindow()?.rootViewController ?: return
        val top = findTopViewController(root)

        activityViewController.popoverPresentationController?.let { popover ->
            popover.sourceView = top.view
            popover.sourceRect = top.view.bounds
        }

        top.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }

    private fun findKeyWindow(): UIWindow? {
        val app = UIApplication.sharedApplication
        return app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
    }

    private fun findTopViewController(controller: UIViewController): UIViewController {
        val presented = controller.presentedViewController
        return if (presented != null) {
            findTopViewController(presented)
        } else {
            controller
        }
    }
}

@Composable
actual fun rememberFileSaver(): FileSaver = remember { IosFileSaver() }
