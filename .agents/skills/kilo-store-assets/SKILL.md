---
name: kilo-store-assets
description: Kilom için Apple (2026) seviyesi App Store ve Google Play ekran görüntüleri, pazarlama görselleri, Dynamic Island, Titanyum donanım çerçevesi, SF Pro Display gradyan tipografisi, sRGB renk profili ve mağaza çözünürlük standartlarını tanımlar ve üretir.
---

# Kilo Store Assets & Screenshot Standards (Apple 2026)

Bu doküman, **Kilom** projesinin Apple App Store ve Google Play Store için hazırlanan pazarlama görsellerinin, cihaz içi ekran görüntülerinin, donanım çerçevelerinin (Titanyum kasa, Dynamic Island, temiz durum çubuğu) ve tipografik hiyerarşisinin değişmez üretim ve kalite standartlarını belirler.

---

## 1. Temel Tasarım İlkeleri (Apple 2026 Liquid Glass)

1. **Katı Katman & Alfa Kuralı (Asla Düz Beyaz Kutu Yok):**
   - PIL / Pillow ile çizim yaparken yarı saydam cam rozetler (`badge`) veya elemanlar **asla** doğrudan ana `RGBA` tuval üzerine çizilmemelidir. Doğrudan çizim alfa kanalını ezer; dosya `RGB` formatına dönüştürüldüğünde o bölge **tamamen opak beyaz** bir kutuya dönüşür.
   - Tüm cam, rozet, ışık ve parıltı efektleri ayrı bir `Image.new("RGBA", ...)` katmanında oluşturulup `canvas.alpha_composite(...)` ile birleştirilmelidir.

2. **Apple San Francisco Tipografisi & İnci Gradyanı:**
   - Sistem fontları doğrudan düzensiz kalınlıkta kullanılamaz. macOS yerel `SFNS.ttf` değişken fontu (Variable Font) açık eksenlerle (`Weight`, `Optical Size`) yüklenmelidir:
     - Ana Başlık (Headline): `Weight=770` (Display Bold), `Optical Size=72`. Düz tek renk beyaz yerine yukarıdan aşağıya metalik inci gradyanı (`#FFFFFF` $\rightarrow$ `#DCEBFF`) ve yumuşak derinlik gölgesi (`drop shadow`).
     - Alt Başlık (Subtitle): `Weight=510` (Medium), `Optical Size=32`, `#BACCE4` gümüş-arduvaz tonu.
     - Rozet (Kicker Pill): `Weight=660` (Semibold), genişlik `104`, mikro-aralıklı büyük harf.

3. **Her Slayta Özel Sıvı Cam Atmosferi:**
   - Düz gri/siyah arka plan yasaktır. Derin gece yarısı-obsidyen gradyanı (`#070B16` $\rightarrow$ `#0F172A`) üzerine her özelliğin anlamına uygun ışık aurası (caustics) yerleştirilir:
     - **Dashboard / Trend:** Sky Cyan (`#38BDF8`) & Indigo (`#6366F1`)
     - **Apple Vision OCR:** Emerald Mint (`#34D399`) & Ocean Blue (`#0EA5E9`)
     - **DOMS Kas Onarım Kalkanı:** Warm Amber (`#FBBF24`) & Coral Rose (`#F43F5E`)
     - **1RM Göreceli Kuvvet:** Electric Violet (`#A78BFA`) & Royal Blue (`#3B82F6`)
     - **Hormonal Döngü & Beslenme:** Rose Pink (`#F472B6`) & Soft Lavender (`#C084FC`)
     - **İstikrar & Gizlilik:** Teal Mint (`#2DD4BF`) & Sapphire (`#3B82F6`)

---

## 2. Donanım & Çerçeve Standartları

1. **Apple Dynamic Island (Piksel Hassasiyetinde Donanım Kesimi):**
   - Simülatör ekran görüntülerinde Dynamic Island donanımsal olarak yer almaz.
   - Master ekranda (`1320×2868` ölçeği için `372×108 px`, `radius=54`, `center_y=96 px`):
     - Dış anti-aliasing koyu çerçeve (`rgba(28, 32, 42, 95)`).
     - Derin zift siyahı ana ada gövdesi (`#06070A`).
     - Sol iç kısımda TrueDepth kızılötesi sensör modülü (`#0F1118`).
     - Sağ iç kısımda mavi-mor çift yansıma kaplamalı FaceTime HD kamera lensi (`#060912` çekirdek, `#4682EB` ve `#6EAFFA` optik glint).

2. **Apple Resmi `9:41` Durum Çubuğu:**
   - Ekran görüntülerinde rastgele saatler (`20:08` vb.) ve şebeke arama noktaları (`....`) **asla** bulunamaz.
   - Sol üstte San Francisco Semibold fontla `9:41` saati.
   - Sağ üstte 4 tam çeken Apple hücresel şebeke çubuğu, Wi-Fi ikonu ve %100 dolu pil ikonu.
   - Eski saat ve noktaları silme işlemi tek renk blokla değil, arka plandaki yuvarlak kart veya gradyan çizgilerini bozmamak için **dikey doğrusal interpolasyonla** (`y=94..120` arası yumuşak geçiş) yapılmalıdır.

