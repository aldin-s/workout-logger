# 🏋️ Custom Exercises Feature

> **Erstellt:** 16.01.2026  
> **Status:** 📋 Planung  
> **Priorität:** Hoch

---

## 📋 Anforderungen

### Funktionale Anforderungen

| ID | Anforderung | Priorität |
|----|-------------|-----------|
| F1 | Eigene Übungen können hinzugefügt werden | Must |
| F2 | Alle Übungen erscheinen als Kacheln im Grid (einheitliches Design) | Must |
| F3 | Alle Übungen werden persistent gespeichert | Must |
| F4 | Long-Press öffnet Bottom Sheet mit Optionen (Umbenennen / Löschen) | Must |
| F5 | Alle Übungen können umbenannt werden | Must |
| F6 | Alle Übungen können gelöscht werden (mit Bestätigung) | Must |
| F8 | Reihenfolge aller Übungen kann geändert werden (Drag & Drop) | Must |

### Nicht-funktionale Anforderungen

| ID | Anforderung |
|----|-------------|
| NF1 | Einheitliches Design für alle Übungen (kein Unterschied vordefiniert/eigene) |
| NF2 | Room Database für Persistenz |
| NF3 | Repository-Pattern für saubere Architektur |
| NF4 | History bleibt erhalten (exerciseName in CompletedSet unverändert) |
| NF5 | Vordefinierte Übungen werden bei erstem Start initialisiert |

---

## 🎨 Design

### Mockup: Vollständiges Grid

```
╔═════════════════════════════════════════╗
║           NEUES WORKOUT                 ║
╠═════════════════════════════════════════╣
║  ÜBUNG WÄHLEN                           ║
║                                         ║
║  ┌───────────┐ ┌───────────┐           ║
║  │Kreuzheben │ │Bankdrücken│  ← Alle    ║
║  └───────────┘ └───────────┘    editier-║
║  ┌───────────┐ ┌───────────┐    bar!   ║
║  │  Rudern   │ │ Kniebeuge │           ║
║  └───────────┘ └───────────┘           ║
║  ┌───────────┐ ┌───────────┐           ║
║  │  Bizeps   │ │   Dips    │           ║
║  └───────────┘ └───────────┘           ║
║  ┌─────────────────────────┐           ║
║  │       + Hinzufügen      │           ║
║  └─────────────────────────┘           ║
║                                         ║
║  ─────────────────────────────────      ║
║  PARAMETER                              ║
║  ...                                    ║
╚═════════════════════════════════════════╝
```

### Interaktion: Hinzufügen

```
1. User tippt "+ Hinzufügen"
   ↓
┌─────────────────────────────┐
│  Neue Übung                 │
│                             │
│  [Übungsname eingeben___]   │
│                             │
│   [Abbrechen]  [Speichern]  │
└─────────────────────────────┘
   ↓
2. Nach Speichern: Neue Kachel erscheint im Grid
```

### Interaktion: Long-Press → Bottom Sheet

```
Long-Press auf beliebige Übung (z.B. "Kreuzheben" oder "Bizeps"):
   ↓
╭─────────────────────────────────────╮
│           KREUZHEBEN                │
│─────────────────────────────────────│
│  📝  Umbenennen                     │
│─────────────────────────────────────│
│  🗑️  Löschen                        │  ← Rote Schrift
╰─────────────────────────────────────╯
```

### Interaktion: Umbenennen

```
User tippt "Umbenennen" im Bottom Sheet:
   ↓
┌─────────────────────────────┐
│  Übung umbenennen           │
│                             │
│  [Bizeps Curls_____]        │
│                             │
│   [Abbrechen]  [Speichern]  │
└─────────────────────────────┘
```

### Interaktion: Löschen

```
User tippt "Löschen" im Bottom Sheet:
   ↓
┌─────────────────────────────┐
│   "Kreuzheben" löschen?   │
│                             │
│  Die Übung wird aus der     │
│  Auswahl entfernt. Deine    │
│  Workout-Historie bleibt    │
│  erhalten.                  │
│                             │
│   [ABBRECHEN]   [LÖSCHEN]   │
└─────────────────────────────┘
```

### Interaktion: Drag & Drop (Reihenfolge ändern)

