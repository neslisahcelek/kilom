package app.kilo.platform

import androidx.compose.runtime.Composable

/**
 * Platform abstraction for saving/exporting backup files (CSV/JSON).
 * On Android, opens the system document save picker.
 * On iOS, presents native UIDocumentPickerViewController / UIActivityViewController.
 */
interface FileSaver {
    fun saveFile(suggestedName: String, extension: String, data: String)
}

@Composable
expect fun rememberFileSaver(): FileSaver
