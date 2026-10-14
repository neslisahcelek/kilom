package app.kilo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

private const val REMINDER_ID = "kilom.daily.reminder"

/** iOS implementation of [ReminderScheduler] using local notifications (UNUserNotificationCenter). */
class IosReminderScheduler : ReminderScheduler {
    private val center: UNUserNotificationCenter get() = UNUserNotificationCenter.currentNotificationCenter()

    override val isSupported: Boolean = true

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
        ) { granted, error ->
            if (cont.isActive) {
                cont.resume(granted && error == null)
            }
        }
    }

    override fun schedule(hour: Int, minute: Int, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }
        val components = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = true)
        val request = UNNotificationRequest.requestWithIdentifier(REMINDER_ID, content, trigger)
        // Same identifier replaces any pending request, so scheduling is idempotent.
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancel() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(REMINDER_ID))
    }
}

@Composable
actual fun rememberReminderScheduler(): ReminderScheduler = remember { IosReminderScheduler() }
