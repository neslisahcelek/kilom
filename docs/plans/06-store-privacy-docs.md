# 06 – Mağaza Metni, Gizlilik, Info.plist, Review Notları — Faz 1 (kod dosyalarına dokunmaz)

## Değişiklikler
1. **`iosApp/iosApp/Info.plist`**: `NSHealthShareUsageDescription` → "Kilom reads your past weight entries from Apple Health so you can import them. Data stays on your device."; `NSHealthUpdateUsageDescription` → "Kilom saves your weight entries to Apple Health. Data stays on your device." (Okuma eklendiği için ikisi de kalır.)
2. **`docs/STORE_LISTING.md`** (TR + EN, Apple + Play): Apple Health entegrasyonu (yazma + içe aktarma), hedef kilo & tahmini tarih, haftalık/aylık özet, günlük hatırlatıcı, 7-segment/tartı OCR, trend grafiği, CSV/JSON yedek. "Sadece sade" yerine değer odaklı anlatım. Karakter limitlerine uy (subtitle 30, promo 170, keywords 100). Apple açıklamasında Health kullanımını açıkça belirt.
3. **`PRIVACY_POLICY.md` + `docs/index.html`**: Apple Health'e yazma/okuma, hedef & hatırlatıcı yerel veriler, hiçbir veri sunucuya gitmez, bildirimler yereldir, analitik/reklam yok. Mevcut içerikle tutarlı ton; tarihi güncelle.
4. **`docs/APP_REVIEW_NOTES.md`** (yeni): App Review Notes taslağı + Apple'a yanıt metni (TR/EN) + fiziksel cihaz ekran kaydı senaryosu:
   Onboarding → Health izin diyaloğu → ana ekranda Health rozeti → bir kayıt ekle → Health uygulamasında görünsün → içe aktarma → hedef/özet/hatırlatıcı.
   Yanıt metni: HealthKit'in uygulamada nerede göründüğünü (ana ekran rozeti, onboarding, ayarlar) belirtir; v1.0 (6) yeni özellikleri listeler.
5. **`README.md`**: özellik listesine yeni maddeler.

## Kabul
Tüm metinler EN+TR tutarlı; Health ifadesi gerçek davranışla uyumlu; `.agents/skills/kilo-store-checklist` maddeleriyle çelişki yok.
