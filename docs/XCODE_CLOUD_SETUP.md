# Kilom — Xcode Cloud & TestFlight Kurulum Rehberi (Mac Olmadan Sürüm Çıkma)

Bu rehber, **Kilom** uygulamasını fiziksel bir Mac bilgisayar kullanmadan, Windows üzerinden yalnızca Git commit/push işlemleriyle Apple **Xcode Cloud** üzerinden derleyip **TestFlight** ve **App Store**'a nasıl göndereceğinizi adım adım açıklar.

---

## 0. Ücret Çıkmamasını Garantileme İlkeleri (Sıfır Maliyet Kalkanı)

> [!IMPORTANT]
> **25 Saat Ücretsiz Kota:** Apple Developer Program (\$99/yıl) üyeliği olan her geliştiriciye her ay **25 saat ücretsiz** Xcode Cloud derleme süresi tanımlanır. Kilom'un optimize edilmiş bir bulut derlemesi yaklaşık **4–5 dakika** sürer. Bu da ayda yaklaşık **300 adet TestFlight sürümünün tamamen ücretsiz** çıkılabilmesi demektir.

Faturanıza kesinlikle **hiçbir ek ücret yansımaması** için aşağıdaki 3 kural projede yapılandırılmıştır:

1. **Yalnızca Tag ile Tetikleme (Branch push'larında derleme başlatmama):**
   - Her kod commit'inde Xcode Cloud **çalıştırılmaz**.
   - Yalnızca siz bilinçli olarak bir sürüm tag'i (`v1.0.0`, `v1.0.1` vb.) attığınızda veya App Store Connect web panelinden "Start Build" butonuna bastığınızda derleme başlar.
   - Böylece günlük geliştirme commit'leriniz tek bir dakika bile kota harcamaz.
2. **Yüksek Hızlı JDK Kurulumu (adoptium tarball):**
   - Projedeki `ci_scripts/ci_post_clone.sh` betiği, Homebrew yerine doğrudan Adoptium Temurin binary'sini indirir (~10 saniye). Homebrew'un her derlemede 3-5 dakika süren `brew update` adımını atlayarak aylık ücretsiz kotanızı maksimum düzeyde korur.
3. **Timeout Koruması (20 Dakika Tavanı):**
   - Workflow ayarlarında maksimum derleme süresi 20 dakika olarak sınırlandırılır; olası bir ağ takılmasında saatlerce arka planda kota tüketimi engellenir.
4. **App Store Connect Otomatik Yükseltmeyi Kapalı Tutma:**
   - App Store Connect -> Xcode Cloud -> Subscription ayarlarında planınızın varsayılan **"25 hours/month (Included)"** olduğundan ve ek ücretli planlara otomatik yükseltmenin (auto-upgrade) kapalı olduğundan emin olun.

---

## 1. Projede Yapılan Otomatik Hazırlıklar

Bu depoda Xcode Cloud için gereken tüm teknik gereksinimler önceden hazırlanmıştır:

| Dosya / Yapılandırma | Görevi |
| :--- | :--- |
| `iosApp/.../xcshareddata/xcschemes/iosApp.xcscheme` | Xcode Cloud'un projeyi ve Release arşiv hedefini tanımasını sağlayan paylaşılan şema. |
| `ci_scripts/ci_post_clone.sh` | Xcode Cloud sanal makinesine ~10 saniyede JDK 17 kuran ve Gradle ortamını hazırlayan yaşam döngüsü betiği. |
| `iosApp/ci_scripts/ci_post_clone.sh` | Xcode Cloud'un `.xcodeproj` komşuluğunda arama yapabilmesi için yönlendirici betik. |
| `iosApp.xcodeproj/project.pbxproj` | Xcode build aşamasında Java yolunu otomatik tespit eden dayanıklı betik. |
| `iosApp/Configuration/Config.xcconfig` | Sürüm (`MARKETING_VERSION=1.0.0`) ve build numarası (`CURRENT_PROJECT_VERSION=8`). |
| `.gitattributes` | Betiklerin Unix uyumlu (LF) olarak klonlanmasını garanti eden ayar. |

---

## 2. App Store Connect Üzerinden İlk Kurulum (Web / Tarayıcı)

Bu adımı Windows bilgisayarınızdaki herhangi bir web tarayıcısından yapabilirsiniz (Mac gerekmez):

### Adım 1: GitHub Deposunu Bağlama
1. [App Store Connect](https://appstoreconnect.apple.com/) adresine gidin ve Apple ID'nizle giriş yapın.
2. Üst menüden **Kullanıcılar ve Erişim (Users and Access)** -> **Entegrasyonlar (Integrations)** -> **Xcode Cloud** sekmesine tıklayın.
3. Kaynak kodu sağlayıcısı olarak **GitHub**'ı seçin ve GitHub hesabınıza yetki verin (Apple'ın GitHub App'ine Kilom reposuna erişim izni verin).

### Adım 2: Xcode Cloud İş Akışını (Workflow) Oluşturma
1. App Store Connect ana sayfasına dönüp **Uygulamalarım (My Apps)** -> **Kilom** sayfasına girin.
2. Üstteki sekmelerden **Xcode Cloud** sekmesine tıklayın.
3. **Create Workflow (İş Akışı Oluştur)** butonuna basın.
4. Karşınıza gelen sihirbazda şu ayarları yapın:
   - **General (Genel):**
     - Workflow Name: `Kilom TestFlight Release`
     - Description: `Zero-cost automated TestFlight deployment on release tags.`
   - **Primary Repository:**
     - Repository: `eager-shannon` (veya `kilom` reponuz)
   - **Branch / Tag Changes (TETİKLEYİCİ):**
     - **Branch Changes:** **EKLEYİN**
     - **Branch:** `master` (veya `main`)
     - **Files and Folders:** Any changes
   - **Build Number (OTOMATİK SÜRÜM KODU YÜKSELTME):**
     - **Build Number:** `Automatically increment build number` seçeneğini aktif edin (Next build number: `9` veya üzeri).
     - *Not: Depodaki `ci_scripts/ci_pre_xcodebuild.sh` betiği bu build numarasını (`$CI_BUILD_NUMBER`) derleme anında otomatik olarak `Config.xcconfig`'e işler; her master push'unda sürüm kodu otomatik olarak 1 artar.*
   - **Actions (Aksiyonlar):**
     - `+` butonuna basıp **Archive** aksiyonunu seçin.
     - Platform: **iOS**
     - Scheme: **`iosApp`** (Otomatik olarak listede görünecektir).
     - Deployment Preparation: **TestFlight & App Store**.
   - **Post-Actions (Derleme Sonrası):**
     - `+` butonuna basıp **TestFlight (Internal Testing)** ekleyin.
     - Gruba kendinizi ekleyin (Derleme biter bitmez iPhone'unuza TestFlight bildirimi gelir).
5. **Save (Kaydet)** butonuna basarak iş akışını tamamlayın.

---

## 3. Windows'tan Sürüm Çıkma Rutini (Adım Adım)

Artık kod yazarken veya yeni bir özellik eklediğinizde Mac'e hiç el sürmeden sürüm çıkabilirsiniz:

### Adım 1: Sürüm Numarasını Belirleyin (İsteğe bağlı)
Gerekirse `iosApp/Configuration/Config.xcconfig` dosyasındaki build numarasını artırın:
```text
MARKETING_VERSION=1.0.1
CURRENT_PROJECT_VERSION=9
```

### Adım 2: Değişiklikleri Commit Edin
```powershell
git add .
git commit -m "feat: new release ready for testflight"
git push origin main
```
*(Bu push tek başına Xcode Cloud'u çalıştırmaz ve sıfır kota harcar).*

### Adım 3: Sürüm Tag'i Atın ve Push Edin (Derlemeyi Başlatır)
```powershell
git tag v1.0.1
git push origin v1.0.1
```

### Adım 4: Canlı İzleyin ve TestFlight'tan İndirin
1. App Store Connect -> Kilom -> **Xcode Cloud** sekmesine gidin.
2. `v1.0.1` etiketli derlemenin başladığını göreceksiniz.
3. Yaklaşık 4–5 dakika içinde:
   - `ci_post_clone.sh` JDK 17'yi 10 saniyede kurar.
   - Kotlin Multiplatform framework'ü derlenir.
   - iOS uygulaması arşivlenir ve Apple tarafından otomatik imzalanır.
   - TestFlight'a yüklenir.
4. iPhone'unuzdaki **TestFlight** uygulamasından bildirim gelir; uygulamayı tek tıkla cihazınıza kurup test edebilirsiniz.
5. Memnun kaldığınızda App Store Connect üzerinden derlemeyi seçip **"İncelemeye Gönder" (Submit for Review)** butonuna basarak App Store'da yayına alabilirsiniz.

---

## 4. Sorun Giderme (Troubleshooting)

- **Soru: Git push ettim ama Xcode Cloud başlamadı?**
  - Tag'in `v` ile başladığından (`v1.0.0`) ve `git push origin v1.0.0` komutunu çalıştırdığınızdan emin olun. Veya App Store Connect web arayüzünden doğrudan **"Start Build"** butonuna basarak manuel başlatabilirsiniz.
- **Soru: `ci_post_clone.sh` çalıştırma izni hatası verirse?**
  - Windows terminalinde şu komutu çalıştırıp commit edin:
    ```powershell
    git update-index --chmod=+x ci_scripts/ci_post_clone.sh iosApp/ci_scripts/ci_post_clone.sh gradlew
    git commit -m "fix: enforce chmod +x on ci scripts"
    git push origin main
    ```
- **Soru: Build numarası çakışması (Redundant Binary Upload)?**
  - Her yeni sürüm etiketinden önce `iosApp/Configuration/Config.xcconfig` içindeki `CURRENT_PROJECT_VERSION` değerini bir artırın veya Xcode Cloud ayarlarında *"Automatically increment build number"* seçeneğini aktif edin.
