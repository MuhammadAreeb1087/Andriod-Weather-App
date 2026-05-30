# Pakistan Weather Hub — Technical Documentation
**Version:** 1.0.0
**Author:** AI Coding Assistant
**Project Type:** Android App (Kotlin + Jetpack Compose)
**Package/Application ID:** `com.aistudio.pakweather.kqlmop`

---

## 1. Executive Summary
**Pakistan Weather Hub** is a robust, high-fidelity meteorological application specifically optimized for users in Pakistan. The application integrates real-time weather retrieval with a localized interactive radar map, a context-aware AI travel advisor, local climate push notifications, and a conversational weather chatbot powered by Google Gemini. Data security and session management are handled via a local SQLite/Room database utilizing a simulated email/password and social Google Authentication system.

---

## 2. System Architecture & Tech Stack

The application conforms stringently to the modern **MVVM (Model-View-ViewModel)** architectural pattern. It features unidirectional data flows and a cleanly decoupled codebase separated into distinct layers:

```
                  ┌─────────────────────────────────────┐
                  │          Jetpack Compose            │
                  │              (UI Layer)             │
                  └──────────────────┬──────────────────┘
                                     │
                  ┌──────────────────▼──────────────────┐
                  │          WeatherViewModel           │
                  │         (State Holder Layer)        │
                  └──────────────────┬──────────────────┘
                                     │
             ┌───────────────────────┴───────────────────────┐
             ▼                                               ▼
┌─────────────────────────┐                     ┌─────────────────────────┐
│     UserRepository      │                     │    Retrofit / API       │
│    (Local Data Store)   │                     │  (Remote Network Store) │
└────────────┬────────────┘                     └────────────┬────────────┘
             │                                               │
             ▼                                               ▼
┌─────────────────────────┐                     ┌─────────────────────────┐
│       Room DB           │                     │  - Open-Meteo Weather   │
│    (SQLite Store)       │                     │  - Google Gemini AI     │
└─────────────────────────┘                     └─────────────────────────┘
```

### Core Technologies
*   **Language:** Kotlin (100% Type-safe)
*   **UI Framework:** Jetpack Compose (Declarative Android UI)
*   **Theme Engine:** Material Design 3 (Dynamic Color, Eye-Safe Dark Mode Theme)
*   **Local Databases:** Android Room SQLite Persistence Engine
*   **Networking Client:** Retrofit 2 + OkHttp 3 HTTP Stack
*   **Serialization Parser:** Moshi (Kotlin JSON serialization)
*   **AI Engine:** Google Gemini Pro / Flash API v1beta
*   **Positioning Services:** Google Play Services Location SDK

---

## 3. Detailed Component Breakdown

### 3.1 Network & API Services
The app manages outgoing requests via two decoupled Retrofit API clients:

#### A. Weather Service (`com.example.network.WeatherApiService`)
Fetches accurate, real-time meteorological conditions for geographical coordinates using the Open-Meteo Weather API endpoint.
*   **Endpoint:** `https://api.open-meteo.com/v1/forecast`
*   **Parameters Tracked:**
    *   `current`: Raw temperature at 2 meters, relative humidity, wind speed, and meteorological WMO weather codes.
    *   `daily`: Min and Max temperatures, daily WMO weather codes.
    *   `hourly`: 12-hour high-resolution wind speeds and relative humidity indices.
    *   `timezone`: Configured to `auto` to fetch localized Pakistani time.

#### B. Gemini AI Service (`com.example.network.GeminiApiService`)
Provides state-of-the-art conversational recommendations and context-aware travel insights.
*   **Endpoint:** `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent`
*   **Authentication:** Dynamically injected via the secure `BuildConfig.GEMINI_API_KEY` system.
*   **Parameters:** Managed with low temperature configurations (`0.7f`) to avoid AI hallucinations and maintain extreme regional advisory precision.

---

### 3.2 Database & Local Storage

Local data caching and robust authentication verification is structured via Android Room.

#### A. User Entity (`com.example.database.UserEntity`)
Represents the SQLite Database Schema representing authenticated application users.
*   `email` (Primary Key): Unique identifier for email-registered/Google social logins.
*   `fullName`: User-facing interactive profile metadata display.
*   `passwordHash`: Stored hashes for secure email/password validations, or flagged with a `"GOOGLE_AUTH"` bypass hash for federated sign-ins.
*   `isGoogleLogin`: Binary flag matching authentication channels.
*   `isLoggedIn`: State persistent indicator for seamless login redirection.

#### B. User Data Access Object (`com.example.database.UserDao`)
Handles asynchronous room query functions:
*   `getUserByEmail(email: String)`: Selects corresponding credentials.
*   `getActiveSession()`: Emits continuous lifecycle Flow representing current active session.
*   `clearActiveSessions()`: Safeguards credential leakage on user sign-out.

