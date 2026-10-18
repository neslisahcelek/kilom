package app.kilo

import androidx.compose.ui.window.ComposeUIViewController
import app.kilo.platform.IncomingFileBuffer

fun MainViewController() = ComposeUIViewController { App() }

fun handleIncomingFileContent(content: String) {
    IncomingFileBuffer.onFileReceived(content)
}
