# Privacy Policy / Gizlilik Politikası — Kilom

**Last Updated / Son Güncelleme:** October 8, 2026 / 8 Ekim 2026  
**Application / Uygulama:** Kilom  
**Developer / Geliştirici:** Neslişah Çelek  
**Contact / İletişim:** [neslisah.celek@outlook.com](mailto:neslisah.celek@outlook.com)  
**Live Policy URL / Canlı Web Bağlantısı:** [https://neslisahcelek.github.io/kilom/](https://neslisahcelek.github.io/kilom/)

---

## Table of Contents / İçindekiler

1. [English Version](#english-version)
   - [1. Introduction & Core Privacy Principles](#1-introduction--core-privacy-principles)
   - [2. Information We Do NOT Collect (Zero Data Collection)](#2-information-we-do-not-collect-zero-data-collection)
   - [3. Device Permissions & On-Device Processing](#3-device-permissions--on-device-processing)
   - [4. Data Storage & Local Retention](#4-data-storage--local-retention)
   - [5. Third-Party Services, SDKs & Analytics](#5-third-party-services-sdks--analytics)
   - [6. Children's Privacy (COPPA & GDPR Compliant)](#6-childrens-privacy-coppa--gdpr-compliant)
   - [7. User Rights and Data Deletion (GDPR / CCPA / KVKK)](#7-user-rights-and-data-deletion-gdpr--ccpa--kvkk)
   - [8. App Store & Google Play Declarations](#8-app-store--google-play-declarations)
   - [9. Changes to This Privacy Policy](#9-changes-to-this-privacy-policy)
   - [10. Contact Us](#10-contact-us)
2. [Türkçe Sürüm](#türkçe-sürüm)
   - [1. Giriş ve Temel Gizlilik İlkesi](#1-giriş-ve-temel-gizlilik-ilkesi)
   - [2. Toplanmayan Veriler (Sıfır Veri Toplama)](#2-toplanmayan-veriler-sıfır-veri-toplama)
   - [3. Cihaz İzinleri ve Cihaz Üzerinde İşleme (On-Device OCR)](#3-cihaz-izinleri-ve-cihaz-üzerinde-işleme-on-device-ocr)
   - [4. Veri Depolama ve Yerel Saklama](#4-veri-depolama-ve-yerel-saklama)
   - [5. Üçüncü Taraf Servisler, Reklamlar ve Analitikler](#5-üçüncü-taraf-servisler-reklamlar-ve-analitikler)
   - [6. Çocukların Gizliliği](#6-çocukların-gizliliği)
   - [7. Kullanıcı Hakları ve Veri Silme (KVKK / GDPR Uyumu)](#7-kullanıcı-hakları-ve-veri-silme-kvkk--gdpr-uyumu)
   - [8. App Store ve Google Play Mağaza Beyanları](#8-app-store-ve-google-play-mağaza-beyanları)
   - [9. Bu Politikadaki Değişiklikler](#9-bu-politikadaki-değişiklikler)
   - [10. İletişim](#10-iletişim)

---

# English Version

## 1. Introduction & Core Privacy Principles

Welcome to **Kilom**. Kilom is designed with a **privacy-first, on-device architecture**.

We believe that your personal health and weight tracking data belongs exclusively to you. Kilom does not require you to create an account, log in, or transmit any data across the internet.

**Summary of Our Privacy Guarantee:**
- We do **not** collect, store, sell, or share any personal information.
- All weight entries, dates, and settings remain stored **only on your device**.
- Photos taken or selected for scale reading are analyzed **100% locally on your device** and are never sent to external servers.
- The app operates completely **offline** and contains **no tracking, no analytics, and no advertisements**.

---

## 2. Information We Do NOT Collect (Zero Data Collection)

When you use Kilom:
- **No Personal Identifiers:** We do not collect your name, email address, phone number, physical address, or device unique identifiers (IDFA, AAID).
- **No Health Data Off-Device:** Your body weight measurements, dates, and progress differences are stored exclusively in your device's local storage.
- **No Account Information:** Kilom has no user accounts, passwords, or cloud profiles.
- **No Location Data:** We never request, access, or track your GPS or network location.
- **No Financial Data:** Kilom does not process payments or collect payment credentials.

---

## 3. Device Permissions & On-Device Processing

Kilom requests limited runtime permissions solely to provide core on-device scanning functionality:

### A. Camera (`android.permission.CAMERA` / `NSCameraUsageDescription`)
- **Purpose:** Used only when you choose to scan your bathroom scale display using the in-app camera scanner.
- **Usage Description (iOS):** *"Kilom uses the camera to read your weight from a photo of the scale."*
- **Privacy Assurance:** The camera stream and captured photos are processed in temporary memory using native on-device optical character recognition (OCR) and immediately discarded. Images are **never** transmitted over a network or stored on any server.

### B. Photo Library / Gallery (`NSPhotoLibraryUsageDescription`)
- **Purpose:** Used only if you choose an existing photo of a scale display from your photo gallery.
- **Usage Description (iOS):** *"Kilom reads your weight from a scale photo you choose from your library."*
- **Privacy Assurance:** Only the specific image you select is accessed by the application. Kilom does not browse, index, or upload your photo library.

### C. On-Device OCR Technology
- On **iOS**, text recognition is performed using Apple's native **Vision Framework** (`VNRecognizeTextRequest`).
- On **Android**, text recognition is performed using **Google ML Kit Text Recognition** in on-device mode.
- Neither framework transmits photos or OCR results to external servers.

---

## 4. Data Storage & Local Retention

- **Local Storage:** All user data (weight entries, timestamps, and preferences) is stored locally on your device in secure, sandboxed application storage.
- **No Cloud Synchronization:** Kilom does not synchronize your data with cloud servers or third-party storage providers.
- **Backup:** Your data is backed up only according to your own operating system backup policies (e.g., encrypted local iTunes/Finder backup or standard Android backup).

---

## 5. Third-Party Services, SDKs & Analytics

- **No Third-Party Analytics:** We do not use Google Analytics, Firebase Analytics, Mixpanel, or any user telemetry SDKs.
- **No Advertising Networks:** Kilom is 100% ad-free. No advertising identifiers (IDFA, GAID) are read or shared.
- **No Third-Party Trackers:** No tracking libraries, marketing pixels, or social media SDKs are integrated into Kilom.
- **No Server Infrastructure:** We do not operate application servers that receive user activity data.

---

## 6. Children's Privacy (COPPA & GDPR Compliant)

Kilom is suitable for general audiences and complies fully with the Children's Online Privacy Protection Act (**COPPA**) in the United States and the General Data Protection Regulation (**GDPR**) in the European Union.

Because Kilom does not collect, retain, or transmit personal information from any user, we do not knowingly collect personal information from children under 13 (or under 16 in certain European jurisdictions).

---

## 7. User Rights and Data Deletion (GDPR / CCPA / KVKK)

Under international data protection regulations (including GDPR, CCPA, and Turkish KVKK):
- **Right to Access:** You can view all your stored weight entries directly inside the Kilom app at any time.
- **Right to Erasure:** You can delete individual weight records inside the app. To delete all data, you can uninstall the app or clear app data in your device settings. Since no data is stored on remote servers, deleting the app permanently removes all associated data.
- **Data Control:** Because all data is stored locally, you remain in complete control of your data at all times.

---

## 8. App Store & Google Play Declarations

For review transparency and user disclosure:

### Apple App Store Privacy Nutrition Label
- **Data Used to Track You:** None
- **Data Linked to You:** None
- **Data Not Linked to You:** None
- **Classification:** **"Data Not Collected"** (Kilom does not collect any data from this app).

### Google Play Data Safety
- **Does the app collect or share user data?** **No**.
- **Is all user data collected by this app encrypted in transit?** Not applicable (no data collected or transmitted).
- **Can users request that data be deleted?** Yes, by deleting entries within the app or uninstalling the app.

---

## 9. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect improvements or regulatory changes. Any modifications will be posted to this page with an updated "Last Updated" date.

---

## 10. Contact Us

If you have questions, feedback, or concerns regarding this Privacy Policy or Kilom's privacy practices, please contact us:

- **Developer:** Neslişah Çelek
- **Email:** [neslisah.celek@outlook.com](mailto:neslisah.celek@outlook.com)
- **GitHub Repository:** [https://github.com/neslisahcelek/Kilom](https://github.com/neslisahcelek/Kilom)

---

# Türkçe Sürüm

## 1. Giriş ve Temel Gizlilik İlkesi

**Kilom** uygulamasına hoş geldiniz. Kilom, gizliliği merkeze alan ve tamamen cihaz üzerinde çalışan (on-device) bir mimariyle geliştirilmiştir.

Kişisel kilo ve sağlık verilerinizin yalnızca size ait olduğuna inanıyoruz. Kilom; hesap açmanıza, giriş yapmanıza veya internet üzerinden herhangi bir veri aktarmanıza gerek kalmayacak şekilde tasarlanmıştır.

**Gizlilik Güvencemizin Özeti:**
- Kişisel bilgilerinizi **toplamıyoruz, saklamıyoruz, satmıyoruz ve paylaşmıyoruz**.
- Tüm kilo kayıtları, tarihler ve tercihler **yalnızca kendi cihazınızda** saklanır.
- Tartı ekranından kilo okumak için çekilen veya seçilen fotoğraflar **%100 yerel olarak cihazınızda** işlenir; hiçbir sunucuya iletilmez.
- Uygulama tamamen **çevrimdışı (offline)** çalışır; **reklam, analitik araç ve üçüncü taraf izleyici içermez**.

---

## 2. Toplanmayan Veriler (Sıfır Veri Toplama)

Kilom uygulamasını kullanırken:
- **Kişisel Kimlik Bilgileri Toplanmaz:** Ad, soyad, e-posta adresi, telefon numarası, ev adresi veya cihaz kimlikleri (IDFA, AAID) alınmaz.
- **Sağlık Verileri Cihaz Dışına Çıkmaz:** Vücut ağırlığı ölçümleriniz, kilo değişim farklarınız ve geçmiş kayıtlarınız yalnızca cihazınızın yerel hafızasında tutulur.
- **Hesap Bilgisi Yoktur:** Kilom'da kullanıcı hesabı veya girişi bulunmaz. Parola, profil veya bulut hesabı yoktur.
- **Konum Bilgisi Alınmaz:** GPS veya ağ tabanlı konumunuza hiçbir zaman erişilmez ve konum takibi yapılmaz.
- **Finansal Bilgi İstenmez:** Uygulama ödeme bilgilerinizi veya kart verilerinizi toplamaz.

---

## 3. Cihaz İzinleri ve Cihaz Üzerinde İşleme (On-Device OCR)

Kilom, yalnızca tartı ekranı okuma işlevini yerine getirebilmek için işletim sisteminden asgari düzeyde izin talep eder:

### A. Kamera İzni (`android.permission.CAMERA` / `NSCameraUsageDescription`)
- **Kullanım Amacı:** Yalnızca uygulama içerisindeki tartı tarama özelliğini kullanarak tartı ekranının fotoğrafını çekmek istediğinizde kullanılır.
- **iOS İzin Gerekçesi:** *"Kilom uses the camera to read your weight from a photo of the scale."* (Kilom, tartı fotoğrafınızdan kilonuzu okumak için kamerayı kullanır.)
- **Gizlilik Güvencesi:** Kamera akışı ve çekilen fotoğraf, yalnızca anlık bellekte (RAM) yerel metin okuma için işlenir ve işlem bittiğinde bellekten silinir. Görseller asla bir sunucuya yüklenmez veya saklanmaz.

### B. Fotoğraf Galerisi İzni (`NSPhotoLibraryUsageDescription`)
- **Kullanım Amacı:** Yalnızca galerinizde önceden bulunan bir tartı fotoğrafını seçerek kilonuzu otomatik okutmak istediğinizde kullanılır.
- **iOS İzin Gerekçesi:** *"Kilom reads your weight from a scale photo you choose from your library."* (Kilom, galerinizden seçtiğiniz tartı fotoğrafından kilonuzu okur.)
- **Gizlilik Güvencesi:** Yalnızca seçtiğiniz görsel işlenir. Fotoğraf kütüphaneniz taranmaz, yedeklenmez ve dışarıya aktarılmaz.

### C. Cihaz Üzerinde Optik Karakter Tanıma (On-Device OCR)
- **iOS'ta:** Metin okuma işlemi doğrudan Apple'ın yerel **Vision Framework** (`VNRecognizeTextRequest`) motoruyla cihaz donanımında gerçekleştirilir.
- **Android'de:** Metin okuma işlemi doğrudan **Google ML Kit Text Recognition** motorunun cihaz içi (on-device) kütüphanesiyle çalışır.
- Her iki sistemde de fotoğraflar internet bağlantısına ihtiyaç duymadan, sıfır veri transferi ile cihazın kendi işlemcisi üzerinde analiz edilir.

---

## 4. Veri Depolama ve Yerel Saklama

- **Yerel Depolama:** Tüm tartı kayıtlarınız, tarih damgaları ve kullanıcı tercihleriniz cihazınızın güvenli yerel depolama alanında tutulur.
- **Bulut Senkronizasyonu Yok:** Kilom, verilerinizi hiçbir bulut sunucusuna veya harici depolama sağlayıcısına aktarmaz.
- **Yedekleme:** Verileriniz yalnızca cihazınızın kendi yerel işletim sistemi yedekleme politikalarına tabidir (örneğin yerel şifrelenmiş iTunes/Finder yedeği veya standart Android sistem yedeği).

---

## 5. Üçüncü Taraf Servisler, Reklamlar ve Analitikler

- **Analitik Araç Yok:** Google Analytics, Firebase Analytics, Mixpanel gibi kullanıcı davranışını takip eden hiçbir telemetri veya analitik kütüphanesi bulunmaz.
- **Reklam Ağı Yok:** Kilom %100 reklamsızdır. Reklam kimlikleri (IDFA, GAID) okunmaz ve reklam ağlarıyla paylaşılmaz.
- **İzleyici (Tracker) Yok:** Facebook SDK, pazarlama pikselleri veya harici takip kodları projede yer almaz.
- **Sunucu Altyapısı Yok:** Kullanıcı verilerini kaydeden veya işleyen hiçbir özel sunucu işletilmemektedir.

---

## 6. Çocukların Gizliliği

Kilom genel kullanıcı kitlesine yöneliktir ve 6698 sayılı KVKK, ABD COPPA (Çocukların Çevrimiçi Gizliliğini Koruma Yasası) ile AB GDPR düzenlemelerine tam uyumludur.

Uygulama hiçbir kullanıcıdan kişisel veri toplamadığı için, 13 (veya ilgili bölgede 16) yaşından küçük çocuklara ait herhangi bir kişisel veri de toplanmamaktadır.

---

## 7. Kullanıcı Hakları ve Veri Silme (KVKK / GDPR Uyumu)

6698 sayılı Kişisel Verilerin Korunması Kanunu (KVKK) ve Avrupa Genel Veri Koruma Tüzüğü (GDPR) kapsamında:
- **Verilere Erişim Hakkı:** Kaydettiğiniz tüm kilo verilerini uygulama içerisindeki geçmiş listesinden dilediğiniz an görebilirsiniz.
- **Silme Hakkı:** İstediğiniz kilo kaydını uygulama içinden silebilirsiniz. Uygulamaya ait tüm verileri kalıcı olarak silmek için uygulamayı cihazınızdan kaldırmanız veya uygulama verilerini temizlemeniz yeterlidir. Sunucularda hiçbir veriniz tutulmadığından, uygulamayı sildiğinizde tüm veriler tamamen silinmiş olur.
- **Veri Kontrolü:** Verileriniz tamamen kendi cihazınızda kaldığı için verilerinizin kontrolü her zaman sizdedir.

---

## 8. App Store ve Google Play Mağaza Beyanları

Mağaza inceleme süreçleri için standart beyanlar:

### Apple App Store Gizlilik Kartı (Nutrition Label)
- **Sizi Takip Etmek İçin Kullanılan Veriler:** Yok
- **Sizinle İlişkilendirilen Veriler:** Yok
- **Sizinle İlişkilendirilmeyen Veriler:** Yok
- **Kategori:** **"Veri Toplanmaz" (Data Not Collected)**

### Google Play Veri Güvenliği (Data Safety)
- **Uygulama kullanıcı verisi topluyor veya paylaşıyor mu?** **Hayır**.
- **Veriler aktarım sırasında şifreleniyor mu?** Uygulanabilir değil (veri aktarımı ve toplanması yoktur).
- **Kullanıcı veri silme talebinde bulunabilir mi?** Evet, uygulama içinden kayıt silerek veya uygulamayı kaldırarak.

---

## 9. Bu Politikadaki Değişiklikler

Yasal düzenlemeler değiştikçe veya yeni özellikler eklendikçe bu Gizlilik Politikası güncellenebilir. Değişiklikler bu sayfada "Son Güncelleme" tarihiyle birlikte yayımlanır.

---

## 10. İletişim

Bu Gizlilik Politikası ile ilgili her türlü soru, öneri veya bilgi talebi için bizimle iletişime geçebilirsiniz:

- **Geliştirici:** Neslişah Çelek
- **E-posta:** [neslisah.celek@outlook.com](mailto:neslisah.celek@outlook.com)
- **GitHub Deposu:** [https://github.com/neslisahcelek/Kilom](https://github.com/neslisahcelek/Kilom)