#### C. User Repository (`com.example.database.UserRepository`)
Provides clean abstraction mappings:
*   `registerUser()`: Validates that duplicates do not exist prior to committing writes.
*   `loginWithEmail()`: Compares cryptographic signatures of input passwords.
*   `loginWithGoogle()`: Seamlessly provisions and persists virtual social Google accounts dynamically.

---

### 3.3 State Management (`com.example.ui.viewmodel.WeatherViewModel`)
Architected to maintain persistent UI states across severe application recreation lifecycles:
*   **Theme Control:** Exposes dynamic `isDarkMode` state flow toggleable securely from the dashboard header bar.
*   **Session Lifecycle:** Exposes state-in registered Flows (`activeSession`) mapping user state to navigation routers in `MainActivity.kt`.
*   **Asynchronous Launchers:** Offloads expensive SQLite insertions and Retrofit transactions using standard `viewModelScope` coroutines.

---

## 4. UI Layer & Compose Screens

The design language uses **Material Design 3 (M3)** with custom geometric gradients, sleek glassmorphism cards, distinct styling typography, and responsive touch layout indicators (>48dp targets).

### 4.1 Login / Registration Screen (`AuthScreen.kt`)
Provides an ambient UI wrapper allowing signups and signins:
*   **Visual Assets:** Radial styling background gradient shifting between Midnight Teal and Slate Blue.
*   **Form Validation:** Multi-stage validation covering empty inputs, incorrect character specifications, and short passwords (<6 chars).
*   **Interactive Components:** Floating animated error cards, leading visual indicator icons, password visibility togglers, and a high-fidelity stylized Google Social Sign-In widget.

### 4.2 Comprehensive Weather Dashboard (`WeatherDashboardScreen.kt`)
Combines complex meteorological instruments onto one elegant scrolling screen:
*   **Interactive Location Search:** Quick-search filter displaying autocomplete pins matching 18 major Pakistani meteorological hubs (Karachi, Lahore, Islamabad, Quetta, Gilgit, etc.).
*   **GPS Position Receiver:** Integrates `play-services-location` directly. With one click, translates localized coordinates using the math-based Haversine distance formula to map users directly to their nearest Pakistani weather sensor.
*   **Meteorological Cards:** Displays giant real-time temperature values alongside active indicators for Humidity, Wind Speeds, and UV Indexes.
*   **Visual 7-Day Outlook:** A Horizontal scrolling carousel projecting localized 7-day weather expectations.
*   **Ultra-Detailed Hourly Graphs:** Renders next 12 hours of high-precision humidity fluctuations and wind gusts.
*   **Interactive Radar Map:** Custom vectorized map canvas tracking Pakistani geopolitical borders. Users can tap Balochistan, Sindh, Punjab, KP, or Gilgit-Baltistan on the map canvas to instantly lock weather sensors onto those provinces.

### 4.3 Severe Climate Warnings and Simulator
To address emergency guidelines, the application displays high-impact warning bands for severe events (e.g., Karachi heatwaves >40°C, Punjabi monsoons). 
*   **Urgent Push Notifications:** Implements a designated notification channel (`PAK_WEATHER_ALERTS`). Triggers active system tray hazard warnings when extreme criteria are fulfilled.
*   **Simulator Buttons:** Includes debugging buttons which allow users to run live simulations of severe lightning storms and dust storms on Android system bars.

### 4.4 AI Weather Advisor Chatbot (`ChatbotScreen.kt`)
Encapsulates real-time chatbot interactions powered by Gemini:
*   **Scope Restriction:** Configured with robust instructions to focus exclusively on clothing advisories (lawn, linen, wools), safety instructions (landslides in Murree, monsoonal blockades), and regional tips.
*   **Responsive Typing Indicators:** Live UI feedback to signify background API network queries.
*   **Bubble Alignment:** Visually distinct user and system bubbles paired with timestamps.

### 4.5 Context-Aware Travel Advisor (`TravelSuggestionsScreen.kt`)
Analyzes real-time search trends and current conditions to generate traveling guides:
*   **Highway Status Advisory:** Includes live status panels for key Pakistani transit networks (M-2 Motorway, Hazara Expressway, Karakoram Highway).
*   **Google Trends Integration:** Uses Gemini to synthesize simulated regional search query metrics and suggest appropriate nearby tourist destinations automatically.

---

## 5. Testing & Verification

Comprehensive local JVM testing is supported with Robolectric:
*   **Resource ID Tests (`ExampleRobolectricTest.kt`):** Secures localized resource naming configurations.
*   **Screenshot Integration Unit Testing (`GreetingScreenshotTest.kt`):** Ensures visual pixel-level layouts are immune to UI regressions.

**Compilation Verification:**
```bash
# To run local tests
gradle :app:testDebugUnitTest

# To check system builds
compile_applet
```

---
*Documentation Compiled Successfully under the Google AI Engineering Program.*
