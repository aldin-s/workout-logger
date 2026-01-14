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

### Phase 1: Übung erstellen (Create Exercise)

#### Aktueller Flow
```
[Übungsname] → [Weight] → [Reps] → [Pause] → [Sets] → START
```

#### Neuer Flow mit Type-Auswahl
```
[Übungsname] → [Type wählen] → [Felder je nach Type] → START
```

---

#### UI Design: Type-Auswahl

**Option A: Toggle/Segmented Control** ⭐ Empfohlen
```
┌─────────────────────────────────────────┐
│         CREATE NEW EXERCISE             │
├─────────────────────────────────────────┤
│                                         │
│  Name: [  Plank________________]        │
│                                         │
│  Type:                                  │
│  ┌──────────────┬──────────────┐       │
│  │  ● REPS     │  ○ TIME      │       │
│  │  (Standard)  │  (Stoppuhr)  │       │
│  └──────────────┴──────────────┘       │
│                                         │
└─────────────────────────────────────────┘
```

**Option B: Radio Buttons**
```
│  Type:                                  │
│  ◉ Rep-based (Gewicht & Wiederholungen) │
│  ○ Time-based (Stoppuhr)                │
```

**Empfehlung:** Option A (Segmented Control) - moderner, platzsparender

---

#### Felder je nach Type

**REPS (Standard) - wie aktuell:**
```
┌─────────────────────────────────────────┐
│  Weight:       [ 80.0 ] kg              │
│  Repetitions:  [ 10   ]                 │
│  Rest Time:    [ 60   ] sec             │
│  Sets:         [ 4    ]                 │
│                                         │
│            [ START ]                    │
└─────────────────────────────────────────┘
```

**TIME (neu) - vereinfacht:**
```
┌─────────────────────────────────────────┐
│                                         │
│  Rest Time:    [ 45   ] sec             │
│  Sets:         [ 3    ]                 │
│                                         │
│  ℹ️ Timer läuft hoch bis du             │
│     "DONE" drückst                      │
│                                         │
│            [ START ]                    │
└─────────────────────────────────────────┘
```

**Felder-Vergleich:**

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
