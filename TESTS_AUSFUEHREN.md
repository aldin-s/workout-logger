# 🧪 Tests schnell ausführen

## ✅ Automatisierte Tests JA - sie testen genau das was du gefragt hast!

Die Tests simulieren einen **echten Benutzer** der durch die App klickt:
1. App starten
2. "Start Workout" klicken
3. Übung auswählen (z.B. Bankdrücken oder eigene Übung)
4. Gewicht/Reps/Pause/Sätze eingeben
5. "Start" klicken
6. Timer läuft herunter ✅

**ABER:** Um diese Tests auszuführen brauchst du ein **Android-Gerät oder Emulator**!

---

## 📱 So führst du die Tests aus:

### Option 1: Mit deinem Xiaomi 12 Lite / S25 Ultra

```bash
# 1. Handy per USB verbinden und USB-Debugging aktivieren
# 2. Prüfen ob Gerät erkannt wird:
adb devices

# 3. KOMPLETTEN WORKFLOW testen (Start → Übung → Timer):
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.EndToEndWorkoutFlowTest

# Das testet:
# ✅ Standard-Übung (Bankdrücken)
# ✅ Eigene Übung (Bizeps Curl) - Android 14 Xiaomi Fix!
# ✅ Minimale Werte (0.5kg, 1 rep)
# ✅ Maximale Werte (999kg, 100 reps)
```

### Option 2: Mit Android Studio Emulator

```bash
# 1. Android Studio öffnen
# 2. Tools → Device Manager → Virtuelles Gerät erstellen
# 3. Empfohlen: Pixel 6 mit Android 14 (API 34)
# 4. Emulator starten
# 5. Terminal:

adb devices
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asstudio.berlin.reps.EndToEndWorkoutFlowTest
```

### Option 3: ALLE Tests ausführen

```bash
# Database + UI + Service + End-to-End (dauert ~5-10 Minuten)
./gradlew connectedDebugAndroidTest
```

---

## 📊 Was die Tests testen:

### ✅ End-to-End Workflow Tests (NEU!)
**Datei:** `EndToEndWorkoutFlowTest.kt`

1. **`completeWorkoutFlow_StandardExercise_Success()`**
   - MainActivity starten
   - "Start Workout" klicken
   - Bankdrücken auswählen
   - 100kg, 5 Reps, 120s Pause, 3 Sätze eingeben
   - "Start" klicken
   - Timer läuft herunter ✅
   - **Dauer:** ~7 Sekunden

2. **`completeWorkoutFlow_CustomExercise_Success()`**
   - Eigene Übung "Bizeps Curl" erstellen
   - 30kg, 12 Reps, 90s Pause, 3 Sätze
   - **WICHTIG:** Testet Android 14 Xiaomi Fix!
   - **Dauer:** ~7 Sekunden

3. **`completeWorkoutFlow_MinimalValues_Success()`**
   - Edge Case: 0.5kg, 1 Rep, 10s, 1 Satz
   - **Dauer:** ~7 Sekunden

4. **`completeWorkoutFlow_MaximalValues_Success()`**
   - Edge Case: 999kg, 100 Reps, 600s, 50 Sätze
   - **Dauer:** ~7 Sekunden

### ✅ Weitere Tests

- **Database Tests** - CRUD Operationen (Insert/Update/Delete)
- **UI Tests** - Einzelne Screens und Eingaben
- **Service Tests** - Foreground Service startet/stoppt korrekt
- **Navigation Tests** - Zwischen Screens navigieren

---

## 🎯 Vor dem Release ausführen:

```bash
# 1. Alle Tests auf echtem Gerät
./gradlew connectedDebugAndroidTest

# 2. Warten bis fertig (~5-10 Minuten)

# 3. Test-Report öffnen:
open app/build/reports/androidTests/connected/index.html
```

Der Report zeigt:
- ✅ Welche Tests erfolgreich waren
- ❌ Welche Tests fehlgeschlagen sind
- 📊 Test-Coverage
- ⏱️ Ausführungszeit

---

## 🚨 Wichtig für Tests:

### Auf dem Test-Gerät deaktivieren:
```bash
# Animationen deaktivieren (damit Tests schneller/stabiler laufen)
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

### Permissions erteilen:
Die App fragt während des Tests nach Permissions (POST_NOTIFICATIONS).
Am besten vorher manuell in den Einstellungen erlauben.

---

## ❓ Unterschied: Automatisierte Tests vs. Manuell

### Automatisierte Tests (mit Gerät):
- ✅ Testen EXAKT wie ein echter Benutzer
- ✅ Klicken durch die gesamte App
- ✅ Überprüfen ob alles funktioniert
- ✅ Können wiederholt werden (vor jedem Release!)
- ⚠️ Brauchen Android-Gerät oder Emulator

### Manuell testen:
- ✅ Kannst du jederzeit machen
- ⚠️ Musst du jedes Mal wiederholen
- ⚠️ Kann man was vergessen
- ⚠️ Zeitaufwendig

### Beide sind wichtig!
- **Automatisierte Tests:** Für repetitive Aufgaben, vor jedem Release
- **Manuelle Tests:** Für UX, Performance, spezielle Szenarien

---

## 📝 Beispiel Output:

```
> Task :app:connectedDebugAndroidTest

com.asstudio.berlin.reps.EndToEndWorkoutFlowTest > 
  completeWorkoutFlow_StandardExercise_Success PASSED

com.asstudio.berlin.reps.EndToEndWorkoutFlowTest > 
  completeWorkoutFlow_CustomExercise_Success PASSED

com.asstudio.berlin.reps.EndToEndWorkoutFlowTest > 
  completeWorkoutFlow_MinimalValues_Success PASSED

com.asstudio.berlin.reps.EndToEndWorkoutFlowTest > 
  completeWorkoutFlow_MaximalValues_Success PASSED

BUILD SUCCESSFUL in 2m 15s
```

✅ **Alle Tests erfolgreich = App funktioniert wie erwartet!**

---

**Tipp:** Führe die Tests aus bevor du eine neue Version in den Play Store hochlädst!
