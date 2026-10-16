# Kilom – App Review Notes & Apple Yanıtı

Bu doküman, Apple App Store Connect'teki **"App Review Information (Notes)"** bölümüne girilecek metinleri, olası inceleme sorularına verilecek resmi yanıtları, fiziksel cihaz test adımlarını ve mağaza gönderim kontrol listesini içerir.

---

## 1. App Review Notes (App Store Connect → "App Review Information → Notes")

```text
Kilom is a privacy-first personal health companion, body weight tracker, and biomechanical workout analytics application. No account, login, or registration is required; there is no demo account needed.

HEALTHKIT INTEGRATION (Guideline 2.5.1)
- Kilom uses HealthKit exclusively to:
  1. WRITE the user's logged weight entries to Apple Health.
  2. READ (import) the user's past weight entries from Apple Health into Kilom.
- HealthKit is clearly and prominently identified in the user interface:
  • First launch: An onboarding sheet explains why Kilom requests HealthKit access prior to presenting the system authorization dialog.
  • Main dashboard: A dedicated "Apple Health" liquid-glass status badge in the top navigation header displays real-time connection status.
  • Data Management / Backup: An "Import from Apple Health" button allows users to initiate on-demand synchronization anytime.
- Health data stays strictly on the physical device. It is never transmitted across a network, never shared with third parties, and never used for advertising, marketing, analytics, or data brokering.
- Purpose strings: NSHealthShareUsageDescription and NSHealthUpdateUsageDescription are detailed in Info.plist.

FEATURE RICHNESS & UTILITY (Guideline 4.2 – Minimum Functionality)
Kilom provides a deep, comprehensive suite of native health and fitness tools far beyond a basic entry form:
1. On-Device Scale OCR (Apple Vision): Captures digits from bathroom scale displays (including digital 7-segment LED/LCD screens) in volatile memory; requires explicit user confirmation before saving.
2. Two-Way Apple Health Sync: Seamless writing of new entries and historical data import.
3. Weigh-In Consistency Matrix: Tracks current/longest streak, monthly consistency percentage, and an interactive 14-day visual habit heatmap.
4. Milestone Share Card: Generates an aesthetic, privacy-preserving liquid-glass summary card rendered natively via UIActivityViewController (with optional weight concealment for privacy).
5. Universal Strength & Biomechanical Analytics:
   - Evaluates compound lifts (Squat, Deadlift, Bench Press, Overhead Press) using the scientifically validated Epley 1RM formula.
   - 4-Tier Normative Relative Strength Scales (Beginner, Intermediate, Advanced, Elite) stratified by biological sex based on NSCA standards and peer-reviewed athletic norms.
   - Calculates real-time bodyweight multipliers (xBW) and target load bounds for the user's current weight.
   - Muscle group volume distribution (Legs, Back, Chest, Shoulders, Core).
6. DOMS & Glycogen Water Retention Shield:
   - Analyzes heavy resistance training sessions (>8,000 kg volume or high intensity in past 48 hours).
   - Reassures users when scale weight temporarily spikes (+0.5 to +1.8 kg) due to exercise-induced muscle damage (EIMD) inflammation and glycogen-bound water (1g glycogen : 3-4g water), preventing psychological discouragement.
7. Decoupled Infradian Menstrual Cycle & Nutrition Module:
   - Independent full-screen module with customizable 21–45 day cycles using Backward Luteal Mapping (Ovulation = Cycle Length - 14).
   - 14 micro-period hormonal profiles, phase-specific macronutrient balance, and micronutrient recommendations (iron, magnesium, zinc) based on ACOG and PubMed literature.
   - Cycle-synced strength guidelines, ACL ligament laxity precautions during ovulatory estrogen surges, and PMS deload strategies.
8. 100% On-Device Parametric Natural Language Generation (NLG):
   - Deterministic, zero-hallucination data storytelling engine built in pure Kotlin.
   - Analyzes actual user PR and volume distribution across cycle phases versus expected literature norms (McNulty et al., 2020 meta-analysis).
   - Delivers supportive, non-judgmental, personalized coaching insights with zero cloud LLM/AI dependencies.
9. Goal Weight & Smart ETA: Dynamic progress percentage and arrival projections based on moving averages.
10. Moving Average Trend Line & Period Summaries: Filters out fluid fluctuations to reveal true body mass trajectory.
11. Local Daily Reminder: Scheduled strictly on-device via UNUserNotificationCenter with zero push servers.
12. Complete Data Ownership: Full export and restore via CSV and JSON.

PRIVACY & SENSITIVE HEALTH DATA (Guideline 5.1.1)
- 100% on-device architecture: Zero servers, zero user accounts, zero third-party SDKs, zero analytics, and zero advertising trackers.
- Scale photos, workout logs, menstrual cycle dates, and weight metrics are stored strictly in local application storage. Photos are processed in RAM and immediately released.
- Zero Cloud LLM / AI dependencies: All cycle, nutrition, biomechanical strength, and NLG coaching insights run locally using deterministic Kotlin algorithms.

MEDICAL DISCLAIMER & SOURCES (Guideline 1.4.1)
- Kilom's nutrition, cycle, and workout analytics are strictly educational and for health awareness; they do not diagnose, treat, or prevent any medical condition.
- A prominent medical disclaimer is displayed in both the Cycle and Workout Analytics screens.
- Dedicated "Sources" tabs cite peer-reviewed academic literature (ACOG, PubMed, NSCA, ACSM) with complete citations and DOIs.

STEP-BY-STEP TESTING GUIDE FOR REVIEWERS
1. Launch Kilom → review the Apple Health onboarding explanation → tap Continue → grant Weight read/write access in the system dialog.
2. Main Dashboard:
   • Observe the "Apple Health Connected" badge and the Consistency (Streak) Card with the 14-day habit heatmap.
   • Tap "Share" on the streak card to test the native UIActivityViewController milestone card.
3. Log a Weight:
   • Tap '+' to log manually or use the Camera button to scan a scale display (review and confirm the on-device recognized value).
   • Open the native Apple Health app (Browse → Body Measurements → Weight) to verify the entry was written.
4. Universal Strength & Workout Analytics Screen:
   • On the main dashboard, tap the blue "Universal Strength & Biomechanics" card (or tap "View Analytics & Scales").
   • Toggle between "Female Standard" and "Male Standard" at the top of Tab 1.
   • Inspect the 4-tier visual normative scale bar (Beginner to Elite) showing both bodyweight ratios (xBW) and target kilograms calculated for current body weight.
   • Check the inline Epley 1RM formula explanation.
   • Switch to Tab 2 ("Volume & DOMS Shield") to review muscle group tonnage, PR distribution, and the DOMS fluid retention threshold.
   • Switch to Tab 3 ("Methodology & Sources") to inspect peer-reviewed exercise physiology citations (NSCA, LeSuer, Peake, Kreitzman).
5. Independent Cycle & Nutrition Module:
   • On the main dashboard, tap the pink "Cycle & Nutrition Guide" card.
   • Tap different days across the 28-day interactive strip (e.g., Day 12 for Follicular Peak, Day 14 for Ovulation, Day 26 for Late Luteal).
   • Navigate through all 4 tabs: "Body & Hormones", "Nutrition", "Strength Training", and "Sources".
   • Check the parametric data storytelling analysis comparing personal PR data with literature expectations, the biomechanical ACL warning on Day 14, and the PMS deload advice on Day 26.
6. Goal Weight & Summaries:
   • Tap the Goal Card to set a target weight; review dynamic progress and weekly/monthly summaries.
7. Local Reminder & Backup:
   • Tap Settings/Backup to toggle the daily weigh-in reminder (verify local notification permission).
   • Test CSV/JSON export and import capabilities.

Contact: neslisah.celek@outlook.com
```

