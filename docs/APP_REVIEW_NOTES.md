# Kilom – App Review Notes & Apple Yanıtı

Bu doküman, Apple App Store Connect'teki **"App Review Information (Notes)"** bölümüne girilecek metinleri, olası inceleme sorularına verilecek resmi yanıtları ve App Store Review ekibine yönelik test senaryolarını içerir.

---

## 1. App Review Notes (App Store Connect → "App Review Information → Notes")

```text
Kilom is a privacy-first personal health companion and weight tracker. No account, login, or registration is required; there is no demo account.

HEALTHKIT INTEGRATION (Guideline 2.5.1)
- Kilom uses HealthKit exclusively to:
  1. WRITE the user's logged weight entries to Apple Health.
  2. READ (import) the user's past weight entries from Apple Health into Kilom.
- HealthKit is clearly and prominently identified in the user interface:
  • First launch: An onboarding sheet explains why Kilom requests HealthKit access prior to presenting the system authorization dialog.
  • Main screen: A dedicated "Apple Health" liquid-glass badge displays real-time connection status.
  • Data Management / Backup: An "Apple Health Import" button allows users to initiate on-demand synchronization.
- Health data stays strictly on the physical device. It is never transmitted across a network, never shared with third parties, and never used for advertising, marketing, analytics, or data mining.
- Purpose strings: NSHealthShareUsageDescription and NSHealthUpdateUsageDescription in Info.plist.

FEATURE RICHNESS & UTILITY (Guideline 4.2 – Minimum Functionality)
Kilom provides a deep, comprehensive suite of health and wellness tools far beyond a basic entry form:
1. On-Device Scale OCR (Apple Vision): Captures digits from bathroom scale photos (including digital 7-segment LED/LCD screens) in volatile memory; requires explicit user confirmation before saving.
2. Two-Way Apple Health Sync: Writing new entries and importing historical data.
3. Weigh-In Consistency Matrix: Tracks current/longest streak, monthly consistency percentage, and an interactive 14-day visual habit heatmap.
4. Milestone Share Card: Generates an aesthetic, privacy-preserving liquid-glass summary card rendered natively via UIActivityViewController.
5. Dynamic Infradian Cycle Tracking: Deterministic cycle analytics (customizable 21–45 days with Backward Luteal Mapping) correlating hormonal shifts with natural water retention (preventing user discouragement).
6. Evidence-Based Nutrition Guide: Micronutrient recommendations (iron, magnesium, zinc, omega-3) and macronutrient timing tailored to cycle phases (ACOG & PubMed literature).
7. Cycle-Synced Strength Training Guide: 4-tier workout intensity mapping (Rest, Moderate, High, Peak Power) with PR lift windows (Squat/Deadlift), biomechanical injury prevention alerts (ACL ligament laxity during ovulatory estrogen peak), thermoregulation guidance, and PMS deload strategies (Dr. Stacy Sims principles).
8. Resistance Training & DOMS Shield: 100% on-device CSV parser for workout logs (`workouts.csv`). Evaluates muscle repair inflammation and glycogen retention after heavy sessions to reassure users that post-workout weight spikes are not fat gain.
9. Workout & Cycle Correlation Sheet: Visualizes 4-phase volume load, PR distribution heatmaps, luteal RPE shift, and relative strength ratios.
10. Goal Weight & Smart ETA: Dynamic progress percentage and arrival projections.
11. Moving Average Trend Line & Period Summaries: Filters out fluid fluctuations to display true weight trajectory.
12. Optional Daily Reminder: Scheduled locally on-device with zero push servers.
13. Complete Data Ownership: Full export and restore via CSV and JSON.

PRIVACY & SENSITIVE HEALTH DATA (Guideline 5.1.1)
- 100% on-device architecture: Zero servers, zero accounts, zero third-party SDKs, zero analytics, and zero advertising trackers.
- Menstrual cycle dates, symptom correlations, scale photos, and weight metrics are stored strictly in local application storage. Photos are processed in RAM and immediately released.
- Zero Cloud LLM / AI dependencies: All cycle, nutrition, and workout recommendations are calculated locally using deterministic Kotlin algorithms.

MEDICAL DISCLAIMER & SOURCES (Guideline 1.4.1)
- Kilom's nutrition and workout guides are strictly educational and for health awareness; they do not diagnose, treat, or prevent any medical condition.
- A prominent medical disclaimer is displayed in the app.
- A dedicated "Sources" tab inside the Cycle Sheet explicitly cites peer-reviewed medical and exercise physiology literature (ACOG, PubMed, IOC, ACSM).

STEP-BY-STEP TESTING GUIDE FOR REVIEWERS
1. Launch Kilom → review the Apple Health onboarding explanation → tap Continue → grant Weight read/write access in the system dialog.
2. Main Screen: Observe the "Apple Health Connected" badge and the Consistency (Streak) Card with the 14-day habit heatmap. Tap "Share" to test the native UIActivityViewController milestone card.
3. Log a Weight: Tap the '+' button to log manually or use the Camera button to scan a scale display (confirm the on-device recognized value). Open the native Apple Health app (Browse → Body Measurements → Weight) to verify the entry was written.
4. Cycle, Nutrition & Strength Training Sheet:
   • On the main screen, tap the pink liquid-glass Cycle Card (or open Backup/Settings to toggle Cycle Tracking).
   • Tap different days across the 28-day interactive strip (e.g., Day 12 for Follicular Peak, Day 14 for Ovulation, Day 26 for Late Luteal).
   • Navigate through all 4 tabs: "Body & Hormones", "Nutrition", "Strength Training", and "Sources".
   • Check the biomechanical injury warning (ACL laxity) on Day 14, and the deload/leucine advice on Day 26.
5. Goal Weight & Summaries: Tap the Goal Card to set a target weight; review the dynamic progress and weekly/monthly summaries.
6. Local Reminder: Open Settings to toggle the daily weigh-in reminder and verify local notification permissions.

Contact: neslisah.celek@outlook.com
```

