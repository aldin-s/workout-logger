# 🚀 Feature Requests

Dokumentiere hier geplante Features und Anforderungen.

---

## In Planung

### Feature #1: Zeitbasierte Übungen (Time-Based Exercises)

**Status:** 📋 Geplant  
**Priorität:** Hoch  
**Erstellt:** 14.01.2026  
**Aktualisiert:** 14.01.2026

---

#### User Story
> Als Benutzer möchte ich zeitbasierte Übungen erstellen können (Plank, Wall Sit), damit ich auch Halteübungen tracken kann.

---

#### Design-Entscheidung: Stoppuhr statt Countdown

Nach Analyse von Strong, Hevy und anderen erfolgreichen Apps:

| Aspekt | Entscheidung | Begründung |
|--------|--------------|------------|
| Timer-Typ | ⏱️ **Stoppuhr (hochzählend)** | Flexibler, motivierend (Personal Best) |
| Duration-Feld | ❌ Nicht nötig | User hält so lange wie möglich |
| Speicherung | Erreichte Zeit bei "DONE" | Automatisch, kein Extra-Input |

---

### Phase 1: Übung erstellen (Create Exercise) - DETAILLIERT

---

## 📋 Übersicht

| Aspekt | Details |
|--------|---------|
| **Ziel** | Type-Auswahl (REPS/TIME) in Workout-Erstellung integrieren |
| **Betroffene Dateien** | 7 Dateien (4 Kotlin, 2 XML, 1 SQL) |
| **Geschätzter Aufwand** | 2-3 Stunden |
| **Risiko** | Niedrig (additive Änderungen, keine Breaking Changes) |

---

## 🗂️ Aktuelle Codestruktur

### Betroffene Dateien

```
app/src/main/
├── java/.../data/model/
│   └── CustomExercise.kt          # ⬅️ ÄNDERN: isTimeBased hinzufügen
├── java/.../data/dao/
│   └── CustomExerciseDao.kt       # ⬅️ ÄNDERN: Query für TimeBased
├── java/.../ui/workout/
│   └── WorkoutInputActivity.kt    # ⬅️ ÄNDERN: Type-Logik
├── res/layout/
│   └── activity_workout_input.xml # ⬅️ ÄNDERN: Segmented Button
└── res/values/
    └── strings.xml                # ⬅️ ÄNDERN: Neue Strings
```

---

## 🔧 Änderung 1: Datenmodell (CustomExercise.kt)

### Aktueller Code
```kotlin
@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0
)
```

### Neuer Code
```kotlin
@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
    val isTimeBased: Boolean = false  // NEU: Time-based Exercise
)
```

### Datenbank-Migration

```kotlin
// In WorkoutDatabase.kt - Migration 3 → 4
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE custom_exercises ADD COLUMN isTimeBased INTEGER NOT NULL DEFAULT 0"
        )
    }
}
```

**⚠️ Wichtig:** Default `false` = bestehende Übungen bleiben Rep-based

---

## 🎨 Änderung 2: Layout (activity_workout_input.xml)

### Neue UI-Komponente: Segmented Button

**Position:** Nach der Übungsauswahl, VOR den Input-Feldern

```xml
<!-- TYPE AUSWAHL - NEU -->
<TextView
    android:id="@+id/typeLabel"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/exercise_type_label"
    android:textSize="12sp"
    android:textColor="#808080"
    android:letterSpacing="0.05"
    android:layout_marginTop="16dp"
    android:layout_marginBottom="8dp"
    android:visibility="gone" />

<com.google.android.material.button.MaterialButtonToggleGroup
    android:id="@+id/exerciseTypeToggle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="24dp"
    android:visibility="gone"
    app:singleSelection="true"
    app:selectionRequired="true">

    <com.google.android.material.button.MaterialButton
        android:id="@+id/buttonTypeReps"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:text="@string/type_reps"
        android:textSize="14sp"
        app:strokeColor="#606060"
        app:cornerRadius="8dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/buttonTypeTime"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:text="@string/type_time"
        android:textSize="14sp"
        app:strokeColor="#606060"
        app:cornerRadius="8dp" />

</com.google.android.material.button.MaterialButtonToggleGroup>

<!-- Info-Text für Time-based -->
<TextView
    android:id="@+id/timeBasedInfoText"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/time_based_info"
    android:textSize="13sp"
    android:textColor="#808080"
    android:drawableStart="@android:drawable/ic_menu_info_details"
    android:drawablePadding="8dp"
    android:visibility="gone"
    android:layout_marginBottom="16dp" />
```

