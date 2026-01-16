# REPS - Copilot Instructions

## Project Overview
REPS is a minimalist Android workout tracker built with Kotlin. The app uses a multi-Activity architecture with XML layouts and is actively migrating to Jetpack Compose (see [COMPOSE_MIGRATION_PLAN.md](../COMPOSE_MIGRATION_PLAN.md)).

## Architecture

### Current Structure
```
app/src/main/java/com/example/workouttracker/
├── MainActivity.kt              # Entry point
├── data/
│   ├── database/                # Room DB (WorkoutDatabase, CompletedSetDao)
│   └── model/                   # Entities (CompletedSet, Set, Workout)
├── ui/
│   ├── components/              # Reusable UI elements
│   ├── history/                 # Workout history (RecyclerView-based)
│   ├── settings/                # Theme settings
│   ├── theme/                   # Compose theme (Color.kt, Theme.kt, Typography.kt)
│   ├── timer/                   # Rest timer with countdown
│   ├── tracking/                # Workout completion screen
│   └── workout/                 # Workout input form
└── utils/                       # ThemeManager, TimerUtils, TestDataGenerator
```

### Key Patterns
- **Navigation**: Intent-based between Activities (migration to Navigation Compose planned)
- **Database**: Room with KSP annotation processing, singleton pattern via `WorkoutDatabase.getDatabase(context)`
- **State preservation**: Activities save state via `onSaveInstanceState`
- **Coroutines**: Database operations use `CoroutineScope(Dispatchers.IO).launch { }`

## Build & Development

### Build Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk  # Manual install
```

### Configuration
- **Gradle**: 8.5 with Android Gradle Plugin 8.1.4
- **Kotlin**: 1.9.20 with JVM target 17
- **Room**: 2.6.1 with KSP (not KAPT - KAPT fails with Java 17+)
- **SDK**: minSdk 21, targetSdk 34, compileSdk 34

## Internationalization (i18n)

The app defaults to German with English as secondary language. Device locale auto-selects.

### String Resources
- `res/values/strings.xml` - German (default)
- `res/values-en/strings.xml` - English

### Convention
Always use string resources, never hardcode UI text:
```kotlin
// ✓ Correct
getString(R.string.exercise_deadlift)
String.format(getString(R.string.set_format), currentSet, totalSets)

// ✗ Wrong
"Deadlift"
"Set $currentSet/$totalSets"
```

## Design System

### Theme Colors (Brutalist Dark Theme)
- Background: `#121212` (DarkBackground)
- Surface: `#1E1E1E` (DarkSurface)
- Primary: Blue80 (`#90CAF9`)
- See [Color.kt](app/src/main/java/com/example/workouttracker/ui/theme/Color.kt)

### Design Principles
- Flat design, no elevation or shadows
- High contrast typography (18sp → 14sp → 12sp hierarchy)
- Minimal animations

## Active Migration: XML → Compose

The project has Compose dependencies configured and theme files ready. When adding new screens:
1. Use Jetpack Compose with `RepsTheme` wrapper
2. Embed in existing Activities via `ComposeView` if needed
3. Follow the interop strategy in [COMPOSE_MIGRATION_PLAN.md](../COMPOSE_MIGRATION_PLAN.md#00-interop-strategie-xml--compose)

## Debug Features

Long-press on timer display in MainActivity to access debug menu (test data generation, database operations).
