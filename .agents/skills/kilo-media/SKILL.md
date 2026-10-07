---
name: kilo-media
description: Kilo'daki tartı fotoğrafı seçme/çekme, hazırlama ve OCR akışlarını uygular veya inceler. FileKit picker, görsel dönüşümü (EXIF), ScaleOcr ve WeightParser değişikliklerinde kullan.
---

# Kilo görsel ve OCR akışları

Önce mevcut picker ve OCR çağrısını `ui/`, `ocr/`, `androidMain` ve `iosMain` boyunca izle. Repodaki gerçek yolları doğrula. Temel mimari için [kilo-architecture](../kilo-architecture/SKILL.md) skill'ini kullan.

## Piksel ve platform

- Foto `FileKit` ile (kamera + galeri) alınır. Picker gerçek seçme/iptal akışı sağlamalı; iptal hata gibi ele alınmamalı. Android çözümünü `commonMain`'e taşıma.
- Android'de ham decode sonrası EXIF bilgisini atan yeniden JPEG üretimi yön/ayna bilgisini kaybettirir. Decode API'si bunu zaten uygulamıyorsa EXIF dönüşünü ve aynalamayı piksele uygula; iki kez döndürme. Yanlış yönlü görsel OCR doğruluğunu düşürür.
- Büyük kaynak görseli tam boy belleğe almadan önce boyutlarını oku ve OCR için makul bir hedefe göre örnekle (7-segment ekranlar için aşırı küçültme okunabilirliği bozar). Stream'leri kapat; yalnızca sahip olduğun, artık kullanılmayan geçici bitmap'leri geri bırak.
- iOS'ta kullanılan API'nin gerektirdiği izin açıklamalarını doğrula: `NSCameraUsageDescription`, `NSPhotoLibraryUsageDescription` (`iosApp/.../Info.plist`). Android'de `CAMERA` izni ve reddedilme durumunu ele al.
- EXIF okunamamasını piksel decode edilememesinden ayır; okunabilir görseli yalnızca EXIF yok diye reddetme.

## OCR sözleşmesi

- `ScaleOcr` (`expect`): Android'de ML Kit Text Recognition, iOS'ta Apple Vision (`VNRecognizeTextRequest`). İkisi de cihaz içinde, offline çalışır; ağ çağrısı ekleme. Ham satırları `List<String>` olarak döndürür.
- Ayıklama ortak `WeightParser` içindedir: `\d{2,3}[.,]\d` regex, kg/lb etiketi yakınına bonus, `O→0` düzeltmesi, 20–500 kg aralığı dışını eleme (lb ise kg'ye çevrilip kontrol). Parser saf kalmalı ve `commonTest`'te test edilmeli; yeni tartı biçimi için önce başarısız test örneği ekle.
- Sonuç giriş alanına **önceden doldurulur**, kullanıcı onayı zorunludur; otomatik kaydetme yok. Başarıda haptic verilir. Okunamayan/boş sonuç kullanıcıya anlaşılır, lokalize mesajla (EN + TR) bildirilir ve manuel girişe izin verir.

## Seçimden kayda sözleşme

- Hazırlama/OCR tamamlanmadan önce onay/kaydet aksiyonu yanlış veriyle çalışmamalı; ViewModel kaydet yolu eksik/geçersiz kg ile kaydı önlemeli.
- Kullanıcı ikinci foto seçer, sheet'i kapatır veya iptal ederse önceki OCR sonucu yeni durumu ezmemeli. İptal ve seçim nesli/sürümü kontrolü kullan; sonucu uygulamadan önce doğrula.
- Alanlar ve OCR sonucu ViewModel state'inde tutulur. Picker görünür gibi geçici UI state'i ayrı kalabilir.
- Foto yalnızca OCR için işlenir; gereksiz kalıcı saklama veya paylaşım yapma.

## Doğrulama

- Değişen platformları derle (`:composeApp:assembleDebug`; iOS yalnızca macOS/CI'da). Cihaz doğrulaması gerekiyorsa şu senaryoları seç: döndürülmüş/aynalı foto, büyük görsel, picker iptali, hızlı ikinci seçim, OCR sürerken sheet kapatma, izin reddi.
- Yalnızca derleme yapılmışsa EXIF/galeri/OCR akışı cihazda doğrulanmış gibi raporlama; Windows'ta iOS doğrulanamadığını belirt. Review talebinde bulgu bildir; açık düzeltme yetkisi yoksa kod değiştirme.
