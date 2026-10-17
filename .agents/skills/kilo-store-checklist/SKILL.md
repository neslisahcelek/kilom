---
name: kilo-store-checklist
description: Kilo uygulamasının Apple App Store ve Google Play Store'a gönderilmeden önce mağaza gereksinimleri, assetler, yasal belgeler, Privacy Manifest, HealthKit/Health Connect ve release build kontrollerini adım adım doğrular.
---

# Kilo Store Release Checklist & Review Compliance

Bu skill ve ilişkili `store-compliance` alt ajanı, **Kilom** uygulamasını Apple App Store ve Google Play Store'a göndermeden önce tamamlanması gereken teknik, görsel, yasal ve mağaza inceleme uyumluluk (App Store Review Guidelines & Google Play Developer Program Policies) gereksinimlerini adım adım doğrulamak için kullanılır.

---

## 1. Görsel & Kimlik Varlıkları (Assets)

Her iki mağaza da varsayılan sistem ikonlarını veya kalitesiz görselleri reddeder.

### iOS (App Store - Guideline 2.1 & 5.2.2)
- [ ] **App Icon (1024x1024 px):** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset` içinde **şeffaflık (alpha kanalı) içermeyen** 24-bit RGB PNG ikon (Alpha kanalı varsa upload aşamasında reddedilir).
- [ ] **Splash / Launch Screen:** `UILaunchScreen` storyboard veya native asset kontrolü (siyah ekran başlatma hatası olmamalı).
- [ ] **Ekran Görüntüleri (Screenshots):**
  - Gerçek UI ekran görüntüleri olmalı (Figma mock-up tek başına reddedilir).
  - 6.9" iPhone (1320 x 2868 px), 6.7" iPhone (1290 x 2796 px) ve 6.5" iPhone (1284 x 2778 px / 1242 x 2688 px) — detaylar ve üretim betiği için [kilo-store-assets](../kilo-store-assets/SKILL.md) becerisine bakın.
  - iPad desteği aktifse 13" iPad Pro ekran görüntüleri.
  - **DİKKAT:** Ekran görüntülerinde Android cihaz çerçeveleri, Material Design FAB veya "Android" ibaresi bulunamaz (Guideline 2.3.3).
  - **Bağlam Koruması:** Ekran görüntülerinin boyut, `hasAlpha: no` ve `sRGB` kontrollerini `view_file` ile 10+ görsel okuyarak değil, `sips` CLI komutu ile doğrulayın.


### Android (Google Play - Metadata & Store Listing Policy)
- [ ] **Uygulama İkonu:** `res/mipmap-*/ic_launcher.png` ve adaptive icon (`ic_launcher_foreground.xml` + `background`).
- [ ] **Google Play Store İkonu:** 512x512 px 32-bit PNG (maks 1024 KB).
- [ ] **Öne Çıkan Grafik (Feature Graphic):** 1024x500 px JPG veya PNG (24-bit, şeffaflıksız).
- [ ] **Ekran Görüntüleri:** En az 2 adet telefon ekran görüntüsü (16:9 veya 9:16 oranında).
- [ ] **DİKKAT:** Ekran görüntülerinde Apple Health, iPhone çerçevesi veya iOS referansı bulunamaz.

---

## 2. Yasal ve Gizlilik Gereksinimleri (Privacy & Data Safety)

Kilom kamera, fotoğraf galerisi, yerel bildirimler ve Apple Health kullandığı için mağazalar sıkı gizlilik beyanları zorunlu kılar.

### Apple App Store (Guidelines 5.1.1, 5.1.2, 5.1.3)
- [ ] **Gizlilik Politikası (Privacy Policy URL - Guideline 5.1.1):**
  - Herkese açık canlı bir HTTPS web sayfası (örn. `https://neslisahcelek.github.io/kilom/`).
  - Uygulama içinde (örn. `BackupSheet` / Ayarlar) dokunulabilir bağlantı.
  - İçerik: *"Kilo fotoğrafları ve kilo verilerini yalnızca kullanıcının cihazında yerel olarak işler. Hiçbir veri veya görsel üçüncü taraf sunuculara iletilmez, analiz veya reklam amacıyla paylaşılmaz."*
