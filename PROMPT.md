# 📱 Prompt to Create Android Workout Tracker App

This document contains the complete specification used to create this Android workout tracker application.

## App Requirements:

### Main Screen (Initial State):
- Display a timer showing "00:00"
- Display set counter showing "0/0"
- Button labeled "Start Workout" at the bottom

### Workflow:
1. When user clicks "Start Workout", navigate to input screen
2. Input screen (in German) titled "Übung eingeben" with fields:
   - Exercise name (Übung)
   - Weight in kg (Gewicht)
   - Number of repetitions (Wiederholungen)
   - Pause time in seconds (Pausenzeit)
   - Total number of sets (Anzahl Sätze)
   - "Start" button at bottom
3. After clicking "Start", return to main screen which now shows:
   - Countdown timer (starts at pause time, counts down to 00:00)
   - Set counter showing "1/[total sets]"
   - "DONE" button (only enabled when timer reaches 00:00)
4. When user completes exercise and presses "DONE":
   - Log completed set to SQLite database
   - Increment set counter (e.g., "2/3")
   - Start countdown timer again for next set
   - Disable "DONE" button until timer reaches 00:00 again
5. After all sets completed, show completion screen
6. Include a workout history screen to view all logged workouts

### Database Schema:
Log each completed set with:
- Exercise name (String)
- Weight in kg (Double)
- Planned repetitions (Int)
- Completed repetitions (Int) - same as planned when DONE pressed
- Set number (Int)
- Timestamp (Date)

### Technical Requirements:
- Language: Kotlin
- Architecture: Activities with proper lifecycle management
- Database: Room (SQLite) with suspend functions
- UI: Material Design components
- Min SDK: 21
- Target SDK: 34
- Compile SDK: 34
- Gradle: 8.5
- Android Gradle Plugin: 8.1.4
- Kotlin: 1.9.20
- Use KSP instead of KAPT for Room annotation processing
- JVM Target: 17

### Dependencies to Include:
- AndroidX AppCompat, ConstraintLayout, RecyclerView, CardView
- Room Database 2.6.1 with KSP
- Kotlin Coroutines 1.5.0
- Lifecycle components 2.3.1
- Material Design components

### Key Features:
- Timer countdown with visual feedback
- Set tracking with clear current/total display
- SQLite database persistence using Room
- German UI for input form
- Workout history viewer with RecyclerView
- Proper error handling and validation
- Clean architecture with separation of concerns

### File Structure:
```
workout-tracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/workouttracker/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/timer/TimerActivity.kt
│   │   │   ├── ui/workout/WorkoutInputActivity.kt
│   │   │   ├── ui/history/WorkoutHistoryActivity.kt
│   │   │   ├── ui/history/WorkoutHistoryAdapter.kt
│   │   │   ├── ui/tracking/TrackingActivity.kt
│   │   │   ├── data/model/CompletedSet.kt
│   │   │   ├── data/model/Workout.kt
│   │   │   ├── data/dao/CompletedSetDao.kt
│   │   │   ├── data/database/WorkoutDatabase.kt
│   │   │   └── data/database/Converters.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_timer.xml
│   │   │   │   ├── activity_workout_input.xml
│   │   │   │   ├── activity_workout_history.xml
│   │   │   │   ├── activity_tracking.xml
│   │   │   │   └── item_workout_history.xml
│   │   │   ├── drawable/
│   │   │   │   ├── ic_launcher.xml
│   │   │   │   ├── ic_launcher_round.xml
│   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   └── ic_launcher_foreground.xml
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       ├── colors.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

### Build Configuration:
- Use Gradle wrapper 8.5
- Configure for Java 17 compatibility
- Use KSP for Room annotation processing (not KAPT)
- Proper namespace configuration: `com.example.workouttracker`
- Include .gitignore for Android projects
- Make buildable via command line: `./gradlew assembleDebug`
- Make installable via ADB: `adb install app/build/outputs/apk/debug/app-debug.apk`

### gradle.properties Configuration:
```properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64
```

### Root build.gradle Configuration:
```gradle
buildscript {
    ext.kotlin_version = "1.9.20"
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.4'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.20-1.0.14"
    }
}
```

### App build.gradle Configuration:
```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.devtools.ksp'
}

android {
    namespace 'com.example.workouttracker'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.workouttracker"
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.3.1'
    implementation 'androidx.core:core-ktx:1.6.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.3.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1'
    implementation 'androidx.activity:activity-ktx:1.3.1'
    
    // Room database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    ksp 'androidx.room:room-compiler:2.6.1'
    
    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0'
}
```

### Additional Notes:
- All UI text should be in strings.xml for localization
- Use proper Material Design colors and themes
- Implement proper error handling and input validation
- Use Kotlin coroutines for database operations
- Follow Android best practices and architecture guidelines
- Ensure app works on API 21+ devices
- Create launcher icons (can be simple vector drawables)
- Configure proper AndroidManifest with all activities
- Use Room TypeConverters for Date objects
- Timer countdown uses CountDownTimer class
- Database operations must be on background thread using coroutines

### Installation Instructions:
```bash
# Build the APK
cd workout-tracker
./gradlew assembleDebug

# Install to connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use combined command
./gradlew installDebug
```

### Troubleshooting:
- If KAPT errors occur, ensure using KSP instead
- If JVM target errors occur, ensure Gradle uses JDK 17
- If build tools errors occur, use build tools version 34.0.0 or let Gradle auto-select
- For Android Studio sync issues, ensure JDK 17 is configured in project structure

---

**Created:** October 2, 2025  
**Author:** AI-assisted development with GitHub Copilot  
**License:** Open Source
