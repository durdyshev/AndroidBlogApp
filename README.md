# Aura — Production-Ready Location-Based Social Discovery & Dating App

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-orange.svg)](https://developer.android.com/topic/architecture)
[![Backend](https://img.shields.io/badge/Backend-Supabase%20%2B%20PostgreSQL-3ECF8E.svg)](https://supabase.com)
[![Firebase](https://img.shields.io/badge/Notifications-FCM-FFCA28.svg)](https://firebase.google.com/)

Aura is a modern, location-based social discovery and dating application for adults aged 18+. Built from the ground up with Kotlin, Jetpack Compose, Material 3, Clean Architecture, Hilt, Room, Ktor, Supabase, and Firebase Cloud Messaging.

---

## 🌟 Features

- **2026 Mobile UI & Design System**: Custom Obsidian dark theme, glassmorphism surfaces, smooth spring swipe physics, status indicators, and shimmer skeleton loading.
- **Adult (18+) Authentication & Onboarding**:
  - Email signup, login, OTP verification, password recovery, and secure session management via Encrypted DataStore.
  - Strict 18+ age verification on birthday selection.
- **Privacy-Preserving Geolocation Discovery**:
  - Uses Fused Location Provider with battery-efficient throttling (updates on >500m movement or >30 min intervals).
  - Exact GPS coordinates are never exposed; distances are computed on the backend (Haversine formula) and displayed as approximate strings (`"2.4 km away"`).
  - Nearby Radar view showing active members without disclosing precise coordinates.
- **Atomic Swipe & Matching Engine**:
  - Swipe actions: `PASS`, `LIKE`, and `SUPER_LIKE`.
  - Stored PostgreSQL procedure `process_swipe` executes atomic reciprocal like detection, creates match and conversation, and triggers push notifications.
  - Instant Match Celebration modal with quick chat kickoff.
- **Real-Time Messaging & Chat**:
  - Real-time chat powered by Supabase Realtime WebSocket channels + offline-first Room database cache.
  - Supports `TEXT`, `IMAGE` (with automatic WebP compression), and `SYSTEM` message types.
  - Read receipts, delivered ticks, and typing indicators.
- **Safety, Moderation & Blocking**:
  - Instant Block & Unblock: Blocked users disappear from discovery, matches, and chats.
  - Comprehensive Reporting system (`SPAM`, `FAKE_PROFILE`, `HARASSMENT`, `INAPPROPRIATE_CONTENT`, `SCAM`, `OTHER`).
  - GDPR-compliant soft account deletion (`soft_delete_user_account`).
- **Push Notifications (FCM)**:
  - Background and foreground alerts for `NEW_LIKE`, `NEW_MATCH`, `NEW_MESSAGE`, and `SUPER_LIKE`.

---

## 🏗 Technology Stack & Architecture

### Android Client
| Layer / Component | Technology |
|---|---|
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | Feature-Based Clean Architecture + MVVM |
| **Dependency Injection** | Dagger Hilt 2.50 |
| **Local Database** | Room 2.6.1 + Coroutine Flow |
| **Networking** | Ktor Client (CIO Engine, WebSockets, JSON Serialization) |
| **Image Loading** | Coil 2.6.0 |
| **Location** | Google Play Services Fused Location Provider 21.2.0 |
| **Storage & Security** | Jetpack DataStore Preferences + AndroidX Security Crypto |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Testing** | JUnit 4, MockK, Kotlinx Coroutines Test, Turbine, Room Testing |

### Backend (Supabase + PostgreSQL)
| Component | Implementation |
|---|---|
| **Authentication** | Supabase Auth (GoTrue REST API) |
| **Database** | PostgreSQL with UUIDs, foreign keys, and indexes |
| **Security** | Strict Row Level Security (RLS) policies per table |
| **Storage** | Supabase Storage bucket `profile-photos` and `chat-media` |
| **Realtime** | Supabase Realtime (WebSocket PostgreSQL CDC channels) |
| **Procedures** | `process_swipe`, `get_discovery_candidates`, `calculate_distance_km`, `soft_delete_user_account` |
| **Edge Functions** | `send-push-notification`, `process-swipe`, `moderate-content` |

---

## 🏛 Clean Architecture Layers

```
UI (Jetpack Compose Screens & Design System)
       ↓
ViewModel (StateFlow & UI Events)
       ↓
UseCase (Domain Business Logic & Validations)
       ↓
Repository Interface (Domain Layer)
       ↓
Repository Implementation (Data Layer)
     ↙                     ↘
Local DataSource (Room)     Remote DataSource (Ktor / Supabase)
```

---

## 📁 Repository Directory Structure

```
AndroidBlogApp/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/aura/dating/
│       │       ├── AuraApplication.kt
│       │       ├── MainActivity.kt
│       │       ├── core/
│       │       │   ├── common/ (Result, AppError, DateTimeUtils, DistanceUtils, ImageCompressor, Dispatchers)
│       │       │   ├── designsystem/ (Theme, Color, Type, Dimens, Components)
│       │       │   ├── navigation/ (Screen, AuraNavHost)
│       │       │   ├── network/ (SupabaseClientProvider, NetworkMonitor)
│       │       │   ├── database/ (AuraDatabase, DAOs, Entities, Converters)
│       │       │   ├── location/ (FusedLocationProvider)
│       │       │   ├── notifications/ (AuraFirebaseMessagingService, NotificationHandler)
│       │       │   └── security/ (DataStoreTokenStorage)
│       │       ├── data/ (Auth, Profile, Discovery, Matching, Chat, Notifications, Moderation)
│       │       ├── domain/ (Models, Repository Interfaces, UseCases)
│       │       ├── feature/
│       │       │   ├── onboarding/ (Splash, Welcome, LocationPermission)
│       │       │   ├── auth/ (Login, Register, Verification, ForgotPassword)
│       │       │   ├── profile_creation/ (CreateProfile, AddPhotos, SelectInterests, DatingPreferences)
│       │       │   ├── home/ (MainScreen scaffold with floating bottom bar)
│       │       │   ├── discover/ (DiscoverScreen, FilterSheet, CelebrationDialog, NearbyMapScreen)
│       │       │   ├── profile/ (ProfileScreen, UserProfileDetailScreen, EditProfile, EditPhotos, EditInterests)
│       │       │   ├── matches/ (MatchesScreen)
│       │       │   ├── chat/ (ConversationScreen)
│       │       │   ├── notifications/ (NotificationsScreen)
│       │       │   └── settings/ (Settings, Privacy, NotificationSettings, BlockedUsers, AccountSettings)
│       │       └── di/ (AppModule, DatabaseModule)
│       └── test/ (Comprehensive Unit & Repository Tests)
├── supabase/
│   ├── migrations/
│   │   ├── 01_schema_tables.sql
│   │   ├── 02_indexes_and_constraints.sql
│   │   ├── 03_functions_and_triggers.sql
│   │   ├── 04_rls_policies.sql
│   │   └── 05_admin_and_moderation.sql
│   └── functions/
│       ├── send-push-notification/index.ts
│       ├── process-swipe/index.ts
│       └── moderate-content/index.ts
├── local.properties.example
└── README.md
```

---

## 🗄 Database & Supabase Setup

1. **Create a Supabase Project**: Go to [Supabase](https://supabase.com) and create a new project.
2. **Execute Migrations**:
   Run the SQL scripts located in `supabase/migrations/` sequentially in your Supabase SQL Editor:
   - `01_schema_tables.sql` (Creates all tables and initial interests catalog)
   - `02_indexes_and_constraints.sql` (Creates performance indexes)
   - `03_functions_and_triggers.sql` (Creates `calculate_distance_km`, `process_swipe`, `get_discovery_candidates`, `unmatch_user`)
   - `04_rls_policies.sql` (Enables Row Level Security and creates Storage bucket policies)
   - `05_admin_and_moderation.sql` (Sets up moderation views and soft-delete procedure)
3. **Storage Buckets**:
   - Bucket `profile-photos` is created automatically as public with RLS folder restrictions `(storage.foldername(name))[1] = auth.uid()::text`.
   - Bucket `chat-media` is created for chat attachments.

---

## ⚙️ Environment Configuration

1. Copy `local.properties.example` to `local.properties`:
   ```properties
   sdk.dir=C\:\\Users\\your_user\\AppData\\Local\\Android\\Sdk
   SUPABASE_URL="https://your-project-ref.supabase.co"
   SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   GOOGLE_MAPS_API_KEY="AIzaSy..."
   ```
2. Place your `google-services.json` from Firebase console into `app/google-services.json`.

---

## 🚀 Build & Test Instructions

### Running Tests
Execute unit tests across domain UseCases, repositories, and ViewModels:
```bash
./gradlew testDebugUnitTest
```

### Assembling the Debug APK
```bash
./gradlew assembleDebug
```

---

## 🛡 Security & Privacy Rules

- Exact coordinates are never exposed in profile responses.
- Users can only read conversations and messages where they are participants.
- Passwords and sensitive credentials are never stored locally.
- Authenticated JWT tokens are stored in secure Android DataStore.
- Image uploads are compressed to WebP on the client before upload to prevent server storage bloat.
