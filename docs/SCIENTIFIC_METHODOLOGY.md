# Kilom – Bilimsel Metodoloji ve Biyomekanik Kanıt Dosyası
*(Scientific Methodology, Biomechanics & Physiological Evidence Dossier)*

Bu doküman; **Kilom** uygulamasının menstrüel döngü fazlama, tartı su tutma (ödem) tahminleri, egzersize bağlı kas hasarı (DOMS) kalkanı, evrensel göreceli kuvvet (1RM / BW) ölçekleri ve %100 cihaz içi parametrik veri hikayesi (NLG) motorunda kullandığı tüm biyolojik modelleri, matematiksel algoritmaları ve hakemli akademik kaynakları detaylandırmak amacıyla hazırlanmıştır.

Spor hekimleri, kadın doğum ve jinekoloji uzmanları, biyomekanistler, endokrinologlar ve performans antrenörleri tarafından incelenmeye uygun bilimsel titizlikle yapılandırılmıştır.

---

## 1. Menstrüel Döngü Modeli & Dinamik Faz Algoritması

### 1.1. Biyolojik Gerçeklik: Luteal Faz Sabiti & Foliküler Değişkenlik
Klasik takvim uygulamaları kadın döngüsünü sabit 28 gün (14 gün foliküler + 14 gün luteal) kabul eder; ancak klinik çalışmalar kadınların yalnızca %13'ünün tam 28 günlük döngüye sahip olduğunu göstermektedir (Fehring et al., 2006).

Döngü uzunluğu normal sınırlarda 21 ile 45 gün arasında değişebilir (ACOG Committee Opinion No. 651). Ancak endokrinolojideki temel biyolojik gerçek şudur:
* **Korpus Luteum Ömrü Sabittir (Luteal Sabiti):** Yumurtlamadan sonra çatlayan folikülün oluşturduğu korpus luteumun ömrü, döngü uzunluğundan bağımsız olarak eumenoreik kadınlarda **12–14 gün** ile sınırlıdır (Lenton et al., 1984; Sherman & Korenman, 1975).
* **Değişen Faz Foliküler Fazdır:** Döngüsü 35 gün süren bir kadında foliküler faz 21 gün sürer; döngüsü 24 gün süren bir kadında ise foliküler faz 10 gün sürer.

### 1.2. Kilom'un Matematiksel Algoritması (Geriye Doğru Luteal Eşleme - Backward Luteal Mapping)
Kilom, kullanıcının girdiği `cycleLength` (varsayılan 28 gün, 21–45 gün aralığında kullanıcı tarafından ayarlanabilir) değerine göre faz sınırlarını dinamik olarak geriye doğru ölçekler:

$$\text{Yumurtlama Günü (Ovulation Day)} = \text{Döngü Uzunluğu} - 14$$

| Mikro Faz Adı | Döngü Günü Formülü (Örn. 28 Gün) | Döngü Günü Formülü (Örn. 35 Gün) | Hormonal Profil |
| :--- | :--- | :--- | :--- |
| **Menstrüel (Regl)** | Gün $1 \dots 4$ | Gün $1 \dots 5$ | Östrojen ve progesteron taban seviyede; prostaglandinler yüksek. |
| **Erken Foliküler** | Gün $5 \dots 8$ | Gün $6 \dots 12$ | FSH yükselir, folikül gelişimi başlar; östrojen kademeli artar. |
| **Geç Foliküler (Tepe Güç)** | Gün $9 \dots (\text{Ov} - 2)$ (9–12) | Gün $13 \dots 19$ | Östrojen pik yapar, testosteron yükselir; anabolik ortam. |
| **Yumurtlama Penceresi** | Gün $(\text{Ov} - 1) \dots (\text{Ov} + 1)$ (13–15) | Gün $20 \dots 22$ | LH ve FSH dalgası; tepe östrojenin ardından ani düşüş. |
| **Erken Luteal** | Gün $(\text{Ov} + 2) \dots (\text{Döngü} - 8)$ (16–20) | Gün $23 \dots 27$ | Progesteron hızla yükselir; östrojen ikinci hafif piki yapar. |
| **Orta Luteal (Ödem)** | Gün $(\text{Döngü} - 7) \dots (\text{Döngü} - 4)$ (21–24) | Gün $28 \dots 31$ | Progesteron tepe noktası; aldosteron aktivasyonu ve su tutumu. |
| **Geç Luteal / PMS** | Gün $(\text{Döngü} - 3) \dots \text{Döngü}$ (25–28) | Gün $32 \dots 35$ | Hormonların çöküşü (progesteron/östrojen düşüşü); MSS yorgunluğu. |