3. **Gerçekçi Titanyum Kasa (Natural / Dark Titanium Bezel):**
   - Dış Titanyum gövde (`#383E4C`, 3px kenarlık, üst kenar speküler ışık çizgisi).
   - Fırçalanmış iç pah halkası (`#1C1F26`).
   - Ekranı çevreleyen derin OLED siyah çerçeve (`bezel`, 18px kalınlık, `screen_radius = 118 * scale`).
   - Üst çerçevede ahize mikro-yarığı (speaker slit).
   - Sol tarafta Eylem Butonu (Action Button) ve Ses Açma/Kapama tuşları; sağ tarafta Güç/Kamera Kontrol tuşu.
   - Cihaz altında çok katmanlı difüz zemin gölgesi (ambient & contact drop shadow).

---

## 3. App Store Connect Çözünürlük ve Doğrulama Standartları

Apple'ın sıkı validator'ünün `"File dimensions are invalid"` veya şeffaflık hatası vermemesi için şu kurallar zorunludur:

1. **Şeffaflık / Alfa Kanalı Kesinlikle YASAKTIR:**
   - Tüm pazarlama görselleri ve `raw` ekran görüntüleri **24-bit RGB** formatında kaydedilmelidir (`hasAlpha: no`).
2. **Renk Profili ve DPI:**
   - Tüm PNG dosyalarına macOS `sips` aracı ile açıkça **`sRGB IEC61966-2.1`** renk profili ve `72 DPI` işlenmelidir.
3. **Piksel Doğruluğunda Boyutlar:**
   - Görseller 1 piksel dahi sapamaz. Her App Store Connect sekmesi için ayrılmış klasör yapısı:

| App Store Connect Sekmesi | Klasör Yolu | Piksel Boyutları (Genişlik × Yükseklik) |
| :--- | :--- | :--- |
| **iPhone 6.9" Display** *(Ana Zorunlu / 16-17 Pro Max)* | `art/app-store/{lang}/iphone-6.9/` | **`1320 × 2868`** |
| **iPhone 6.7" Display** *(14-15 Pro Max)* | `art/app-store/{lang}/iphone-6.7/` | **`1290 × 2796`** |
| **iPhone 6.5" Display** *(12-14 Pro Max & Plus)* | `art/app-store/{lang}/iphone-6.5/` | **`1284 × 2778`** |
| **iPhone 6.5" Display (Alternatif)** *(11 Pro Max / XS Max)* | `art/app-store/{lang}/iphone-6.5-alt/` | **`1242 × 2688`** |
| **iPhone 6.3" Display** *(16-17 Pro)* | `art/app-store/{lang}/iphone-6.3/` | **`1206 × 2622`** |
| **iPhone 6.1" Display** *(14-16 Standart & Pro)* | `art/app-store/{lang}/iphone-6.1/` | **`1179 × 2556`** |
| **iPhone 5.5" Display** *(8 Plus / 7 Plus)* | `art/app-store/{lang}/iphone-5.5/` | **`1242 × 2208`** |

---

## 4. Üretim Komutları

Ekran görüntüleri tek bir Python betiği ile tam otomatik üretilir:

```bash
# 1. Ham ekranları simülatörden çekip tüm boyutlarda pazarlama bannerlarını oluşturma
/Library/Frameworks/Python.framework/Versions/3.12/bin/python3 scripts/capture_and_generate_store_assets.py

# 2. Var olan ham ekranları kullanarak yalnızca bannerları yeniden render etme (hızlı mod)
/Library/Frameworks/Python.framework/Versions/3.12/bin/python3 scripts/capture_and_generate_store_assets.py --render-only

# 3. Dosyaların sRGB profilini, alfa durumunu ve boyutlarını doğrulama
sips -g pixelWidth -g pixelHeight -g hasAlpha -g space -g profile art/app-store/tr/*/1_dashboard.png
```

---

## 5. Bağlam (Context) Koruması ve Kilitlenme Önleme Kuralı (ZORUNLU)

Yüksek çözünürlüklü (`1320×2868`, `1290×2796` vb.) ekran görüntüleri üzerinde çalışırken oturumun şişip (100K+ token) stream timeout nedeniyle asılı kalmasını önlemek için şu kurallar **kesinlikle** uygulanmalıdır:

1. **`view_file` ile Çoklu Görsel Okuma YASAKTIR (Maksimum 2 Görsel Kuralı):**
   - Üretilen 100+ PNG dosyasını doğrulamak için asla döngü halinde `view_file` çağırmayın. Her tam boy PNG okuma çağrısı bağlama binlerce token ekler ve 10+ görselden sonra oturumun yanıt verememesine (hang/timeout) yol açar.
   - Görsel kalite kontrolü gerektiğinde **en fazla 1 veya 2 temsili görseli** (`1_dashboard.png` ve `2_ocr_scanner.png` gibi) `view_file` ile inceleyin.
2. **Boyut, Renk Uzayı ve Alfa Doğrulamasını CLI (`sips`) ile Yapın:**
   - Tüm çözünürlüklerin (`iphone-6.9`, `iphone-6.7`, `iphone-6.5` vb.) piksel boyutu, `hasAlpha: no` ve `sRGB` profili kontrollerini `view_file` yerine terminalde `sips` veya tek satırlık Python betiği ile sıfır görsel token maliyetiyle doğrulayın.
3. **Büyük Görsel Üretim Görevlerini Alt Ajana (`invoke_subagent`) Devredin veya `/compact` Önerin:**
   - Eğer aynı oturumda hem kod değişikliği hem de çoklu görsel/ekran görüntüsü üretimi yapılacaksa, görsel üretim ve doğrulama işini alt ajana (`self`) devredin ki ana oturumun bağlamı temiz kalsın.
   - Oturum 60+ adıma ulaştığında işlemin tamamlandığını hemen özetleyip kullanıcıya `/compact` komutunu önerin.

