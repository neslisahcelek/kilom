---
name: code-review
description: Kilo (KMP / Compose Multiplatform) kod değişikliklerini gizlilik, doğruluk, mimari ve KMP uyumu açısından inceler. Code review istendiğinde (/code-review, /kilo-code-review) ve commit/push öncesinde kullan.
---

# Code review

Review yaparken önce varsa `AGENTS.md` ve `.agents/skills/kilo-architecture/SKILL.md` oku. Güncel kod ve diff kanıttır; varsayımla bulgu yazma. İnceleme istenmişse kodu kendiliğinden değiştirme. Review tamamlanıp bulgular gösterilmeden commit/push yapma.

## Kapsam ve kullanıcı değişiklikleri

```bash
git status --short
git diff --stat
git diff
git diff --cached
git ls-files --others --exclude-standard
```

- Base/head ve değişen dosyaları belirle; gerekirse diff'in bağlamını kaynak dosyadan oku.
- Kullanıcının önceden var olan değişikliklerini ayır. Geri alma, izinsiz stage etme veya `git add -A` kullanma.
- Gizli/yerel yapılandırmaları ve kopya dosyaları (`* 2.*`) commit kapsamına alma.
- Her bulgu için değiştirilmiş satıra yakın dosya ve satır belirt; etki ve somut tetiklenme koşulunu açıkla.

## Gizlilik ve secret

- Diff'te API key/token/parola/private key/keystore sırlarını ara. `local.properties`, keystore (`*.jks`, `*.keystore`), `.env*`, imzalama parolaları stage edilmemeli.
- Log/`println` içinde kullanıcının kilo verisi veya foto içeriği yazılmamalı.
- Veri yalnızca cihazda kalmalı; yeni ağ çağrısı, analitik veya üçüncü taraf SDK eklenmişse gerekçesi ve gizlilik etkisi sorgulanmalı. Foto/OCR işlemi cihaz içinde (offline) kalmalı.
- Foto yalnızca OCR için bellekte işlenmeli; gereksiz yere kalıcı saklanmamalı.

## Mobil / KMP

- `commonMain`'de `android.*`, `platform.*` importu, `Dispatchers.Main/IO` veya Android lifecycle artifact'ı var mı? Lifecycle-aware collection ve platform `actual`'ları tamam mı?
- Her `expect` (`ScaleOcr`, `Haptics`, vb.) için Android ve iOS `actual` gerçek mi, yoksa placeholder/stub mı?
- Yeni izin dar kapsamlı mı? iOS `Info.plist` açıklamaları (`NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`) ve Android manifest izinleri ekli mi?
- Yeni bağımlılık her iki platformda çalışıyor mu, doğru source set'te mi? Sürüm/kaynak güvenilir mi?
- `AppContainer` dışında gizli singleton veya DI kütüphanesi eklenmiş mi?

## Doğruluk, mimari ve kullanıcı deneyimi

- İş kuralı repository/UI içine sızmış mı? Repository yalnızca veri erişimi; ViewModel iş/UI state; Composable state tüketip event gönderiyor mu?
- kg her zaman kg saklanıyor mu? lb↔kg çevirisi yalnızca gösterim/girişte mi, çift çevirme veya yuvarlama kaynaklı veri kaybı var mı? Delta saklanmıyor, türetiliyor mu (önceki kayıt `at` sıralamasına göre; geçmiş tarihli giriş doğru mu)?
- Doğrulama (20–500 kg, virgül/nokta, 1 ondalık) lb girişinde de kg'ye çevrilip uygulanıyor mu?
- JSON okuma/yazma: eski kayıtlar, eksik alanlar, bozuk JSON güvenli varsayılana düşüyor mu? Serialization şeması geriye uyumlu mu? Yazma hatası sessizce yutuluyor mu?
- `WeightParser`: aralık dışı, çoklu sayı, `O→0`, kg/lb etiketi senaryoları doğru mu? OCR sonucu önceden doldurulup kullanıcı onayı isteniyor mu (otomatik kaydetme yok)?
- Async OCR/foto: kullanıcı sheet'i kapatır, ikinci foto seçer veya iptal ederse eski sonuç yeni durumu ezmiyor mu? Hata görünür mü, boş sonuç hata gibi ele alınıyor mu?
- `StateFlow.update {}` kullanımı, `collectAsStateWithLifecycle`, lazy list stabil `key` (kayıt `id`), ortak glass bileşen kullanımı, önemli ekranlarda `@Preview` varlığı.
- Kullanıcı metinleri `strings.xml`'de mi (EN + TR ikisi de)? Hata etiketleri kaynak kimliğine map ediliyor mu? Compose resource formatında yüzde literal'i (`%%`) doğru mu? Renkler tema dosyasında mı?
- `!!`, zorla cast, boş liste/index sınırları, tekrar submit, silme (sola kaydır) sonrası liste ve delta güncellemesi, boş/loading/error durumları.
- Zaman: gece yarısı geçişi, saat dilimi/DST değişimi ve geçmiş tarihli giriş gün farkını bozuyor mu?
- Haptic: başarı/hata geri bildirimi doğru olaylara bağlı mı, iki platformda da çalışıyor mu?

## Secret taraması için örnek

```bash
git diff --cached -U0 | grep -nEi 'api[_-]?key|secret|token|password|passwd|storePassword|keyPassword|private[_-]?key|BEGIN [A-Z ]*PRIVATE'
```

Stage edilmiş diff yoksa aynı kontrolü incelemenin diff kapsamına göre staged/unstaged diff üzerinde yap.

## Doğrulama

- Yalnızca değişiklikle ilgili mevcut test/derleme komutlarını seç; test/verify kullanıcı tarafından istenmediyse yeni test ekleme veya tüm suite'i gereksiz çalıştırma.
- Ortak mantık: `./gradlew :composeApp:allTests`. Android derleme: `./gradlew :composeApp:assembleDebug`. iOS: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` (yalnızca macOS/CI).
- Bağımlılık değişikliğinde paket kaynağı/sürümünü gözden geçir.
- Çalışmayan/çalıştırılmayan komutları ve kısıtları açıkça yaz (ör. Windows'ta iOS derlenemez). Komutu çalıştırmadıysan başarılı gibi gösterme.
- Domain/parser mantığı değiştiyse ilgili `commonTest` testlerinin güncellenip güncellenmediğini incele; eksik test için bulgu yaz.

## Rapor

Raporu kısa ve net tut; uzun özet, gereksiz kapsam açıklaması veya alan tekrarları yazma.

- **Bulgu varsa:** Yalnızca düzeltilmesi gereken bulguları önem sırasıyla yaz:
  `[Kritik|Yüksek|Orta|Düşük] dosya:satır — Sorun, gerçekleşme koşulu/etkisi ve uygulanabilir düzeltme.`
- **Bulgu yoksa:** Yalnızca `"Bulgu yok."` yaz (commit/push da istenmişse commit hash ve push durumunu tek satırda ekle).
- Kritik/Yüksek bulgu varken commit atma.