---

## 2. Egzersiz Endokrinolojisi & Döngü Fazı İlkeleri

### 2.1. Geç Foliküler Fazda Maksimal Kuvvet ve Anabolik Ortam
* **Mekanizma:** Östrojen ($17\beta\text{-estradiol}$), iskelet kasında yalnızca bir üreme hormonu değil, güçlü bir **nöromüsküler uyarıcı ve anabolik faktördür**. Kas miyozin fonksiyonunu optimize eder, kas hasarına karşı membran stabilitesini korur ve glikojen depolanmasını hızlandırır (Häggström, 2014; Enns & Tiidus, 2010).
* **Meta-Analiz Bulgusu (McNulty et al., 2020):** *Sports Medicine* dergisinde yayımlanan kapsamlı meta-analizde (51 çalışma, 1071 katılımcı), eumenoreik kadınlarda egzersiz performansının erken foliküler faza kıyasla östrojenin yüksek olduğu geç foliküler fazda hafif artış sergilediği doğrulanmıştır.
* **Kilom Uygulaması:** Egzersiz motoru bu pencerede Squat, Deadlift ve Hip Thrust gibi bileşik hareketlerde maksimal kuvvet (%85–90 1RM) ve rekor denemelerine elverişli bir zemin olduğunu belirtir.

### 2.2. Yumurtlama Döneminde Ligamentöz Laksite & ACL (Ön Çapraz Bağ) Riski
* **Mekanizma:** Yumurtlama öncesi östrojen reseptörleri (ER-$\alpha$ ve ER-$\beta$) eklem bağlarındaki fibroblastlarda kolajen sentezini baskılar ve bağ gevşemesine (laxity) yol açar.
* **Sistematik Derleme (Herzberg et al., 2017):** *Orthopaedic Journal of Sports Medicine* dergisindeki meta-analizde, kadın sporcularda non-kontakt ön çapraz bağ (ACL) yırtılmalarının **yumurtlama dönemindeki östrojen dalgalanmaları sırasında istatistiksel olarak anlamlı derecede arttığı** doğrulanmıştır.
* **Kilom Uygulaması:** Bu 48–72 saatlik pencerede yüksek güç üretimi korunurken; ani yön değiştirmeli sıçramalardan ve diz valgusundan (dizlerin içe kaçması) kaçınılması yönünde belirgin bir **Biyomekanik Sakatlık Uyarısı** tetiklenir.

### 2.3. Luteal Fazda Sıvı Dengesi, Termoregülasyon ve Deload
* **Mekanizma 1 (Aldosteron & RAAS Sıvı Tutumu):** Progesteron antimineralokortikoid etkisiyle distal böbrek tübüllerinde aldosteron ile yarışır; telafi edici olarak Renin-Anjiyotensin-Aldosteron Sistemi (RAAS) aktive olur ve hücre dışı interstisyel alanda **0.8 ila 2.5 kg geçici su tutulur** (Stachenfeld, 2008).
* **Mekanizma 2 (Termoregülasyon Artışı):** Progesteron hipotalamik termostatı etkileyerek **vücut çekirdek sıcaklığını +0.3°C ila +0.5°C yükseltir** (Baker et al., 2020; Sims et al., 2007). Bu durum erken terlemeye, daha yüksek kalp atış hızına ve kardiyovasküler zorlanmaya yol açar.
* **Mekanizma 3 (Bazal Metabolizma Hızı Artışı):** Luteal fazda bazal metabolizma hızı günlük yaklaşık 100–300 kcal artar (Solomon et al., 1982).
* **Kilom Uygulaması:** Luteal fazdaki kilo artışlarının hormonal sıvı tutulumu olduğu açıklanır; PMS evresinde serbest ağırlık 1RM zorlamaları yerine eklem dostu kablo/makine hareketleri ve planlı Deload önerilir.

