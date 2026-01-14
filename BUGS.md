# 🐛 Bekannte Bugs

Dokumentiere hier Bugs, damit sie behoben werden können.

---

## Offene Bugs

*Keine offenen Bugs! 🎉*

---

## Behobene Bugs

### ~~Bug #2: Gelöschte Standard-Übungen erscheinen wieder~~
- **Status:** ✅ Behoben
- **Seite/Datei:** WorkoutInputActivity.kt
- **Beschreibung:** Wenn man eine Standard-Übung löschte, war sie beim nächsten Öffnen wieder da
- **Datum:** 14.01.2026

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
