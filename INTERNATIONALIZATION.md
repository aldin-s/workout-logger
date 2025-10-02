# Internationalization (i18n) Implementation

## Overview
The app now supports multiple languages with professional internationalization following Android best practices.

## Supported Languages
- **German (Deutsch)** - Default language
- **English** - Secondary language

The app automatically switches language based on the device's system language settings.

## Implementation Details

### 1. String Resources Structure
```
app/src/main/res/
├── values/              # German (default)
│   └── strings.xml
└── values-en/           # English
    └── strings.xml
```

### 2. Complete Coverage
All screens now use string resources:

#### Main Activity
- START WORKOUT / START WORKOUT
- WORKOUT HISTORY / WORKOUT HISTORY
- Timer display: 00:00 / 0/0

#### Workout Input Activity
- ÜBUNG WÄHLEN / SELECT EXERCISE
- Exercise names:
  - Kreuzheben / Deadlift
  - Bankdrücken / Bench Press
  - Rudern / Rowing
  - Kniebeuge / Squat
  - + Eigene Übung / + Custom Exercise
- Form labels:
  - Gewicht (kg) / Weight (kg)
  - Wiederholungen / Repetitions
  - Pausenzeit (Sekunden) / Rest Time (Seconds)
  - Anzahl Sätze / Number of Sets
- Validation messages (6 error messages)

#### Timer Activity
- PAUSE LÄUFT… / REST PERIOD…
- SATZ FERTIG / SET DONE
- Dynamic formats:
  - 80.0 kg (both languages)
  - Satz 1/3 / Set 1/3

#### Completion Screen (Tracking)
- WORKOUT ABGESCHLOSSEN / WORKOUT COMPLETED
- ZURÜCK ZUM HAUPTMENÜ / BACK TO MAIN MENU
- HISTORIE ANSEHEN / VIEW HISTORY
- Plurals:
  - "1 Satz abgeschlossen" / "1 set completed"
  - "3 Sätze abgeschlossen" / "3 sets completed"

### 3. Professional Features

#### Format Strings
Dynamic content uses format strings:
```xml
<!-- Weight display -->
<string name="weight_format">%.1f kg</string>

<!-- Set counter -->
<string name="set_format">Satz %1$d/%2$d</string>  <!-- German -->
<string name="set_format">Set %1$d/%2$d</string>    <!-- English -->
```

Usage in code:
```kotlin
weightTextView.text = String.format(getString(R.string.weight_format), weight)
setsTextView.text = String.format(getString(R.string.set_format), currentSet, totalSets)
```

#### Plurals
Grammatically correct plural forms:
```xml
<plurals name="sets_completed">
    <item quantity="one">%d Satz abgeschlossen</item>    <!-- 1 Satz -->
    <item quantity="other">%d Sätze abgeschlossen</item> <!-- 3 Sätze -->
</plurals>
```

Usage in code:
```kotlin
setsCompletedTextView.text = resources.getQuantityString(
    R.plurals.sets_completed, 
    setsCompleted, 
    setsCompleted
)
```

### 4. Code Changes

#### XML Layouts
All hardcoded strings replaced with `@string/` references:
```xml
<!-- Before -->
<Button android:text="START WORKOUT" />

<!-- After -->
<Button android:text="@string/start_workout" />
```

#### Kotlin Activities
All hardcoded strings replaced with `getString()`:
```kotlin
// Before
selectExercise("Kreuzheben", false)

// After
selectExercise(getString(R.string.exercise_deadlift), false)
```

### 5. Testing Instructions

#### Test Language Switching
1. **Change to English:**
   - Open device Settings
   - Go to System → Languages → Add language
   - Add English and move it to top position
   - Return to app - all text should be in English

2. **Change to German:**
   - Open device Settings
   - Go to System → Languages
   - Move Deutsch to top position
   - Return to app - all text should be in German

#### Test Dynamic Content
- Start a workout with 3 sets
- Verify timer shows "Set 1/3" (English) or "Satz 1/3" (German)
- Complete 1 set → verify "1 set completed" or "1 Satz abgeschlossen"
- Complete 3 sets → verify "3 sets completed" or "3 Sätze abgeschlossen"

#### Test Validation Messages
- Try to start workout without selecting exercise
- Try to start without entering weight/reps/etc.
- All error messages should appear in current language

### 6. Organization
Strings are organized by screen for maintainability:
```xml
<!-- Main Activity -->
<string name="start_workout">START WORKOUT</string>
<string name="workout_history">WORKOUT HISTORY</string>

<!-- Workout Input Activity -->
<string name="select_exercise">SELECT EXERCISE</string>
<string name="exercise_deadlift">Deadlift</string>
<!-- ... -->

<!-- Timer Activity -->
<string name="set_format">Set %1$d/%2$d</string>
<!-- ... -->
```

## Benefits

✅ **Professional UX**: Users can use the app in their preferred language
✅ **Maintainability**: All text in one place, easy to update
✅ **Scalability**: Easy to add more languages (just create values-es/, values-fr/, etc.)
✅ **Type Safety**: Compile-time checking of string resource references
✅ **Consistency**: Consistent terminology across the entire app
✅ **Grammar**: Plurals ensure grammatically correct text
✅ **Best Practices**: Follows Android's official i18n guidelines

## Future Enhancements
To add more languages:
1. Create new folder: `values-<language_code>/` (e.g., `values-es/` for Spanish)
2. Copy `values-en/strings.xml` to the new folder
3. Translate all strings
4. Build and test

Example language codes:
- Spanish: `values-es/`
- French: `values-fr/`
- Italian: `values-it/`
- Turkish: `values-tr/`

## Statistics
- **Total strings**: 40+ (covering entire app)
- **Languages**: 2 (German, English)
- **Files updated**: 8 (3 layouts, 3 activities, 2 strings.xml)
- **Plurals**: 1 (sets_completed)
- **Format strings**: 4 (weight, sets, workout date, etc.)
- **Build status**: ✅ Successful
- **Installation**: ✅ Tested on device

## Commit
```
commit 9db39b4
Add internationalization support (German/English)

- Created values/strings.xml with complete German translations
- Created values-en/strings.xml with English translations
- Updated all XML layouts to use @string/ references
- Updated all Kotlin activities to use getString() and getQuantityString()
- Implemented format strings for dynamic content
- Added plurals support for grammatically correct translations
- Organized strings by screen/activity for maintainability
```