### Visuelles Mockup

```
┌─────────────────────────────────────────────┐
│            ÜBUNG AUSWÄHLEN                  │
│         ─────────────────────               │
│                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │Bankdrück │ │ Rudern   │ │  Squat   │    │
│  └──────────┘ └──────────┘ └──────────┘    │
│                                             │
│  [ + NEUE ÜBUNG ]                           │
│  ───────────────────────────────────────    │
│                                             │
│  Ausgewählt: Plank                          │ ← Erscheint nach Auswahl
│                                             │
│  Typ:                                       │ ← NEU
│  ┌──────────────┬──────────────┐           │ ← NEU
│  │   ● REPS    │   ○ ZEIT     │           │ ← NEU
│  └──────────────┴──────────────┘           │ ← NEU
│                                             │
│  ───────── WENN REPS ─────────              │
│                                             │
│  Weight:       [ 80.0 ] kg                  │
│  Repetitions:  [ 10   ]                     │
│  Rest Time:    [ 60   ] sec                 │
│  Sets:         [ 4    ]                     │
│                                             │
│  ───────── WENN ZEIT ─────────              │
│                                             │
│  ℹ️ Timer läuft hoch bis "FERTIG"           │ ← Info-Text
│                                             │
│  Rest Time:    [ 45   ] sec                 │
│  Sets:         [ 3    ]                     │
│                                             │
│             [ START ]                       │
└─────────────────────────────────────────────┘
```

---

## 🔄 Änderung 3: Activity-Logik (WorkoutInputActivity.kt)

### Neue Properties

```kotlin
class WorkoutInputActivity : AppCompatActivity() {
    
    // ... bestehende Properties ...
    
    // NEU: Type-Auswahl
    private lateinit var typeLabel: TextView
    private lateinit var exerciseTypeToggle: MaterialButtonToggleGroup
    private lateinit var buttonTypeReps: MaterialButton
    private lateinit var buttonTypeTime: MaterialButton
    private lateinit var timeBasedInfoText: TextView
    
    // NEU: State
    private var isTimeBased: Boolean = false
```

### Neue Methoden

```kotlin
/**
 * Zeigt Type-Toggle nach Übungsauswahl an
 */
private fun showTypeSelection() {
    typeLabel.visibility = View.VISIBLE
    exerciseTypeToggle.visibility = View.VISIBLE
    
    // Default: REPS ausgewählt
    exerciseTypeToggle.check(R.id.buttonTypeReps)
    updateFieldsForType(isTimeBased = false)
}

/**
 * Schaltet Felder basierend auf Type um
 */
private fun updateFieldsForType(isTimeBased: Boolean) {
    this.isTimeBased = isTimeBased
    
    if (isTimeBased) {
        // TIME: Weight und Reps ausblenden
        weightEditText.visibility = View.GONE
        repsEditText.visibility = View.GONE
        timeBasedInfoText.visibility = View.VISIBLE
        
        // Layout-Parent auch ausblenden (TextInputLayout)
        (weightEditText.parent.parent as View).visibility = View.GONE
        (repsEditText.parent.parent as View).visibility = View.GONE
    } else {
        // REPS: Alles anzeigen
        (weightEditText.parent.parent as View).visibility = View.VISIBLE
        (repsEditText.parent.parent as View).visibility = View.VISIBLE
        timeBasedInfoText.visibility = View.GONE
    }
}

/**
 * Toggle-Listener Setup
 */
private fun setupTypeToggle() {
    exerciseTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
        if (isChecked) {
            when (checkedId) {
                R.id.buttonTypeReps -> updateFieldsForType(isTimeBased = false)
                R.id.buttonTypeTime -> updateFieldsForType(isTimeBased = true)
            }
        }
    }
}
```

