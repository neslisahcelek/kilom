# Kilom Mağaza Yayın Öncesi Kontrol İş Akışı (Release Checklist Workflow)

Uygulamanın yeni bir sürümünü App Store veya Google Play Store'a göndermeden önce tüm teknik, görsel ve yasal gereksinimleri adım adım doğrula.

## Adımlar

1. **Mağaza Yönergelerini Oku**
   - `.agents/skills/kilo-store-checklist/SKILL.md` dosyasını baştan sona incele.

2. **İzin ve Yasal Beyanları Doğrula**
   - iOS `Info.plist`: `NSCameraUsageDescription` ve `NSPhotoLibraryUsageDescription` metinlerini kontrol et.
   - Android `AndroidManifest.xml`: `android.permission.CAMERA` tanımlı mı?
   - Gizlilik politikası bağlantısını ve cihaz içi veri işleme beyanını kontrol et (`PRIVACY_POLICY.md` / `https://neslisahcelek.github.io/kilom/`).

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
