---
name: kilo-health-nlg
description: >-
  Kilom'un %100 cihaz içi, gizlilik odaklı Parametrik Doğal Dil Üretim (NLG - Natural Language Generation) ve Veri Hikayesi (Data Storytelling) becerisi. Sayısal sağlık verilerini, literatür beklentileriyle kıyaslayan, kişiye özel, sıfır halüsinasyonlu ve yumuşak koçluk diliyle yazılmış akıllı metinlere dönüştürmek için kullan.
---

# Kilo Cihaz İçi Doğal Dil Üretim (NLG) ve Veri Hikayesi Becerisi (`kilo-health-nlg`)

Bu beceri, **Kilom** projesinde ham kullanıcı verilerini (tartı geçmişi, menstrüel döngü fazları, antrenman tonajı, PR dağılımı, DOMS ödemi) **sabit ve buyurgan kalıp cümleler yerine**, kullanıcının gerçek verisini literatür beklentisiyle kıyaslayan **kişiselleştirilmiş veri hikayelerine (Data Stories)** dönüştürme standartlarını belirler.

---

## 1. Neden Hazır Bulut LLM Yerine Parametrik NLG?

| Kriter | Bulut LLM (Gemini/OpenAI API) | Cihaz İçi Parametrik NLG (Kilom Motoru) |
| :--- | :--- | :--- |
| **Gizlilik** | ❌ Regl ve kilo verisi sunucuya gider |  **%100 Cihaz İçi, Sıfır Veri Çıkışı** |
| **Sayısal Doğruluk** | ❌ Halüsinasyon riski (sayıları karıştırabilir) |  **%100 Matematiksel Kesinlik (Sıfır Hata)** |
| **Çevrimdışı Çalışma** | ❌ İnternet bağlantısı zorunlu |  **Uçak modunda / Salonda anında çalışır** |
| **Gecikme (Latency)** | ❌ 1000–3000 ms ağ gecikmesi |  **0 ms (Anında Composable render)** |
| **Uygulama Boyutu** | ❌ Cihaz içi SLM için +1.5–2 GB model |  **0 MB ek boyut (Saf Kotlin kodu)** |
| **Donanım Uyumu** | ❌ Yalnızca son model NPU'lu telefonlar |  **En eski Android ve iPhone'da bile akıcı** |

> [!NOTE]
> Apple Health, WHOOP ve Oura Ring gibi giyilebilir teknoloji ve dijital sağlık devleri, günlük içgörü metinlerinde serbest LLM yerine bu deterministik NLG karar ağaçlarını kullanır.

---

## 2. Üç Katmanlı Veri Hikayesi Mimarisi (Expected vs. Observed)

Her akıllı sağlık içgörüsü mutlaka 3 katmandan sentezlenir:

```
[1. Gözlem (Observed)]       --> Kullanıcının gerçek verileri, zirve ve ikincil evreleri.
[2. Kıyaslama (Comparative)] --> Spor bilimi literatürünün (Expected) teorisi ile fark/uyum tespiti.
[3. Yumuşak Yorum (Insight)] --> Bireysel farklılığı takdir eden, destekleyici ve esnek koçluk dili.
```

### 1. Katman: Gerçek Veri Tespiti (Observed Reality)
- Verinin yalnızca tek bir boyutu değil, çoklu boyutları incelenir (ör. sadece 1. evre değil, 2. en yüksek evre de taranır).
- Sayayarak ve oranlayarak: Zirve evre, ikincil evre, fark yüzdesi, homojenlik (dağılım dengesi).

### 2. Katman: Beklenen ile Kıyaslama (Expected vs. Observed)
- Hakemli literatürün teorik öngörüsü referans alınır (örn. McNulty vd., 2020: östrojen yükselişinde kuvvet artışı eğilimi).
- Kullanıcı verisiyle karşılaştırılır:
  - **Teoriyle Uyumlu:** Veri literatür genel eğilimiyle örtüşüyor.
  - **Pozitif Direnç (Farklılaşma):** Beklenenin aksine zorlu bir evrede (ör. Luteal) yüksek başarı var.
  - **Dengeli Dağılım:** Hormonal fazlardan etkilenmeyen istikrarlı güç.

