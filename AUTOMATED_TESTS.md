# Automatisierte Tests - Workout Tracker

## Übersicht

Diese App enthält umfassende automatisierte Tests für kritische Komponenten und Benutzerszenarien.

## Test-Struktur

### Unit Tests (`app/src/test/`)
- **ValidationTest.kt** - Validierungslogik für Benutzereingaben
  - Gewicht: 0.5-999.0 kg
  - Wiederholungen: 1-100 reps
  - Pausenzeit: 10-600 Sekunden
  - Sätze: 1-50
  - Übungsname: Nicht leer
  - Datum/Zeit-Formatierung

### Instrumented Tests (`app/src/androidTest/`)

#### 1. CompletedSetDaoTest.kt - Database Tests
✅ **Kritische Datenbankoperationen**
- `insertAndRetrieveCompletedSet()` - Einfache Insert/Select Operation
- `insertMultipleSetsAndRetrieve()` - Mehrere Sätze speichern
- `deleteAllSets()` - Alle Daten löschen
- `updateCompletedSet()` - Update-Operation
- `deleteSpecificSet()` - Einzelnen Satz löschen
- `testMinimalValues()` - Edge-Case: Minimale Werte (0.5kg, 1 rep)
- `testMaximalValues()` - Edge-Case: Maximale Werte (999kg, 100 reps)

#### 2. WorkoutInputActivityTest.kt - UI Eingabe Tests
✅ **Kritische Benutzerinteraktionen**
- `testStandardExerciseSelection()` - Standard-Übung auswählen
- `testCustomExerciseInput()` - Eigene Übung erstellen
- `testCompleteWorkoutInputHappyPath()` - Kompletter Workflow mit gültigen Daten
- `testEmptyInputShowsError()` - Validierung: Leere Eingaben
- `testMinimalValuesInput()` - Edge-Case: 0.5kg, 1 rep, 10s Pause, 1 Satz
- `testMaximalValuesInput()` - Edge-Case: 999kg, 100 reps, 600s Pause, 50 Sätze
- `testCustomExerciseCompleteFlow()` - **KRITISCH** Eigene Übung (Android 14+ Xiaomi Fix)

#### 3. TimerServiceTest.kt - Foreground Service Tests
✅ **Android 14+ Kompatibilität**
- `testServiceStartsSuccessfully()` - Service startet korrekt
- `testServiceBindsCorrectly()` - Binding funktioniert
- `testTimerCountsDown()` - Timer läuft herunter
- `testServiceStopsCorrectly()` - Service stoppt sauber
- `testResetTimerFunctionality()` - Reset für nächsten Satz

#### 4. MainActivityTest.kt - Navigation Tests
✅ **End-to-End Navigation**
- `testAppLaunchesSuccessfully()` - App startet erfolgreich
- `testNavigationToWorkoutInput()` - Navigation zum Workout-Input
- `testNavigationToHistory()` - Navigation zur Historie
- `testNavigationToSettings()` - Navigation zu Einstellungen
- `testInitialTimerDisplay()` - Initiale Timer-Anzeige
- `testInitialSetsDisplay()` - Initiale Satz-Anzeige

#### 5. EndToEndWorkoutFlowTest.kt - Kompletter Workout Flow
✅ **KOMPLETTE USER JOURNEYS** (Das was du gefragt hast!)
- `completeWorkoutFlow_StandardExercise_Success()` - **Kompletter Flow**: Start → Übung auswählen → Eingaben → Timer läuft
- `completeWorkoutFlow_CustomExercise_Success()` - **Eigene Übung**: Start → Eigene Übung → Timer läuft (Android 14 Fix)
- `completeWorkoutFlow_MinimalValues_Success()` - **Edge Case**: 0.5kg, 1 rep, 10s, 1 Satz
- `completeWorkoutFlow_MaximalValues_Success()` - **Edge Case**: 999kg, 100 reps, 600s, 50 Sätze

**Diese Tests simulieren einen echten Benutzer der die App komplett durchklickt!**

## Tests ausführen

