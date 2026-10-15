# Kilom Proje Talimatları (AI ve Antigravity Ajanları İçin)

Bu doküman, **Kilom** projesinde geliştirme yaparken ve kod incelerken uyulması gereken mimari standartları, alan kurallarını, KMP sınırlarını ve Antigravity ajan prensiplerini tanımlar.

---

## 0. Proje Özeti ve Temel Felsefe

- **Uygulama Adı:** Kilom
- **Tanım:** iOS ve Android için minimal, dikkat dağıtmayan, sıvı cam (liquid-glass) arayüzlü tartı ve kilo takip uygulaması.
- **%100 Cihaz İçi Gizlilik (On-Device Privacy):**
  - Sıfır kullanıcı hesabı, sıfır sunucu, sıfır bulut veritabanı, sıfır üçüncü taraf izleyici/analitik ve sıfır reklam.
  - Tartı fotoğrafları ve kilo verileri asla cihaz dışına çıkmaz.
  - OCR (optik karakter tanıma) ve EXIF ayrıştırma tamamen yerel çalışır.
  - Fotoğraflar yalnızca OCR işlemi süresince bellekte tutulur; kalıcı olarak diskte saklanmaz ve ağ üzerinden iletilmez.

---

## 1. Teknoloji Yığını (Tech Stack)

- **Platform:** Kotlin Multiplatform (KMP) & Compose Multiplatform
- **OCR:** Native Apple Vision (`VNRecognizeTextRequest` - iOS) / Google ML Kit Text Recognition (Android)
- **7-Segment OCR:** Özel `SevenSegmentReader` & `WeightParser`
- **Depolama:** `multiplatform-settings` (şifreli/güvenli yerel anahtar-değer deposu) + `kotlinx.serialization` (JSON)
- **Görsel/Kamera:** `FileKit` (galeri ve kamera seçici) + `androidx.exifinterface` (Android) / Native EXIF (iOS)
- **Tasarım / Cam Efekti:** `dev.chrisbanes.haze` (`hazeSource` + `hazeEffect`) + özel specular gradient ve ultra ince kenarlıklar
- **Haptik:** iOS `UIImpactFeedbackGenerator` / Android `Vibrator` & `HapticFeedbackConstants`
- **Sağlık Entegrasyonu:** Apple Health (`HealthSync`)

---

## 2. Mimari ve Katman Kuralları

Kaynak dizini: `composeApp/src/commonMain/kotlin/app/kilo/`

```
app.kilo/
├── App.kt                  # Ana uygulama girişi, AppContainer ve Theme sarmalayıcı
├── data/
│   ├── Repositories.kt     # WeightRepository, PrefsRepository, WorkoutRepository
│   ├── HevyModels.kt       # HevyWorkoutSession, HevyExercise, HevySet, MuscleCategory
│   └── HevyCsvParser.kt    # Hevy workouts.csv ayrıştırıcı (kas grupları, tonaj, 1RM)
├── domain/
│   ├── Weight.kt           # WeightEntry, WeightUnit, WeightDelta modelleri ve hesaplamaları
│   ├── WeightInput.kt      # Giriş doğrulama (20-500 kg aralığı, virgül/nokta toleransı)
│   ├── TrendAnalytics.kt   # Ağırlık trend ve hareketli ortalama hesaplamaları
│   ├── GoalProgress.kt     # Hedef kilo ilerlemesi ve tahmini varış süresi (ETA)
│   ├── PeriodSummary.kt    # Haftalık ve aylık özet hesaplamaları
│   ├── StreakAnalytics.kt  # İstikrar matrisi ve 14 günlük alışkanlık ısı haritası
│   ├── CycleAnalytics.kt   # Dinamik döngü (21-45 gün), backward luteal mapping, 14 mikro-dönem tavsiyeleri
│   ├── WorkoutCorrelationAnalytics.kt # DOMS inflamatuar ödem kalkanı, döngü-antrenman korelasyonu ve göreceli kuvvet
│   └── WeightExportImport.kt # CSV / JSON dışa/içe aktarım
├── ocr/
│   ├── ScaleOcr.kt         # expect ScaleOcr arayüzü
│   ├── WeightParser.kt     # Ham OCR metninden ağırlık ayıklayan saf Kotlin parser
│   ├── SevenSegmentReader.kt # 7 segment LED/LCD ekran okuyucu
│   └── OcrDiagnostics.kt   # OCR hata ayıklama yardımcıları
├── platform/
│   ├── Haptics.kt          # expect Haptics ve rememberHaptics
│   ├── PhotoPicker.kt      # FileKit sarmalayıcısı (galeri & kamera)
│   ├── CameraCapture.kt    # expect kamera yakalama arayüzü
│   ├── ExifDateParser.kt   # Fotoğraf çekim tarihini EXIF'ten okuma
│   ├── HealthSync.kt       # Apple Health / Health Connect çift yönlü senkronizasyonu
│   ├── ReminderScheduler.kt# expect yerel bildirim planlayıcı (UNUserNotificationCenter / AlarmManager)
│   └── ShareSheet.kt       # expect yerel paylaşım sayfası (UIActivityViewController / Intent)
└── ui/
    ├── DashboardScreen.kt  # Ana ekran Composable'ı
    ├── DashboardViewModel.kt # Ana ekran iş mantığı ve StateFlow yönetimi
    ├── theme/
    │   ├── Colors.kt       # Tema renkleri (Liquid glass paleti)
    │   └── Type.kt         # Tipografi
    └── components/
        ├── ActionBar.kt    # Birincil işlem çubuğu (Kamera, Galeri, Manuel, kg/lb)
        ├── GlassCard.kt    # Temel sıvı cam kart sarmalayıcısı
        ├── HeroCard.kt     # Güncel kilo, delta ve gün farkı kartı
        ├── DomsAlertBanner.kt # Kas onarım ödemi ve glikojen tutulumu uyarı kalkanı
        ├── GoalCard.kt     # Hedef kilo ve ilerleme kartı
        ├── GoalSheet.kt    # Hedef kilo belirleme alt sayfası
        ├── CycleCard.kt    # Menstrüel döngü & beslenme rehberi kartı
        ├── CycleSheet.kt   # 4 sekmeli döngü, beslenme, antrenman ve kaynaklar sayfası
        ├── WorkoutCorrelationSheet.kt # Hacim yükü, PR dağılımı ve göreceli kuvvet korelasyon sayfası
        ├── StreakCard.kt   # İstikrar matrisi & 14 günlük alışkanlık ısı haritası kartı
        ├── ShareCardSheet.kt # Başarı ve seri sıvı cam paylaşım kartı sayfası
        ├── SummaryCard.kt  # Dönemsel özet kartı (Haftalık / Aylık)
        ├── TrendChart.kt   # Trend grafik bileşeni
        ├── HistoryRow.kt   # Geçmiş kayıt satırı (sola kaydırarak silme)
        ├── EntrySheet.kt   # Manuel giriş ve OCR onay alt sayfası
        ├── CameraViewfinderScreen.kt # Özel vizör ekranı
        ├── IosDatePicker.kt# Sıvı cam tarih seçici
        └── BackupSheet.kt  # Yedekleme, Hevy CSV içe aktarma ve ayarlar sayfası
```

