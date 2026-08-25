# Electricity Meter Collector (Android) ⚡📱

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A specialized, offline-first Android application designed for commercial electricity generator operators and utility collectors to manage subscribers, record smart meter readings, calculate kilowatt tariffs, and issue invoices in real-time.

---

## ⚡ Core Functionality

- **👥 Subscriber & Route Management:** Categorize customers by geographical routes, meter IDs, and tariff categories.
- **📟 Meter Reading Input & Validation:** Automatic delta calculation between previous and current readings to eliminate input errors.
- **🧾 Instant Billing & Invoicing:** Automatic computation of consumption, fixed network fees, and total balances.
- **📊 Collection & Debt Tracking:** Real-time ledger of paid vs. overdue accounts with financial summaries.
- **📴 Offline-First Reliability:** Operates with 100% reliability in remote field locations with no cellular network.

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Database:** Room (SQLite) with automatic JSON export/backup
- **Architecture:** MVVM Architecture

---

## 🚀 Build Instructions

```bash
git clone https://github.com/monestw-cell/electricity-meter-collector.git
cd electricity-meter-collector
./gradlew assembleRelease
```

---

## 📄 License
Licensed under the [MIT License](LICENSE).
