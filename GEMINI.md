# Work Time Tracker - Project Overview

Comprehensive work time tracking system consisting of a **Laravel 12 REST API** and a modern *
*Android Application**.

## Project Structure

- `work-time-tracker/`: Backend API built with Laravel.
- `app/`: Android mobile application built with Kotlin and Jetpack Compose.
- Root: Gradle configuration for the Android project and shared workspace.

---

## 🚀 Backend (`work-time-tracker/`)

A high-performance RESTful API using the Repository-Service pattern.

### Tech Stack

- **Core:** PHP 8.2+, Laravel 12, PostgreSQL 16+.
- **Authentication:** JWT (php-open-source-saver/jwt-auth).
- **WebSockets:** Laravel Reverb for real-time chat.
- **Performance:** Laravel Octane (FrankenPHP).
- **Monitoring/Docs:** Scramble (OpenAPI), Laravel Telescope.

### Key Features

- **Attendance Validation:** GPS geo-fencing (Haversine formula) and daily rotating QR tokens.
- **Work Modes:** Office, Remote, Hybrid configurations.
- **Leave Management:** Request workflow with Manager/Admin approval and Push Notifications.
- **Real-time Chat:** Encrypted private messages between employees.
- **Reporting:** Excel (.xlsx) exports for time entries and statistics.
- **Audit Logs:** Automated tracking of all sensitive model changes.

### Useful Commands

- **Install:** `composer install`
- **Setup:** `php artisan migrate && php artisan jwt:secret && php artisan reverb:install`
- **Run (Development):** `composer run dev` (Runs Octane + Reverb)
- **Test:** `composer test` (Runs Pest)
- **Lint:** `composer lint` (PHPStan/Larastan)

---

## 📱 Android App (`app/`)

A modern Android application designed for employees and managers.

### Tech Stack

- **UI:** Jetpack Compose (Material 3).
- **Architecture:** MVVM with Clean Architecture principles.
- **DI:** Hilt.
- **Networking:** Retrofit 3 + OkHttp 5.
- **Local Data:** Room (Cache) + DataStore (Preferences).
- **Hardware:** CameraX + ML Kit (QR Scanning), Play Services Location (GPS).
- **Notifications:** Firebase Cloud Messaging (FCM).

### Key Features

- **Clock-in/Out:** Validated via QR or GPS location.
- **Security:** PIN code required for clock-out operations.
- **Profile:** Avatar management and security settings.
- **Real-time:** Integrated chat functionality.
- **FCM:** Instant updates for leave requests and schedule changes.

### Build & Run

- **Build Debug:** `./gradlew assembleDebug`
- **Run Tests:** `./gradlew test`
- **Install to Device:** `./gradlew installDebug`
- **Minimum SDK:** 31 (Android 12)
- **Target SDK:** 36 (Android 15+)

---

## 🛠️ Development Conventions

### General

- **Naming:** Follow PSR-12 for PHP and Kotlin style guides for Android.
- **Git:** Work in feature branches; use descriptive commit messages.
- **Documentation:** API changes must be reflected in the Scramble OpenAPI documentation.

### Backend

- Adhere to the **Repository-Service pattern**. Business logic should reside in Services.
- Every new feature should include **Pest tests**.
- Keep models **Auditable** by using the `Auditable` trait for sensitive data.

### Android

- Use **Hilt** for all dependency injections.
- UI must be built exclusively with **Jetpack Compose**.
- Handle network states gracefully using a standard `Resource` or `Result` wrapper.
- All strings should be localized in `res/values/strings.xml`.

---

## 🔗 Environment Setup (Summary)

1. **API:** Copy `work-time-tracker/.env.example` to `.env` and configure DB/JWT/Firebase.
2. **Android:** Ensure `google-services.json` is present in `app/` if using Firebase features.
3. **Connectivity:** For local development, the Android app should point to the host machine's IP (
   e.g., `10.0.2.2` for emulator or local LAN IP for physical device).