---

## 3. Direnç Antrenmanı, DOMS ve Tartı Ödemi Korelasyonu

### 3.1. Tonaj / Hacim Yükü (Volume Load) Formülasyonu
İçe aktarılan `workouts.csv` antrenman kayıtları egzersiz bazında ayrıştırılarak toplam hacim yükü hesaplanır:

$$\text{Volume Load (kg)} = \sum_{i=1}^{n} (\text{Set}_i \times \text{Tekrar}_i \times \text{Ağırlık}_i)$$

### 3.2. Egzersize Bağlı Kas Hasarı (EIMD) ve İnflamatuar Sıvı Tutumu
* **Fizyoloji:** Özellikle eksantrik kasılmaların yoğun olduğu bacak ve sırt antrenmanları miyofibriler mikro yırtıklara yol açar. Bu durum nötrofil ve makrofaj infiltrasyonuna, sitokin salınımına ve interstisyel alana sıvı toplanmasına (hücre dışı ödem) neden olur (Peake et al., 2017; Clarkson & Hubal, 2002).
* **Zamanlama (Time-Lag):** İnflamasyon antrenmandan 12 saat sonra başlar, **24–48 saat sonra tepe noktasına ulaşır** ve 72 saat sonra azalır (Gecikmiş Kas Ağrısı / DOMS penceresi).

### 3.3. Kas İçi Glikojen Süperkompansasyonu ve Su Bağlama Oranı
* **Biyokimya:** Direnç antrenmanı sonrasında kas hücreleri tükenen glikojen depolarını hızla yeniler.
* **Bağlanma Oranı:** Her **1 gram kas glikojeni, yaklaşık 3.0 ila 4.0 gram suyu** beraberinde bağlar (Kreitzman et al., 1992; Fernandez-Elias et al., 2015).
* **Örnek Matematik:** Ağır bacak seansının ardından 300 gram glikojen depolayan bir sporcunun vücudunda $300\text{g} + (300 \times 3.5\text{g}) = 1350\text{g}$ (yaklaşık **+1.35 kg**) geçici su tutulur.

### 3.4. Kilom DOMS Kalkanı Algoritması:
Kilom, sabah tartısında görülen artışları (`currentWeight - previousWeight > +0.3 kg`) analiz ederken son 48 saatteki antrenman kayıtlarını tarar:
* *Koşul:* Son 48 saatte büyük kas grubu tonajı $\ge 8.000\text{ kg}$ veya yüksek şiddetli bileşik seans tamamlanmışsa;
* *Karar:* Tartıdaki artışın kesinlikle yağ kütlesi olmadığı, **EIMD ödemi ve glikojen hidrasyonu** olduğu kullanıcıya bilimsel verilerle bildirilir.

---

## 4. Evrensel Göreceli Kuvvet (Relative Strength) ve 1RM Normatif Standartları

Kilo verme (kalori açığı) sürecinde sporcuların en büyük korkusu kas kaybıdır. Vücut ağırlığı azalırken saf kuvvetin korunması veya artması, kas kütlesinin korunduğunun (kaybın saf yağdan olduğunun) en güvenilir biyomekanik göstergesidir (Suchomel et al., 2016; Zatsiorsky & Kraemer, 2006).

### 4.1. 1RM Tahmin Formülasyonu: Epley (1985) Formülü
Bileşik egzersizlerde maksimal tek tekrar kuvveti (1RM), submaksimal ağırlık ve tekrar verisinden Epley formülüyle türetilir:

$$\text{1RM} = \text{Ağırlık} \times \left(1 + \frac{\text{Tekrar}}{30}\right)$$