---

## 2. Apple'a Resmi Yanıt Metni / Formal Reply to App Review

### English
```text
Hello App Review Team,

Thank you for your review and guidance on Kilom. In this version, we have expanded both the functional depth and the clear presentation of our on-device health features.

1. Guideline 2.5.1 – HealthKit:
Kilom uses HealthKit exclusively to write user-logged weights and import past weight records. HealthKit is explicitly identified throughout the app:
- An educational onboarding sheet is presented before the system permission prompt.
- An "Apple Health" status badge is permanently visible on the dashboard.
- An "Apple Health Import" option is accessible within the settings/backup sheet.
Health data never leaves the device and is never used for advertising, marketing, or analytics. Both NSHealthShareUsageDescription and NSHealthUpdateUsageDescription in Info.plist detail this usage.

2. Guideline 4.2 – Minimum Functionality:
Kilom offers a rich, full-featured personal health suite with deep native functionality:
- On-device optical character recognition (OCR) via Apple Vision for bathroom scales (including 7-segment digital screens).
- Consistency Matrix & Habit Heatmap: Tracking weigh-in streaks, monthly consistency scores, and milestone sharing via native UIActivityViewController.
- Dynamic Infradian Cycle Tracking (21–45 Days) & Water Retention Forecaster: Correlating hormonal shifts with weight fluctuations to prevent user discouragement.
- Evidence-Based Nutrition Guide: Phase-specific macronutrient balance and micronutrient recommendations (iron, zinc, magnesium) based on ACOG and PubMed literature.
- Cycle-Synced Strength Training Guide: 4-tier workout intensity mapping (Rest to Peak Power), compound lift windows (Squat, Deadlift, Hip Thrust), biomechanical injury prevention alerts (ACL laxity risk during ovulatory estrogen peak), and PMS deload strategies based on Dr. Stacy Sims’ exercise endocrinology research.
- Resistance Training & Workout CSV Integration with DOMS Shield: 100% on-device CSV parser for workout logs (`workouts.csv`), muscle category detection, volume & 1RM analytics, and DOMS muscle repair fluid retention alerts that prevent confusing temporary post-workout scale spikes with fat gain.
- Workout & Cycle Correlation Sheet: Visualizing 4-phase volume load, PR heatmaps, and relative strength progress.
- Goal weight tracking with dynamic arrival projections, moving average trend filtering, period summaries, local reminders, and CSV/JSON backup.

3. Privacy & Sensitive Data (Guideline 5.1.1):
All cycle and weight analytics run 100% on-device with zero cloud servers, zero analytics SDKs, and zero AI API calls. All advice includes an explicit medical disclaimer and direct citations to peer-reviewed literature in the in-app Sources tab.

Detailed testing steps are provided in the App Review Information section.

Thank you for your time and evaluation.

Sincerely,
Neslişah Çelek
```

