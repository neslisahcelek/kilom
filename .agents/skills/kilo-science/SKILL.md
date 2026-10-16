---
name: kilo-science
description: >-
  Kilom'un egzersiz fizyolojisi, biyomekanik kuvvet standartları (1RM, Göreceli Kuvvet), DOMS/glikojen ödem modelleri, menstrüel endokrinoloji ve hakemli bilimsel kaynakça (PubMed/DOI) doğrulama becerisi. Yeni hesaplama, ölçek, fizyolojik açıklama veya kaynakça eklerken kullan.
---

# Kilo Bilimsel Metodoloji ve Hakemli Literatür Becerisi (`kilo-science`)

Bu skill, **Kilom** projesinde yer alan tüm biyomekanik, fizyolojik, metabolik ve endokrin hesaplamaların **%100 kanıta dayalı (evidence-based)**, matematiksel olarak şeffaf ve yalnızca uluslararası kabul görmüş hakemli literatüre (*peer-reviewed journals & clinical guidelines*) dayalı olmasını garanti eder.

---

## 1. Temel İlkeler (Sıfır Taviz Kuralları)

1. **Şeffaf Hesaplama ve Ölçek Zorunluluğu:**
   - Kullanıcıya bir sınıflandırma veya sonuç gösterildiğinde (örn. `Elit Kuvvet`, `İleri Seviye`, `DOMS Kas Ödemi`, `Luteal Ödem`), bu sonucun **hangi matematiksel formülle** hesaplandığı, **ölçeğin tamamı** ve **alt/üst eşik sınırları** (`min–max` aralıkları hem katsayı hem `kg` karşılığı olarak) açıkça gösterilmelidir.
2. **Cinsiyet ve Biyomekanik Kalibrasyon:**
   - Göreceli kuvvet (`1RM / Vücut Ağırlığı`) gibi kas kütlesi dağılımına duyarlı metrikler hem **Kadın** hem **Erkek** normatif standartlarını desteklemelidir.
   - Hareket kalıpları biyomekanik kaldıraçlarına göre ayrıştırılmalıdır:
     - Alt Vücut Bileşik (*Squat, Deadlift, Hip Thrust*)
     - Yatay İtiş / Çekiş (*Bench Press, Barbell Row*)
     - Dikey İtiş (*Overhead / Military Press*)
3. **Yalnızca Birinci Sınıf Hakemli Kaynaklar (Tier-1 Evidence):**
   - Blog yazıları, doğrulanmamış popüler kültür iddiaları veya tekil anekdotlar **KESİNLİKLE KULLANILAMAZ**.
   - Yalnızca sistematik derlemeler, meta-analizler, randomize kontrollü çalışmalar (RCT), yüksek etki faktörlü fizyoloji dergileri (*Sports Medicine*, *Journal of Applied Physiology*, *JSCR*, *American Journal of Clinical Nutrition*, *Exercise and Sport Sciences Reviews*) ve resmi klinik kılavuzlar (*ACOG*, *NSCA*) kaynak gösterilebilir.
   - Her bilimsel kaynak kartında **Yazar(lar), Yıl, Dergi/Kurum, Cilt/Sayfa ve PMID/DOI** yer almalıdır.
4. **Tıbbi Sorumluluk Sınırı (Store Compliance):**
   - Kilom tanı veya tedavi koymaz; tüm fizyolojik ve beslenme/antrenman bilgileri sağlıklı bireyler için eğitsel ve biyomekanik farkındalık amaçlıdır (`cycle_medical_disclaimer`).

---

## 2. Doğrulanmış Matematiksel Modeller ve Ölçekler

### A. 1RM (Tek Tekrar Maksimumu) Tahmini — Epley (1985)
- **Formül:**
  $$\text{1RM} = \text{Ağırlık (kg)} \times \left(1 + \frac{\text{Tekrar}}{30}\right)$$
- **Doğrulama:** LeSuer ve ark. (1997, *JSCR*, `DOI: 10.1519/00124278-199711000-00001`), Squat ($r = 0.97$), Bench Press ($r = 0.99$) ve Deadlift ($r = 0.95$) hareketlerinde gerçek 1RM ile $\ge 0.95$ korelasyon kanıtlamıştır.

### B. Göreceli Kuvvet ($\text{1RM} \div \text{Vücut Ağırlığı}$) Normatif Eşikleri
*Kaynak: NSCA (Haff & Triplett, 2016) & Rippetoe, Kilgore & Pendlay (2006) & Suchomel ve ark. (2016, Sports Medicine, `PMID: 26838985`)*