### Katman İlkeleri:
1. **DI Kütüphanesi YOK:** Projeye Koin, Hilt veya başka bir DI kütüphanesi eklemeyin. Bağımlılıklar elle `AppContainer` içinde oluşturulur ve bağlanır.
2. **`commonMain` Platformdan Bağımsızdır:**
   - `android.*`, `platform.*` (iOS), `com.android.*`, `Dispatchers.Main/IO` veya Android lifecycle kütüphaneleri `commonMain`'e eklenemez.
   - Coroutine işleri `viewModelScope.launch` ile başlatılır.
   - Flow verisi UI'da `collectAsStateWithLifecycle()` ile toplanır.
3. **`expect` / `actual` Bütünlüğü:**
   - Her `expect` bildirimi için hem `androidMain` hem de `iosMain` tarafında eksiksiz `actual` uygulaması bulunmalıdır.
   - iOS tarafı asla boş (stub/placeholder) bırakılamaz.

---

## 3. Alan (Domain) ve Veri Kuralları

1. **Ağırlık Her Zaman KG Olarak Saklanır:**
   - Veritabanına/ayarlara yazılan tüm `WeightEntry.weightKg` değerleri daima kilogram cinsindendir.
   - `kg` / `lb` seçimi yalnızca gösterim ve girdi anında çevrilir.
   - `lb` olarak girilen değer önce `kg`'ye çevrilip doğrulanır, öyle kaydedilir. Kayıtlı veri asla birim çevrimleriyle deforme edilmez.
2. **Delta Asla Saklanmaz (Listeden Türetilir):**
   - Kayıt listesi tarihe göre (`at` alanı) azalan sırada tutulur.
   - Bir kaydın önceki kilosu, sıralı listedeki bir sonraki kayıttır.
   - Delta (`diffKg`), gün farkı (`daysAgo`) ve haftalık değişimler gösterim anında yerel saat dilimine göre dinamik hesaplanır.
3. **Giriş Doğrulama:**
   - Geçerli aralık: **20.0 kg – 500.0 kg** (veya lb karşılığı).
   - Hem virgül (`,`) hem nokta (`.`) ondalık ayracı olarak kabul edilir.
   - En fazla 1 ondalık basamağa izin verilir.
4. **JSON Geriye Dönük Uyumluluk:**
   - Serileştirme şeması (`kotlinx.serialization`) genişletildiğinde, eski sürümlerde kaydedilmiş JSON dosyalarının sorunsuz okunabilmesi için yeni alanlar varsayılan değerlere (`default value`) sahip olmalıdır.
   - Bozuk veya geçersiz bir JSON kaydı tüm uygulamayı çökertmemeli, güvenli varsayılana düşmelidir.

---

## 4. OCR ve Medya İşleme Kuralları