* **Akademik Doğrulama (LeSuer et al., 1997):** *Journal of Strength and Conditioning Research* dergisinde yayımlanan karşılaştırmalı çalışmada, 7 farklı 1RM tahmin formülü incelenmiş; Epley formülünün Bench Press, Squat ve Deadlift egzersizlerinde submaksimal tekrarlarda ($\le 10$ tekrar) en yüksek korelasyona ve en düşük hata payına sahip modellerden biri olduğu kanıtlanmıştır.

### 4.2. Göreceli Kuvvet Oranı (Relative Strength Index)
Sporcunun mutlak gücünün vücut kütlesine oranı:

$$\text{Relative Strength} = \frac{\text{Tahmini 1RM (kg)}}{\text{Güncel Vücut Ağırlığı (kg)}}$$

### 4.3. NSCA & Kilgore-Rippetoe Normatif Standartları (Kadın & Erkek Kademeleri)
Kilom, kullanıcının biyolojik cinsiyetine ve güncel vücut ağırlığına göre 4 kademeli normatif ölçek sunar (NSCA Essentials of Strength Training and Conditioning, 4th ed.; Kilgore, Rippetoe & Pendlay, 2006):

#### Kadın Standartları (x Vücut Ağırlığı)
| Hareket | Başlangıç (Beginner) | Orta Seviye (Intermediate) | İleri Seviye (Advanced) | Elit (Elite) |
| :--- | :--- | :--- | :--- | :--- |
| **Squat** | $0.5 \dots 0.8\times\text{BW}$ | $0.8 \dots 1.2\times\text{BW}$ | $1.2 \dots 1.5\times\text{BW}$ | $\ge 1.5\times\text{BW}$ |
| **Deadlift** | $0.6 \dots 1.0\times\text{BW}$ | $1.0 \dots 1.4\times\text{BW}$ | $1.4 \dots 1.8\times\text{BW}$ | $\ge 1.8\times\text{BW}$ |
| **Bench Press** | $0.3 \dots 0.5\times\text{BW}$ | $0.5 \dots 0.75\times\text{BW}$ | $0.75 \dots 1.0\times\text{BW}$ | $\ge 1.0\times\text{BW}$ |
| **Overhead Press** | $0.2 \dots 0.35\times\text{BW}$ | $0.35 \dots 0.5\times\text{BW}$ | $0.5 \dots 0.65\times\text{BW}$ | $\ge 0.65\times\text{BW}$ |

#### Erkek Standartları (x Vücut Ağırlığı)
| Hareket | Başlangıç (Beginner) | Orta Seviye (Intermediate) | İleri Seviye (Advanced) | Elit (Elite) |
| :--- | :--- | :--- | :--- | :--- |
| **Squat** | $0.8 \dots 1.2\times\text{BW}$ | $1.2 \dots 1.6\times\text{BW}$ | $1.6 \dots 2.0\times\text{BW}$ | $\ge 2.0\times\text{BW}$ |
| **Deadlift** | $1.0 \dots 1.5\times\text{BW}$ | $1.5 \dots 2.0\times\text{BW}$ | $2.0 \dots 2.5\times\text{BW}$ | $\ge 2.5\times\text{BW}$ |
| **Bench Press** | $0.5 \dots 0.8\times\text{BW}$ | $0.8 \dots 1.2\times\text{BW}$ | $1.2 \dots 1.5\times\text{BW}$ | $\ge 1.5\times\text{BW}$ |
| **Overhead Press** | $0.35 \dots 0.55\times\text{BW}$ | $0.55 \dots 0.75\times\text{BW}$ | $0.75 \dots 0.95\times\text{BW}$ | $\ge 0.95\times\text{BW}$ |

*Uygulama İçi Dinamik Hesaplama:* Kullanıcının güncel kilosu $W$ kg olduğunda, ekrandaki ölçek çubuğu yalnızca çarpanı değil; o kademeye denk gelen hedef aralığı doğrudan kilogram cinsinden ($W \times \text{min} \dots W \times \text{max}\text{ kg}$) hesaplayarak gösterir.

---

