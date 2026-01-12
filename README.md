# 🚖 SafarLink

**SafarLink** is a smart ride aggregator Android application designed to simplify urban commuting.  
It aggregates ride options from major providers like **Uber, Ola, Rapido, and Namma Yatri** into a single, unified interface, allowing users to compare prices and estimates instantly without switching between multiple apps.

This project showcases advanced Android development practices, including **Clean Architecture**, **MVVM**, **Jetpack Compose**, and a custom **Deep Linking Strategy** to handle third-party app integrations.

---

## 🌟 Key Features

### 🔍 Unified Ride Comparison
- Real-time aggregation of ride options.
- Supports major Indian ride-hailing services: **Uber, Ola, Rapido, and Namma Yatri**.
- Displays estimated prices and time-of-arrival (simulated for comparison logic).

### 🚀 Smart "Deep Launch" Engine
A hybrid linking strategy designed to handle different app behaviors:
- **Direct Deep Linking:** Launches **Uber** and **Ola** directly with pickup and drop coordinates pre-filled.
- **Smart Clipboard Integration:** For apps that block direct external automation (like **Rapido** and **Namma Yatri**), SafarLink automatically:
  1. Reverse-geocodes coordinates into a readable address.
  2. Copies the destination address to the clipboard.
  3. Launches the target app for a seamless one-tap experience.

### 📍 Location Intelligence
- **Auto-GPS Detection:** Fetches the user's current location using Android Location Services.
- **Smart Search:** Uses **OpenStreetMap (Nominatim API)** for real-time location suggestions and geocoding — no paid Google APIs required.

### 🔐 Robust Authentication System
- **Firebase Authentication** (Email/Password & Google Sign-In).
- **Strict Login Logic:** Prevents accidental account creation on the Login screen, eliminating ghost accounts and enforcing a proper Sign-Up flow.

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **UI Toolkit:** Jetpack Compose (Material Design 3)  
- **Architecture:** MVVM + Clean Architecture  
- **Dependency Injection:** Dagger Hilt  
- **Async:** Kotlin Coroutines & Flow  
- **Backend:** Firebase Authentication & Firestore  
- **Networking:** `HttpURLConnection` (Custom Geocoding Implementation)  
- **Navigation:** Jetpack Navigation Compose  

---

## 🏗️ Architecture Overview

SafarLink follows a strict **Clean Architecture** approach:

### 1️⃣ Presentation Layer
- Built entirely with **Jetpack Compose**
- ViewModels manage UI state using `StateFlow`
- Handles UI logic and deep-launch intent triggering

### 2️⃣ Domain Layer
- Business logic and repository interfaces
- Ride comparison logic & authentication rules
- Pure Kotlin (no Android dependencies)

### 3️⃣ Data Layer
- Repository implementations
- Integrates Firebase, Android location services, and deep link URI builders

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/f882e44e-fa73-47f6-88c6-644d15364340" width="220" />
  <img src="https://github.com/user-attachments/assets/16460f27-ad10-46e6-9d84-20bd54ea0281" width="220" />
  <img src="https://github.com/user-attachments/assets/11ab9071-6f5c-4c27-80ee-92bbcca32956" width="220" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/f68fed0b-af0c-4d91-b0cb-36eeda801f9e" width="220" />
  <img src="https://github.com/user-attachments/assets/74062198-e4df-4fa9-b731-5a93053dc1b4" width="220" />
</p>