```
Long-Press + Ziehen auf beliebige Übung:
   ↓
┌───────────────────────────────────────────┐
│  ÜBUNG WÄHLEN                             │
│                                           │
│  ┌───────────┐ ┌───────────┐             │
│  │Kreuzheben │ │░░░░░░░░░░░│ ← Platzhalter│
│  └───────────┘ └───────────┘             │
│  ┌───────────┐ ┌───────────┐             │
│  │  Rudern   │ │ Kniebeuge │             │
│  └───────────┘ └───────────┘             │
│                                           │
│           ┌─────────────┐                │
│           │ Bankdrücken │ ← Wird gezogen │
│           └─────────────┘   (erhöht)     │
└───────────────────────────────────────────┘
   ↓
Nach Loslassen: Neue Reihenfolge wird gespeichert
```

---

## 🏗️ Technischer Plan

### Phase 1: Data Layer (45 min)

1. **Exercise Entity erstellen** (ersetzt alte sealed class)
   ```kotlin
   @Entity(tableName = "exercises")
   data class Exercise(
       @PrimaryKey val id: String,              // "predefined_deadlift" oder UUID
       val name: String,
       val nameResId: Int? = null,              // Nur für vordefinierte (für i18n)
       val sortOrder: Int = 0,
       val createdAt: Long = System.currentTimeMillis()
   )
   ```

2. **ExerciseDao erstellen**
   ```kotlin
   @Dao
   interface ExerciseDao {
       @Query("SELECT * FROM exercises ORDER BY sortOrder ASC")
       fun getAll(): Flow<List<Exercise>>
       
       @Insert(onConflict = OnConflictStrategy.IGNORE)
       suspend fun insert(exercise: Exercise): Long
       
       @Insert(onConflict = OnConflictStrategy.IGNORE)
       suspend fun insertAll(exercises: List<Exercise>)
       
       @Query("DELETE FROM exercises WHERE id = :id")
       suspend fun delete(id: String)
       
       @Query("UPDATE exercises SET name = :newName WHERE id = :id")
       suspend fun updateName(id: String, newName: String)
       
       @Query("UPDATE exercises SET sortOrder = :order WHERE id = :id")
       suspend fun updateSortOrder(id: String, order: Int)
       
       @Update
       suspend fun updateAll(exercises: List<Exercise>)
       
       @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE LOWER(name) = LOWER(:name))")
       suspend fun existsByName(name: String): Boolean
       
       @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE LOWER(name) = LOWER(:name) AND id != :excludeId)")
       suspend fun existsByNameExcluding(name: String, excludeId: String): Boolean
       
       @Query("SELECT COUNT(*) FROM exercises")
       suspend fun count(): Int
   }
   ```