---

## 2. Apple'a Resmi Yanıt Metni / Formal Reply to App Review

### English
```text
Hello App Review Team,

Thank you for your review and ongoing guidance on Kilom. In this version, we have significantly expanded the functional depth, scientific rigor, and privacy-first presentation of our on-device health features.

1. Guideline 2.5.1 – HealthKit:
Kilom uses HealthKit exclusively to write user-logged weights and import past weight records. HealthKit is explicitly identified throughout the app:
- An educational onboarding sheet is presented before the system permission prompt.
- A prominent "Apple Health" liquid-glass status badge is permanently visible in the top navigation header of the dashboard.
- An "Import from Apple Health" option is accessible within the settings/backup sheet.
Health data never leaves the device and is never used for advertising, marketing, or analytics. Both NSHealthShareUsageDescription and NSHealthUpdateUsageDescription in Info.plist detail this usage.

2. Guideline 4.2 – Minimum Functionality:
Kilom offers a rich, full-featured personal health and fitness suite with deep native functionality:
- On-Device Optical Character Recognition (OCR) via Apple Vision Framework for bathroom scale displays (including 7-segment digital LED/LCD screens).
- Consistency Matrix & Habit Heatmap: Tracking weigh-in streaks, monthly consistency scores, and milestone sharing via native UIActivityViewController.
- Universal Strength & Biomechanical Analytics: Sex-stratified 4-tier relative strength scales (Beginner to Elite) based on NSCA norms, transparent Epley 1RM prediction equations, muscle group volume distribution, and DOMS fluid retention shield that reassures users when scale weight temporarily spikes after heavy lifting.
- Decoupled Infradian Menstrual Cycle & Nutrition Module: 21–45 day backward luteal mapping, 14 micro-periods, phase-specific macronutrient and micronutrient balance (ACOG and PubMed literature), ACL ligament laxity warnings, and thermoregulation guidance.
- 100% On-Device Parametric Natural Language Generation (NLG): Deterministic data storytelling in pure Kotlin that compares actual user PR data with literature expectations (McNulty et al., 2020), providing supportive, non-judgmental coaching with zero cloud AI/LLM dependencies.
- Goal weight tracking with dynamic arrival projections, moving average trend filtering, period summaries, local reminders, and CSV/JSON backup.

3. Privacy & Sensitive Data (Guideline 5.1.1):
All cycle, weight, and workout analytics run 100% on-device with zero cloud servers, zero analytics SDKs, and zero AI API calls. Scale photos are processed strictly in RAM and immediately released. All advice includes an explicit medical disclaimer and direct citations to peer-reviewed literature in the in-app Sources tabs.

Detailed step-by-step testing instructions are provided in the App Review Information section.

Thank you for your time and evaluation.

Sincerely,
Neslişah Çelek
```

