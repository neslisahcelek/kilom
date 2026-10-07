---
name: kilo-architecture
description: Kilo'nun KMP, Compose Multiplatform ve katmanlı mimari kurallarını uygular. Özellik tasarlarken, kod yazarken, refactor yaparken veya mimari karar verirken kullan.
---

# Kilo mimari rehberi

Bu skill, varsa repodaki `AGENTS.md` ile birlikte okunur. Çelişki varsa kullanıcı talimatı ve güncel `AGENTS.md` önceliklidir. Önce mevcut örnekleri ve gerçek kaynak yollarını incele; doğrulamadan varsayma. Yeni platform API'si eklerken akışın tüm katmanlarını (common, Android, iOS) takip et.

## Proje yapısı

- Tek ekranlı kilo takip uygulaması: Kotlin Multiplatform + Compose Multiplatform. Kaynaklar `composeApp/src/commonMain`, `androidMain`, `iosMain`; testler `commonTest` (`kotlin.test`). iOS sarmalayıcı: `iosApp/`.
- Paket: `app.kilo`. Katmanlar:
  - `domain/`: `WeightEntry`, `WeightUnit`, delta hesabı. Saf Kotlin, test edilebilir.
  - `data/`: `WeightRepository`, `PrefsRepository` (`multiplatform-settings` + `kotlinx.serialization` JSON).
  - `ocr/`: `ScaleOcr` (`expect`) ve ortak `WeightParser`.
  - `platform/`: `Haptics` (`expect`) gibi platform sarmalayıcıları.
  - `ui/`: ekran, ViewModel, glass bileşenleri, `theme/`.
- DI kütüphanesi yok: bağımlılıklar elle `AppContainer` içinde kurulur. Koin/Hilt ekleme.
- Sunucu, hesap, rol veya Firebase yok; veri yalnızca cihazda tutulur.

## Alan kuralları