### Geänderte validateInput()

```kotlin
private fun validateInput(): Boolean {
    val exerciseName = getExerciseName()
    
    when {
        exerciseName.isEmpty() -> {
            // ... bestehende Logik ...
            return false
        }
    }
    
    // NUR validieren wenn REPS-Modus
    if (!isTimeBased) {
        val weightText = weightEditText.text.toString().trim()
        val repsText = repsEditText.text.toString().trim()
        
        when {
            weightText.isEmpty() -> {
                weightEditText.error = getString(R.string.error_enter_weight)
                return false
            }
            repsText.isEmpty() -> {
                repsEditText.error = getString(R.string.error_enter_reps)
                return false
            }
        }
        
        val weight = weightText.toDoubleOrNull()
        val reps = repsText.toIntOrNull()
        
        when {
            weight == null || weight <= 0 -> {
                weightEditText.error = getString(R.string.error_weight_invalid)
                return false
            }
            reps == null || reps <= 0 -> {
                repsEditText.error = getString(R.string.error_reps_invalid)
                return false
            }
        }
    }
    
    // Pause und Sets immer validieren
    val pauseTimeText = pauseTimeEditText.text.toString().trim()
    val setsText = setsEditText.text.toString().trim()
    
    // ... Rest der Validierung ...
    
    return true
}
```

### Geänderte startWorkout()

```kotlin
private fun startWorkout() {
    val exerciseName = getExerciseName()
    
    // Werte je nach Modus
    val weight = if (isTimeBased) 0.0 else 
        weightEditText.text.toString().toDoubleOrNull() ?: 0.0
    val reps = if (isTimeBased) 0 else 
        repsEditText.text.toString().toIntOrNull() ?: 0
    val pauseTime = pauseTimeEditText.text.toString().toIntOrNull() ?: 60
    val totalSets = setsEditText.text.toString().toIntOrNull() ?: 1

    lifecycleScope.launch {
        // ... Übung speichern mit isTimeBased ...
        
        val existing = database.customExerciseDao().getExerciseByName(exerciseName)
        
        if (existing != null) {
            // Update: auch isTimeBased aktualisieren falls geändert
            database.customExerciseDao().updateExercise(
                existing.copy(
                    lastUsed = System.currentTimeMillis(),
                    usageCount = existing.usageCount + 1,
                    isTimeBased = isTimeBased  // NEU
                )
            )
        } else {
            // Neue Übung mit isTimeBased
            database.customExerciseDao().insertExercise(
                CustomExercise(
                    name = exerciseName,
                    createdAt = System.currentTimeMillis(),
                    isTimeBased = isTimeBased  // NEU
                )
            )
        }
        
        // Intent mit zusätzlichem Flag
        val intent = Intent(this@WorkoutInputActivity, TimerActivity::class.java).apply {
            putExtra("EXERCISE_NAME", exerciseName)
            putExtra("WEIGHT", weight)
            putExtra("REPS", reps)
            putExtra("PAUSE_TIME", pauseTime)
            putExtra("TOTAL_SETS", totalSets)
            putExtra("IS_TIME_BASED", isTimeBased)  // NEU
        }
        
        startActivity(intent)
    }
}
```

---

## 📝 Änderung 4: Strings (strings.xml)

```xml
<!-- Exercise Type Selection -->
<string name="exercise_type_label">Typ</string>
<string name="type_reps">REPS</string>
<string name="type_time">ZEIT</string>
<string name="time_based_info">Timer läuft hoch bis du \"FERTIG\" drückst</string>

<!-- German -->
<string name="type_reps" translatable="true">WDHLG.</string>
<string name="type_time" translatable="true">ZEIT</string>
```

