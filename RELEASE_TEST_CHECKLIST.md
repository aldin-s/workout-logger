# 🧪 Release Test Checkliste

## Testziel
Stelle sicher, dass alle Features auf verschiedenen Geräten und Android-Versionen korrekt funktionieren.

---

## 📱 Geräte-Matrix

Teste auf mindestens:
- [ ] **Android 5-7** (API 21-25) - Altes Gerät
- [ ] **Android 8-10** (API 26-29) - Mittleres Gerät  
- [ ] **Android 11-13** (API 30-33) - Modernes Gerät
- [ ] **Android 14+** (API 34+) - Neuestes (Xiaomi/Samsung!)

Hersteller-Varianten (wichtig wegen Restriktionen):
- [ ] Xiaomi (MIUI) - strengste Restriktionen
- [ ] Samsung (One UI) - eigene Optimierungen
- [ ] Google Pixel - Stock Android
- [ ] Huawei/Honor (falls verfügbar)

---

## ✅ Funktionale Tests

### 1. App-Start & Installation
- [ ] Frische Installation funktioniert
- [ ] App öffnet ohne Crash
- [ ] Keine Fehler in Logcat: `adb logcat | grep -i "reps\|error\|exception"`
- [ ] Dunkelmodus ist aktiviert
- [ ] Versionsnummer korrekt angezeigt (in Einstellungen)

### 2. Workout-Erstellung (Standard-Übungen)
- [ ] **Kreuzheben** auswählen
  - [ ] Gewicht eingeben (z.B. 100 kg)
  - [ ] Wiederholungen eingeben (z.B. 5)
  - [ ] Pausenzeit eingeben (z.B. 120s)
  - [ ] Anzahl Sätze eingeben (z.B. 5)
  - [ ] "Start" drücken
- [ ] Timer-Screen öffnet sich
- [ ] Timer startet automatisch
- [ ] Benachrichtigung erscheint ✓

**Wiederhole für:** Bankdrücken, Rudern, Kniebeuge

### 3. Workout-Erstellung (Eigene Übung) ⚠️ KRITISCH
- [ ] "Eigene Übung" auswählen
- [ ] Namen eingeben (z.B. "Bizeps Curl")
- [ ] Alle Parameter eingeben
- [ ] "Start" drücken
- [ ] **KEIN CRASH** auf Android 14+ (Xiaomi!)
- [ ] Timer startet korrekt

### 4. Timer-Funktionalität
- [ ] Timer zählt korrekt runter (z.B. 2:00 → 1:59 → ...)
- [ ] Timer erreicht 0:00
- [ ] **Vibration** bei Timer-Ende (falls aktiviert)
- [ ] **Sound** bei Timer-Ende (falls aktiviert)
- [ ] Benachrichtigung zeigt korrekten Stand
- [ ] "Fertig"-Button ist während Timer deaktiviert
- [ ] "Fertig"-Button aktiviert sich bei 0:00

### 5. Satz-Verwaltung
- [ ] Satz 1 abschließen → Timer startet für Satz 2
- [ ] Satzanzeige aktualisiert (1/5 → 2/5 → ...)
- [ ] Alle Sätze durchführen
- [ ] Nach letztem Satz: Tracking-Screen erscheint
- [ ] Workout wird in Datenbank gespeichert

### 6. Hintergrund-Verhalten
- [ ] Timer läuft im Hintergrund weiter
- [ ] Benachrichtigung bleibt sichtbar
- [ ] App aus Recent Apps entfernen → Timer läuft
- [ ] Bildschirm sperren → Timer läuft
- [ ] 5 Minuten warten → Timer noch aktiv
- [ ] App wieder öffnen → korrekter Stand

### 7. Historie
- [ ] "Verlauf" öffnen
- [ ] Workouts sind gruppiert nach Datum
- [ ] Heutiges Workout sichtbar
- [ ] Details anzeigen funktioniert
- [ ] Workout bearbeiten funktioniert
- [ ] Gewicht/Wiederholungen ändern und speichern
- [ ] Workout löschen funktioniert
- [ ] Nach Löschen: Workout ist weg

### 8. Einstellungen

#### Vibration
- [ ] Vibration ein/aus schaltet
- [ ] Vibrationsdauer ändern (Kurz/Mittel/Lang)
- [ ] Test bei Timer-Ende: Vibration funktioniert

