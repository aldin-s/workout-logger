# Migration nach Compose - Issues & Lösungen

Dieses Dokument dokumentiert Probleme und deren Lösungen während der Compose-Migration.

---

## Issue #1: Unresolved Material Icons (Extended Icons)

**Datum:** 16.01.2026  
**Phase:** Phase 1 (Stats Screen)  
**Schweregrad:** 🔴 Build-Fehler

### Problem

Nach der Implementierung des `StatsScreen` und `StatCard` schlug der Build mit folgenden Fehlern fehl:

```
e: Unresolved reference: FitnessCenter
e: Unresolved reference: CalendarToday
e: Unresolved reference: Scale
e: Unresolved reference: FormatListNumbered
e: Unresolved reference: Today
e: Unresolved reference: Schedule
e: Unresolved reference: TrendingUp
```

### Ursache

Die verwendeten Icons (`FitnessCenter`, `Scale`, `CalendarToday`, etc.) gehören zur **Material Icons Extended** Bibliothek, die nicht standardmäßig in Compose Material3 enthalten ist.

Die Standard `androidx.compose.material:material-icons-core` enthält nur ~200 häufig verwendete Icons, während Extended über 2.000 Icons bereitstellt.

### Lösungsoptionen

#### Option A: Extended Icons Dependency hinzufügen
```gradle
implementation 'androidx.compose.material:material-icons-extended'
```
- ❌ Erhöht APK-Größe um ~5MB
- ✅ Alle Icons verfügbar

#### Option B: Standard-Icons verwenden (gewählt ✅)
Ersetze Extended Icons durch verfügbare Standard-Icons.

### Durchgeführte Änderungen

**Dateien:**
- `ui/stats/StatsScreen.kt`
- `ui/components/StatCard.kt`

**Icon-Mapping:**

| Original (Extended) | → | Ersatz (Standard) | Verwendung |
|---------------------|---|-------------------|------------|
| `FitnessCenter` | → | `Check` | Letztes Workout |
| `FitnessCenter` | → | `Star` | Previews |
| `CalendarToday` | → | `DateRange` | Gesamtanzahl Workouts |
| `Scale` | → | `Favorite` | Gesamtgewicht |
| `FormatListNumbered` | → | `AutoMirrored.Filled.List` | Gesamtanzahl Sets |
| `Today` / `Schedule` | → | `Refresh` | Workouts diese Woche |

### Imports nach der Änderung

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
```

### Ergebnis

✅ Build erfolgreich  
✅ APK-Größe bleibt minimal  
✅ Alle Funktionalität erhalten

### Lessons Learned

1. **Vor der Verwendung von Material Icons prüfen**, ob das Icon in `material-icons-core` enthalten ist
2. **Dokumentation konsultieren:** [Material Icons - Android Developers](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary)
3. **Verfügbare Standard-Icons:** `Star`, `Favorite`, `Check`, `Close`, `Add`, `Remove`, `Edit`, `Delete`, `Search`, `Settings`, `Home`, `Menu`, `ArrowBack`, `Refresh`, `DateRange`, `List`, etc.

---

## Vorlage für zukünftige Issues

```markdown
## Issue #X: [Titel]

**Datum:** TT.MM.JJJJ  
**Phase:** Phase X (Screen-Name)  
**Schweregrad:** 🟢 Niedrig | 🟡 Mittel | 🔴 Hoch

### Problem
[Beschreibung des Problems]

### Ursache
[Root Cause Analyse]

### Lösung
[Durchgeführte Änderungen]

### Ergebnis
[Verifizierung]
```