---

## 🧪 Testfälle für Phase 1

### Unit Tests

| Test | Beschreibung | Erwartung |
|------|--------------|-----------|
| `testRepsFieldsVisible` | REPS ausgewählt | Weight + Reps sichtbar |
| `testTimeFieldsHidden` | TIME ausgewählt | Weight + Reps unsichtbar |
| `testValidationSkipsWeightForTime` | TIME + keine Weight | Keine Fehler |
| `testExerciseSavedWithTimeBased` | TIME Übung erstellt | `isTimeBased = true` |

### UI Tests (Espresso)

```kotlin
@Test
fun testTypeToggleSwitchesFields() {
    // 1. Übung auswählen
    onView(withText("Plank")).perform(click())
    
    // 2. Type Toggle erscheint
    onView(withId(R.id.exerciseTypeToggle)).check(matches(isDisplayed()))
    
    // 3. ZEIT auswählen
    onView(withId(R.id.buttonTypeTime)).perform(click())
    
    // 4. Weight-Feld verschwindet
    onView(withId(R.id.weightEditText)).check(matches(not(isDisplayed())))
    
    // 5. Info-Text erscheint
    onView(withId(R.id.timeBasedInfoText)).check(matches(isDisplayed()))
}
```

---

## 📊 Ablaufdiagramm

```
                    ┌────────────────┐
                    │  User öffnet   │
                    │ WorkoutInput   │
                    └───────┬────────┘
                            │
                            ▼
                    ┌────────────────┐
                    │ Übung auswählen│
                    │ (Grid/Button)  │
                    └───────┬────────┘
                            │
                            ▼
               ┌────────────────────────┐
               │   Type-Toggle zeigen   │
               │   (Default: REPS)      │
               └───────────┬────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
      ┌──────────────┐          ┌──────────────┐
      │   REPS       │          │    TIME      │
      │   gewählt    │          │   gewählt    │
      └──────┬───────┘          └──────┬───────┘
             │                         │
             ▼                         ▼
      ┌──────────────┐          ┌──────────────┐
      │ Zeige:       │          │ Zeige:       │
      │ - Weight     │          │ - Info-Text  │
      │ - Reps       │          │ - Pause      │
      │ - Pause      │          │ - Sets       │
      │ - Sets       │          │              │
      └──────┬───────┘          └──────┬───────┘
             │                         │
             └────────────┬────────────┘
                          ▼
                 ┌────────────────┐
                 │  START drücken │
                 └───────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │  Validate Input │
                │  (je nach Type) │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Exercise in DB  │
                │ speichern mit   │
                │ isTimeBased     │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ TimerActivity   │
                │ mit IS_TIME_    │
                │ BASED Extra     │
                └─────────────────┘
```

---

## ✅ Akzeptanzkriterien (Phase 1)

- [ ] **UI:** Segmented Control erscheint nach Übungsauswahl
- [ ] **UI:** Default-Auswahl ist "REPS"
- [ ] **UI:** Bei "TIME" verschwinden Weight und Reps Felder
- [ ] **UI:** Info-Text erscheint bei "TIME"
- [ ] **DB:** `isTimeBased` Spalte existiert (Migration)
- [ ] **DB:** Neue Übungen speichern `isTimeBased` korrekt
- [ ] **Intent:** `IS_TIME_BASED` wird an TimerActivity übergeben
- [ ] **Validation:** Weight/Reps werden bei TIME nicht validiert
- [ ] **Tests:** Alle Unit Tests grün
- [ ] **Tests:** UI Tests bestehen

---

## ⏱️ Felder-Vergleich

| Feld | REPS | TIME |
|------|------|------|
| Weight | ✅ | ❌ |
| Repetitions | ✅ | ❌ |
| Rest Time | ✅ | ✅ |
| Sets | ✅ | ✅ |

