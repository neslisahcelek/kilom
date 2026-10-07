---
name: code-review
description: >-
  Kotlin Multiplatform ve Compose Multiplatform kodunu hızlı biçimde mimari, state yönetimi ve UI tutarlılığı açısından gözden geçirmek için kullan. Kapsamlı inceleme için kilo-code-review'a bak.
---

# Code review skill'i

Değişiklikleri proje standartlarına göre incelersin. Derin/commit öncesi review için [kilo-code-review](../kilo-code-review/SKILL.md) kullan.

## Kontroller

1. **Mimari ve katmanlar**:
   - UI state'i yalnızca ViewModel'den okur (`StateFlow` + `collectAsStateWithLifecycle`).
   - UI state değişiklikleri ViewModel fonksiyonlarından geçer; state güncellemesinde `MutableStateFlow.update {}` kullanılır.
   - Repository yalnızca veri erişimi yapar, UI state mantığı tutmaz.
2. **UI ve Compose**:
   - Sabit string/renk yok: metinler compose resources `strings.xml` (EN + TR), renkler tema dosyasından gelir.
   - `LazyColumn` öğelerinde stabil `key` var.
   - Önemli ekranlarda `@Preview` var.
3. **Veri**:
   - JSON okuma bozuk/eksik veride çökmez, güvenli varsayılana düşer.
   - Hatalar kullanıcıya anlaşılır, lokalize metinle yansır; ham exception gösterilmez.
4. **KMP sınırı**: `commonMain` içinde `android.*` / `platform.*` importu yok; her `expect` için Android ve iOS `actual` var.

## Uygulama adımları

1. Verilen kodu veya değişen dosyaları incele (`git diff`, `git diff --cached`).
2. Yukarıdaki kurallarla karşılaştır.
3. Sapmaları ve önerilen düzeltmeleri yapılandırılmış bir raporla sun: önce bulgular (önem sırasıyla, `dosya:satır`), sonra kapsam ve çalıştırılan kontroller. Bulgu yoksa açıkça "Bulgu yok" de.
4. İnceleme istendiyse kodu kendiliğinden değiştirme.
