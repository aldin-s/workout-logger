# Code Review: Import/Export Funktionalität

> **Datum:** 16.01.2026  
> **Reviewer:** Senior App Developer  
> **Datei:** `SettingsViewModel.kt`

---

## 🟡 Gesamtbewertung: **Funktional, aber verbesserungswürdig**

---

## ❌ Kritische Probleme

### 1. Keine Duplikat-Prüfung beim Import von Workouts

```kotlin
// Aktuell: Jeder Import fügt NEUE Einträge hinzu - auch bei Re-Import!
completedSetDao.insert(set)  // Ohne Duplikat-Check
```

**Risiko:** Daten-Duplizierung bei mehrfachem Import derselben Datei.

### 2. Inkonsistente Timestamp-Formate

```kotlin
// Export: createdAt als Long (Millisekunden)
exerciseJson.put("createdAt", exercise.createdAt)

// Aber timestamp bei Workouts als ISO-8601 String
setJson.put("timestamp", dateFormat.format(set.timestamp))
```

**Risiko:** Verwirrung, inkonsistentes Schema.

### 3. CSV-Export ohne Escaping

```kotlin
sb.append("${set.exerciseName},")  // ❌ Was wenn exerciseName ein Komma enthält?
```

**Risiko:** Korrupte CSV bei Übungen mit Sonderzeichen (z.B. "Schulter, Brust").

### 4. Kein Schema-Versioning

```kotlin
json.put("appVersion", BuildConfig.VERSION_NAME)  // Nur zur Info, nicht genutzt
```

**Risiko:** Bei Schema-Änderungen kein Migrations-Pfad für alte Export-Dateien.

---

## 🟡 Verbesserungswürdige Bereiche

| Bereich | Problem | Best Practice |
|---------|---------|---------------|
| **Serialisierung** | Manuelle JSONObject/JSONArray | `kotlinx.serialization` oder `Moshi` |
| **Testbarkeit** | Export/Import-Logik im ViewModel | In separate `ExportService`/`ImportService` Klassen extrahieren |
| **Fehlerbehandlung** | Generisches `Exception` | Spezifische Exceptions (`JSONException`, `IOException`, `ParseException`) |
| **Validierung** | Keine JSON-Schema-Validierung | JSON-Schema oder Datenklassen mit Validierung |
| **Transaktionen** | Kein Rollback bei Teil-Fehler | Room `@Transaction` für atomare Operationen |

---

## ✅ Was gut ist

- ✅ StateFlow für UI-Feedback (isExporting, isImporting)
- ✅ Dispatchers.IO für Dateioperationen
- ✅ FileProvider für sichere Dateifreigabe
- ✅ Backward-Compatibility: `if (json.has("exercises"))`

---

## 💡 Empfohlene Refactoring-Schritte

### 1. kotlinx.serialization verwenden

```kotlin
@Serializable
data class ExportData(
    val schemaVersion: Int = 1,
    val exportDate: Long,
    val workouts: List<WorkoutExport>,
    val exercises: List<ExerciseExport>
)
```

### 2. Dedizierte Export/Import Klassen

```kotlin
class ExportService @Inject constructor(
    private val completedSetDao: CompletedSetDao,
    private val exerciseRepository: ExerciseRepository
) {
    suspend fun exportToJson(): Result<ExportData>
    suspend fun importFromJson(data: ExportData): Result<ImportSummary>
}
```

### 3. CSV mit korrektem Escaping

```kotlin
private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}
```

### 4. Atomarer Import mit Rollback

```kotlin
@Transaction
suspend fun importWorkouts(sets: List<CompletedSet>): Result<Int>
```

---

## 📊 Bewertung

| Kriterium | Score | Anmerkung |
|-----------|-------|-----------|
| **Funktionalität** | 7/10 | Funktioniert, aber Duplikat-Problem |
| **Stabilität** | 5/10 | CSV-Escaping, keine Transaktionen |
| **Modernität** | 4/10 | Manuelle JSON-Serialisierung veraltet |
| **Testbarkeit** | 4/10 | Logik im ViewModel schwer testbar |
| **Best Practice** | 5/10 | Einige Patterns, aber viel Verbesserungspotential |

**Gesamt: 5/10** - Funktioniert für den Happy Path, aber nicht produktionsreif.

---

## 🔧 Priorisierte Fixes

### Priorität 1 (Kritisch)
- [x] CSV-Escaping implementieren
- [x] Duplikat-Prüfung beim Workout-Import

### Priorität 2 (Wichtig)
- [x] Schema-Version im Export-Format
- [x] Konsistente Timestamp-Formate (alle als Long)

### Priorität 3 (Nice-to-have)
- [x] kotlinx.serialization Migration
- [x] ExportService/ImportService Extraktion
- [x] Room @Transaction für atomare Imports

---

## ✅ Implementierung abgeschlossen (16.01.2026)

Alle Fixes wurden implementiert:

### Neue Dateien erstellt:
- `data/export/ExportModels.kt` - Data Classes mit kotlinx.serialization
- `data/export/ExportService.kt` - CSV/JSON Export mit Escaping
- `data/export/ImportService.kt` - JSON Import mit Duplikat-Prüfung

### Geänderte Dateien:
- `app/build.gradle` - kotlinx.serialization Plugin + Dependencies
- `CompletedSetDao.kt` - existsByKey() + @Transaction importAll()
- `SettingsViewModel.kt` - Refactored to use Services

### Features:
- ✅ Schema-Version 1 im JSON-Export
- ✅ Alle Timestamps als Long (konsistent)
- ✅ CSV mit RFC-4180 konformem Escaping
- ✅ Duplikat-Skip bei Import (exerciseName + timestamp + setNumber)
- ✅ Atomarer Import via Room @Transaction
- ✅ kotlinx.serialization statt manuellem JSON

---

*Implementierung abgeschlossen: 16.01.2026*
