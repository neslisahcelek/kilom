# 00 – Genel Bakış ve Uygulama Sırası

Amaç: App Store reddini (4.2 Minimum Functionality + 2.5.1 HealthKit görünürlüğü) gidermek. Hedef: yalnızca iOS yeniden gönderim, Android derlemesi bozulmayacak.

## Planlar
| Plan | Konu | Tür |
|---|---|---|
| [01](01-health-visibility.md) | HealthKit'i görünür kıl + onboarding | UI (2.5.1) |
| [02](02-health-import.md) | Apple Health'ten okuma / içe aktarma | Platform + UI (4.2) |
| [03](03-goal-weight.md) | Hedef kilo + ilerleme | Domain + UI (4.2) |
| [04](04-summary-card.md) | Haftalık/aylık özet kartı | Domain + UI (4.2) |
| [05](05-reminders.md) | Günlük hatırlatıcı | Platform + UI (4.2) |
| [06](06-store-privacy-docs.md) | Mağaza metni, gizlilik, Info.plist, review notları | Doküman |

Widget bu sürümde YOK (WidgetKit Windows'ta doğrulanamaz).

## Fazlar (çakışmayı önlemek için)
Çakışma riski olan ortak dosyalar: `DashboardViewModel.kt`, `DashboardScreen.kt`, `Repositories.kt`, `strings.xml` (EN/TR), `HeroCard.kt`, `BackupSheet.kt`.

- **Faz 1 – paralel, ayrık dosyalar** (ortak dosyalara DOKUNMAZ):
  - Ajan A: Plan 03 + 04'ün **sadece domain** kısmı (yeni dosyalar + commonTest).
  - Ajan B: Plan 02 + 05'in **sadece platform** kısmı (`HealthSync*`, yeni `ReminderScheduler*`, Android manifest).
  - Ajan C: Plan 06 (dokümanlar + `Info.plist`).
- **Faz 2 – tek ajan, sıralı**: Plan 01 + 02/03/04/05'in UI/ViewModel/Prefs/strings entegrasyonu + testler.
- **Faz 3**: `reviewer` ajanı salt-okunur inceleme; build/test doğrulaması.

## Ortak kurallar (AGENTS.md)
- DI kütüphanesi yok; `AppContainer`/manuel bağlama.
- `commonMain` platform bağımsız; her `expect`/`actual` için Android + iOS gerçek uygulama (iOS stub yok).
- kg her zaman kg saklanır; delta saklanmaz.
- Yeni JSON/prefs alanları varsayılan değerli (geriye uyum).
- Her yeni string EN (`values/strings.xml`) + TR (`values-tr/strings.xml`); renkler `ui/theme/Colors.kt`; yüzde için `%%`.
- Gizlilik: ağ yok, analitik yok; loglara kilo/fotoğraf verisi yazılmaz.
- Doğrulama: `.\gradlew.bat :composeApp:allTests`, `:composeApp:assembleDebug`, `:composeApp:testDebugUnitTest`. iOS derlemesi Windows'ta yapılamaz → statik inceleme, raporda belirt.
