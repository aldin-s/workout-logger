# Automated Testing Prompt für GitHub Copilot

Kopiere diesen Prompt und füge ihn in GitHub Copilot ein:

---

**Erstelle umfassende automatisierte Tests für meine Android Workout-Tracker App:**

**App-Details:**
- Kotlin-basierte Android App (minSdk 21, targetSdk 36)
- Features: Workout-Tracking, Timer mit Foreground Service, Room Database, Export/Import
- Kritische Komponenten: TimerService (Foreground Service mit Notifications), WorkoutInputActivity, MainActivity, SettingsActivity

**Benötigte Test-Suites:**

1. **Unit Tests** für:
   - Room Database Operationen (Insert, Query, Delete von CompletedSet)
   - Date/Time Utilities
   - Timer-Logik (CountDownTimer)
   - Validation-Logik in WorkoutInputActivity

2. **Instrumented Tests** für:
   - UI-Tests: Standard-Übungen auswählen und starten
   - UI-Tests: Eigene Übung erstellen und starten (wichtig für Android 14 Bug)
   - Timer-Service startet korrekt mit Notification
   - Foreground Service läuft im Hintergrund weiter
   - Daten werden in Database gespeichert
   - Export/Import Funktionalität
   - Settings-Änderungen persistieren

3. **Test-Szenarien:**
   - Happy Path: Komplettes Workout durchführen (5 Sätze)
   - Edge Case: Minimale Werte (0.5kg, 1 Rep, 10s Pause)
   - Edge Case: Maximale Werte (999kg, 100 Reps, 600s Pause)
   - Error Case: Leere Eingaben sollten Fehler zeigen
   - Lifecycle: App minimieren während Timer läuft
   - Permission: Android 13+ POST_NOTIFICATIONS Permission-Flow

4. **Technische Anforderungen:**
   - Verwende JUnit 4, AndroidX Test, Espresso
   - Mock Room Database für Unit Tests
   - Teste auf verschiedenen API-Levels (21, 26, 30, 34)
   - Teste Xiaomi/Samsung-spezifische Service-Restrictions
   - Prüfe Memory Leaks
   - Teste ProGuard/R8 kompatibilität

5. **Test-Coverage Ziele:**
   - Mindestens 70% Code Coverage
   - 100% Coverage für kritische Komponenten (TimerService, Database)
   - Alle Public Methods getestet

Erstelle die Tests in korrekter Verzeichnisstruktur (`app/src/test/` und `app/src/androidTest/`) mit klaren Kommentaren und Best Practices.