### Türkçe
```text
Merhaba App Review Ekibi,

Kilom hakkındaki incelemeniz ve geri bildirimleriniz için teşekkür ederiz. Bu sürümde hem uygulamanın işlevsel derinliğini hem de cihaz içi sağlık özelliklerinin bilimsel sunumunu kapsamlı şekilde genişlettik.

1. Guideline 2.5.1 – HealthKit:
Kilom, HealthKit'i yalnızca kullanıcının kaydettiği kiloları Apple Sağlık'a yazmak ve geçmiş kayıtları içe aktarmak için kullanır. HealthKit uygulama arayüzünde açıkça tanımlanmıştır:
- Sistem izin diyaloğundan önce kullanıcıyı bilgilendiren bir onboarding sayfası gösterilir.
- Ana ekranda kalıcı bir "Apple Sağlık Bağlı" rozeti yer alır.
- Yedekleme/ayarlar sayfasından tek dokunuşla "Apple Sağlık'tan İçe Aktar" özelliği sunulur.
Sağlık verileri asla cihaz dışına çıkmaz; reklam veya analitik için kullanılmaz. Info.plist içerisindeki NSHealthShareUsageDescription ve NSHealthUpdateUsageDescription metinleri bu kullanımı detaylandırır.

2. Guideline 4.2 – Minimum İşlevsellik:
Kilom, temel bir kilo formunun çok ötesinde zengin ve yerel bir sağlık deneyimi sunar:
- Tartı ekranları (7 segmentli LED/LCD dijital göstergeler dahil) için Apple Vision ile cihaz içi OCR.
- İstikrar Matrisi ve Alışkanlık Isı Haritası: Tartılma serileri (streak), aylık tutarlılık yüzdesi ve yerel UIActivityViewController ile estetik başarı paylaşım kartı.
- Evrensel Kuvvet ve Biyomekanik Analitiği: Kadın ve erkek standartları için NSCA normlarına dayalı 4 kademeli göreceli kuvvet ölçekleri (Başlangıç - Elit), şeffaf Epley 1RM formülü, kas grubu tonajı ve ağır antrenman sonrası tartı artışlarının yağ değil kas onarım ödemi olduğunu açıklayan DOMS kalkanı.
- Bağımsız Menstrüel Döngü ve Beslenme Modülü: 21–45 gün dinamik geriye doğru luteal eşleme, 14 mikro-dönem, evreye özel makro ve mikrobesin rehberliği, ACL bağ gevşekliği uyarısı ve PMS deload stratejileri.
- %100 Cihaz İçi Parametrik Doğal Dil Üretim (NLG): Kullanıcının gerçek PR ve hacim dağılımını literatür beklentileriyle (McNulty 2020) kıyaslayan, saf Kotlin ile yazılmış, sıfır halüsinasyonlu ve yumuşak koçluk dili sunan veri hikayesi motoru (bulut yapay zeka bağımlılığı yoktur).
- Hedef kilo, dinamik bitiş tarihi tahmini, hareketli ortalama trend eğrisi, dönem özetleri, yerel hatırlatıcı ve CSV/JSON yedekleme.

3. Gizlilik ve Hassas Sağlık Verileri (Guideline 5.1.1):
Tüm döngü, tartı ve antrenman analizleri sıfır bulut sunucusu, sıfır analitik SDK ve sıfır harici yapay zeka API'si ile %100 cihaz üzerinde çalışır. Tartı fotoğrafları yalnızca RAM'de işlenir ve hemen serbest bırakılır. Tüm tavsiyeler belirgin bir tıbbi feragatname içerir ve uygulama içindeki Kaynaklar sekmelerinde hakemli tıp literatürüne şeffafça atıfta bulunur.

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
   - Ana ekran üst başlık barında "Apple Sağlık" rozetini gösterin.

2. **Kilo Kaydı & Apple Sağlık Doğrulaması (00:15–00:30):**
   - '+' butonu ile veya tartı fotoğrafı çekerek bir kilo kaydedin.
   - Apple Sağlık uygulamasını açıp yeni kaydın eklendiğini teyit edin.

3. **İstikrar Matrisi & Başarı Paylaşımı (00:30–00:40):**
   - Ana ekrandaki İstikrar Kartına dokunarak mevcut seriyi ve 14 günlük ısı haritasını gösterin.
   - "Paylaş" butonuna dokunarak sıvı cam paylaşım sayfasını ve yerel iOS paylaşım menüsünü (UIActivityViewController) açın.

4. **Evrensel Kuvvet & Biyomekanik Analiz Ekranı (00:40–00:55):**
   - Ana ekrandaki mavi Antrenman Kartına dokunarak tam ekran `WorkoutCorrelationSheet`'i açın.
   - Üst kısımdaki "Kadın Standardı" / "Erkek Standardı" hap butonlarına dokunarak 4 kademeli göreceli kuvvet ölçeklerinin (`x BW` ve hedef kg değerleri) anlık değişimini gösterin.
   - Epley 1RM şeffaf formül kutusunu gösterin.
   - "Hacim & DOMS Kalkanı" sekmesine geçerek kas grubu dağılımını ve 8.000 kg ödem uyarısını gösterin.
   - "Metodoloji & Kaynakça" sekmesini açarak hakemli bilimsel makale atıflarını gösterin.

5. **Bağımsız Döngü, Beslenme ve Parametrik NLG Rehberi (00:55–01:15):**
   - Ana ekrana dönüp pembe cam Döngü Kartına dokunarak `CycleSheet`'i açın.
   - 28 günlük etkileşimli gün şeridinde Gün 12'ye (Tepe Güç), Gün 14'e (Yumurtlama & ACL uyarısı) ve Gün 26'ya (PMS Deload) dokunun.
   - Beden, Beslenme, Ağırlık ve Kaynaklar sekmeleri arasında gezinin.
   - Parametrik Doğal Dil Üretim (NLG) kutusundaki gerçek veriye dayalı yumuşak koçluk metnini ve tıbbi feragatnameyi gösterin.

6. **Hedef Kilo ve Yedekleme (01:15–01:25):**
   - Hedef kartını ve trend grafiğini gösterin.
   - Ayarlar/Yedekleme sayfasından Apple Sağlık içe aktarma ve JSON/CSV yedeklemeyi gösterin.

---

## 4. Gönderim Öncesi Son Kontrol Listesi

- [x] Info.plist: `NSHealthShareUsageDescription` ve `NSHealthUpdateUsageDescription` çift yönlü kullanımı açıklıyor.
- [x] App Store Connect Nutrition Label: **"Data Not Collected"** (Hiçbir veri sunucuya gönderilmez).
- [x] `docs/STORE_LISTING.md` metinleri App Store Connect ve Google Play Console'a kopyalanmaya hazır.
- [x] Evrensel Kuvvet (Erkek/Kadın normları) ve bağımsız Döngü modülleri tam ekran olarak uygulandı.
- [x] %100 Cihaz İçi Parametrik NLG metinleri gerçek veriye göre dallanıyor ve yumuşak üslup korundu.
- [x] Tıbbi feragatname ve Kaynaklar sekmeleri uygulama içinde ve mağaza açıklamasında açıkça belirtildi.
- [x] Gizlilik Politikası URL'si canlı yayında: [https://neslisahcelek.github.io/kilom/](https://neslisahcelek.github.io/kilom/)