- Ağırlık **her zaman kg** saklanır. kg/lb yalnızca gösterim ve girişte çevrilir (lb girişi kg'ye çevrilip doğrulanır); kaydedilen veri lb dönüşümüyle bozulmaz.
- Delta saklanmaz, listeden türetilir: liste `at` azalan sıralı, önceki kayıt sıralamada bir sonraki elemandır; gün farkı yerel saat diliminde tarih bazlı hesaplanır.
- Doğrulama: 20–500 kg aralığı, virgül/nokta kabulü, 1 ondalık.
- Saklanan JSON şeması değişirse eski kayıtlar okunabilmeli (eksik alanlar için varsayılan); bozuk JSON uygulamayı çökertmemeli.

## KMP sınırları

- `commonMain` platformdan bağımsızdır. `android.*`, `platform.*` (iOS), `com.android.*`, `Dispatchers.Main/IO` veya Android lifecycle artifact'ı ekleme. Coroutine işlerini `viewModelScope.launch` ile başlat.
- Flow UI'da `collectAsStateWithLifecycle()` ile toplanır; Compose Multiplatform için gereksiz Swift Flow köprüsü ekleme.
- Her `expect` için Android ve iOS `actual` gerekir (`ScaleOcr`: Android ML Kit Text Recognition, iOS Apple Vision `VNRecognizeTextRequest`; `Haptics`: Android Vibrator/HapticFeedback, iOS `UIImpactFeedbackGenerator`/`UINotificationFeedbackGenerator`). iOS tarafı stub bırakılamaz.
- Bağımlılığı `commonMain`'e eklemeden önce iki platform binary'sinde de çalıştığını doğrula; platform SDK bağımlılıklarını `androidMain`/`iosMain` source set'inde tut.
- Foto seçme/çekme `FileKit` ile yapılır; gerçek iptal/seçim akışı iki platformda çalışmalı. Gerekli izin açıklamaları eklenmeli (bkz. kapanış kontrolü).
- Glass efekti `dev.chrisbanes.haze` (`hazeSource` + `hazeEffect`) ile yapılır; iOS 26 native Liquid Glass'a doğrudan erişim yoktur, görsel yaklaşım haze + ince border + üst specular gradient'tir.

## Katman ve UI state

- Repository veri erişimi yapar; ViewModel UI state ve kullanıcı aksiyonlarını yönetir; Composable state'i gösterip event iletir.
- UI state `StateFlow` içinde tutulur ve güncellenirken `MutableStateFlow.update {}` kullanılır. İş doğrulama ve iş kurallarını Composable'a taşıma; ViewModel iş state'inin sahibidir.
- Depolama/OCR hatalarında UI'ya ham exception taşıma; anlaşılır, lokalize metin/kaynak anahtarı üret. Hata, boş liste veya sıfır değere çevrilmemeli.
- Kullanıcı metinleri repository/domain içinde üretilmez. Compose kaynakları `composeApp/src/commonMain/composeResources/values/strings.xml` (EN) ve `values-tr/strings.xml` (TR) içinde tutulur; yeni string her iki dilde eklenir. Renkler `ui/theme/Colors.kt` üzerinden kullanılır.
- Ekran için mevcut ortak bileşenleri önce ara (`GlassCard`, `HeroCard`, `ActionBar`, `EntrySheet`, `HistoryRow`); yinelenen yapıyı ortaklaştır.
- Lazy list öğelerine stabil `key` ekle (kayıt `id`). Görünürlük/scroll state'ini ViewModel iş state'inin yerine koyma.
- Önemli yeni Compose bileşenlerine Preview ekle.

## OCR akışı

- `ScaleOcr.readText` ham satırları döndürür; ayıklama ortak `WeightParser` içindedir (regex, kg/lb etiketi bonusu, `O→0`, aralık dışını ele). Parser saf kalır ve `commonTest` ile test edilir.
- OCR sonucu giriş alanına **önceden doldurulur**, kullanıcı onayı zorunludur; sonucu doğrudan kaydetme.
- Async OCR sonucu, kullanıcı sheet'i kapatmış veya başka foto seçmişse yeni durumu ezmemeli (iptal/sürüm kontrolü).

## Değişiklik akışı

1. İlgili source set'leri, ViewModel/repository zincirini, platform `actual`'larını ve yakın testleri keşfet.
2. Planda etkilenen platformları ve depolanan veri uyumluluğunu belirle.
3. En küçük tutarlı değişikliği uygula; string/renk/ikonları merkezileştir.
4. Sadece değişen alana uygun doğrulamayı çalıştır (`./gradlew :composeApp:allTests`, `:composeApp:assembleDebug`; iOS için `:composeApp:linkDebugFrameworkIosSimulatorArm64` yalnızca macOS'ta). Kullanıcı test istemediyse gereksiz tam suite çalıştırma.
5. Derleme/doğrulamayı çalıştırmadıysan sonucu açıkça belirt; çalışmış gibi raporlama. Windows'ta iOS derlenemez; iOS doğrulanmadıysa bunu yaz.

Foto seçme/hazırlama/OCR görsel hazırlığı değişikliğinde [kilo-media](../kilo-media/SKILL.md) rehberini kullan.

## Kapanış kontrolü

- `commonMain` platform bağımsız mı; her `expect` için Android ve iOS `actual` tamam mı?
- State tek yönlü mü; ViewModel iş state'inin sahibi mi?
- Strings (EN + TR), renkler, ortak bileşenler ve stabil lazy key'ler kullanıldı mı?
- kg her zaman kg saklanıyor, delta türetiliyor mu; JSON geriye dönük uyumlu mu?
- Gerekli iOS `Info.plist` izin metinleri (`NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription`) ve Android `CAMERA` izni/manifest güncellemeleri yapıldı mı?
- İlgili `commonTest` testleri (delta, gün farkı, parser) güncel mi?
