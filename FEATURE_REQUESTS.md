# 🚀 Feature Requests

Dokumentiere hier geplante Features und Anforderungen.

---

## In Planung

### Feature #1: Zeitbasierte Übungen (Time-Based Exercises)

**Status:** 📋 Geplant  
**Priorität:** Hoch  
**Erstellt:** 14.01.2026

#### Beschreibung
Benutzer sollen zeitbasierte Übungen erstellen können (z.B. Plank, Wall Sit, Dead Hang), bei denen statt Gewicht und Wiederholungen eine **Dauer (Duration)** angegeben wird.

#### User Story
> Als Benutzer möchte ich zeitbasierte Übungen erstellen können, damit ich auch isometrische Übungen und Halteübungen tracken kann.

#### Akzeptanzkriterien
- [ ] Bei "Create Exercise" gibt es eine Option "Time-based" / "Zeitbasiert"
- [ ] Wenn ausgewählt, werden folgende Felder angezeigt:
  - ~~Weight (Gewicht)~~ → **Entfällt**
  - ~~Repetitions (Wiederholungen)~~ → **Entfällt**
  - **Duration (Dauer)** → Neu (in Sekunden oder mm:ss Format)
  - Sets (Sätze) → Bleibt
  - Pause Time (Pausenzeit) → Bleibt
- [ ] Timer zeigt Countdown für die Dauer an (nicht Pausenzeit)
- [ ] Nach Ablauf der Dauer: Vibration/Sound
- [ ] Automatisch zum nächsten Satz wechseln (oder manuell bestätigen?)

#### UI/UX Konzept

```
┌─────────────────────────────────────────┐
│         CREATE NEW EXERCISE             │
├─────────────────────────────────────────┤
│                                         │
│  Exercise Name: [________________]      │
│                                         │
│  Type:                                  │
│  ┌─────────────┐  ┌─────────────┐      │
│  │ ● Rep-based │  │ ○ Time-based│      │
│  │  (Standard) │  │  (Duration) │      │
│  └─────────────┘  └─────────────┘      │
│                                         │
│  ─────────────────────────────────────  │
│                                         │
│  [Wenn Time-based ausgewählt:]          │
│                                         │
│  Duration:     [__30__] seconds         │
│  Sets:         [___3__]                 │
│  Rest Time:    [__60__] seconds         │
│                                         │
│           [ CREATE EXERCISE ]           │
│                                         │
└─────────────────────────────────────────┘
```

#### Timer-Screen für zeitbasierte Übungen

```
┌─────────────────────────────────────────┐
│              PLANK                      │
│         ─────────────                   │
│                                         │
│              00:45                      │
│           (remaining)                   │
│                                         │
│            Set 2/3                      │
│                                         │
│      [ SKIP ]    [ DONE EARLY ]         │
│                                         │
└─────────────────────────────────────────┘
```

#### Technische Überlegungen

1. **Datenbank-Änderung:**
   ```kotlin
   @Entity
   data class Exercise(
       val name: String,
       val isTimeBased: Boolean = false,  // NEU
       val defaultDurationSeconds: Int? = null  // NEU
   )
   
   data class CompletedSet(
       // ... existierende Felder
       val durationSeconds: Int? = null,  // NEU (alternativ zu reps)
       val isTimeBased: Boolean = false   // NEU
   )
   ```

2. **Timer-Logik:**
   - Bei Rep-based: Timer = Pausenzeit (wie aktuell)
   - Bei Time-based: Timer = Duration, dann Pausenzeit

3. **History-Anzeige:**
   - Rep-based: "3x10 @ 80kg"
   - Time-based: "3x 45s" oder "3x 0:45"

#### Offene Fragen
- [ ] Soll nach Ablauf der Duration automatisch pausiert werden oder manuell bestätigt?
- [ ] Soll es einen "Done Early" Button geben?
- [ ] Duration als Sekunden-Input oder als mm:ss Picker?
- [ ] Sollen zeitbasierte Übungen auch ein optionales Gewicht haben? (z.B. Weighted Plank)

#### Beispiel-Übungen (Time-based)
- Plank
- Side Plank
- Wall Sit
- Dead Hang
- L-Sit
- Hollow Body Hold
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