- [ ] **Privacy Manifest (`PrivacyInfo.xcprivacy` - Guideline 5.1.2):**
  - Spring 2024 sonrası zorunlu: `NSUserDefaults` (`multiplatform-settings` için `CA92.1`), `NSFileModificationDate` vb. Required Reason API beyanları.
  - Eksikse Apple upload doğrulaması derlemeyi reddeder.
- [ ] **App Store Gizlilik Beyanı (Nutrition Label - Guideline 5.1.1):**
  - Data Collection: **"Data Not Collected"** (Uygulama sunucuya veri göndermediği için veri toplamıyor olarak işaretlenir).
- [ ] **Apple Health / HealthKit Kuralları (Guideline 5.1.3):**
  - Sağlık verisi yalnızca kişisel takip için yerel kullanılır; reklam veya analitik için kullanılamaz.
  - `NSHealthShareUsageDescription` ve `NSHealthUpdateUsageDescription` metinleri `Info.plist` içinde kullanıcı dostu ve açık olmalıdır.
- [ ] **İzin Gerekçeleri (`Info.plist` - Guideline 5.1.1):**
  - `NSCameraUsageDescription`: *"Kilom uses the camera to read your weight from a photo of the scale."*
  - `NSPhotoLibraryUsageDescription`: *"Kilom reads your weight from a scale photo you choose from your library."*
  - `NSHealthShareUsageDescription`: *"Kilom reads your past weight entries from Apple Health so you can import them. Data stays on your device."*
  - `NSHealthUpdateUsageDescription`: *"Kilom saves your weight entries to Apple Health. Data stays on your device."*

### Google Play Store (User Data Policy & Permissions)
- [ ] **Google Play Veri Güvenliği (Data Safety Section):**
  - *"Does your app collect or share any user data?"* -> **No**.
  - Reklam, konum takibi veya üçüncü taraf analitik SDK'sı bulunmadığını doğrula.
- [ ] **Kamera İzni Donanım Şartı (`AndroidManifest.xml`):**
  - `<uses-permission android:name="android.permission.CAMERA" />`
  - `<uses-feature android:name="android.hardware.camera" android:required="false" />` (Kamerasız cihazlar indirebilsin).
- [ ] **Geniş Depolama İzni Kullanmama:**
  - `READ_EXTERNAL_STORAGE` veya `MANAGE_EXTERNAL_STORAGE` izni eklenmemeli; Android Photo Picker veya FileKit/SAF kullanılmalıdır.
- [ ] **Bildirim İzni (`POST_NOTIFICATIONS`):**
  - Android 13+ için bağlamsal olarak (kullanıcı hatırlatıcı kurduğunda) istenmeli.

---

## 3. Teknik & Release Build Hazırlığı