### 3. Katman: Yumuşak Koçluk Dili (Supportive Tone & No Command)
- **YASAK:** Buyurgan, zorlayıcı emir kipleri: *"Bu günleri ağır rekor denemelerine ayırın"*, *"Asla tartılmayın"*, *"Şunu yapmalısınız"*.
- **ZORUNLU:** Nazik, bilimsel, öneri ve farkındalık tonu: *"Kendinizi enerjik hissettiğinizde yeni ağırlıklar veya PR deneyebilirsiniz"*, *"Antrenman şiddetini o günkü toparlanma hissinize göre ayarlamak en sağlıklısıdır"*.

---

## 3. Kilom İçindeki 8 Uygulama Alanı

Bu NLG motoru Kilom'un şu 8 kritik bileşeninde uygulanır:

1. **Evrelere Göre PR Dağılımı (`CycleSheet` - PR Kartı):**
   - *Beklenen:* Foliküler östrojen zirvesi.
   - *Gerçekleşen:* Zirve ve ikincil evreler (ör. Foliküler %44, Luteal %36).
   - *Sentez:* Luteal başarısını kutlayan, Foliküler'de hazır hissedildiğinde PR öneren yumuşak metin.
2. **Hormonal Hacim ve Tonaj Analizi (`CycleSheet` - Volume Kartı):**
   - Foliküler/Ovülasyon vs. Luteal seans başı ortalama tonaj kıyaslaması. Progesteron bazal ısı ve toparlanma açıklaması.
3. **Luteal RPE ve Algılanan Zorluk (`CycleSheet` - RPE Kartı):**
   - RPE farkı (ör. +0.8 RPE) ile bazal vücut ısısı (+0.4°C) ve RAAS sıvı değişiminin eşleştirilmesi.
4. **Akut Tartı Dalgalanması & DOMS Ödemi (`HeroCard` & `DomsAlertBanner`):**
   - Dün yapılan ≥8.000 kg bacak/sırt antrenmanı sonrası tartıda +0.5 kg artış görüldüğünde: "Bu bir yağ artışı değil; 1g kas glikojeninin 3-4g su bağlaması ve kas onarım ödemidir."
5. **Döngü Sıvı Tutulumu & Tartı Farkı (`HeroCard` & `CycleCard`):**
   - Luteal veya Menstrüel fazdaki +1–2 kg artışın progesteron/aldosteron kaynaklı geçici ödem olduğunun açıklanması.
6. **Hareketli Ortalama Trend Analizi (`SummaryCard` & `TrendChart`):**
   - Günlük dalgalanmalara karşı 7 günlük EMA trendinin yönünün (kilo düşüşünün korunduğunun) sakinleştirici dille aktarılması.
7. **Hedef Kilo ve Varış Süresi İlerlemesi (`GoalCard`):**
   - Haftalık kilo kaybı hızının (%0.5–1.0 vücut ağırlığı) güvenli metabolik sınırlar içinde olup olmadığının yorumlanması.
8. **İstikrar ve Alışkanlık Isı Haritası (`StreakCard`):**
   - 14 günlük sabah tartılma ritminin kognitif ağırlık kontrolündeki (NWCR) rolünün açıklanması.

---

## 4. Kodlama Kuralları ve Standartları

1. **Sıfır Hardcoded String:**
   - Üretilen tüm cümle parçaları `values/strings.xml` ve `values-tr/strings.xml` içinde parametrik şablonlar (`%1$s`, `%2$s`) olarak tanımlanır.
2. **Güvenli Yüzde Formatı:**
   - Sayı ve yüzdeler Kotlin tarafında `"${value}%"` olarak biçimlendirilip `%1$s` token'ına iletilir. Asla XML içinde çıplak `%` bırakılmaz.
3. **Doğal Türkçe Dil Bilgisi:**
   - Cümle akışı Türkçe konuşma ve tıp diline uygun, akıcı olmalıdır; motamot çeviri yasaktır.
4. **Kapsamlı Test Koruması:**
   - Her yeni NLG karar dalı (`follicular_luteal`, `luteal_dominant`, `balanced`, `anabolic_dominant`, `empty`) için `commonTest` altında birim test yazılmalıdır.
