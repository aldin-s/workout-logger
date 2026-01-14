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

## 🚀 Tech Debt: Compose Migration

### Warum Jetpack Compose?

| Aspekt | Status 2026 |
|--------|-------------|
| **Google Empfehlung** | ✅ Offiziell "Recommended modern toolkit" |
| **Adoption** | ✅ 50%+ neue Apps (Google I/O 2025) |
| **Stabilität** | ✅ Production-ready seit 2021 |
| **Material 3** | ✅ Compose-first Design System |

---

### Aktueller Stand vs. Best Practice

| Aspekt | Aktuell | Modern (Best Practice) |
|--------|---------|------------------------|
| **UI Framework** | XML Layouts | 🆕 Jetpack Compose |
| **Type Modellierung** | `isTimeBased: Boolean` | 🆕 Sealed Class |
| **DI** | Manuell | 🆕 Hilt |
| **Navigation** | Intent + startActivity | 🆕 Navigation Compose |
| **Tests** | Espresso | 🆕 Compose Testing |
| **Toggle Component** | MaterialButtonToggleGroup | 🆕 Material 3 SegmentedButton |

---

### Sealed Class statt Boolean (empfohlen)

```kotlin
// AKTUELL (okay, aber weniger erweiterbar)
val isTimeBased: Boolean = false

// BESSER (zukunftssicher)
sealed class ExerciseType {
    object Reps : ExerciseType()
    object Time : ExerciseType()
    // Später einfach erweiterbar:
    // object Distance : ExerciseType()  // für Laufen
    // object Calories : ExerciseType()  // für Cardio
}
```

---

### Compose Beispiel: Type Selector

```kotlin
@Composable
fun ExerciseTypeSelector(
    selectedType: ExerciseType,
    onTypeSelected: (ExerciseType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        ExerciseType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index, 
                    count = ExerciseType.entries.size
                )
            ) {
                Text(text = type.displayName)
            }
        }
    }
}

// 15 Zeilen statt 50+ XML + Activity Code!
```

---

### Compose Beispiel: Timer Screen

```kotlin
@Composable
fun TimeBasedTimerScreen(
    uiState: TimerUiState,
    onSetComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Übungsname
        Text(
            text = uiState.exerciseName,
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stoppuhr (hochzählend)
        Text(
            text = formatTime(uiState.elapsedSeconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Stoppuhr ↑",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Satz-Anzeige
        Text(
            text = "Satz ${uiState.currentSet}/${uiState.totalSets}",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Previous & Best (wenn vorhanden)
        if (uiState.previousTime != null || uiState.bestTime != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                uiState.previousTime?.let {
                    StatItem(label = "Letzter", value = formatTime(it))
                }
                uiState.bestTime?.let {
                    StatItem(label = "Best", value = formatTime(it))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Fertig-Button (immer aktiv bei TIME)
        Button(
            onClick = onSetComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 32.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SATZ FERTIG")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

### Migration Roadmap

| Phase | Beschreibung | Priorität | Status |
|-------|--------------|-----------|--------|
| 0 | Time-Based Feature mit XML liefern | Hoch | 🔄 Geplant |
| 1 | Neuen Screen (z.B. Stats) in Compose | Mittel | ⏳ Später |
| 2 | WorkoutInputActivity → Compose | Mittel | ⏳ Später |
| 3 | TimerActivity → Compose | Mittel | ⏳ Später |
| 4 | Hilt einführen | Niedrig | ⏳ Später |
| 5 | Navigation Compose | Niedrig | ⏳ Später |

---

### Vorteile nach Migration

| Aspekt | Verbesserung |
|--------|--------------|
| **Code-Zeilen** | ~40% weniger |
| **Entwicklungszeit** | 2-3x schneller |
| **Testing** | Einfacher (Compose Testing) |
| **Maintenance** | Weniger Boilerplate |
| **Preview** | @Preview für alle UI |
| **State** | Deklarativ, weniger Bugs |

---

### Entscheidung

| Option | Für Time-Based Feature |
|--------|------------------------|
| **A) XML** | ✅ Schnell liefern, pragmatisch |
| **B) Compose** | 🔄 Lernen + Modern, mehr Aufwand |

**Aktuelle Wahl:** Option A (XML) für schnelle Lieferung, Compose-Migration später.

---

## Behobene Bugs

<!-- Verschiebe behobene Bugs hierher -->

### ~~Bug #0: Beispiel~~
- **Status:** ✅ Behoben
- **Lösung:** Beschreibung der Lösung
- **Datum:** TT.MM.JJJJ
