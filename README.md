<div align="center">

# ⛅ Pakistan Weather Hub

**A real-time weather application built natively for Pakistan**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/AI-Gemini%20Flash-FF6F00?style=flat-square&logo=google&logoColor=white)](https://aistudio.google.com)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024-blue?style=flat-square)](https://android.com)

*Combining live weather data, AI-powered travel advice, and extreme-weather alerts — all tailored for Pakistan's 18 major cities.*

</div>

---

## 📖 About

Pakistan Weather Hub is a fully native Android application that delivers real-time meteorological data, a 7-day forecast, province-level weather mapping, and AI-powered climate guidance — all built specifically around Pakistan's geography and culture. The AI advisor speaks to you in context: it knows whether you're in Karachi's summer heat or Gilgit's winter chill, and adjusts its recommendations accordingly.

This project was built from scratch as part of a hands-on exploration of Android development, REST API integration, and on-device AI implementation.

---

## ✨ Features

### 🌤 Weather Data
- Live conditions for **18 major Pakistani cities** (temperature, humidity, wind speed)
- **7-day forecast** with daily min/max and WMO weather icons
- **12-hour hourly charts** for wind speed and humidity trends
- Powered by the **Open-Meteo API** — no API key required for weather data

### 🗺 Interactive Province Map
- Tap any province on a hand-drawn Pakistan map to switch cities instantly
- Covers **Sindh, Punjab, Khyber Pakhtunkhwa, Balochistan, Gilgit-Baltistan**, and Azad Kashmir
- Searchable city dropdown with province labels

### 📍 GPS Auto-Detection
- Detects your location using the **Fused Location Provider**
- Uses the **Haversine great-circle formula** to match your coordinates to the nearest supported city

### 🚨 Extreme Weather Alerts
- Automatic **push notifications** when thresholds are crossed:
  - Heatwave — Temperature > 40°C
  - Duststorm — Wind speed > 45 km/h
  - Flood/Monsoon — Heavy rain WMO codes detected
- Manual alert simulators for testing included in the dashboard

### 🤖 AI Weather Chatbot
- Conversational assistant powered by **Google Gemini Flash**
- Specializes in clothing recommendations (lawn, cotton, woolens) based on your city and temperature
- Warns about regional hazards — Murree landslides, Punjab fog on GT Road/M-2, Jacobabad heatwaves
- Greets you with *Assalam-o-Alaikum* and keeps responses culturally grounded

### ✈️ Travel Advisor
- AI-generated highway status for M-2 Motorway, Karakoram Highway, Coastal Highway, GT Road
- Context-aware destination recommendations (e.g., Kund Malir from Karachi, Monal from Islamabad)
- Simulated Google Trends data showing regional weather search interest

### 🔐 User Authentication
- Email/password registration and login stored locally via **Room database**
- Simulated Google Sign-In flow
- Session persists across app restarts; last selected city is remembered per user

### 🌙 Dark Mode
- Material Design 3 theming with dark mode enabled by default
- Toggle accessible from the dashboard header

---

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.09) + Material 3 |
| Architecture | MVVM with StateFlow |
| Local Database | Room 2.7 (SQLite) |
| Networking | Retrofit 2.12 + OkHttp 4.10 + Moshi |
| AI | Google Gemini Flash via REST API |
| Weather Data | Open-Meteo (free, no auth) |
| Location | Google Play Services FusedLocationProvider |
| Async | Kotlin Coroutines + viewModelScope |
| DI / Config | Secrets Gradle Plugin (`.env` → BuildConfig) |
| Testing | JUnit 4 + Robolectric + Roborazzi + Espresso |

---

## 📸 Screenshots

> *Interface showing the main dashboard, AI chatbot, and travel advisor screens.*

| Dashboard | AI Chatbot | Travel Advisor |
|:-:|:-:|:-:|
| Real-time weather + 7-day forecast | Gemini-powered regional advisor | Highway status + AI trip suggestions |

