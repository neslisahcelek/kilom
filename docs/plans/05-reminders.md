# 05 – Günlük Hatırlatıcı (yerel bildirim) — Faz 1 (platform) + Faz 2 (UI)

Yalnızca yerel bildirim; sunucu/push/analitik YOK. Varsayılan kapalı, tamamen opt-in.

## Faz 1 – Platform (yeni dosyalar; ortak dosyalara dokunma)
- `commonMain/platform/ReminderScheduler.kt`
  ```kotlin
  interface ReminderScheduler {
      val isSupported: Boolean
      suspend fun requestPermission(): Boolean
      fun schedule(hour: Int, minute: Int, title: String, body: String)  // günlük tekrar
      fun cancel()
  }
  object NoOpReminderScheduler : ReminderScheduler { ... }
  @Composable expect fun rememberReminderScheduler(): ReminderScheduler
  ```
- `iosMain/.../ReminderScheduler.ios.kt`: `UNUserNotificationCenter` — `requestAuthorizationWithOptions(Alert|Sound)`, `UNCalendarNotificationTrigger(dateMatchingComponents(hour, minute), repeats=true)`, sabit identifier `kilom.daily.reminder`; `cancel()` → `removePendingNotificationRequestsWithIdentifiers`. Info.plist anahtarı gerekmez. Gerçek uygulama, stub değil.
- `androidMain/.../ReminderScheduler.android.kt`: `AlarmManager.setInexactRepeating(RTC_WAKEUP, ...)` + `BroadcastReceiver` (`ReminderReceiver`) + `NotificationChannel`; Android 13+ için `POST_NOTIFICATIONS` runtime izni (`rememberLauncherForActivityResult` yerine Compose `actual` içinde izin isteği). `AndroidManifest.xml`: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` + receiver kaydı. Exact alarm izni İSTEME.
- Bildirim metni common tarafından (`stringResource`) parametre olarak gelir; platform kodunda sabit metin yok.

## Faz 2 – UI
- `PrefsRepository`: `reminderEnabled` (default false), `reminderMinutes` (default 480 = 08:00).
- ViewModel: `setReminderEnabled(Boolean)` (izin → schedule/cancel), `setReminderTime(h,m)`; ayarlar `BackupSheet`/yeni "Hatırlatıcı" bölümünde toggle + saat seçici (mevcut `IosDatePicker` stilini yeniden kullan veya basit saat satırı).
- Strings EN+TR: `reminder_title`, `reminder_subtitle`, `reminder_time`, `reminder_permission_denied`, `reminder_notification_title`, `reminder_notification_body`.
- Uygulama açılışında kayıtlı ayara göre yeniden schedule (idempotent).

## Test
`commonTest`: ViewModel fake `ReminderScheduler` ile enable → schedule çağrıldı, disable → cancel, izin reddi → pref false.