### Türkçe
```text
Merhaba App Review Ekibi,

Kilom hakkındaki incelemeniz ve geri bildirimleriniz için teşekkür ederiz. Bu sürümde hem uygulamanın işlevsel derinliğini hem de cihaz içi sağlık özelliklerinin sunumunu kapsamlı şekilde genişlettik.

1. Guideline 2.5.1 – HealthKit:
Kilom, HealthKit'i yalnızca kullanıcının girdiği kiloları Apple Sağlık'a yazmak ve geçmiş kayıtları içe aktarmak için kullanır. HealthKit uygulama arayüzünde açıkça tanımlanmıştır:
- Sistem izin diyaloğundan önce kullanıcıyı bilgilendiren bir onboarding sayfası gösterilir.
- Ana ekranda kalıcı bir "Apple Sağlık Bağlı" rozeti yer alır.
- Yedekleme/ayarlar sayfasından tek dokunuşla "Apple Sağlık'tan İçe Aktar" özelliği sunulur.
Sağlık verileri asla cihaz dışına çıkmaz; reklam veya analitik için kullanılmaz. Info.plist içerisindeki NSHealthShareUsageDescription ve NSHealthUpdateUsageDescription metinleri bu kullanımı detaylandırır.

2. Guideline 4.2 – Minimum İşlevsellik:
Kilom, temel bir kilo formunun çok ötesinde zengin ve yerel bir sağlık deneyimi sunar:
- Tartı ekranları (7 segmentli LED/LCD dijital göstergeler dahil) için Apple Vision ile cihaz içi OCR.
- İstikrar Matrisi ve Alışkanlık Isı Haritası: Tartılma serileri (streak), aylık tutarlılık yüzdesi ve yerel UIActivityViewController ile estetik başarı paylaşım kartı.
- Dinamik Menstrüel Döngü Takibi (21–45 Gün) ve Su Tutma (Ödem) Öngörüsü: Hormonal değişimleri kilo dalgalanmalarıyla ilişkilendirerek motivasyon kaybını önler.
- Kanıta Dayalı Beslenme Rehberi: ACOG ve PubMed literatürüne dayalı evreye özel makro ve mikrobesin (demir, magnezyum, çinko) önerileri.
- Döngüye Senkronize Ağırlık Antrenmanı Rehberi: 4 seviyeli yoğunluk eşlemesi (Dinlenme - Tepe Güç), rekor/maksimal kuvvet pencereleri (Squat, Deadlift, Hip Thrust), biyomekanik sakatlık önleme uyarıları (yumurtlama döneminde östrojen piki ve ACL bağ gevşekliği riski) ve Dr. Stacy Sims ekolüne dayalı PMS deload stratejileri.
- Direnç Antrenmanı, Antrenman CSV İçe Aktarma ve DOMS Ödem Kalkanı: Standart antrenman kayıtları (`workouts.csv`) için %100 cihaz içi CSV ayrıştırıcı, kas grubu analizi, tonaj & 1RM hesaplamaları ve ağır antrenman sonrası tartı artışlarının yağ değil kas onarım ödemi olduğunu belirten kalkan banner'ı.
- Döngü-Antrenman Korelasyon Sayfası: 4 döngü evresindeki tonaj yükü, PR dağılımı ve göreceli kuvvet analizleri.
- Hedef kilo, dinamik bitiş tarihi tahmini, hareketli ortalama trend eğrisi, dönem özetleri, yerel hatırlatıcı ve CSV/JSON yedekleme.

3. Gizlilik ve Hassas Sağlık Verileri (Guideline 5.1.1):
Tüm döngü ve tartı analizleri sıfır bulut sunucusu, sıfır analitik SDK ve sıfır harici yapay zeka API'si ile %100 cihaz üzerinde çalışır. Beslenme ve antrenman önerileri belirgin bir tıbbi feragatname içerir ve uygulama içindeki Kaynaklar sekmesinde hakemli tıp literatürüne şeffafça atıfta bulunur.

Detaylı test adımları App Review Information bölümünde sunulmuştur.

Zamanınız ve değerlendirmeniz için teşekkür ederiz.

Saygılarımla,
Neslişah Çelek
```

