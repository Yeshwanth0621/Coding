# Booktopia - Kalam Knowledge Club Reading Challenge

A native Android app built with Kotlin and Jetpack Compose for tracking reading progress, competing on leaderboards, and building a reading community.

## Features

- 📱 **User Authentication** - Sign up/login with email or Google
- 📚 **Daily Reading Log** - Track pages read daily (supports past 3 days)
- 🏆 **Leaderboard** - Compete with fellow readers
- 📊 **Reading History** - View your complete reading journey
- 👥 **Community** - Connect with the Kalam Knowledge Club

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM
- **Backend:** Supabase (Auth + PostgreSQL + Storage)
- **Navigation:** Compose Navigation
- **Image Loading:** Coil

## Setup

### 1. Configure Supabase

1. Create a project at [supabase.com](https://supabase.com)
2. Run the SQL schema from your web project's `supabase-schema.sql`
3. Open `app/src/main/java/com/kalamclub/booktopia/data/SupabaseClient.kt`
4. Replace the placeholder values:
   ```kotlin
   private const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
   private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
   ```

### 2. Build & Run

1. Open the project in Android Studio
2. Sync Gradle files (File > Sync Project with Gradle Files)
3. Build the project (Build > Make Project)
4. Run on an emulator or physical device

## Project Structure

```
app/src/main/java/com/kalamclub/booktopia/
├── BooktopiaApplication.kt      # Application class
├── MainActivity.kt              # Main activity
├── data/
│   ├── Models.kt               # Data classes
│   ├── SupabaseClient.kt       # Supabase configuration
│   └── BooktopiaRepository.kt  # Repository layer
├── navigation/
│   └── Navigation.kt           # Navigation graph
├── ui/
│   ├── components/
│   │   └── Components.kt       # Reusable UI components
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── LandingScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── SignupScreen.kt
│   │   ├── DashboardScreen.kt
│   │   └── CommunityScreen.kt
│   └── theme/
│       └── Theme.kt            # Colors, typography, theme
└── viewmodel/
    ├── AuthViewModel.kt
    └── DashboardViewModel.kt
```

## Requirements

- Android Studio Hedgehog or later
- Android SDK 34
- Minimum SDK 24 (Android 7.0)
- Kotlin 2.0+

## License

Part of the Kalam Knowledge Club initiative.