## 5. %100 Cihaz İçi Parametrik Doğal Dil Üretim (NLG) ve Veri Hikayesi

### 5.1. Neden Deterministik Motor? (Sıfır Halüsinasyon İlkesi)
Büyük Dil Modelleri (LLM'ler) serbest metin üretiminde etkileyici olsa da, aritmetik hesaplamalarda ve tablo analizlerinde %15–30 oranında halüsinasyon riski taşır (Hendrycks et al., MATH Benchmark). Sağlık ve kuvvet verilerinde bir kullanıcının yanlış yönlendirilmesi sakatlık veya motivasyon kaybına yol açabilir.

Kilom, koçluk metinlerini üretmek için **%100 Cihaz İçi Parametrik Doğal Dil Üretim (NLG)** mimarisini kullanır:
1. **Veri Ayrıştırma:** Kullanıcının antrenman geçmişindeki tüm PR'lar ve hacimler döngü fazlarına göre gruplanır.
2. **Gerçekleşen vs. Literatür Beklentisi Kıyaslaması:**
   - Literatür beklentisi (McNulty et al., 2020), yüksek östrojen sebebiyle PR'ların çoğunlukla foliküler veya ovülasyon fazında kırılacağı yönündedir.
   - Ancak bireysel sporcuların günlük uyku kalitesi, iş stresi, beslenme zamanlaması veya antrenman programı dağılımı farklılık gösterebilir.
3. **Dinamik Dallanma Mantığı:**
   - **Foliküler/Ovülasyon Ağırlıklıysa:** Verilerin literatürdeki anabolik östrojen modeliyle birebir örtüştüğü yumuşak bir dille doğrulanır.
   - **Luteal Ağırlıklıysa:** Sabit ve ezbere bir metin yerine; *"Literatürde yüksek östrojen sebebiyle güç artışları genelde foliküler fazda görülse de, sende rekorların çoğunluğu luteal fazda gelmiş. Düzenli beslenme, karbonhidrat alımı ve bireysel toparlanma kapasiten bu evrede de yüksek kuvvet üretmeni sağlamış"* şeklinde bilimsel kıyaslama ve destekleyici koçluk sunulur.
   - **Menstrüel Ağırlıklıysa:** Düşük hormon tabanında sağlanan bu odaklanmanın antrenman disiplinini gösterdiği belirtilir.
   - **Dengeli Dağılımsa:** Tutarlı kuvvet dağılımı tebrik edilir.

---

## 6. Hakemli Akademik Kaynakça (Peer-Reviewed References)

1. **ACOG (American College of Obstetricians and Gynecologists).** (2015). Menstruation in Girls and Adolescents: Using the Menstrual Cycle as a Vital Sign. *Obstetrics & Gynecology*, Committee Opinion No. 651, 126(6), e143–e146.
2. **Baker, F. C., Siboza, F., & Fuller, A.** (2020). Temperature regulation in women: Effects of the menstrual cycle. *Temperature*, 7(3), 226–262. DOI: [10.1080/23328940.2020.1735927](https://doi.org/10.1080/23328940.2020.1735927)
3. **Clarkson, P. M., & Hubal, M. J.** (2002). Exercise-induced muscle damage in humans. *American Journal of Physical Medicine & Rehabilitation*, 81(11 Suppl), S52–S69. DOI: [10.1097/00002060-200211001-00007](https://doi.org/10.1097/00002060-200211001-00007)
4. **Epley, B.** (1985). *Poundage Chart.* Boyd Epley Workout, Lincoln, NE.
5. **Fehring, R. J., Schneider, M., & Raviele, K.** (2006). Variability in the phases of the menstrual cycle. *Journal of Obstetric, Gynecologic & Neonatal Nursing*, 35(3), 376–384. DOI: [10.1111/j.1552-6909.2006.00051.x](https://doi.org/10.1111/j.1552-6909.2006.00051.x)
6. **Fernandez-Elias, V. E., Ortega, J. F., Nelson, R. K., & Mora-Rodriguez, R.** (2015). Relationship between muscle water and glycogen recovery after prolonged exercise in the heat in humans. *European Journal of Applied Physiology*, 115(9), 1919–1926. DOI: [10.1007/s00421-015-3175-z](https://doi.org/10.1007/s00421-015-3175-z)
7. **Haff, G. G., & Triplett, N. T. (Eds.).** (2016). *Essentials of Strength Training and Conditioning* (4th ed.). Human Kinetics / National Strength and Conditioning Association (NSCA).
8. **Herzberg, S. D., Motu'apuaka, M. L., Lambert, W., Fu, R., Brady, J., & Guise, J. M.** (2017). The Effect of Menstrual Cycle and Contraceptives on ACL Injuries and Laxity: A Systematic Review and Meta-analysis. *Orthopaedic Journal of Sports Medicine*, 5(7), 2325967117718781. DOI: [10.1177/2325967117718781](https://doi.org/10.1177/2325967117718781)
9. **Kilgore, J. L., Rippetoe, M., & Pendlay, G.** (2006). *Starting Strength: Basic Barbell Training & Weightlifting Performance Standards.* The Aasgaard Company.
10. **Kreitzman, S. N., Coxon, A. Y., & Szaz, K. F.** (1992). Glycogen storage: illusions of easy weight loss, excessive weight regain, and distortions in estimates of body composition. *The American Journal of Clinical Nutrition*, 56(1 Suppl), 292S–293S. DOI: [10.1093/ajcn/56.1.292S](https://doi.org/10.1093/ajcn/56.1.292S)
11. **Lenton, E. A., Landgren, B. M., Sexton, L., & Harper, R.** (1984). Normal variation in the length of the luteal phase of the menstrual cycle: identification of the short luteal phase. *British Journal of Obstetrics and Gynaecology*, 91(7), 685–689.
12. **LeSuer, D. A., McCormick, J. H., Mayhew, J. L., Wasserstein, R. L., & Arnold, M. D.** (1997). The Accuracy of Prediction Equations for Estimating 1-RM Performance in the Bench Press, Squat, and Deadlift. *The Journal of Strength & Conditioning Research*, 11(4), 211–213.
13. **McNulty, K. L., Elliott-Sale, K. J., Dolan, E., Swinton, P. A., Ansdell, P., Goodall, S., Thomas, K., & Hicks, K. M.** (2020). The Effects of Menstrual Cycle Phase on Exercise Performance in Eumenorrheic Women: A Systematic Review and Meta-Analysis. *Sports Medicine*, 50(10), 1813–1827. DOI: [10.1007/s40279-020-01319-3](https://doi.org/10.1007/s40279-020-01319-3)
14. **Peake, J. M., Neubauer, O., Della Gatta, P. A., & Nosaka, K.** (2017). Muscle damage and inflammation during recovery from exercise. *Journal of Applied Physiology*, 122(3), 559–570. DOI: [10.1152/japplphysiol.00971.2016](https://doi.org/10.1152/japplphysiol.00971.2016)
15. **Solomon, S. J., Kurzer, M. S., & Calloway, D. H.** (1982). Menstrual cycle and basal metabolic rate in women. *The American Journal of Clinical Nutrition*, 36(4), 611–616. DOI: [10.1093/ajcn/36.4.611](https://doi.org/10.1093/ajcn/36.4.611)
16. **Stachenfeld, N. S.** (2008). Sex hormone effects on body fluid regulation. *Exercise and Sport Sciences Reviews*, 36(3), 152–159. DOI: [10.1097/JES.0b013e31817be928](https://doi.org/10.1097/JES.0b013e31817be928)
17. **Suchomel, T. J., Nimphius, S., & Stone, M. H.** (2016). The Importance of Muscular Strength in Athletic Performance. *Sports Medicine*, 46(10), 1419–1449. DOI: [10.1007/s40279-016-0486-0](https://doi.org/10.1007/s40279-016-0486-0)
18. **Zatsiorsky, V. M., & Kraemer, W. J.** (2006). *Science and Practice of Strength Training* (2nd ed.). Human Kinetics.
