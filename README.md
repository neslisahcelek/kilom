# Kilom ⚖️🌸🏋️

[![Platform](https://img.shields.io/badge/Platform-iOS%20%7C%20Android-blue.svg)](https://github.com/neslisahcelek/kilom)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device-success.svg)](https://neslisahcelek.github.io/kilom/)
[![Apple Health](https://img.shields.io/badge/HealthKit-Two--Way%20Sync-FF2D55.svg)](https://developer.apple.com/healthkit/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A minimal, distraction-free, liquid-glass weight tracker and hormonal & fitness companion for iOS and Android. Step on your scale, snap a photo or enter your weight, and get on with your day—with zero cloud servers, zero trackers, and 100% on-device privacy.

---

## ✨ Features

### 📸 Scale Photo OCR & Instant Weigh-In
- **On-Device Vision Recognition:** Point your camera or pick from the gallery—digits are parsed locally using Apple Vision Framework (iOS) and Google ML Kit (Android).
- **7-Segment Display Support:** Dedicated on-device reader for LED/LCD bathroom scales with noise and glare tolerance.
- **Immediate Top-of-Screen Access:** Camera, gallery, and manual entry buttons are positioned right below your current weight card for frictionless daily check-ins.
- **Safety First:** OCR values are never saved automatically; they pre-fill the input sheet for user review and confirmation.
- **Instant Delta Context:** See weight difference vs. your last weigh-in (`-0.7 kg` / `+0.4 kg`) and days elapsed at a glance.
- **Unit Toggle:** Seamlessly switch between `kg` and `lb` anytime (data is always stored natively in kilograms).

### 🌸 Dynamic Menstrual Cycle & Evidence-Based Nutrition
- **Dynamic Cycle Length (21–45 Days):** Customizable cycle length with Backward Luteal Mapping ($ovulation = length - 14$) accommodating diverse biological rhythms.
- **14 Micro-Period Hormonal Analytics:** Daily insights into estrogen, progesterone, and aldosterone fluctuations.
- **Water Retention Shield:** Understand natural 0.8–2.5 kg premenstrual fluid shifts so you never confuse hormonal water retention with body fat.
- **Phase-Specific Nutrition:** Macronutrient ratios and critical micronutrient guidance (iron, magnesium, zinc, omega-3) backed by ACOG and PubMed literature.
- **Cycle-Synced Strength Training:** 4-tier workout intensity mapping (Rest, Moderate, High, Peak Power), PR lift windows (Squat, Deadlift, Hip Thrust), ACL ligament laxity precautions during ovulatory estrogen peaks, and PMS deload strategies (based on Dr. Stacy Sims' research).
- **In-App Scientific Sources:** Direct medical citations and peer-reviewed studies available in the Sources tab.

### 🏋️‍♂️ Resistance Training & Workout CSV Integration
- **100% On-Device Workout Parser:** Import your resistance training history (`workouts.csv`) without third-party APIs or cloud sync.
- **Automatic Muscle Grouping:** Automatically classifies exercises into Legs/Glutes, Back/Pull, Chest/Push, Shoulders/Arms, and Core.
- **Volume & 1RM Calculations:** Calculates total session tonnage and estimated 1RM using the validated Epley formula.
- **DOMS Muscle Repair Fluid Retention Alert:** Automatically detects heavy leg/back sessions (>8,000 kg volume or high intensity in the last 48 hours). If your scale increases, Kilom reassuringly alerts you that it is temporary muscle inflammation fluid and glycogen retention, not body fat!
- **Workout & Cycle Correlation Sheet:** Visualizes 4-phase volume load, PR distribution heatmaps across cycle phases, luteal RPE/exertion shifts, and compound lift relative strength (ratio to body weight).

### 🔥 Weigh-In Consistency Matrix & Liquid Glass Share Card
- **Streak & Habit Tracking:** Tracks current streak, best streak, total weigh-in days, and monthly consistency percentage.
- **14-Day Visual Heatmap:** Clean, liquid-glass habit matrix displaying your recent weigh-in regularity.
- **Privacy-Preserving Share Card:** Export beautiful liquid-glass milestone cards via the native OS share sheet, with an option to hide your actual weight and share only your streak.

### ❤️ Apple Health & Data Sovereignty
- **Two-Way HealthKit Integration (iOS):** Writes weight logs to Apple Health and imports past weight records on demand. A live status badge on the dashboard shows connection status.
- **Goal Weight & Smart ETA:** Set your target weight and view dynamic progress percentage with estimated completion dates based on your moving average.
- **Moving Average Trend Line & Period Summaries:** Filters out short-term water fluctuations to reveal your true body mass trajectory.
- **Optional Local Daily Reminder:** Schedule a daily notification at your preferred time—generated strictly by the local OS scheduler with zero push servers.
- **Full Data Ownership:** Complete export and restore in standard CSV and JSON formats.

---

## 🔒 100% On-Device Privacy Architecture

Kilom is built from the ground up on the principle of zero data leakage:
- **Zero Accounts:** No email, phone number, passwords, or profile setup required.
- **Zero Cloud Servers:** No remote database, no cloud storage, and no backend API.
- **Zero Third-Party SDKs / Trackers:** Zero advertising networks, zero analytics SDKs (no Firebase Analytics, Google Analytics, Mixpanel, etc.).
- **Zero Cloud AI / LLM Calls:** All cycle calculations, nutrition advice, workout correlations, and DOMS assessments run locally in deterministic Kotlin.
- **Volatile Image Processing:** Scale photos are processed strictly in RAM for OCR and immediately released; photos are never stored to disk or transmitted over the network.

---

## 🛠️ Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Platform** | Kotlin Multiplatform (KMP), Compose Multiplatform |
| **Platforms Supported** | iOS (Native UIKit / Compose), Android |
| **Vision & OCR** | Apple Vision Framework (`VNRecognizeTextRequest`) on iOS, Google ML Kit on Android |
| **Storage & Serialization** | `multiplatform-settings` (Encrypted local key-value store), `kotlinx.serialization` (JSON) |
| **UI & Styling** | Liquid-glass aesthetic, `dev.chrisbanes.haze` (blur & glass refraction), custom specular gradients |
| **Health Integration** | Apple HealthKit (`HKHealthStore`) on iOS |
| **Haptics** | iOS `UIImpactFeedbackGenerator`, Android `Vibrator` |

---

## 🏗️ Project Architecture

```
composeApp/src/commonMain/kotlin/app/kilo/
├── App.kt                          # Root entry, AppContainer DI, theme wrapper
├── data/
│   ├── Repositories.kt             # WeightRepository, PrefsRepository, WorkoutRepository
│   ├── HevyModels.kt               # WorkoutSession, Exercise, Set, MuscleCategory
│   └── HevyCsvParser.kt            # On-device workouts.csv parser
├── domain/
│   ├── Weight.kt                   # WeightEntry, WeightUnit, WeightDelta
│   ├── WeightInput.kt              # Input validation (20–500 kg range)
│   ├── TrendAnalytics.kt           # Moving average & rate of change
│   ├── GoalProgress.kt             # Goal progress & dynamic ETA calculation
│   ├── PeriodSummary.kt            # Weekly & monthly aggregations
│   ├── StreakAnalytics.kt          # Consistency matrix & habit heatmap
│   ├── CycleAnalytics.kt           # Dynamic cycle length (21–45d), backward luteal mapping, advice
│   ├── WorkoutCorrelationAnalytics.kt # DOMS risk evaluation & cycle-workout correlation
│   └── WeightExportImport.kt       # CSV / JSON backup and restore
├── ocr/
│   ├── ScaleOcr.kt                 # expect ScaleOcr interface
│   ├── WeightParser.kt             # Pure Kotlin weight candidate extractor
│   └── SevenSegmentReader.kt       # 7-segment digital screen reader
├── platform/
│   ├── Haptics.kt                  # expect Haptics & rememberHaptics
│   ├── PhotoPicker.kt              # FileKit wrapper (Camera & Gallery)
│   ├── CameraCapture.kt            # expect CameraCapture interface
│   ├── ExifDateParser.kt           # Photo capture date parser
│   ├── HealthSync.kt               # Two-way Apple Health & Health Connect sync
│   ├── ReminderScheduler.kt        # Local on-device notification scheduler
│   └── ShareSheet.kt               # Native OS share sheet integration
└── ui/
    ├── DashboardScreen.kt          # Main dashboard composable
    ├── DashboardViewModel.kt       # Dashboard state machine & StateFlow
    ├── theme/                      # Liquid-glass palette & typography
    └── components/
        ├── ActionBar.kt            # Primary action bar (Camera, Gallery, Manual, Unit toggle)
        ├── GlassCard.kt            # Core liquid-glass card wrapper
        ├── HeroCard.kt             # Current weight, delta, and days elapsed
        ├── DomsAlertBanner.kt      # Muscle repair fluid retention alert banner
        ├── GoalCard.kt             # Goal weight & progress card
        ├── GoalSheet.kt            # Goal setting bottom sheet
        ├── CycleCard.kt            # Menstrual cycle & nutrition guide card
        ├── CycleSheet.kt           # 4-tab interactive cycle, nutrition & strength sheet
        ├── WorkoutCorrelationSheet.kt # Volume by phase, PR heatmaps, and relative strength
        ├── StreakCard.kt           # Consistency matrix & 14-day habit heatmap
        ├── ShareCardSheet.kt       # Milestone & streak liquid-glass share card
        ├── SummaryCard.kt          # Period summary card (Week / Month)
        ├── TrendChart.kt           # Interactive moving average weight trend chart
        ├── HistoryRow.kt           # Swipe-to-delete weight history item
        ├── EntrySheet.kt           # Manual entry & OCR verification modal
        ├── BackupSheet.kt          # Backup, export/import, Workout CSV, and settings sheet
        └── IosDatePicker.kt        # Liquid-glass calendar date picker
```

---

## 🧪 Verification & Testing

```powershell
# Run all unit tests (common & Android)
.\gradlew.bat :composeApp:testDebugUnitTest

# Verify iOS compilation (via iOS Simulator ARM64)
.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64

# Build Android Debug APK
.\gradlew.bat :composeApp:assembleDebug
```

---

## 📄 Privacy Policy

Kilom collects zero personal data. Read the full policy:  
👉 [https://neslisahcelek.github.io/kilom/](https://neslisahcelek.github.io/kilom/)

---

## 👤 Author

Developed with care by **Neslişah Çelek**  
Contact: [neslisah.celek@outlook.com](mailto:neslisah.celek@outlook.com)