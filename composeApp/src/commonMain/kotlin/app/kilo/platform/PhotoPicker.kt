package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

/** Thin wrapper over FileKit gallery/camera pickers. Cancellation is silently ignored. */
class PhotoPicker internal constructor(
    private val gallery: () -> Unit,
    private val camera: () -> Unit,
) {
    fun pickFromGallery() = gallery()
    fun takePhoto() = camera()
}

@Composable
fun rememberPhotoPicker(onImage: (ByteArray) -> Unit): PhotoPicker {
    val scope = rememberCoroutineScope()
    val currentOnImage by rememberUpdatedState(onImage)

    fun handle(file: PlatformFile?) {
        if (file == null) return
        scope.launch {
            val bytes = try {
                file.readBytes()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (bytes != null && bytes.isNotEmpty()) currentOnImage(bytes)
        }
    }

    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { handle(it) }
    val cameraLauncher = rememberCameraPickerLauncher { handle(it) }

    return remember(galleryLauncher, cameraLauncher) {
        PhotoPicker(
            gallery = { galleryLauncher.launch() },
            camera = { cameraLauncher.launch() },
        )
    }
}