3. **Database Migration** (Version 1 → 2)
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("""
               CREATE TABLE IF NOT EXISTS exercises (
                   id TEXT PRIMARY KEY NOT NULL,
                   name TEXT NOT NULL,
                   nameResId INTEGER,
                   sortOrder INTEGER NOT NULL DEFAULT 0,
                   createdAt INTEGER NOT NULL
               )
           """.trimIndent())
       }
   }
   ```

4. **WorkoutDatabase erweitern**
   ```kotlin
   @Database(
       entities = [CompletedSet::class, Exercise::class],
       version = 2
   )
   abstract class WorkoutDatabase : RoomDatabase() {
       abstract fun completedSetDao(): CompletedSetDao
       abstract fun exerciseDao(): ExerciseDao
   }
   ```

5. **Vordefinierte Übungen bei App-Start initialisieren**
   ```kotlin
   // In Repository oder Application:
   suspend fun initializePredefinedExercises() {
       if (dao.count() == 0) {
           dao.insertAll(listOf(
               Exercise("predefined_deadlift", "Kreuzheben", R.string.exercise_deadlift, 0),
               Exercise("predefined_bench", "Bankdrücken", R.string.exercise_bench_press, 1),
               Exercise("predefined_rowing", "Rudern", R.string.exercise_rowing, 2),
               Exercise("predefined_squat", "Kniebeuge", R.string.exercise_squat, 3)
           ))
       }
   }
   ```

### Phase 2: Repository Layer (30 min)

1. **ExerciseRepository Interface**
   ```kotlin
   interface ExerciseRepository {
       fun getAllExercises(): Flow<List<Exercise>>
       suspend fun initializePredefined()
       suspend fun addExercise(name: String): Result<Unit>
       suspend fun renameExercise(id: String, newName: String): Result<Unit>
       suspend fun deleteExercise(id: String)
       suspend fun reorderExercises(exercises: List<Exercise>)
   }
   ```

2. **ExerciseRepositoryImpl** mit Validierung
   ```kotlin
   class ExerciseRepositoryImpl @Inject constructor(
       private val dao: ExerciseDao,
       @ApplicationContext private val context: Context
   ) : ExerciseRepository {
       
       override fun getAllExercises() = dao.getAll()
       
       override suspend fun initializePredefined() {
           if (dao.count() == 0) {
               dao.insertAll(listOf(
                   Exercise("predefined_deadlift", context.getString(R.string.exercise_deadlift), 
                            R.string.exercise_deadlift, 0),
                   Exercise("predefined_bench", context.getString(R.string.exercise_bench_press),
                            R.string.exercise_bench_press, 1),
                   Exercise("predefined_rowing", context.getString(R.string.exercise_rowing),
                            R.string.exercise_rowing, 2),
                   Exercise("predefined_squat", context.getString(R.string.exercise_squat),
                            R.string.exercise_squat, 3)
               ))
           }
       }
       
       override suspend fun addExercise(name: String): Result<Unit> {
           val trimmed = name.trim()
           if (trimmed.isBlank()) {
               return Result.failure(IllegalArgumentException("Name darf nicht leer sein"))
           }
           if (dao.existsByName(trimmed)) {
               return Result.failure(IllegalArgumentException("Übung existiert bereits"))
           }
           val maxOrder = dao.getAll().first().maxOfOrNull { it.sortOrder } ?: 0
           dao.insert(Exercise(
               id = UUID.randomUUID().toString(),
               name = trimmed,
               sortOrder = maxOrder + 1
           ))
           return Result.success(Unit)
       }
       
       override suspend fun renameExercise(id: String, newName: String): Result<Unit> {
           val trimmed = newName.trim()
           if (trimmed.isBlank()) {
               return Result.failure(IllegalArgumentException("Name darf nicht leer sein"))
           }
           if (dao.existsByNameExcluding(trimmed, id)) {
               return Result.failure(IllegalArgumentException("Übung existiert bereits"))
           }
           dao.updateName(id, trimmed)
           return Result.success(Unit)
       }
       
       override suspend fun deleteExercise(id: String) = dao.delete(id)
       
       override suspend fun reorderExercises(exercises: List<Exercise>) {
           val updated = exercises.mapIndexed { index, exercise ->
               exercise.copy(sortOrder = index)
           }
           dao.updateAll(updated)
       }
   }
   ```

3. **Hilt Module**
   ```kotlin
   // In DatabaseModule.kt ergänzen:
   @Provides
   fun provideExerciseDao(db: WorkoutDatabase): ExerciseDao {
       return db.exerciseDao()
   }
   
   // Neues RepositoryModule.kt:
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class RepositoryModule {
       @Binds
       abstract fun bindExerciseRepository(
           impl: ExerciseRepositoryImpl
       ): ExerciseRepository
   }
   ```

### Phase 3: ViewModel (30 min)

1. **WorkoutInputViewModel erweitern**
   - `exercises: StateFlow<List<Exercise>>` (alle)
   - `addExercise(name: String)`
   - `renameExercise(id: String, newName: String)`
   - `deleteExercise(id: String)`
   - `reorderExercises(exercises: List<Exercise>)`

2. **State erweitern**
   - `showAddExerciseDialog: Boolean`
   - `showBottomSheet: Exercise?`
   - `showRenameDialog: Exercise?`
   - `showDeleteConfirmDialog: Exercise?`
   - `errorMessage: String?` (für Validierungsfehler)
   - `isReorderMode: Boolean` (für Drag & Drop)

### Phase 4: UI (75 min)

1. **Dependency hinzufügen** (build.gradle)
   ```gradle
   // Reorderable - stabile Library für Drag & Drop
   implementation "org.burnoutcrew.composereorderable:reorderable:0.9.6"
   ```

2. **ExerciseCardsGrid dynamisch machen**
   - Alle Übungen aus DB laden (keine hardcoded sealed class mehr)
   - "+ Hinzufügen" Kachel am Ende
   - Long-Press für alle Übungen aktivieren

3. **Drag & Drop mit Reorderable** (ohne Animationen)
   ```kotlin
   @Composable
   fun ReorderableExerciseGrid(
       exercises: List<Exercise>,
       onReorder: (List<Exercise>) -> Unit,
       onExerciseClick: (Exercise) -> Unit,
       onExerciseLongPress: (Exercise) -> Unit,
       onAddClick: () -> Unit
   ) {
       val state = rememberReorderableLazyGridState(
           onMove = { from, to ->
               // Nur Exercises verschieben, nicht "+ Hinzufügen"
               if (from.index < exercises.size && to.index < exercises.size) {
                   val mutableList = exercises.toMutableList()
                   mutableList.add(to.index, mutableList.removeAt(from.index))
                   onReorder(mutableList)
               }
           }
       )
       
       LazyVerticalGrid(
           columns = GridCells.Fixed(2),
           state = state.gridState,
           modifier = Modifier.reorderable(state)
       ) {
           items(exercises, key = { it.id }) { exercise ->
               ReorderableItem(state, key = exercise.id) { isDragging ->
                   ExerciseCard(
                       exercise = exercise,
                       modifier = Modifier
                           .detectReorderAfterLongPress(state)
                           .combinedClickable(
                               onClick = { onExerciseClick(exercise) },
                               onLongClick = { onExerciseLongPress(exercise) }
                           )
                   )
               }
           }
           item {
               AddExerciseCard(onClick = onAddClick)
           }
       }
   }
   ```

4. **AddExerciseDialog Composable**
   - TextField mit Validierung
   - Duplikat-Prüfung
   - Error-Anzeige bei ungültigem Namen

5. **ExerciseBottomSheet Composable**
   - Umbenennen-Option
   - Löschen-Option (rote Schrift)

6. **RenameExerciseDialog Composable**
   - Pre-filled TextField
   - Validierung

7. **DeleteConfirmDialog Composable**
   - Hinweis dass History erhalten bleibt

### Phase 5: Export/Import Erweiterung (20 min)

1. **SettingsViewModel erweitern**
   ```kotlin
   // Export-Datenstruktur erweitern:
   data class ExportData(
       val completedSets: List<CompletedSet>,
       val exercises: List<Exercise>  // Alle Übungen (sichtbar + versteckt)
   )
   ```

2. **Import-Logik erweitern**
   - Exercises aus JSON lesen
   - Duplikate beim Import überspringen
   - Sortierung beibehalten

### Phase 6: Testing & Polish (30 min)

1. Unit Tests für Repository (add, rename, delete, reorder, validation)
2. Unit Tests für Migration
3. Unit Tests für Initialization (vordefinierte Übungen)
4. UI Tests für Dialog-Flow
5. UI Tests für Drag & Drop
6. Edge Cases (leerer Name, Duplikate, alle löschen, letzte Übung)

---

## ❓ Offene Fragen

> Bitte hier beantworten:

### Frage 1: Visuelle Unterscheidung
Sollen eigene Übungen visuell von vordefinierten unterscheidbar sein?

**Optionen:**
- A) Nein, alle Kacheln sehen identisch aus
- B) Ja, subtiler Indikator (z.B. kleiner Punkt oder anderer Rand)

**Antwort:** ✅ A) Nein, alle identisch

---

### Frage 2: Maximale Anzahl
Soll es eine maximale Anzahl eigener Übungen geben?

**Optionen:**
- A) Unbegrenzt
- B) Maximum (z.B. 20)

**Antwort:** ✅ A) Unbegrenzt

---

### Frage 3: Duplikate
Darf eine eigene Übung denselben Namen haben wie eine vordefinierte (z.B. "Kreuzheben")?

**Optionen:**
- A) Ja, erlaubt
- B) Nein, blockieren

**Antwort:** ✅ A) Ja, erlaubt

---

### Frage 4: Umbenennen
Soll Umbenennen in V1 enthalten sein oder später?

**Optionen:**
- A) Ja, in V1 (Long-Press öffnet Optionen: Umbenennen / Löschen)
- B) Später, V1 nur Löschen

**Antwort:** ✅ A) Ja, mit Bottom Sheet

---

### Frage 5: Export/Import
Sollen eigene Übungen im JSON-Export enthalten sein (für Backup/Restore)?

**Optionen:**
- A) Ja, mit exportieren
- B) Nein, nur Workout-Daten

**Antwort:** ✅ A) Ja, mit exportieren

---

### Frage 6: Vordefinierte Übungen editierbar?
Sollen vordefinierte Übungen (Kreuzheben, Bankdrücken, etc.) auch editierbar sein?

**Optionen:**
- A) Ja, alles editierbar
- B) Nein, nur eigene

**Antwort:** ✅ A) Ja, alles editierbar (permanent löschen, kein Wiederherstellen)

---

## 📅 Zeitschätzung

| Phase | Dauer |
|-------|-------|
| Data Layer (Entity, DAO, Migration, Init) | 45 min |
| Repository + Hilt Module | 30 min |
| ViewModel | 30 min |
| UI (Dialogs, Bottom Sheet, Drag & Drop) | 75 min |
| Export/Import Erweiterung | 20 min |
| Testing & Polish | 30 min |
| **Gesamt** | **~4h 10min** |

---

## 📝 Notizen

_Hier können zusätzliche Notizen oder Entscheidungen dokumentiert werden._

---

*Letzte Aktualisierung: 16.01.2026*