---

## 🏗 Architecture

The app follows clean **MVVM** with unidirectional data flow:

```
UI (Compose Screens)
        │
        ▼
WeatherViewModel (AndroidViewModel)
        │
   ┌────┴────┐
   │         │
UserRepo   Retrofit
(Room DB)  (APIs)
```

- **UI layer** — Pure Compose composables, no business logic
- **ViewModel** — Holds `StateFlow<UserEntity?>` for session; drives navigation reactively
- **Repository** — Abstracts all Room DAO operations (register, login, logout, city update)
- **Network** — Separate Retrofit clients for Open-Meteo and Gemini with independent timeouts

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- Android SDK with API 24–36 installed
- A Google Gemini API key — get one free at [aistudio.google.com](https://aistudio.google.com)

### Installation

**1. Clone or extract the project**

```bash
git clone https://github.com/your-username/pakistan-weather-hub.git
cd pakistan-weather-hub
```

**2. Add your API key**

Create a `.env` file in the project root:

```
GEMINI_API_KEY=your_gemini_api_key_here
```

> See `.env.example` for the required format. Never commit this file.

**3. Fix the signing config**

Open `app/build.gradle.kts` and remove this line from the `debug` block:

```kotlin
signingConfig = signingConfigs.getByName("debugConfig")
```

**4. Build and run**

Open the project in Android Studio, select your device or emulator, and hit **Run ▶**.

On first launch the app will ask for notification permission, then take you straight to the login screen.

---

## 🗂 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Entry point + screen router
├── database/
│   ├── UserEntity.kt                # Room entity (users table)
│   ├── UserDao.kt                   # DAO with suspend + Flow queries
│   ├── UserRepository.kt            # Auth business logic
│   └── WeatherDatabase.kt           # Singleton DB instance
├── models/
│   └── WeatherModels.kt             # API response data classes + 18 city definitions
├── network/
│   ├── WeatherApiService.kt         # Open-Meteo Retrofit interface
│   └── GeminiApiService.kt          # Gemini AI Retrofit client
└── ui/
    ├── screens/
    │   ├── AuthScreen.kt            # Login / Register UI
    │   ├── WeatherDashboardScreen.kt # Main weather view
    │   ├── ChatbotScreen.kt         # Gemini AI chat
    │   ├── TravelSuggestionsScreen.kt # Travel advisor
    │   └── LocationHelper.kt        # GPS + Haversine city matching
    ├── theme/                       # M3 colors, typography, theme
    └── viewmodel/
        └── WeatherViewModel.kt      # Session + dark mode state
```

---

## 🌍 Supported Cities

Karachi · Lahore · Islamabad · Rawalpindi · Faisalabad · Peshawar · Quetta · Multan · Sialkot · Hyderabad · Bahawalpur · Sargodha · Gujranwala · Sukkur · Gwadar · Gilgit · Muzaffarabad · Murree

---

## 🔑 Environment Variables

| Variable | Required | Description |
|---|---|---|
| `GEMINI_API_KEY` | Yes | Google Gemini API key for chatbot and travel advisor |

Weather data from Open-Meteo requires no key — it is completely free.

---

## 🧪 Running Tests

```bash
# Unit tests (JVM)
./gradlew :app:testDebugUnitTest

# Instrumented tests (requires connected device/emulator)
./gradlew :app:connectedDebugAndroidTest

# Screenshot test baselines
./gradlew :app:recordRoborazziDebug
```

---

## 📄 License

```
MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.
```

---

## 🙏 Acknowledgements

- [Open-Meteo](https://open-meteo.com) — free, open-source weather API
- [Google Gemini](https://aistudio.google.com) — generative AI platform
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — modern Android UI toolkit
- [Material Design 3](https://m3.material.io) — design system

---

<div align="center">
Made with ☕ and a lot of weather-checking in Pakistan
</div>
