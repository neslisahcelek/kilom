package app.kilo.platform

import androidx.compose.runtime.Composable

/**
 * Native platform share sheet integration.
 * Triggers standard Android Intent or iOS UIActivityViewController.
 * Completely on-device: no tracking, no external SDKs.
 */
interface ShareSheet {
    fun shareText(text: String, title: String? = null)
}

/** No-op implementation for testing and previews. */
object NoOpShareSheet : ShareSheet {
    override fun shareText(text: String, title: String?) = Unit
}

@Composable
expect fun rememberShareSheet(): ShareSheet