### Android (Google Play Console)
- [ ] **Hedef SDK & 64-Bit:**
  - `targetSdkVersion` güncel olmalı (Google Play en az 34 ister, Kilom'da 35 tanımlıdır).
  - 64-bit mimari (arm64-v8a) desteği.
- [ ] **Keystore & İmzalamak (Release Signing):**
  - Release keystore ve `gradle.properties` veya ortam değişkenleri yapılandırması.
- [ ] **Android App Bundle (.aab):**
  - Play Store'a `.aab` yüklenmelidir:
    ```powershell
    .\gradlew.bat :composeApp:bundleRelease
    ```
  - Çıktı: `composeApp/build/outputs/bundle/release/composeApp-release.aab`
- [ ] **Sürüm Kodları:**
  - `versionCode` (artan tamsayı), `versionName` ("1.0.0").

### iOS (App Store Connect / TestFlight)
- [ ] **Minimum SDK & Xcode:**
  - Xcode 15+ ve iOS 17 SDK hedefi.
- [ ] **Bundle Display Name:**
  - `CFBundleDisplayName` ("Kilom") ile App Store Connect uygulama adı tutarlı olmalıdır (Guideline 1.2).
- [ ] **Archive & Upload:**
  - Xcode: `Any iOS Device (arm64)` -> **Product -> Archive** -> **Distribute App** -> **App Store Connect**.

---

## 4. Mağaza Listeleme & İnceleme Notları (Metadata & Review Notes)

- [ ] **Hesap / Giriş Bilgileri (Guideline 5.1.1 & 2.1):**
  - Kilom hesap gerektirmez. App Store Connect "App Review Information" alanına:
    *"No account, login, or registration is required to use this app. All features are accessible immediately."* notu eklenmelidir.
- [ ] **Apple Health İnceleme Notu (Guideline 5.1.3):**
  - Denetçiye HealthKit'in nasıl test edileceğini anlatan adım adım not:
    *"To test Apple Health integration: Go to Settings -> Toggle Apple Health Sync -> Allow Permissions in the iOS system dialog. Then add a weight entry on the main screen; it will instantly sync to Apple Health."*
- [ ] **Karakter Limitleri:**
  - App Store / Play Store Başlık: Maksimum 30 karakter (*"Kilom"* veya *"Kilom – Weight Log"*).
  - iOS Subtitle: Maksimum 30 karakter.
  - Play Store Kısa Açıklama: Maksimum 80 karakter.
- [ ] **Platform Ayrımı:**
  - iOS metninde "Google Play / Android", Android metninde "Apple Health / iOS" ibareleri bulunamaz.
- [ ] **Üçüncü Taraf Ticari Marka ve Uygulama Adı Yasağı (Guideline 2.3.8 & 5.2.1):**
  - Başlık, alt başlık, anahtar kelimeler, mağaza açıklaması veya UI bileşenlerinde üçüncü taraf tescilli uygulama markaları (örn. "Hevy", "Strava") doğrudan veya resmi ortaklık izlenimi verecek şekilde kullanılamaz.
  - İçe aktarma gibi entegrasyonlarda daima jenerik veya dosya formatı uyumluluğunu belirten ifadeler kullanılmalıdır (örn. *"Antrenman CSV İçe Aktar (workouts.csv)"*).
- [ ] **Yaş Derecelendirmesi Sağlık Cevapları (Guideline 2.3.6):**
  - App Information > Age Rating: *Health or Wellness Topics* = **YES** (beslenme, egzersiz, kalori/kilo takibi, döngü rehberi var). *Medical or Treatment Information* = **NONE** (teşhis/tedavi yok, feragatname var).
  - *Regulated Medical Device* = "not a regulated medical device" kalmalı; uygulamada doğurganlık, gebelik veya korunma tahmini iddiası olmamalı.
- [ ] **Build ve Metadata Uyumu (Guideline 2.1 & 2.3.1):**
  - Review Notes ve açıklamada anlatılan her özellik, ASC'de **seçili build'de** bulunmalı. Reddedilen eski build seçiliyken yeni özellik anlatılmaz.
  - Sıra: yeni build (build numarası artmış) -> TestFlight işlenmesi -> ASC'de build seçimi -> notlar -> Reply to App Review -> Resubmit.
- [ ] **Privacy Manifest Xcode Hedefinde (Guideline 5.1.2):**
  - `iosApp/iosApp/PrivacyInfo.xcprivacy` dosyası diskte olmak yeterli değildir; Xcode'da iosApp target'ına (Copy Bundle Resources) eklenmelidir. Archive sonrası Privacy Report ile doğrulanır.
- [ ] **İnceleme Cihazı (iPad) ve Boş Durum (Guideline 4.2):**
  - Apple iPhone-only uygulamayı da iPad'de test edebilir. Notlar ilk adımda özelliklerin nerede açılacağını yazmalı (döngü varsayılan kapalıdır, antrenman CSV gerektirir).
  - HealthKit için (Guideline 2.5.1) fiziksel cihazda ekran kaydı hazırlanıp Notes veya Attachment'a eklenmelidir.
- [ ] **Anahtar Kelimeler (Guideline 2.3.7):** `apple` gibi marka içermemeli; `sağlık` kullanılmalı.

---

## 5. Doğrulama ve Test Komutları

```powershell
# 1. Ortak birim testlerini ve algoritmaları çalıştır:
.\gradlew.bat :composeApp:allTests

# 2. Android release bundle (.aab) derlemesini doğrula:
.\gradlew.bat :composeApp:bundleRelease

# 3. Android debug derlemesini doğrula:
.\gradlew.bat :composeApp:assembleDebug
```

---

## 6. Store Compliance Ajanı ile İnceleme

Yayın öncesi ret risklerini detaylı raporlamak için `store-compliance` ajanını çağırabilir veya şu komutu verebilirsin:
> "Uygulamanın mevcut durumunu App Store ve Google Play Store uyumluluğu açısından analiz et."
