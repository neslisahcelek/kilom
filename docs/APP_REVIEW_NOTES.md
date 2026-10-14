# Kilom – App Review Notes & Apple Yanıtı

Bağlam: v1.0 (5) Guideline **4.2 (Minimum Functionality)** ve **2.5.1 (HealthKit'in arayüzde açıkça belirtilmemesi)** nedeniyle reddedildi. v1.0 (6) bu iki maddeyi ele alır.

---

## 1. App Review Notes (App Store Connect → "Notes")

```text
Kilom is an on-device weight tracker. No account or login is required; there is no demo account.

HEALTHKIT (Guideline 2.5.1)
- Kilom uses HealthKit to (1) WRITE the user's weight entries to Apple Health and (2) READ (import) the user's past weight entries from Apple Health. Nothing else is read or written.
- HealthKit is clearly identified in the UI:
  • First launch: an onboarding screen explains why Kilom uses Apple Health, shown before the system permission dialog.
  • Main screen: an "Apple Health" badge shows the connection status.
  • Settings/Backup: Apple Health import is available from within the app.
- Health data never leaves the device. It is not used for advertising, marketing, analytics or data mining, and is not shared with third parties.
- Usage strings: NSHealthShareUsageDescription / NSHealthUpdateUsageDescription.

MINIMUM FUNCTIONALITY (Guideline 4.2)
Kilom offers more than a simple entry form:
- Scale photo OCR (Apple Vision, on-device), including 7-segment displays; the result must be confirmed by the user before saving.
- Apple Health write + import of past weight entries.
- Goal weight with progress and estimated arrival date.
- Weekly / monthly summary card and trend chart.
- Optional daily reminder (local notification only).
- CSV / JSON export and restore.
- kg / lb support, liquid-glass native-feeling UI with haptics.

PRIVACY
- 100% on-device: no servers, no accounts, no analytics, no ads, no third-party trackers. Scale photos are processed in memory only and are not stored.
- Notifications are local; no push service is used.

HOW TO TEST
1. Launch the app → complete the onboarding → allow Apple Health access in the system dialog.
2. On the main screen, note the Apple Health badge.
3. Add a weight (manually or via the camera/photo button) → open the Health app → Browse → Body Measurements → Weight: the entry appears.
4. In Kilom, use Apple Health import to bring past weights from Health.
5. Set a goal weight → see progress and estimated date; view the weekly/monthly summary card.
6. Enable the daily reminder and grant notification permission.

Contact: neslisah.celek@outlook.com
```

---

## 2. Apple'a Yanıt Metni / Reply to App Review

### English
```text
Hello App Review team,

Thank you for your feedback on Kilom v1.0 (5). We have addressed both points in version 1.0 (6).

Guideline 2.5.1 – HealthKit:
Kilom uses HealthKit to write the user's weight entries to Apple Health and to import (read) their past weight entries. HealthKit is now clearly identified in the app:
- An onboarding screen explains the Apple Health usage before the permission dialog appears.
- An "Apple Health" badge on the main screen shows the connection status.
- Apple Health import is available inside the app, and access can be managed in iOS Settings > Health.
Health data stays on the device and is never used for advertising, analytics or shared with third parties. The purpose strings in Info.plist have also been updated to describe both read and write usage.

Guideline 4.2 – Minimum Functionality:
Version 1.0 (6) adds substantial functionality beyond manual entry:
- Apple Health import of past weight entries (in addition to writing)
- Goal weight with progress and estimated arrival date
- Weekly / monthly summary card
- Optional daily local reminder
- Plus existing on-device scale photo OCR (including 7-segment displays), trend chart, CSV/JSON backup and kg/lb support

A screen recording captured on a physical device, demonstrating onboarding, the Health permission dialog, the Health badge, a new entry appearing in the Health app, import, and the goal/summary/reminder features, is attached. Detailed notes are in the App Review Information section.

Thank you for your time.
Neslişah Çelek
```

### Türkçe
```text
Merhaba App Review ekibi,

Kilom v1.0 (5) hakkındaki geri bildiriminiz için teşekkürler. Her iki maddeyi 1.0 (6) sürümünde ele aldık.

Guideline 2.5.1 – HealthKit:
Kilom, kullanıcının kilo kayıtlarını Apple Sağlık'a yazmak ve geçmiş kilo kayıtlarını içe aktarmak (okumak) için HealthKit kullanır. HealthKit artık uygulamada açıkça belirtilmektedir:
- İzin diyaloğundan önce Apple Sağlık kullanımını açıklayan bir onboarding ekranı gösterilir.
- Ana ekrandaki "Apple Sağlık" rozeti bağlantı durumunu gösterir.
- Apple Sağlık içe aktarma uygulama içinden yapılır; erişim iOS Ayarlar > Sağlık'tan yönetilebilir.
Sağlık verileri cihazda kalır; reklam veya analitik için kullanılmaz, üçüncü taraflarla paylaşılmaz. Info.plist izin metinleri de hem okuma hem yazma kullanımını açıklayacak şekilde güncellendi.

Guideline 4.2 – Minimum İşlevsellik:
1.0 (6), manuel girişin ötesinde kayda değer işlevler ekler:
- Geçmiş kilo kayıtlarının Apple Sağlık'tan içe aktarılması (yazmaya ek olarak)
- İlerleme ve tahmini ulaşma tarihi ile hedef kilo
- Haftalık / aylık özet kartı
- İsteğe bağlı günlük yerel hatırlatıcı
- Mevcut cihaz içi tartı fotoğrafı OCR (7 segment dahil), trend grafiği, CSV/JSON yedek ve kg/lb desteği

Onboarding, Sağlık izin diyaloğu, Sağlık rozeti, Sağlık uygulamasında görünen yeni kayıt, içe aktarma ve hedef/özet/hatırlatıcı özelliklerini gösteren fiziksel cihaz ekran kaydı ektedir. Ayrıntılı notlar App Review Information bölümündedir.

Teşekkürler.
Neslişah Çelek
```

---

## 3. Fiziksel Cihaz Ekran Kaydı Senaryosu (Screen Recording Script)

Fiziksel iPhone'da, uygulamayı temiz kurulumla başlatarak tek kayıtta çekin (~60–90 sn):

1. **Onboarding:** Uygulamayı ilk kez aç; Apple Sağlık açıklama ekranını göster.
2. **Health izin diyaloğu:** Sistem izin diyaloğunu göster; Kilo için okuma ve yazmaya izin ver.
3. **Ana ekran rozeti:** Ana ekrandaki Apple Sağlık rozetini yakınlaştırarak/yavaşça göster.
4. **Kayıt ekle:** Elle (veya fotoğraf/kamera ile, onay adımı dahil) bir kilo ekle.
5. **Health uygulamasında doğrula:** Sağlık uygulaması → Gözat → Vücut Ölçümleri → Ağırlık: yeni kaydın göründüğünü göster.
6. **İçe aktarma:** Kilom'da Apple Sağlık içe aktarmayı çalıştır; geçmiş kayıtların listeye geldiğini göster.
7. **Hedef / özet / hatırlatıcı:** Hedef kilo belirle (ilerleme ve tahmini tarih), haftalık/aylık özet kartını göster, günlük hatırlatıcıyı aç ve bildirim izni ver.

> İpucu: Kayıttan önce Sağlık'ta Kilom izinlerini sıfırlayın (Ayarlar > Sağlık > Veri Erişimi ve Cihazlar > Kilom) ki izin diyaloğu görünsün. Kayıt gerçek cihazdan alınmalı, simülatörden değil.

---

## 4. Gönderim Öncesi Kontrol

- [ ] Info.plist: `NSHealthShareUsageDescription` ve `NSHealthUpdateUsageDescription` güncel metinlerle (HealthKit capability ve entitlement açık).
- [ ] Uygulamadaki Health rozeti/onboarding gerçekten release build'de görünüyor.
- [ ] App Privacy etiketi: "Data Not Collected" (veri cihaz dışına çıkmaz) — HealthKit verileri geliştirici tarafından toplanmaz.
- [ ] `docs/STORE_LISTING.md` metinleri App Store Connect'e girildi (Health kullanımı açıklamada belirtilmiş).
- [ ] Gizlilik politikası URL'si güncel sürümü gösteriyor: https://neslisahcelek.github.io/kilom/