---

## 3. Fiziksel Cihaz Ekran Kaydı Senaryosu (Video Walkthrough Script)

Fiziksel iPhone üzerinde, temiz kurulumla tek seferde kaydedilecek 60–90 saniyelik ekran kaydı akışı:

1. **Onboarding & Apple Health İzni (00:00–00:15):**
   - Uygulamayı açın, Apple Sağlık onboarding kartını gösterin.
   - Sistem izin diyaloğunda kilo okuma ve yazmaya izin verin.
   - Ana ekranda "Apple Sağlık Bağlı" rozetini gösterin.

2. **Kilo Kaydı & Apple Sağlık Doğrulaması (00:15–00:30):**
   - '+' butonu ile veya tartı fotoğrafı çekerek bir kilo kaydedin.
   - Apple Sağlık uygulamasını açıp yeni kaydın eklendiğini teyit edin.

3. **İstikrar Matrisi & Başarı Paylaşımı (00:30–00:45):**
   - Ana ekrandaki İstikrar Kartına dokunarak mevcut seriyi ve 14 günlük ısı haritasını gösterin.
   - "Paylaş" butonuna dokunarak sıvı cam paylaşım sayfasını ve yerel iOS paylaşım menüsünü (UIActivityViewController) açın.

4. **Döngü, Beslenme ve Ağırlık Antrenmanı Rehberi (00:45–01:10):**
   - Ana ekrandaki pembe cam Döngü Kartına dokunarak `CycleSheet`'i açın.
   - 28 günlük etkileşimli gün şeridinde Gün 12'ye (Tepe Güç), Gün 14'e (Yumurtlama & ACL uyarısı) ve Gün 26'ya (PMS Deload) dokunun.
   - 4 sekme arasında geçiş yapın: **Beden & Hormon**, **Beslenme**, **Ağırlık**, **Kaynaklar**.
   - Biyomekanik uyarı kutusunu ve bilimsel kaynak referanslarını gösterin.

5. **Hedef Kilo ve Yedekleme (01:10–01:25):**
   - Hedef kartını ve trend grafiğini gösterin.
   - Ayarlar/Yedekleme sayfasından Apple Sağlık içe aktarma ve JSON/CSV yedeklemeyi gösterin.

---

## 4. Gönderim Öncesi Son Kontrol Listesi

- [x] Info.plist: `NSHealthShareUsageDescription` ve `NSHealthUpdateUsageDescription` çift yönlü kullanımı açıklıyor.
- [x] App Store Connect Nutrition Label: **"Data Not Collected"** (Hiçbir veri sunucuya gönderilmez).
- [x] `docs/STORE_LISTING.md` metinleri App Store Connect ve Google Play Console'a kopyalanmaya hazır.
- [x] Tıbbi feragatname ve Kaynaklar sekmesi uygulama içinde ve mağaza açıklamasında açıkça belirtildi.
- [x] Gizlilik Politikası URL'si canlı yayında: [https://neslisahcelek.github.io/kilom/](https://neslisahcelek.github.io/kilom/)
