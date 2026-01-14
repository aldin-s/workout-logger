# 🐛 Bekannte Bugs

Dokumentiere hier Bugs, damit sie behoben werden können.

---

## Offene Bugs

### Bug #2: Gelöschte Standard-Übungen erscheinen wieder
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt, TimerService.kt
- **Beschreibung:** Die Timer-Implementierung hatte architektonische Schwächen
- **Lösung:** 
  - `TimerUiState` Sealed Class für alle UI-Zustände
  - `TimerViewModel` als Single Source of Truth
  - Activity nur noch für UI-Rendering
  - StateFlow für reaktive UI-Updates
- **Datum:** 14.01.2026

---

### Bug #2: Gelöschte Standard-Übungen erscheinen wieder
- **Status:** 🔴 Offen
- **Seite/Datei:** WorkoutInputActivity.kt
- **Beschreibung:** Wenn man eine Standard-Übung (z.B. "Squat") löscht und zurück geht, ist sie beim nächsten Öffnen von "Start Workout" wieder da
- **Erwartet:** Gelöschte Übungen sollten gelöscht bleiben
- **Schritte:**
  1. "Start Workout" öffnen
  2. "Squat" löschen (Bestätigung erscheint: "gelöscht")
  3. Zurück gehen
  4. "Start Workout" erneut öffnen
  5. ❌ Squat ist wieder da
- **Vermutete Ursache:** `initializeStandardExercises()` in `onCreate()` erstellt Standard-Übungen neu, ohne zu prüfen ob sie vom User gelöscht wurden. Es fehlt ein "isDeleted" Flag oder ähnliches.

---

## Behobene Bugs

### ~~Bug #9: Notification bleibt nach Workout-Ende stehen~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** Nach Abschluss des letzten Satzes blieb die Notification in der Statusleiste
- **Ursache:** Service wurde bei Workout-Ende nicht explizit gestoppt
- **Lösung:** 
  - `stopTimerService()` Methode erstellt (DRY-Prinzip)
  - Expliziter Service-Stop in `navigateToTrackingScreen()`
  - Service sendet `ACTION_STOP_TIMER` → Notification wird entfernt
- **Datum:** 14.01.2026

### ~~Bug #4: Timer-Button nach Ablauf nicht klickbar~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** Nach Ablauf des Timers konnte der "SATZ FERTIG" Button nicht geklickt werden
- **Ursache:** In `onServiceConnected()` wurde bei `currentSet == 1` der Button immer deaktiviert, ohne zu prüfen ob der Timer bereits abgelaufen war
- **Lösung:** Button-Zustand basiert jetzt auf `timeLeft <= 0` statt auf `currentSet`
- **Datum:** 14.01.2026

### ~~Bug #5: Timer startet bei Rotation neu~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** Beim Drehen des Handys startete der Timer von vorne
- **Ursache:** `onDestroy()` stoppte den Service bei Configuration Changes
- **Lösung:** 
  - `isChangingConfigurations` Check hinzugefügt
  - Service läuft bei Rotation weiter
  - Activity rebindet nur zum laufenden Service
- **Datum:** 14.01.2026

### ~~Bug #6: Memory Leak durch CoroutineScope~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** `CoroutineScope(Dispatchers.IO)` wurde nie gecancelled
- **Lösung:** `lifecycleScope` verwendet (automatisches Cancel bei Activity-Destroy)
- **Datum:** 14.01.2026

### ~~Bug #7: Process Death verliert Workout-Daten~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** Bei Low Memory wurden Workout-Daten nicht wiederhergestellt
- **Lösung:** Alle Workout-Daten werden in `savedInstanceState` gespeichert
- **Datum:** 14.01.2026

### ~~Bug #8: Back-Button beendet Workout ohne Warnung~~
- **Status:** ✅ Behoben
- **Seite/Datei:** TimerActivity.kt
- **Beschreibung:** Nutzer konnte versehentlich Workout beenden
- **Lösung:** Bestätigungsdialog "Workout abbrechen?" hinzugefügt
- **Datum:** 14.01.2026

### ~~Bug #1: Spracheinstellung wird nicht angewendet~~
- **Status:** ✅ Behoben
- **Seite/Datei:** SettingsActivity.kt
- **Beschreibung:** Wenn man die Sprache auf Englisch umstellt, bleiben alle Texte auf Deutsch
- **Lösung:** Moderne `AppCompatDelegate.setApplicationLocales()` API implementiert
- **Datum:** 14.01.2026

---

## Vorlage für neue Bugs

```markdown
### Bug #X: [Titel]
- **Status:** 🔴 Offen
- **Seite/Datei:** 
- **Beschreibung:** 
- **Erwartet:** 
- **Schritte:**
  1. 
  2. 
  3. 
- **Screenshot:** 
```

---

## Behobene Bugs

<!-- Verschiebe behobene Bugs hierher -->

### ~~Bug #0: Beispiel~~
- **Status:** ✅ Behoben
- **Lösung:** Beschreibung der Lösung
- **Datum:** TT.MM.JJJJ
