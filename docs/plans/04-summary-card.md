# 04 – Haftalık / Aylık Özet Kartı — Faz 1 (domain) + Faz 2 (UI)

## Faz 1 – Domain (yeni `domain/PeriodSummary.kt`; `TrendAnalytics.kt` yeniden kullanılır, değiştirilmez)
```kotlin
enum class SummaryPeriod { WEEK, MONTH }
data class PeriodSummary(
    val period: SummaryPeriod,
    val entryCount: Int,
    val averageKg: Double,
    val minKg: Double,
    val maxKg: Double,
    val changeKg: Double?,      // dönemdeki son - ilk kayıt (≥2 kayıt yoksa null)
    val previousAverageKg: Double?, // bir önceki eşdeğer dönem ortalaması
)
fun buildPeriodSummary(entries: List<WeightEntry>, period: SummaryPeriod, now: Instant, tz: TimeZone): PeriodSummary?
```
- WEEK = son 7 gün, MONTH = son 30 gün (`filterEntriesForTrend` ile `TrendRange.LAST_7` / `DAYS_30` aynı kesim mantığı). Kayıt yoksa `null`.
- Yuvarlama 1–2 ondalık (`round2` mevcut yardımcı). kg saklanır; görünümde kg/lb çevrilir (UI).
- `commonTest/PeriodSummaryTest.kt`: boş, tek kayıt, çoklu kayıt, DST/gece yarısı sınırı, önceki dönem karşılaştırması.

## Faz 2 – UI
- `ui/components/SummaryCard.kt` (`GlassCard`): Hafta/Ay seçici (segment), ortalama, min–max, değişim oku (renk `Colors`), önceki döneme göre fark.
- `DashboardScreen.kt` içinde `TrendChart` yakınına yerleştir; `UiState.summaryPeriod` + `setSummaryPeriod()`; summary `history`'den türetilir (saklanmaz).
- Strings EN+TR: `summary_title`, `summary_week`, `summary_month`, `summary_average`, `summary_range`, `summary_change`, `summary_vs_previous`, `summary_empty`. `%` kullanılırsa `%%`.
- `@Preview` ekle.