---

### Phase 2: Timer-Screen (Workout)

#### REPS (wie aktuell)
```
┌─────────────────────────────────────────┐
│              BANKDRÜCKEN                │
│            ─────────────                │
│               80 kg                     │
│                                         │
│              00:45                      │
│            (Countdown)                  │
│                                         │
│             Satz 2/4                    │
│                                         │
│      [ PAUSE LÄUFT... ]                 │
│              ↓                          │
│      [ SATZ FERTIG ✓ ]                  │
└─────────────────────────────────────────┘
```

#### TIME (neu)
```
┌─────────────────────────────────────────┐
│                PLANK                    │
│            ─────────────                │
│                                         │
│              00:47                      │
│            (Stoppuhr ↑)                 │
│                                         │
│             Satz 2/3                    │
│                                         │
│  Letzter: 0:42  │  Best: 0:51          │
│                                         │
│         [ ✓ SATZ FERTIG ]               │
└─────────────────────────────────────────┘
```

**Unterschiede:**
- Timer läuft **hoch** statt runter
- Kein Gewicht angezeigt
- Zeigt **Previous** und **Best** Zeit
- Button ist sofort aktiv (kein Warten auf Timer)

---

### Phase 3: History & Statistik

#### Anzeige in History

**REPS:**
```
BANKDRÜCKEN        80 kg · 4×10
```

**TIME:**
```
PLANK              3× 0:47 avg
                   Best: 0:51
```

---

### Technische Implementierung

#### 1. Datenbank-Migration

```kotlin
// Exercise Entity - Änderung
@Entity
data class Exercise(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val isTimeBased: Boolean = false  // NEU
)

// CompletedSet Entity - Änderung
@Entity
data class CompletedSet(
    // ... existierende Felder
    val durationSeconds: Int? = null,  // NEU: für Time-based
    // weight und reps bleiben nullable
)
```

#### 2. UI Komponenten

```kotlin
// Neuer Composable oder XML für Type-Auswahl
// SegmentedButton mit REPS / TIME

// WorkoutInputActivity anpassen:
// - Type-Auswahl hinzufügen
// - Felder basierend auf Type ein/ausblenden

// TimerActivity/ViewModel anpassen:
// - Stoppuhr-Modus (hochzählend)
// - Previous/Best anzeigen
```

#### 3. Ablauf-Logik

```
TIME-BASED WORKFLOW:
1. User drückt START
2. Timer startet bei 00:00 und zählt HOCH
3. User drückt "SATZ FERTIG" wenn er aufgibt
4. Erreichte Zeit wird gespeichert
5. Rest-Time Countdown startet (wie bei REPS)
6. Nach Rest-Time: Nächster Satz
7. Wiederholen bis alle Sätze durch
```

---

### Akzeptanzkriterien

#### Phase 1: Create Exercise
- [ ] Segmented Control für Type (REPS / TIME)
- [ ] Bei TIME: Weight und Reps ausblenden
- [ ] Rest Time und Sets bleiben sichtbar
- [ ] Übung wird mit `isTimeBased=true` gespeichert

#### Phase 2: Timer Screen
- [ ] Bei Time-based: Stoppuhr (hochzählend)
- [ ] Button sofort aktiv (kein Warten)
- [ ] Zeigt "Previous" und "Best" Zeit
- [ ] Speichert `durationSeconds` statt `reps`

#### Phase 3: History
- [ ] Time-based Übungen zeigen Durchschnittszeit
- [ ] Personal Best wird angezeigt

---

### Beispiel-Übungen (Time-based)
- Plank
- Side Plank
- Wall Sit
- Dead Hang
- L-Sit
- Hollow Body Hold

---

### Offene Fragen
- [ ] Soll es einen "Pause" Button geben während der Stoppuhr?
- [ ] Weighted Time-based (z.B. Weighted Plank) in Phase 2?
- Superman Hold

---

## ✅ Geklärte Entscheidungen

### Product Owner Entscheidungen

