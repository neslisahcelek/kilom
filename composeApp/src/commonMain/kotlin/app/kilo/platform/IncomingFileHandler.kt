package app.kilo.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe, in-memory buffer for receiving CSV or JSON file content shared
 * directly into Kilom from external apps (e.g., Hevy -> Export CSV -> Share to Kilom).
 */
object IncomingFileBuffer {
    private val _pendingContent = MutableStateFlow<String?>(null)
    val pendingContent: StateFlow<String?> = _pendingContent.asStateFlow()

    private var activeConsumerId: Long = 0L

    fun registerConsumer(): Long {
        activeConsumerId += 1L
        return activeConsumerId
    }

    fun isCurrentConsumer(id: Long): Boolean = id == activeConsumerId

    fun onFileReceived(content: String) {
        if (content.isNotBlank()) {
            _pendingContent.value = content
        }
    }

    fun consume(consumerId: Long = activeConsumerId) {
        if (consumerId == activeConsumerId) {
            _pendingContent.value = null
        }
    }
}