#### Sound
- [ ] Sound ein/aus schaltet
- [ ] Sound-Auswahl öffnet System-Picker
- [ ] Neuen Sound wählen
- [ ] Test bei Timer-Ende: Sound spielt ab
- [ ] Sound stoppt nach paar Sekunden

#### Bildschirm an
- [ ] "Bildschirm an" aktivieren
- [ ] Im Timer: Bildschirm bleibt an
- [ ] "Bildschirm an" deaktivieren
- [ ] Im Timer: Bildschirm schaltet sich aus

#### Standard-Pausenzeit
- [ ] Pausenzeit ändern (z.B. auf 60s)
- [ ] Neue Übung starten
- [ ] **TODO: Muss implementiert werden** (aktuell nicht genutzt)

#### Export/Import
- [ ] History exportieren (CSV)
- [ ] CSV-Datei öffnen und prüfen
- [ ] History exportieren (JSON)
- [ ] JSON-Datei öffnen und prüfen
- [ ] JSON-Datei importieren
- [ ] Workouts sind wieder da

#### Alle Daten löschen
- [ ] "Alle Daten löschen" drücken
- [ ] Bestätigung erscheint
- [ ] Abbrechen funktioniert
- [ ] Nochmal versuchen und bestätigen
- [ ] Alle Workouts sind gelöscht
- [ ] Einstellungen sind zurückgesetzt

### 9. Navigation & Back-Button
- [ ] Von Main → Workout Input → zurück funktioniert
- [ ] Von Main → History → zurück funktioniert
- [ ] Von Main → Settings → zurück funktioniert
- [ ] Von Timer → zurück: Warnung oder zurück zu Main
- [ ] Von Tracking → zurück: zu Main (nicht zu Timer!)

### 10. Berechtigungen (Android 13+)
- [ ] Beim ersten Start: Notification-Permission-Dialog
- [ ] Erlauben drücken → App funktioniert
- [ ] App deinstallieren und neu installieren
- [ ] Diesmal ablehnen
- [ ] **App sollte trotzdem funktionieren** (nur ohne Notification)
- [ ] Manuelle Berechtigung später erteilen in System-Einstellungen

---

## 🔧 Edge Cases & Fehlerbehandlung

### Extreme Werte
- [ ] Gewicht: 0.5 kg (Minimum)
- [ ] Gewicht: 999 kg (Maximum)
- [ ] Wiederholungen: 1
- [ ] Wiederholungen: 100
- [ ] Pausenzeit: 10 Sekunden
- [ ] Pausenzeit: 600 Sekunden (10 Min)
- [ ] Sätze: 1
- [ ] Sätze: 50

### Leere Eingaben
- [ ] Nichts eingeben und "Start" drücken → Fehlermeldung
- [ ] Nur Übung wählen → Fehlermeldung
- [ ] Nur halbe Daten → Fehlermeldung

### Rotation/Lifecycle
- [ ] Gerät drehen während Timer läuft
- [ ] Timer-Stand bleibt korrekt
- [ ] App minimieren und wieder öffnen
- [ ] Kill App aus Einstellungen → neustart zeigt Daten

### Speicher & Performance
- [ ] 100+ Workouts in Historie erstellen
- [ ] Historie scrollt flüssig
- [ ] Export funktioniert noch
- [ ] App-Größe prüfen (sollte < 5 MB bleiben)

### Xiaomi/Samsung-Spezifisch ⚠️
- [ ] Background Activity Permission erteilen
- [ ] Autostart erlauben
- [ ] Battery Optimization deaktivieren
- [ ] Notification Permission explizit prüfen
- [ ] Timer läuft mit allen Permissions
- [ ] Fehler-Dialog erscheint bei fehlenden Permissions

---

## 🐛 Logcat-Monitoring

Führe Tests mit aktivem Logcat aus:

```bash
# Alle Errors/Exceptions
adb logcat | grep -i "error\|exception\|crash"

# App-spezifische Logs
adb logcat | grep -i "reps\|timer\|workout"

# Memory Leaks
adb shell dumpsys meminfo com.asstudio.berlin.reps
```

### Erwartete Logs (OK):
- `TimerService: Timer started`
- `TimerService: Timer finished`
- Keine SecurityException
- Keine ForegroundServiceStartNotAllowedException

### Kritische Errors (MUSS GEFIXT WERDEN):
- `SecurityException` → Permissions fehlen
- `IllegalStateException` → Service-Problem
- `NullPointerException` → Coding-Fehler
- `SQLiteException` → Datenbank-Problem

