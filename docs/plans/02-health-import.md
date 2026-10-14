# 02 – Apple Health'ten İçe Aktarma (Okuma) — Faz 1 (platform) + Faz 2 (UI)

## Faz 1 – Platform (ortak dosyalara dokunma)
- `platform/HealthSync.kt`: arayüze ekle
  ```kotlin
  /** Reads body-mass samples (kg) from the health store, newest first. Own app samples are excluded. */
  suspend fun readWeights(since: Instant? = null): List<HealthWeightSample>
  ```
  `data class HealthWeightSample(val kg: Double, val at: Instant, val sourceId: String)`. `NoOpHealthSync` boş liste döner.
- `HealthSync.ios.kt`: `requestAuthorization()` içinde `readTypes = setOf(bodyMassType)`; `readWeights` → `HKSampleQuery` (sort by end date desc, limit `HKObjectQueryNoLimit`), `HKQuantity.doubleValueForUnit(HKUnit.gramUnitWithMetricPrefix(Kilo))`. **Kilom'un kendi kaynağından yazılanlar** (`HKSource.defaultSource()`) hariç tutulur → döngüsel çoğalma yok. Hata → boş liste (çökme yok). Yeni `HKQuantity`/query import'ları ekle.
- Android `actual` (varsa `HealthSync.android.kt`): `readWeights` = boş liste (Health Connect okuma bu sürümde kapsam dışı) – gerçek `actual`, derleme bozulmaz.

## Faz 2 – Domain/ViewModel/UI
- `domain/HealthImport.kt` (yeni, saf Kotlin): `fun mergeHealthSamples(existing: List<WeightEntry>, samples: List<HealthWeightSample>): List<WeightEntry>` — aralık dışı (20–500 kg) ele, 1 ondalığa yuvarla, aynı dakika (±60 sn) ve ±0.05 kg içindeki mevcut kayıtlarla çakışanı atla, yeni id üret (`WeightEntry.create`).
- ViewModel: `importFromHealth()` → authorize → read → merge → `weights.replaceEntries` → `HealthSyncNotice`-benzeri `HealthImportNotice(count, isSuccess)`.
- `BackupSheet.kt` Health bölümüne "Apple Health'ten içe aktar" butonu; onboarding sheet'te de "Mevcut kayıtları içe aktar" seçeneği.
- Strings EN+TR: `health_import_button`, `health_import_loading`, `health_import_success` (%1$d), `health_import_empty`, `health_import_denied`.

## Gizlilik
Okunan veri sadece cihazda `weight_entries_v1` içine kg olarak yazılır; ağ yok.

## Testler (`commonTest`)
`HealthImportTest`: aralık dışı elenir, çakışma elenir, boş girdi, ondalık yuvarlama, yeniden içe aktarma çoğaltma yapmaz.
