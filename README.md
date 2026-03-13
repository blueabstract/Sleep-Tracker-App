# 💤 Sleep Tracker by Subarno

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/Min%20SDK-API%2024-blue?style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Gradle%20KTS-02303A?style=for-the-badge&logo=gradle&logoColor=white)

A night-mode Android app to monitor your sleep patterns — track sessions, visualize sleep stages in real time, set bedtime goals, and view historical sleep analytics.

---

## 📱 Screenshots

...Later

## ✨ Features

- ▶ **Start / ■ Stop** sleep tracking with real-time HH:MM:SS chronometer
- 🌙 **Sleep Stage Detection** — automatically transitions through stages based on elapsed time
- 📊 **Goal Progress Bar** — visual fill bar showing progress toward your sleep goal
- ⏰ **Bedtime Goal Setting** — set a target sleep duration via TimePicker
- 💾 **Session History** — saves last 30 sessions using SharedPreferences
- 📈 **Analytics** — shows average sleep, last session duration, and total session count
- ↺ **Reset** with confirmation dialog
- ⚙️ **Settings** — update goal, clear all history
- 🌑 **Dark / Night-mode UI** throughout

---

## 🛏️ Sleep Stage Logic

| Time Elapsed | Stage |
|---|---|
| 0 – 5 sec *(test)* / 0 – 20 min *(real)* | 💤 Light Sleep |
| 5 – 10 sec *(test)* / 20 – 90 min *(real)* | 🌊 NREM Sleep |
| 10 – 15 sec *(test)* / 90 min – 3 hr *(real)* | 🌑 Deep Sleep |
| 15 sec+ *(test)* / 3 hr+ *(real)* | 🌈 REM Sleep |

> The app is currently configured with **test timings (seconds)** for demonstration purposes. To switch to real sleep durations, update the constants in `MainActivity.kt`:
> ```kotlin
> const val NREM_MS = 20L  * 60 * 1000   // 20 min
> const val DEEP_MS = 90L  * 60 * 1000   // 1.5 hr
> const val REM_MS  = 180L * 60 * 1000   // 3 hr
> ```

---

## 🗂️ Project Structure

```
SleepTracker/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/sleeptracker/
│       │   ├── MainActivity.kt          # Core logic
│       │   └── Chronometer.kt           # Custom HH:MM:SS timer widget
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml    # Main UI
│           │   └── dialog_settings.xml  # Settings dialog
│           ├── drawable/
│           │   ├── card_rounded.xml
│           │   ├── card_rounded_dark.xml
│           │   └── progress_bar_style.xml
│           ├── menu/
│           │   └── main_menu.xml
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── gradle.properties
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Kotlin 2.0+
- Android API 24+ (Android 7.0 Nougat)
- Gradle 8.7.3

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/SleepTracker.git
   ```

2. **Open in Android Studio**
   - File → Open → select the `SleepTracker/` folder
   - Wait for Gradle sync to complete

3. **Run the app**
   - Connect a device or start an emulator
   - Press **Shift + F10** or click ▶ Run

---

## 🔧 Built With

| Tool | Purpose |
|---|---|
| Kotlin | Primary language |
| Android SDK (API 24+) | Platform |
| AppCompat | UI components & theming |
| SharedPreferences | Local data persistence |
| Handler + Runnable | Real-time stage updates |
| TimePicker Dialog | Goal setting UI |
| Gradle Kotlin DSL | Build configuration |

---

## 📦 Dependencies

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.0"
coreKtx = "1.13.1"
appcompat = "1.7.0"
material = "1.12.0"
activity = "1.9.0"
constraintlayout = "2.1.4"
```

---

## 🎓 About

This project was built as **Experiment 8** for an Android Development lab —
**"Build a Sleep-Tracker App Using Android Studio"**

**Developer:** Subarno
**Institute:** KIIT University
**Language:** Kotlin
**Build System:** Gradle Kotlin DSL

---

## 📄 License

```
MIT License — feel free to use, modify, and distribute.
```

---

> Made with ☕ and way too little sleep by **Subarno**
