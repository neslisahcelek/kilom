---
name: architect
description: >-
  Yeni bir özellik, ekran veya bileşen tasarlarken; uygulamadan önce yapısal değişiklik planı çıkarmak için kullan.
---

# Mimar skill'i

Uygulamaya başlamadan önce yeni özelliğin gerektirdiği yapısal değişiklikleri planlarsın. Ayrıntılı proje kuralları için [kilo-architecture](../kilo-architecture/SKILL.md) skill'ini kullan.

## Tasarım ilkeleri

1. **Katman yerleşimi**: Kod `composeApp/src/commonMain/kotlin/app/kilo/` altında `domain/`, `data/`, `ocr/`, `platform/`, `ui/` katmanlarına yerleşir. Platforma özel kod yalnızca `androidMain` / `iosMain` içindeki `actual` karşılıklarda olur.
2. **Bileşen yeniden kullanımı**: Yeni UI yazmadan önce `ui/` altındaki mevcut glass bileşenleri (`GlassCard`, `HeroCard`, `ActionBar`, `EntrySheet`, `HistoryRow`) ve `ui/theme/` değerlerini ara; yinelenen yapıyı ortaklaştır.
3. **Veri tasarımı**:
   - Ağırlık her zaman kg saklanır; kg/lb yalnızca gösterimde çevrilir.
   - Depolama `multiplatform-settings` + `kotlinx.serialization` JSON'dur. Şema değişirse geriye dönük uyumu (eksik alan için varsayılan) planla.
   - Türetilebilen değerler (ör. delta) saklanmaz, hesaplanır.
4. **Platform sınırları**: Yeni platform yeteneği için `expect` (commonMain) + Android ve iOS `actual` planla. Biri eksik kalmamalı.
5. **DI yok**: Bağımlılıklar elle `AppContainer` üzerinden bağlanır; DI kütüphanesi ekleme.

## Uygulama adımları

1. Kullanıcının isteğini ve etkilenen katmanları gözden geçir.
2. Oluşturulacak/değiştirilecek dosyaları adım adım listeleyen bir plan taslağı yaz.
3. Planı `AGENTS.md` (varsa) kuralları ve mevcut kod örüntüleriyle doğrula.
4. Kod değişikliğine geçmeden planı kullanıcıya açıkça sun.
