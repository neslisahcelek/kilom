# Kilom Doğrulama ve Kod İnceleme İş Akışı (Review Workflow)

Değişiklikleri commit etmeden veya pull request açmadan önce Kilom standartlarına göre doğrulama adımlarını çalıştır.

## Adımlar

1. **Değişiklikleri ve Çalışma Ağacını İncele**
   - Çalışma ağacı durumunu kontrol et: `git status --short`
   - Yapılan değişikliklerin tam farkını oku: `git diff`

2. **Otomatik Birim Testleri Çalıştır**
   - Ortak KMP testlerini ve parser testlerini çalıştır:
     ```powershell
     .\gradlew.bat :composeApp:allTests
     ```
   - Android birim testlerini doğrula:
     ```powershell
     .\gradlew.bat :composeApp:testDebugUnitTest
     ```

3. **Android Derleme Doğrulaması**
   - Android debug build kontrolü yap:
     ```powershell
     .\gradlew.bat :composeApp:assembleDebug
     ```

4. **Kod İnceleme ve Güvenlik Denetimi**
   - `.agents/skills/code-review/SKILL.md` ve `AGENTS.md` kurallarını denetle.
   - Kontrol noktaları:
     - [ ] %100 yerel gizlilik (ağ çağrısı yok, loglara veri sızması yok).
     - [ ] `commonMain` platformdan bağımsız.
     - [ ] Her `expect` için Android ve iOS `actual` tamam.
     - [ ] Ağırlık her zaman `kg` saklanıyor, delta türetiliyor.
     - [ ] OCR sonucu kullanıcı onayına tabi (otomatik kaydetme yok).
     - [ ] `strings.xml` hem EN hem TR olarak eklenmiş.
   - Herhangi bir `BLOCKER` veya `HIGH` bulgu varsa düzeltilmeden devam edilemez.
