# 01 – HealthKit Görünürlüğü (Guideline 2.5.1) — Faz 2

## Sorun
HealthKit yalnızca `BackupSheet` içindeki gizli bölümde ("Apple Health / Health Connect"). Ana ekranda iz yok.

## Değişiklikler
1. **Ana ekran Health satırı/rozeti** (`DashboardScreen.kt`, HeroCard altında): kalp ikonu + "Apple Health" + durum ("Bağlı"/"Bağlı değil"). Dokununca Health bölümünü içeren sheet açılır (`openBackup()` veya yeni `openHealth()`). Sadece `state.isHealthSyncSupported` iken görünür. Mevcut `GlassCard` kullan.
2. **`HealthOnboardingSheet.kt`** (yeni, `ui/components/`): ilk kez (prefs `healthOnboardingSeen=false`) veya Health açma anında gösterilir. İçerik: Kilom Apple Health'e kilo kayıtlarını **yazar**, istersen **okuyup içe aktarır**; veriler cihaz dışına çıkmaz; istediğin an kapatırsın. Butonlar: "Bağla" (→ `setHealthSyncEnabled(true)`) / "Şimdi değil". Mevcut sheet/glass bileşenleri örnek alınır (`BackupSheet`, `EntrySheet`).
3. **`BackupSheet.kt`**: bölüm başlığı iOS'ta sadece "Apple Health"; Android'de "Health Connect". (`health_section_title` yerine platforma göre iki string: `health_section_title_ios`, `health_section_title_android`; common'da seçim için `HealthSync`'e `val displayName` veya `isIos` flag'i yerine `HealthSync.providerName` enum eklemek yeterli.)
4. **Strings** EN+TR: `health_badge_connected`, `health_badge_disconnected`, `health_onboarding_title`, `health_onboarding_body`, `health_onboarding_connect`, `health_onboarding_later`, `health_section_title_ios`, `health_section_title_android`.
5. **Prefs**: `PrefsRepository.healthOnboardingSeen: StateFlow<Boolean>` (default false) + setter. ViewModel: `UiState.isHealthOnboardingOpen`, `openHealthOnboarding()/dismissHealthOnboarding()/connectHealth()`.
6. `@Preview` ekle.

## Kabul kriterleri
- Ana ekranda Health rozeti görünür (iOS), izin diyalogu onboarding'den tetiklenir.
- Yedekleme sayfası iOS'ta "Apple Health" yazar.
- Eski prefs ile uygulama sorunsuz açılır.
- Test: ViewModel connect/dismiss akışı (`commonTest`, fake HealthSync).
