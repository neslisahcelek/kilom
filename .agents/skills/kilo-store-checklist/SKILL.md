---
name: kilo-store-checklist
description: Kilo uygulamasının Apple App Store ve Google Play Store'a gönderilmeden önce mağaza gereksinimleri, assetler, yasal belgeler ve release build kontrollerini adım adım doğrular.
---

# Kilo Store Release Checklist

Bu skill, **Kilo** uygulamasını Apple App Store ve Google Play Store'a göndermeden önce tamamlanması gereken teknik, görsel ve yasal gereksinimleri adım adım doğrulamak için kullanılır.

---

## 1. Görsel & Kimlik Varlıkları (Assets)

Her iki mağaza da varsayılan sistem ikonlarını reddeder.

### iOS (App Store)
- [ ] **App Icon (1024x1024 px):** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset` içinde şeffaf olmayan (no alpha), 1024x1024 PNG ikon.
- [ ] **Splash / Launch Screen:** `UILaunchScreen` storyboard veya native asset kontrolü.
- [ ] **Ekran Görüntüleri (Screenshots):**
  - 6.9" iPhone (1320 x 2868 px veya 1290 x 2796 px) - en az 3 adet (Dashboard, Hero Card, Entry Sheet / Tarama).
  - 6.5" iPhone (1242 x 2688 px).
  - iPad desteği aktifse 13" iPad Pro ekran görüntüleri.

### Android (Google Play)
- [ ] **Uygulama İkonu:** `res/mipmap-*/ic_launcher.png` ve adaptive icon (`ic_launcher_foreground.xml` + `background`).
- [ ] **Google Play Store İkonu:** 512x512 px 32-bit PNG (maks 1024 KB).
- [ ] **Öne Çıkan Grafik (Feature Graphic):** 1024x500 px JPG veya PNG (24-bit, şeffaflıksız).
- [ ] **Ekran Görüntüleri:** En az 2 adet telefon ekran görüntüsü (16:9 veya 9:16 oranında).

---

## 2. Yasal ve Gizlilik Gereksinimleri

Kilo kamera ve fotoğraf galerisi kullandığı için mağazalar gizlilik politikası ve beyan zorunluluğu getirir.

- [ ] **Gizlilik Politikası (Privacy Policy URL):**
  - Herkese açık bir web sayfası (örn. GitHub Pages veya Notion linki).
  - **İçerik:** *"Kilo fotoğrafları ve kilo verilerini yalnızca kullanıcının cihazında yerel olarak işler. Hiçbir veri veya görsel üçüncü taraf sunuculara iletilmez, analiz veya reklam amacıyla paylaşılmaz."*
- [ ] **Apple App Store Gizlilik Beyanı (App Privacy Nutrition Label):**
  - Data Collection: **"Data Not Collected"** (Uygulama sunucuya veri göndermediği için veri toplamıyor olarak işaretlenir).
- [ ] **Google Play Veri Güvenliği (Data Safety):**
  - *"Does your app collect or share any user data?"* -> **No**.
- [ ] **İzin Gerekçeleri (Permission Strings):**
  - [x] iOS `NSCameraUsageDescription`: *"Kilo uses the camera to read your weight from a photo of the scale."*
  - [x] iOS `NSPhotoLibraryUsageDescription`: *"Kilo reads your weight from a scale photo you choose from your library."*
  - [x] Android Manifest: `android.permission.CAMERA` tanımlı (`required="false"`).

---

## 3. Teknik & Release Build Hazırlığı

### Android (Google Play Console)
- [ ] **Keystore & İmzalamak (Release Signing):**
  - Bir release keystore oluşturulmalı:
    ```bash
    keytool -genkey -v -keystore kilo-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias kilo
    ```
  - `composeApp/build.gradle.kts` içinde `signingConfigs.release` tanımlanmalı veya `gradle.properties` üzerinden ortam değişkenleri okunmalı.
- [ ] **Android App Bundle (.aab):**
  - Google Play artık `.apk` değil `.aab` zorunlu tutar:
    ```powershell
    .\gradlew.bat :composeApp:bundleRelease
    ```
  - Çıktı: `composeApp/build/outputs/bundle/release/composeApp-release.aab`
- [ ] **Hedef SDK & Versiyon:**
  - `versionCode` (artan tamsayı, örn. 1), `versionName` ("1.0.0").
  - Google Play `targetSdk` şartı: Güncel API 34+ (mevcut projede 35 tanımlı, hazır).

### iOS (App Store Connect / TestFlight)
- [ ] **Apple Developer Hesabı & Certificate / Profile:**
  - `com.nesli.kilo` Bundle ID Apple Developer portalında açılmalı.
  - Distribution Certificate ve App Store Provisioning Profile oluşturulmalı.
- [ ] **Archive & Upload:**
  - Mac üzerinde Xcode'dan `iosApp.xcodeproj` açılmalı.
  - Target: `Any iOS Device (arm64)`.
  - **Product -> Archive** -> **Distribute App** -> **App Store Connect**.

---

## 4. Mağaza Listeleme Bilgileri (Metadata)

- [ ] **Uygulama Adı:** Kilo veya *Kilo – Minimal Weight Log* (Maks 30 karakter).
- [ ] **Alt Başlık / Kısa Açıklama (Subtitle / Short Description):**
  - TR: *"Tartı fotoğrafı ile hızlı ve sade kilo takibi."*
  - EN: *"Minimalist weight tracking with scale photo scanning."*
- [ ] **Detaylı Açıklama (Description):**
  - Temel özellikler: Manuel giriş, tartıdan fotoğraf ile otomatik okuma, son tartıya göre kilo farkı (delta) ve geçen gün sayısı, kg & lb desteği, liquid glass iOS tasarımı.
- [ ] **Kategori:** Health & Fitness (Sağlık ve Fitness).
- [ ] **Yaş Derecelendirmesi (Age Rating):** 4+ (Tüm yaşlar).
- [ ] **Destek İletişim URL / E-posta:** Geliştirici destek e-postası.

---

## 5. Doğrulama Komutları

```powershell
# 1. Tüm birim testlerin geçtiğinden emin ol:
.\gradlew.bat :composeApp:testDebugUnitTest --no-configuration-cache

# 2. Release bundle (.aab) derlemesini test et:
.\gradlew.bat :composeApp:bundleRelease --no-configuration-cache

# 3. macOS üzerinde Xcode Archive hazırlığı (Mac'te):
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```