| Hareket Grubu | Standart | Başlangıç (`BEGINNER`) | Orta (`INTERMEDIATE`) | İleri (`ADVANCED`) | Elit (`ELITE`) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Squat / Deadlift / Hip Thrust** | **Kadın** | $< 0.85\text{x}$ | $0.85\text{x} – 1.24\text{x}$ | $1.25\text{x} – 1.74\text{x}$ | $\ge 1.75\text{x}$ |
| | **Erkek** | $< 1.25\text{x}$ | $1.25\text{x} – 1.74\text{x}$ | $1.75\text{x} – 2.24\text{x}$ | $\ge 2.25\text{x}$ |
| **Bench Press / Barbell Row** | **Kadın** | $< 0.55\text{x}$ | $0.55\text{x} – 0.84\text{x}$ | $0.85\text{x} – 1.14\text{x}$ | $\ge 1.15\text{x}$ |
| | **Erkek** | $< 0.85\text{x}$ | $0.85\text{x} – 1.19\text{x}$ | $1.20\text{x} – 1.59\text{x}$ | $\ge 1.60\text{x}$ |
| **Overhead Press (OHP)** | **Kadın** | $< 0.35\text{x}$ | $0.35\text{x} – 0.54\text{x}$ | $0.55\text{x} – 0.79\text{x}$ | $\ge 0.80\text{x}$ |
| | **Erkek** | $< 0.55\text{x}$ | $0.55\text{x} – 0.79\text{x}$ | $0.80\text{x} – 1.04\text{x}$ | $\ge 1.05\text{x}$ |

### C. DOMS / EIMD ve Glikojen-Su Bağlanma Modeli
- **Algoritmik Kural (`evaluateDomsRisk`):** Son 48 saat içinde büyük kas gruplarında (`LEGS_GLUTES` veya `BACK_PULL`) $\ge 8.000\text{ kg}$ seans tonajı + önceki tartıya göre $\ge +0.3\text{ kg}$ akut tartı artışı.
- **Fizyolojik Kanıt:**
  - **EIMD Hücre Dışı Ödem:** Sarkomer Z-disk mikro travması sonrası nötrofil/makrofaj infiltrasyonu ve ekstraselüler sıvı tutulumu 24–72 saatte zirve yapar (*Peake ve ark., 2017, J Appl Physiol, `PMID: 28035017`; Clarkson & Hubal, 2002, `PMID: 12409811`*).
  - **Glikojen Hidrasyonu:** İskelet kasında sentezlenen her $1\text{ g}$ glikojen hücre içine $3\text{–}4\text{ g}$ su bağlar (*Kreitzman ve ark., 1992, Am J Clin Nutr, `PMID: 1615908`; Fernández-Elías ve ark., 2015, `PMID: 25911631`*).

### D. Antrenman Hacmi (Tonaj) ve RIR Tabanlı RPE
- **Tonaj Formülü:** $\sum (\text{Set} \times \text{Tekrar} \times \text{Ağırlık})$ (*Schoenfeld ve ark., 2017, J Sports Sci, `PMID: 27433992`*).
- **RPE / RIR Otoregülasyonu:** $\text{RPE } 10 = 0\text{ RIR}$, $\text{RPE } 9 = 1\text{ RIR}$, $\text{RPE } 8 = 2\text{ RIR}$ (*Helms ve ark., 2016, Strength Cond J, `PMID: 27531969`*).

### E. Menstrüel Döngü ve Endokrin Fizyoloji (`CycleAnalytics.kt`)
- **Backward Luteal Mapping:** Ovülasyon günü sabit 14. gün değil, `cycleLength - 14` (21–45 gün dinamik döngü) olarak hesaplanır.
- **Referans Literatür:**
  - *McNulty ve ark. (2020, Sports Medicine, `PMID: 32661870`)* — 78 çalışmalı menstrüel faz egzersiz performansı meta-analizi.
  - *Stachenfeld (2008, Exerc Sport Sci Rev, `PMID: 18580296`)* — Progesteron ve östrojenin RAAS/aldosteron üzerinden luteal sodyum-su tutulumu (+1–2.5 kg ödem) etkisi.
  - *Baker ve ark. (2020, Temperature, `PMID: 33123618`)* & *Solomon ve ark. (1982, Am J Clin Nutr, `PMID: 7124662`)* — Hipotalamik $+0.3\text{–}0.5^\circ\text{C}$ bazal vücut ısısı artışı ve luteal $+100\text{–}250\text{ kcal/gün}$ bazal metabolizma hızlanması.
  - *Herzberg ve ark. (2017, Orthop J Sports Med, `PMID: 28795075`)* & *ACOG CPG No. 7 (2023, `PMID: 37973069`)* — Ovülasyon evresinde östrojen kaynaklı ön çapraz bağ (ACL) laksitesi ve klinik PMS/demir yönetimi.

---

## 3. Yeni Bilimsel İçerik Ekleme Kontrol Listesi

1. [ ] Hesaplama saf Kotlin (`commonMain/kotlin/app/kilo/domain/`) içinde deterministik ve çevrimdışı mı?
2. [ ] Sonucun yanında ölçeğin alt ve üst sınırları (`TierBounds` / eşik değerleri) ve kullanılan formül kullanıcıya açıkça gösteriliyor mu?
3. [ ] Hem kadın hem erkek kullanıcılar için uygun referans değerleri (`isMaleStandard`) mevcut mu?
4. [ ] Kaynakçada gösterilen makalenin Yazar, Yıl, Dergi, Cilt/Sayfa ve `PMID`/`DOI` bilgileri doğrulandı mı?
5. [ ] Hem `values/strings.xml` (EN) hem `values-tr/strings.xml` (doğal, akıcı Türkçe; sıfır motamot çeviri) güncellendi mi?
6. [ ] `commonTest` altında sınır değerleri (boundary values) için birim testleri yazıldı mı?