1. **Cihaz İçi ve Çevrimdışı:**
   - Android: Google ML Kit Latin Text Recognition.
   - iOS: Apple Vision `VNRecognizeTextRequest` (`VNRequestTextRecognitionLevelAccurate`).
   - İnternet bağlantısı veya harici API gerektirmez.
2. **Ham Metin -> `WeightParser`:**
   - Platform OCR'ı ham satırları (`List<String>`) döner.
   - Metinden kilo ayıklama saf Kotlin olan `WeightParser` tarafından yapılır (`\d{2,3}[.,]\d` tespiti, `O→0` düzeltmesi, `kg`/`lb` yakınlık bonusu, aralık dışını eleme).
3. **Kullanıcı Onayı Zorunludur:**
   - OCR sonucu giriş alanına otomatik doldurulur; ancak **ASLA KULLANICI ONAYI OLMADAN DOĞRUDAN KAYDEDİLMEZ**.
   - Kullanıcı sonucu inceler, gerekirse düzeltir ve "Kaydet" butonuna basar.
4. **Yarış Durumu (Race Condition) Koruması:**
   - Kullanıcı OCR devam ederken sheet'i kapatırsa veya yeni bir fotoğraf seçerse, geciken eski OCR sonucu yeni durumu ezmemelidir (sürüm/nesil kontrolü).
5. **EXIF ve Yönlendirme:**
   - Çekilen fotoğrafların yön bilgisi (`ExifInterface` / native CGImage orientation) korunarak OCR'a düzgün açıda iletilmelidir. Fotoğraf çekim tarihi varsa `ExifDateParser` ile okunur ve kayıt tarihine önerilir.

---

## 5. UI ve Tasarım Kuralları

1. **Liquid Glass Estetiği:**
   - `dev.chrisbanes.haze` kütüphanesi ile cam efekti verilir (`hazeSource` + `hazeEffect`).
   - İnce yarı saydam kenarlıklar ve üst yüzey specular gradyanı ile zenginleştirilir.
   - Standart Material Design 3 bileşenlerini doğrudan kullanmak yerine projeye özel glass bileşenlerini tercih edin.
2. **Ortak Bileşenlerin Yeniden Kullanımı:**
   - `GlassCard`, `HeroCard`, `ActionBar`, `EntrySheet`, `HistoryRow`, `TrendChart`, `CameraViewfinderScreen`, `BackupSheet`, `IosDatePicker`.
   - Yeni ekran veya dialog tasarlamadan önce bu bileşenleri kullanın.
3. **Hardcoded String ve Renk YASAKTIR:**
   - Tüm kullanıcı arayüzü metinleri `composeApp/src/commonMain/composeResources/values/strings.xml` (İngilizce) ve `values-tr/strings.xml` (Türkçe) dosyalarına eklenmelidir. Yeni eklenen her metin **her iki dilde de** tanımlanmalıdır.
   - Compose kaynaklarında yüzde simgesi için `%%` çift kaçış kullanılır.
   - Renkler `app.kilo.ui.theme.Colors` dosyasından alınmalıdır.
4. **State Yönetimi ve Performans:**
   - UI State `StateFlow` olarak tutulur ve `MutableStateFlow.update { }` ile güncellenir.
   - `LazyColumn` öğelerinde stabil `key` olarak `entry.id` kullanılmalıdır.
   - Önemli bileşenlerde `@Preview` desteği korunmalıdır.

---

## 6. Doğrulama ve Test Komutları

```powershell
# Birim testleri çalıştırma (ortak testler, WeightParser, trend hesaplamaları)
.\gradlew.bat :composeApp:allTests

# Android derleme doğrulaması
.\gradlew.bat :composeApp:assembleDebug

# Android birim testleri
.\gradlew.bat :composeApp:testDebugUnitTest
```

> [!NOTE]
> Windows işletim sisteminde iOS binary derlemesi (`:composeApp:linkDebugFrameworkIosSimulatorArm64`) doğrudan çalıştırılamaz (yalnızca macOS/Xcode ortamında mümkündür). Windows üzerinde yapılan değişikliklerde KMP uyumluluğu ve `actual` dosyaları statik analiz ile doğrulanmalı, çalıştırılamadığı açıkça belirtilmelidir.

---

## 7. Proje İçi Antigravity Becerileri (Skills)

Projede `.agents/skills/` altında özel yetenekler tanımlanmıştır:

| Beceri | Yol | Kullanım Amacı |
| :--- | :--- | :--- |
| **kilo-architecture** | `.agents/skills/kilo-architecture` | KMP, Compose Multiplatform ve katman kurallarını uygulama |
| **architect** | `.agents/skills/architect` | Yeni özellik veya büyük refactor öncesi yapısal planlama |
| **code-review** | `.agents/skills/code-review` | Gizlilik, KMP sınırları, doğruluk ve güvenlik kod incelemesi |
| **kilo-media** | `.agents/skills/kilo-media` | Kamera, galeri, EXIF ve OCR entegrasyonu |
| **kilo-store-checklist** | `.agents/skills/kilo-store-checklist` | App Store ve Play Store mağaza yayın kontrol listesi |
