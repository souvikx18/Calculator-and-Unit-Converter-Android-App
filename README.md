# Numera — Scientific Calculator & Offline Unit Converter

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-42%20Passed%20(100%25)-brightgreen?style=for-the-badge&logo=checkmarx&logoColor=white)](#testing)
[![Offline](https://img.shields.io/badge/Network-100%25_Offline-blue?style=for-the-badge&logo=airplay&logoColor=white)](#privacy--offline-first)
[![License](https://img.shields.io/badge/License-Apache_2.0-orange?style=for-the-badge)](#license)

**A high-performance, modern Android calculator and universal unit converter crafted with Jetpack Compose, Material 3, and Kotlin Coroutines.**

[Features](#key-features) • [Installation](#installation--getting-started) • [Architecture](#architecture--tech-stack) • [Building & Testing](#building--testing) • [Screenshots](#ui--theme-modes)

</div>

---

## Overview

**Numera** is designed for students, engineers, and everyday users who demand precision, speed, and clean ergonomics. Built from the ground up using **Modern Android Development (MAD)** best practices, Numera delivers an instant live-evaluating math engine, a comprehensive scientific keypad, persistent calculation history, and a rich 10-category offline unit converter — completely free of ads, tracking, or network latency.

---

## Key Features

### 🧮 1. Advanced Math Engine & Scientific Calculator
* **Live Calculation Previews**: Expressions evaluate in real time on an asynchronous background coroutine as you type.
* **Standard & Scientific Keypads**:
  * **Trigonometry**: `sin`, `cos`, `tan`, and their inverses (`asin`, `acos`, `atan`).
  * **Hyperbolic Functions**: `sinh`, `cosh`, `tanh`.
  * **Logarithms & Powers**: Natural log (`ln`), base-10 log (`log`), square root (`√`), cube root (`∛`), arbitrary powers (`x^y`), `x²`, `x³`, and reciprocals (`1/x`).
  * **Combinatorics & Constants**: Factorials (`n!`), percentages (`%`), $\pi$ (`3.14159...`), and Euler's constant $e$ (`2.71828...`).
* **Precise Degree / Radian Modes**:
  * Instant toggle between **DEG** and **RAD**.
  * Exact textbook evaluations for benchmark degree angles (e.g. $\sin(180^\circ) = 0$, $\cos(90^\circ) = 0$, with undefined asymptote detection for $\tan(90^\circ)$).
* **Smart Keypad Flow**:
  * Seamless operator chaining: Typing an operator (`+`, `−`, `×`, `÷`) after pressing `=` immediately builds upon the previous answer.
  * Smart parenthesis balancing and context-aware unary sign toggling (`±`).
* **Memory Registers**: Full standard memory system: `MC` (Clear), `MR` (Recall), `M+` (Add), and `M−` (Subtract).

### 🔄 2. Instant Offline Unit Converter
Accurate, real-time bidirectional conversion across **10 key measurement categories**:

| Category | Supported Units |
| :--- | :--- |
| 📏 **Length** | Meter, Kilometer, Centimeter, Millimeter, Mile, Yard, Foot, Inch, Nautical Mile |
| ⚖️ **Mass & Weight** | Kilogram, Gram, Milligram, Metric Ton, Pound (lb), Ounce (oz), Stone |
| 🌡️ **Temperature** | Celsius (°C), Fahrenheit (°F), Kelvin (K) |
| 🚀 **Speed** | Meter/sec (m/s), km/h, Miles/hour (mph), Knots (kn), Feet/sec (ft/s) |
| 📐 **Area** | Square Meter, Square Kilometer, Square Foot, Square Yard, Acre, Hectare |
| 🧪 **Volume** | Liter, Milliliter, Cubic Meter, Gallon (US), Quart, Pint, Fluid Ounce, Cup |
| 💾 **Digital Data** | Bit, Byte, Kilobyte (KB), Megabyte (MB), Gigabyte (GB), Terabyte (TB), Petabyte (PB) |
| ⏱️ **Time** | Millisecond, Second, Minute, Hour, Day, Week, Month, Year |
| 💨 **Pressure** | Pascal (Pa), Bar, PSI, Atmosphere (atm), Torr / mmHg |
| ⚡ **Energy** | Joule (J), Kilojoule (kJ), Calorie (cal), Kilocalorie (kcal), Watt-hour (Wh), kWh, BTU |

### 📜 3. Persistent Calculation History
* Powered by **Room Database** with Kotlin Coroutines and `Flow`.
* Timestamped history records preserving expressions and formatted results.
* **One-tap Restore**: Tap any saved calculation to load the expression or result back into the active keypad.
* **Management**: Copy to clipboard, swipe-to-delete individual items, or wipe history with one click.

### 🎨 4. Material 3 Themes & Haptics
* **4 Curated Theme Modes**:
  1. **AMOLED Pure Black** (`#000000`): Maximum battery efficiency for OLED displays.
  2. **Deep Charcoal**: Balanced dark palette with refined contrast.
  3. **Crisp Light**: High-contrast, clean day mode.
  4. **System Default**: Follows Android OS dynamic theme.
* **Tactile Haptic Feedback**: Optional vibration feedback tuned for responsive touch typing.

### 🔒 5. Privacy & Offline-First
* **Zero network requirements**: The calculator and all unit conversions run 100% offline.
* **Zero tracking / Zero ads**: No third-party analytical SDKs or data collection.

---

## Architecture & Tech Stack

Numera adheres strictly to Google's recommended Android architecture guide:

```
app/src/main/java/com/example/
├── data/
│   ├── local/              # Room Database, DAOs, and Entities
│   └── preferences/        # DataStore / SharedPrefs (theme, angles, haptics)
├── domain/
│   ├── calculator/         # Mathematical Expression Evaluator & Tokenizer
│   └── converter/          # Unit Conversion Engine & Metric Models
└── ui/
    ├── calculator/         # Compose UI screens, keypad layouts, and ViewModel
    ├── components/         # Reusable Material 3 buttons, badges, displays
    ├── converter/          # Unit converter Compose screens and selector chips
    ├── history/            # History bottom sheet and item views
    ├── navigation/         # Tab navigation between Calculator and Converter
    ├── settings/           # Personalization and configuration dialog
    └── theme/              # Color palettes, Typography, and Theme providers
```

* **UI Layer**: Jetpack Compose, Material 3 Design Components, Unidirectional Data Flow (UDF).
* **State Management**: Kotlin Coroutines & `StateFlow` (`viewModelScope`).
* **Math Parsing**: Recursive Shunting-Yard expression tokenizer and evaluator with IEEE-754 precision safeguards and degree/radian boundary normalization.
* **Persistence**: Android Jetpack Room with SQLite for calculation history.
* **Dependency & Build**: Gradle 8.11+ / 9.3+ with Android Gradle Plugin (AGP), Kotlin 2.0+, and Java 21.

---

## Installation & Getting Started

### Ready-to-Install APK
You can install the compiled debug build directly:
* **Pre-built APK**: [`Numera-Calculator.apk`](file:///D:/Projects/Calculator-and-Unit-Converter-Android-App/Numera-Calculator.apk) *(22.5 MB)*
* **Build Artifact**: `app/build/outputs/apk/debug/app-debug.apk`

#### Install via ADB
```bash
adb install -r "D:\Projects\Calculator-and-Unit-Converter-Android-App\Numera-Calculator.apk"
```

---

## Building & Testing

### Prerequisites
* **Java Development Kit**: JDK 21 (Adoptium Temurin recommended)
* **Android SDK**: Build-Tools `36.0.0`, Platform `android-36`
* **Gradle Wrapper**: Bundled in repository (`gradlew.bat` / `./gradlew`)

### 1. Run Unit & Robolectric Tests
The test suite validates mathematical correctness, edge cases (division by zero, domain limits, textbook degree values), and ViewModel state transitions:

```powershell
.\gradlew.bat test
```

> **Test Results**: All **42 unit tests pass** with zero failures.

### 2. Assemble Debug APK
To compile and package the latest APK:

```powershell
.\gradlew.bat assembleDebug
```

The output will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## Verification & Test Coverage

The test suite covers:
* ✅ **Arithmetic Precedence (BODMAS/PEMDAS)**: Complex multi-operator formulas and bracket nesting.
* ✅ **Scientific Notation**: Distinguishing standard exponent tokens (e.g., `1.5e3`, `2.4E-6`) from Euler's constant $e$.
* ✅ **Trigonometric Precision**: Exact angle handling for degrees ($\sin 180^\circ = 0$, $\cos 90^\circ = 0$, and domain error handling for $\tan 90^\circ$).
* ✅ **Unary Minus & Sign Toggle (`±`)**: Smart prefix toggling on individual terms, composite expressions, and parenthesized groups.
* ✅ **Converter Normalization**: Correct parsing and zero-state normalization across all 10 unit categories.
* ✅ **Keypad State Flow**: Equal chaining, leading zero replacement, and Unicode minus (`−` / `–`) compatibility.

---

## License

```text
Copyright 2026 Numera Project Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
