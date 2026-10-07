# Kilom

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-blue.svg)](https://github.com/neslisahcelek/Kilom)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device-success.svg)](https://neslisahcelek.github.io/Kilom/)
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey.svg)](#license)

**Kilom**, tartı fotoğrafı ile hızlı ve sade kilo takibi sunan, gizlilik odaklı modern bir mobil uygulamadır.

Kotlin Multiplatform (KMP) ve Compose Multiplatform mimarisiyle geliştirilmiş olup, hem **Android** hem de **iOS** cihazlarda tam performans ve yerel deneyim sağlar.

---

## 🌟 Öne Çıkan Özellikler (Key Features)

- 📸 **Tartı Fotoğrafı ile Otomatik Okuma (Scale OCR):**
  - Kamera ile tartı ekranını çekin veya galeriden seçin.
  - Sayılar **cihaz üzerinde (on-device)** optik karakter tanıma (iOS'ta Apple Vision Framework, Android'de Google ML Kit) ile anında okunur.
- 🔒 **%100 Gizlilik ve Yerel Depolama (Privacy-First):**
  - Sunucu yok, hesap açma yok, internet aktarımı yok.
  - Tüm kilo kayıtları ve görseller yalnızca cihazın yerel veritabanında (Room) kalır.
- ⚖️ **Kilo Değişimi ve İlerleme Takibi (Delta Tracking):**
  - Bir önceki tartıya göre kilo farkı (artış/azalış) ve geçen gün sayısı anlık hesaplanır.
- 🌐 **Birim Desteği:**
  - Metrik (`kg`) ve İmparatorluk (`lb`) birimleri arasında kolay geçiş.
- 💎 **Minimalist Tasarım:**
  - Sade, modern ve göz yormayan arayüz tasarımı.

---

## 🛡️ Gizlilik Politikası & Yasal Uyumluluk (Privacy Policy & Compliance)

Kilom, **Apple App Store** ve **Google Play Store** mağaza gizlilik gereksinimlerini %100 karşılar.

- **Canlı Web Sayfası (GitHub Pages):** [https://neslisahcelek.github.io/Kilom/](https://neslisahcelek.github.io/Kilom/)  
  *(veya doğrudan [https://neslisahcelek.github.io/Kilom/privacy.html](https://neslisahcelek.github.io/Kilom/privacy.html))*
- **Proje İçi Belge:** [PRIVACY_POLICY.md](PRIVACY_POLICY.md) (Türkçe & English)

### Mağaza Beyanları (Store Declarations)
| Mağaza / Platform | Bölüm / Beyan | Durum | Açıklama |
|---|---|---|---|
| **Apple App Store** | App Privacy Nutrition Label | **Data Not Collected** | Uygulama hiçbir veri toplamaz veya sunuculara göndermez. |
| **Google Play** | Veri Güvenliği (Data Safety) | **No Data Collected / Shared** | Veri toplanmaz, üçüncü taraflarla paylaşılmaz. |
| **Cihaz İzinleri** | Kamera & Galeri | **On-Device Only** | Sadece tartı ekranı OCR okuma amacıyla bellek içinde kullanılır; görsel saklanmaz. |

---

## 📱 İzinler ve Güvenlik Beyanları (Permissions)

### iOS (`iosApp/iosApp/Info.plist`)
- `NSCameraUsageDescription`: *"Kilo uses the camera to read your weight from a photo of the scale."*
- `NSPhotoLibraryUsageDescription`: *"Kilo reads your weight from a scale photo you choose from your library."*

### Android (`composeApp/src/androidMain/AndroidManifest.xml`)
- `<uses-permission android:name="android.permission.CAMERA" />` (Kamera taraması için isteğe bağlı izin).

---

## 🛠️ Mimari ve Teknolojiler (Architecture & Tech Stack)

- **UI:** Compose Multiplatform (Material 3 + Adaptive Layouts)
- **OCR Engine:**
  - iOS: Native Apple Vision Framework (`VNRecognizeTextRequest`)
  - Android: Google ML Kit Text Recognition (On-device mode)
- **Local Storage:** Room Database (Sandboxed SQLite)
- **Dependency Injection:** Koin
- **Target SDK:** Android API 35 (Min: API 26), iOS 15+
- **Application ID:** `com.nesli.kilo`

---

## 🚀 Derleme ve Çalıştırma (Build & Run)

### Ön Gereksinimler
- JDK 17+
- Android Studio / IntelliJ IDEA (KMP eklentisi ile)
- Xcode 15+ (iOS çalıştırmak için macOS üzerinde)

### Android Build
```powershell
# Birim testleri çalıştır:
.\gradlew.bat :composeApp:testDebugUnitTest --no-configuration-cache

# Release Android App Bundle (.aab) üretimi:
.\gradlew.bat :composeApp:bundleRelease --no-configuration-cache
```
*Çıktı konumu:* `composeApp/build/outputs/bundle/release/composeApp-release.aab`

### iOS Build (macOS)
```bash
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```
Xcode üzerinden `iosApp/iosApp.xcodeproj` açılarak App Store Connect arşivleme (Archive & Upload) işlemi gerçekleştirilir.

---

## 📬 İletişim & Destek (Contact & Support)

Uygulama ile ilgili her türlü soru, geri bildirim veya destek talebi için:

- **Geliştirici:** Neslişah Çelek
- **Destek E-postası:** [neslisah.celek@outlook.com](mailto:neslisah.celek@outlook.com)
- **GitHub:** [@neslisahcelek](https://github.com/neslisahcelek)