| # | Frage | Entscheidung | Begründung |
|---|-------|--------------|------------|
| 1 | Pause-Button während Stoppuhr? | ❌ **Nein** | Offen (noch zu klären) |
| 2 | Mindestzeit für Satz? | **1 Sekunde** | Verhindert Fehlklicks |
| 3 | Versehentlich "FERTIG" gedrückt? | **Undo-Toast (5 Sek)** | Schneller als Dialog |
| 4 | Personal Best Scope? | **All-Time** | Motivierender |
| 5 | Sound/Vibration bei TIME? | ❌ **Nein** | Erstmal simpel halten |

### UX Entscheidungen

| # | Frage | Entscheidung | Begründung |
|---|-------|--------------|------------|
| 6 | Timer-Farbe bei TIME anders? | ❌ **Nein, gleich** | Konsistentes Design |
| 7 | Animation bei Personal Best? | ❌ **Nein** | Simpel halten |
| 8 | Haptic Feedback bei Satz-Ende? | ❌ **Nein** | Nicht nötig |
| 9 | "Letzter" und "Best" wann anzeigen? | **Immer (wenn vorhanden)** | Ab 1. Satz wenn History existiert |

### Technische Entscheidungen

| # | Frage | Entscheidung | Begründung |
|---|-------|--------------|------------|
| 10 | Type einer Übung änderbar? | **Ja** | Flexibilität, History bleibt getrennt |
| 11 | durationSeconds Datentyp? | **Int** | 2.1 Mrd Sekunden = 68 Jahre, reicht |
| 12 | CompletedSet weight/reps nullable? | **Ja (Phase 2)** | Für TIME: weight=null, reps=null |

---

### UI Mockup: "Letzter" und "Best" Anzeige

```
┌─────────────────────────────────────────┐
│                PLANK                    │
│            ─────────────                │
│                                         │
│              00:47                      │
│            (Stoppuhr ↑)                 │
│                                         │
│             Satz 1/3                    │
│                                         │
│  Letzter: 0:42  │  Best: 0:51          │ ← Zeigt History-Werte
│  (vom letzten   │  (All-Time           │    falls vorhanden
│   Workout)      │   Record)            │
│                                         │
│         [ ✓ SATZ FERTIG ]               │
└─────────────────────────────────────────┘
```

**Logik:**
- **Letzter:** Letzter Satz dieser Übung (Session oder vorheriges Workout)
- **Best:** All-Time Personal Best für diese Übung
- **Keine Daten:** Bereich ausblenden oder "—" anzeigen

---

## 🔋 Screen Wake Management (Time-Based)

### Problem

| Situation | Problem |
|-----------|---------|
| User hält Plank 60 Sekunden | Bildschirm geht nach 30s aus (Device-Default) |
| User muss "FERTIG" drücken | Erst entsperren, dann Button finden |
| Timer läuft im Background | User sieht keine aktuelle Zeit |
| Motivation sinkt | Kein visuelles Feedback während Übung |

---

### Lösung: 3-Schichten Ansatz

```
┌─────────────────────────────────────────────┐
│  Schicht 1: FLAG_KEEP_SCREEN_ON             │
│  → Bildschirm bleibt an während Timer läuft │
├─────────────────────────────────────────────┤
│  Schicht 2: Notification Action Button      │
│  → "FERTIG" auch bei Lock-Screen drückbar   │
├─────────────────────────────────────────────┤
│  Schicht 3: Dimming (optional)              │
│  → Helligkeit reduzieren für Batteriesparen │
└─────────────────────────────────────────────┘
```

---

### Implementierung

#### Schicht 1: Keep Screen On (TimerActivity.kt)

```kotlin
class TimerActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // NEU: Screen Wake für Time-Based
        val isTimeBased = intent.getBooleanExtra("IS_TIME_BASED", false)
        if (isTimeBased) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    override fun onDestroy() {
        // Flag wieder entfernen
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }
}
```

