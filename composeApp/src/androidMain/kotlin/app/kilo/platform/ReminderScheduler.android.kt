package app.kilo.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import java.util.Calendar

private const val CHANNEL_ID = "kilom_daily_reminder"
private const val PREFS = "kilom_reminder"
private const val KEY_TITLE = "title"
private const val KEY_BODY = "body"
private const val REQUEST_CODE = 4242
private const val NOTIFICATION_ID = 4243

/** Receives the daily alarm and posts a local notification. Text is stored on-device only. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val title = prefs.getString(KEY_TITLE, null) ?: return
        val body = prefs.getString(KEY_BODY, null) ?: return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Kilom", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launch?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }
}

/** Android implementation of [ReminderScheduler] using an inexact repeating [AlarmManager] alarm. */
class AndroidReminderScheduler(
    context: Context,
    private val permissionRequester: suspend () -> Boolean,
) : ReminderScheduler {
    private val appContext = context.applicationContext

    override val isSupported: Boolean = true

    override suspend fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        val granted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted || permissionRequester()
    }

    private fun pendingIntent(flags: Int): PendingIntent? {
        val intent = Intent(appContext, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(appContext, REQUEST_CODE, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun schedule(hour: Int, minute: Int, title: String, body: String) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TITLE, title)
            .putString(KEY_BODY, body)
            .apply()

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    override fun cancel() {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)?.let { alarmManager.cancel(it) }
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

@Composable
actual fun rememberReminderScheduler(): ReminderScheduler {
    val context = LocalContext.current
    val pending = remember { arrayOfNulls<CompletableDeferred<Boolean>>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pending[0]?.complete(granted)
        pending[0] = null
    }
    val currentLauncher by rememberUpdatedState(launcher)
    return remember(context) {
        AndroidReminderScheduler(context) {
            val deferred = CompletableDeferred<Boolean>()
            pending[0] = deferred
            if (Build.VERSION.SDK_INT >= 33) {
                currentLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                deferred.complete(true)
            }
            deferred.await()
        }
    }
}
