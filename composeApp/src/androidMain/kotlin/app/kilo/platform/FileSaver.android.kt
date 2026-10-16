package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.launch

class AndroidFileSaver(
    private val launchSave: (String, String, String) -> Unit,
) : FileSaver {
    override fun saveFile(suggestedName: String, extension: String, data: String) {
        launchSave(suggestedName, extension, data)
    }
}

@Composable
actual fun rememberFileSaver(): FileSaver {
    val scope = rememberCoroutineScope()
    var pendingData by remember { mutableStateOf<String?>(null) }

    val launcher = rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings.createDefault()) { platformFile: PlatformFile? ->
        val dataToSave = pendingData
        if (platformFile != null && dataToSave != null) {
            scope.launch {
                try {
                    platformFile.writeString(dataToSave)
                } catch (_: Exception) {}
            }
        }
        pendingData = null
    }

    return remember(launcher) {
        AndroidFileSaver { suggestedName, extension, data ->
            pendingData = data
            launcher.launch(
                suggestedName = suggestedName,
                defaultExtension = extension,
            )
        }
    }
}
