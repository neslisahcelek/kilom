# 03 – Hedef Kilo ve İlerleme — Faz 1 (domain) + Faz 2 (UI)

## Faz 1 – Domain (yeni dosya `domain/GoalProgress.kt`, saf Kotlin)
```kotlin
data class GoalProgress(
    val goalKg: Double,
    val startKg: Double,      // hedef belirlendiği andaki / en eski kayıt
    val currentKg: Double,
    val remainingKg: Double,  // |current-goal|
    val fraction: Float,      // 0..1, yönden bağımsız; hedef aşıldıysa 1
    val reached: Boolean,
    val estimatedDate: Instant?, // son 30 günlük eğimden; eğim hedefe doğru değilse null
)
fun computeGoalProgress(goalKg: Double?, startKg: Double?, entries: List<WeightEntry>, now: Instant, tz: TimeZone): GoalProgress?
```
- Hedef 20–500 kg aralığında doğrulanır (`validateGoal` → mevcut `parseWeightInput` yeniden kullanılır).
- Tahmini tarih: son 30 gün (en az 3 kayıt, ≥7 gün aralık) için doğrusal eğim kg/gün; yön hedefe doğru değilse veya |eğim|<0.01 → `null`.
- Delta/tarih hesapları yerel saat dilimi; kg her zaman kg.
- `commonTest/GoalProgressTest.kt`: kilo verme, kilo alma, hedefe ulaşıldı, tek kayıt, boş liste, yanlış yönde eğim, tahmin tarihi.

## Faz 2 – Entegrasyon
- `PrefsRepository`: `goalKg: StateFlow<Double?>` (default `null`), `setGoalKg(Double?)`, `goalStartKg`. Eski kayıtlarda alan yok → null (geriye uyum).
- ViewModel: `UiState.goal: GoalProgress?`, `UiState.isGoalSheetOpen`; `openGoal()/saveGoal(input)/clearGoal()`. Giriş mevcut birime göre (lb → kg çevrilir, aralık doğrulanır).
- UI: `HeroCard` altında `GoalCard` (yeni `ui/components/GoalCard.kt`, `GlassCard` tabanlı): ilerleme çubuğu, "Hedefe X kg kaldı", tahmini tarih (varsa), "Hedef belirle" CTA. Giriş için `EntrySheet` desenine uygun küçük `GoalSheet`.
- Strings EN+TR: `goal_title`, `goal_set_cta`, `goal_remaining` (%1$s), `goal_reached`, `goal_eta` (%1$s), `goal_input_hint`, `goal_clear`.
- `LazyColumn` key'lerine dokunma; `@Preview` ekle.