### Unit Tests (Keine Device/Emulator nötig)
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests (Android Device/Emulator erforderlich)
```bash
# 1. Device/Emulator verbinden
adb devices

# 2. Alle Instrumented Tests ausführen
./gradlew connectedDebugAndroidTest

# 3. NUR KOMPLETTER WORKFLOW (Start → Übung → Timer)
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.EndToEndWorkoutFlowTest

# 4. Nur Database Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.data.database.CompletedSetDaoTest

# 5. Nur UI Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.ui.WorkoutInputActivityTest

# 6. Nur Service Tests
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.service.TimerServiceTest
```

## Test-Coverage

### Abgedeckte Szenarien

#### ✅ Android 14+ Xiaomi Crash Fix
- **Test:** `testCustomExerciseCompleteFlow()`
- **Szenario:** Eigene Übung erstellen und starten
- **Absicherung:** Foreground Service Permission Handling

#### ✅ Android 16 Samsung S25 Ultra Compatibility
- **Test:** `TimerServiceTest` (alle Tests)
- **Szenario:** Service mit FOREGROUND_SERVICE_TYPE_SPECIAL_USE
- **Absicherung:** API 34+ Service-Typ korrekt

#### ✅ Database Integrity
- **Test:** `CompletedSetDaoTest` (7 Tests)
- **Szenario:** CRUD Operations auf Room Database
- **Absicherung:** Keine Datenverluste bei Insert/Update/Delete

#### ✅ Input Validation
- **Test:** `ValidationTest` + UI-Tests
- **Szenario:** Ungültige Werte (0, negative, zu große Werte)
- **Absicherung:** App akzeptiert nur gültige Bereiche

#### ✅ Edge Cases
- **Minimal:** 0.5kg, 1 rep, 10s, 1 Satz
- **Maximal:** 999kg, 100 reps, 600s, 50 Sätze

## Continuous Integration

Für GitHub Actions CI/CD Pipeline:

```yaml
name: Android Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'
      
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
      
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/
```

## Bekannte Einschränkungen

1. **Instrumented Tests** benötigen ein physisches Gerät oder Emulator
2. **TimerServiceTest** kann auf älteren Android-Versionen (<14) fehlschlagen wenn spezifische Permissions fehlen
3. **UI Tests** benötigen Animationen deaktiviert auf dem Test-Gerät:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

## Test-Dependencies

Alle Dependencies sind in `app/build.gradle` definiert:

### Unit Testing
- JUnit 4.13.2
- Mockito 5.8.0
- MockK 5.2.1
- AndroidX Core Testing 2.2.0
- Coroutines Test 1.8.0
- Room Testing 2.6.1

### Instrumented Testing
- AndroidX Test Runner 1.6.2
- AndroidX Test Rules 1.6.1
- Espresso Core 3.6.1
- Espresso Intents 3.6.1
- Espresso Contrib 3.6.1
- Work Testing 2.9.0

## Nächste Schritte

1. ✅ Test-Struktur erstellt
2. ✅ Database Tests implementiert
3. ✅ UI Tests implementiert
4. ✅ Service Tests implementiert
5. ⏳ Tests auf echtem Device ausführen
6. ⏳ Coverage Report generieren
7. ⏳ CI/CD Integration

## Fehlersuche

### Problem: Tests kompilieren nicht
```bash
# Gradle-Cache löschen
./gradlew clean

# Dependencies neu laden
./gradlew build --refresh-dependencies
```

### Problem: Instrumented Tests schlagen fehl
```bash
# Device Status prüfen
adb devices

# Logcat während Test anzeigen
adb logcat -c && ./gradlew connectedDebugAndroidTest & adb logcat
```

### Problem: Service Tests schlagen fehl auf Android 14+
- Stelle sicher dass die App FOREGROUND_SERVICE_SPECIAL_USE Permission hat
- Prüfe ob POST_NOTIFICATIONS Permission erteilt ist (Android 13+)

---

**Stand:** 2026-01-09
**Android API Support:** 21-36 (Android 5.0 - Android 16)
**Test Framework:** JUnit 4 + Espresso + Room Testing