---

## 📊 Performance-Checks

```bash
# CPU-Auslastung
adb shell top | grep reps

# Akku-Verbrauch (über Zeit beobachten)
adb shell dumpsys batterystats | grep reps

# Network (sollte 0 sein - keine Netzwerk-Nutzung)
adb shell dumpsys netstats | grep reps
```

Erwartete Werte:
- CPU: < 5% im Idle
- CPU: < 15% während Timer
- RAM: < 100 MB
- Battery: Minimal (nur Wakelock für Timer)
- Network: 0 Bytes

---

## ✅ Pre-Release Checklist (Final)

### Code
- [ ] Keine `TODO` Kommentare im kritischen Code
- [ ] Keine Debug-Logs (`Log.d`) in Production
- [ ] Keine Hardcoded Test-Daten
- [ ] ProGuard/R8 Rules korrekt

### Build
- [ ] `minifyEnabled true` aktiviert
- [ ] `shrinkResources true` aktiviert
- [ ] Signing-Config korrekt
- [ ] VersionCode erhöht (automatisch via Git)
- [ ] VersionName sinnvoll (z.B. 1.1)

### Play Store
- [ ] AAB-Datei < 150 MB ✓ (aktuell: 2.1 MB)
- [ ] targetSdkVersion = 36 (neueste)
- [ ] Alle Permissions begründbar
- [ ] Privacy Policy vorhanden (falls nötig)
- [ ] Screenshots vorbereitet
- [ ] App-Beschreibung aktualisiert

### Testing
- [ ] Mindestens 3 verschiedene Geräte getestet
- [ ] Mindestens 1x Android 14+ (Xiaomi/Samsung)
- [ ] Alle kritischen Features funktionieren
- [ ] Keine Crashes in 20 Minuten Stress-Test
- [ ] Beta-Tester Feedback eingeholt

---

## 🚀 Automatisierte Tests (Optional)

Wenn du automatisierte Tests willst:

```bash
# UI-Tests ausführen
./gradlew connectedAndroidTest

# Unit-Tests
./gradlew test

# Lint + Test zusammen
./gradlew check
```

---

## 📝 Test-Protokoll Vorlage

Für jeden Test-Durchlauf dokumentieren:

```markdown
## Test-Session [Datum]

**Gerät:** Xiaomi 12 Lite
**Android:** 14
**Build:** Version 1.1 (versionCode 37)

### Durchgeführte Tests
- ✅ Standard-Übungen: OK
- ✅ Eigene Übung: OK
- ✅ Timer: OK
- ⚠️ Vibration: Funktioniert nicht (Permission fehlt?)
- ❌ Export: Crash beim CSV-Export

### Gefundene Bugs
1. CSV-Export crasht bei > 50 Workouts
2. Vibration-Permission wird nicht angefordert

### Notizen
- Performance sehr gut
- Akku-Verbrauch minimal
- UI flüssig
```

---

## 🎯 Minimale Release-Kriterien

Vor Release MÜSSEN folgende Tests grün sein:

### Kritisch ✅
- [ ] App startet auf allen Android-Versionen
- [ ] Eigene Übung funktioniert auf Android 14
- [ ] Timer läuft im Hintergrund
- [ ] Workouts werden gespeichert
- [ ] Keine Crashes bei normaler Nutzung

### Wichtig ⚠️
- [ ] Export/Import funktioniert
- [ ] Benachrichtigungen erscheinen
- [ ] Historie zeigt alle Workouts

### Nice-to-have 💡
- [ ] Vibration funktioniert überall
- [ ] Sound funktioniert überall
- [ ] Alle Übungen getestet

---

## 📞 Troubleshooting

### "App startet nicht"
```bash
adb logcat -c && adb logcat | grep -i "reps\|exception"
```

### "Service startet nicht"
```bash
# Prüfe Permissions
adb shell dumpsys package com.asstudio.berlin.reps | grep permission

# Prüfe Service-Status
adb shell dumpsys activity services | grep TimerService
```

### "Datenbank-Fehler"
```bash
# Lösche App-Daten
adb shell pm clear com.asstudio.berlin.reps

# Prüfe Datenbank
adb shell "run-as com.asstudio.berlin.reps ls /data/data/com.asstudio.berlin.reps/databases"
```

---

## 🎉 Release-Ready?

Wenn alle **Kritisch** ✅ Tests bestanden sind → **GO FOR RELEASE!**

Viel Erfolg! 🚀
