package app.kilo.platform

import androidx.compose.runtime.Composable

/**
 * Local daily reminder notifications. Strictly on-device: no server, no push, no analytics.
 * Notification text is supplied by the caller (localized via string resources).
 */
interface ReminderScheduler {
    /** True if this platform supports local notifications. */
    val isSupported: Boolean

    /** Requests notification permission from the user. Returns true if granted. */
    suspend fun requestPermission(): Boolean

    /** Schedules (or replaces) a repeating daily reminder at [hour]:[minute] local time. Idempotent. */
    fun schedule(hour: Int, minute: Int, title: String, body: String)

    /** Cancels the daily reminder, if any. */
    fun cancel()
}

/** No-op implementation used when reminders are unsupported or in tests. */
object NoOpReminderScheduler : ReminderScheduler {
    override val isSupported: Boolean = false
    override suspend fun requestPermission(): Boolean = false
    override fun schedule(hour: Int, minute: Int, title: String, body: String) = Unit
    override fun cancel() = Unit
}

@Composable
expect fun rememberReminderScheduler(): ReminderScheduler