**Keine Permission nötig!** ✅

---

#### Schicht 2: Notification mit Action (TimerService.kt)

**Aktuelle Notification:**
```
┌─────────────────────────────┐
│ 🏋️ Timer läuft              │
│    00:47                    │
└─────────────────────────────┘
```

**Erweiterte Notification für TIME:**
```
┌─────────────────────────────┐
│ 🏋️ PLANK - Satz 2/3         │
│    00:47 ↑                  │
│                             │
│  [ ✓ SATZ FERTIG ]          │  ← Action Button
└─────────────────────────────┘
```

```kotlin
// In TimerService.kt - Notification Builder erweitern
private fun createNotification(isTimeBased: Boolean): Notification {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(if (isTimeBased) "$exerciseName - Satz $currentSet/$totalSets" else "Timer läuft")
        .setContentText(formatTime(elapsedTime))
        .setSmallIcon(R.drawable.ic_timer)
        .setOngoing(true)
    
    // Action Button NUR für Time-Based
    if (isTimeBased) {
        val finishIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_SET_COMPLETE
        }
        val finishPendingIntent = PendingIntent.getService(
            this, 0, finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        builder.addAction(
            R.drawable.ic_check,
            "SATZ FERTIG",
            finishPendingIntent
        )
    }
    
    return builder.build()
}
```

---

#### Schicht 3: Dimming (Optional - Battery Saver)

```kotlin
/**
 * Reduziert Helligkeit während Time-Based Übung.
 * User kann Timer noch sehen, aber Batterie wird geschont.
 */
private fun enableBatterySaverMode() {
    val layoutParams = window.attributes
    originalBrightness = layoutParams.screenBrightness
    layoutParams.screenBrightness = 0.15f  // 15% Helligkeit
    window.attributes = layoutParams
}

private fun disableBatterySaverMode() {
    val layoutParams = window.attributes
    layoutParams.screenBrightness = originalBrightness
    window.attributes = layoutParams
}
```

**⚠️ Optional:** Nur implementieren wenn User explizit Batterie sparen will.

---

### Vergleich: REPS vs TIME

| Aspekt | REPS | TIME |
|--------|------|------|
| Screen Timeout | Normal (Device-Setting) | Deaktiviert |
| Notification | Einfach (nur Zeit) | Mit Action Button |
| Batterie-Impact | Minimal | Moderat |
| User-Interaktion | Nach Countdown | Jederzeit möglich |

---

### Edge Cases

| Szenario | Verhalten |
|----------|-----------|
| User verlässt App während TIME | Service läuft weiter, Notification bleibt |
| User dreht Handy | Timer läuft weiter (ViewModel) |
| Anruf kommt rein | Timer pausiert NICHT (bewusste Entscheidung) |
| Low Battery Mode | `FLAG_KEEP_SCREEN_ON` wird ggf. vom System ignoriert |

---

### Akzeptanzkriterien

- [ ] **TIME:** Bildschirm bleibt an während Stoppuhr läuft
- [ ] **TIME:** Notification zeigt "SATZ FERTIG" Button
- [ ] **TIME:** Action Button funktioniert auch bei Lock-Screen
- [ ] **REPS:** Keine Änderung am bisherigen Verhalten
- [ ] **Cleanup:** Flag wird bei Activity-Destroy entfernt
- [ ] **Rotation:** Screen-Wake überlebt Configuration Change

---

## Umgesetzt

*Noch keine Features umgesetzt.*

---

## Vorlage

```markdown
### Feature #X: [Titel]

**Status:** 📋 Geplant | 🔄 In Arbeit | ✅ Umgesetzt  
**Priorität:** Hoch | Mittel | Niedrig  
**Erstellt:** TT.MM.JJJJ

#### Beschreibung
...

#### User Story
> Als [Rolle] möchte ich [Funktion], damit [Nutzen].

#### Akzeptanzkriterien
- [ ] ...

#### Technische Überlegungen
...
```
