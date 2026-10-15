# Kilom Mağaza Yayın Öncesi Kontrol İş Akışı (Release Checklist Workflow)

Uygulamanın yeni bir sürümünü App Store veya Google Play Store'a göndermeden önce tüm teknik, görsel ve yasal gereksinimleri adım adım doğrula.

## Adımlar

1. **Mağaza Yönergelerini ve Uyumluluk Ajanını Çalıştır**
   - `.agents/skills/kilo-store-checklist/SKILL.md` kontrol listesini incele.
   - `store-compliance` alt ajanını (`.agents/agents/store-compliance.md`) çalıştırarak App Store Review Guidelines ve Google Play Developer Program Policies uyumluluk raporunu üret.

2. **İzin, Yasal Beyan ve Manifest Doğrulaması**
   - iOS `Info.plist`: `NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`, `NSHealthShareUsageDescription`, `NSHealthUpdateUsageDescription` metinlerini kontrol et.
   - iOS Privacy Manifest: `PrivacyInfo.xcprivacy` dosyasını ve Required Reason API kodlarını (`CA92.1` UserDefaults vb.) doğrula.
   - Android `AndroidManifest.xml`: `android.permission.CAMERA` (`required="false"`), `android.permission.POST_NOTIFICATIONS` ve donanım tanımlarını kontrol et.
   - Gizlilik politikası bağlantısını (`https://neslisahcelek.github.io/kilom/`) ve App Store Nutrition Label ("Data Not Collected") / Google Play Data Safety ("No user data collected") beyanlarını doğrula.

3. **Versiyon ve Derleme Numaralarını Kontrol Et**
   - `build.gradle.kts` veya `versionCode` / `versionName` güncelliğini doğrula.
   - iOS Marketing Version ve Build Version değerlerini kontrol et (`iosApp/iosApp.xcodeproj/project.pbxproj`).

4. **Release Derlemesini Doğrula**
   - Android için AAB derlemesi:
     ```powershell
     .\gradlew.bat :composeApp:bundleRelease
     ```
   - Test suite'in eksiksiz geçtiğini doğrula:
     ```powershell
     .\gradlew.bat :composeApp:allTests
     ```
