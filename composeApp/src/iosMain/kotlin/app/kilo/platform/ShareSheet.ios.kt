package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

/** iOS actual for [ShareSheet] using native [UIActivityViewController]. */
@OptIn(ExperimentalForeignApi::class)
class IosShareSheet : ShareSheet {
    override fun shareText(text: String, title: String?) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
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
actual fun rememberShareSheet(): ShareSheet = remember { IosShareSheet() }
