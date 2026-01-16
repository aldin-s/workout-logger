# 🚀 Jetpack Compose Migration Plan

> **Erstellt:** 14.01.2026  
> **Status:** 🔄 In Arbeit (Phase 6)  
> **Priorität:** Nach Time-Based Feature  
> **Letzte Überarbeitung:** 16.01.2026 - Phase 6 Foreground Service, Phase 9 hinzugefügt

---

## 📋 Übersicht

Dieses Dokument beschreibt die schrittweise Migration von XML-Layouts zu Jetpack Compose für die REPS Workout-Tracker App.

### Aktuelle Architektur
- **UI:** XML Layouts + View Binding
- **Navigation:** Intent + startActivity
- **DI:** Manuell
- **State:** ViewModel + StateFlow ✅ (bereits modern)

### Ziel-Architektur
- **UI:** Jetpack Compose
- **Navigation:** Navigation Compose (String Routes - stabil seit 2021)
- **DI:** Hilt
- **State:** ViewModel + StateFlow (bleibt)

---

## 📅 Phasen-Übersicht

| Phase | Beschreibung | Dauer | Komplexität | Status |
|-------|--------------|-------|-------------|--------|
| **0** | Setup + Theme | 2 Tage | 🟢 Niedrig | ✅ 100% |
| ~~**1**~~ | ~~Stats Screen (Pilot)~~ | - | - | ❌ Entfernt |
| **2** | Settings Screen | **5 Tage** | **🔴 Hoch** | ✅ 100% |
| **3** | History Screen | 3 Tage | 🟡 Mittel | ✅ 100% |
| **4** | Main/Dashboard Screen | **2 Tage** | **🟢 Niedrig** | ✅ 100% |
| **5** | WorkoutInput Screen | **3 Tage** | **🟡 Mittel** | ✅ 100% |
| **6** | Timer Screen + Foreground Service | 4 Tage | 🔴 Hoch | ✅ 100% |
| **7** | Hilt Integration | 3 Tage | 🟡 Mittel | ✅ 100% |
| **8** | Cleanup & Polish | 3 Tage | 🟢 Niedrig | ✅ 100% |
| **9** | Architektur-Verbesserungen (v2.0) | 4 Tage | 🔴 Hoch | ✅ 100% |

**Migration abgeschlossen!** 🎉

> 📝 **16.01.2026:** Phase 1 (Stats) wurde entfernt - Feature nicht mehr benötigt.
> 📝 **16.01.2026:** Phase 9 (v2.0) hinzugefügt - Architektur-Verbesserungen nach Code Review.
> 📝 **16.01.2026:** Navigation Compose entfernt - Multi-Activity-Architektur beibehalten (simpler, funktioniert).
> 📝 **16.01.2026:** Alle XML-Layouts gelöscht - 100% Compose UI.

### 📈 Komplexitätsanalyse (16.01.2026)

**Code-Analyse der bestehenden Activities:**

| Screen | Zeilen | Urspr. Schätzung | Reale Komplexität | Begründung |
|--------|--------|------------------|-------------------|-------------|
| Settings | 487 | 🟢 Niedrig | 🔴 **Hoch** | Import/Export, Theme, Sprache, SharedPrefs, Dialoge |
| WorkoutInput | 268 | 🔴 Hoch | 🟡 Mittel | Card-Auswahl, Formulare (weniger komplex als gedacht) |
| Timer | 259 | 🔴 Hoch | 🔴 Hoch | CountDownTimer, Vibration, Sound, Service-Logik |
| History | 232 | 🟡 Mittel | 🟡 Mittel | RecyclerView + Gruppierung, Dialoge |
| Main | 103 | 🟡 Mittel | 🟢 Niedrig | Nur Navigation-Hub, Debug-Menü |
| Tracking | 62 | - | 🟢 Niedrig | Einfacher Übergangs-Screen |

### Empfohlene Reihenfolge-Anpassung

Basierend auf der Analyse wird die Reihenfolge **beibehalten**, aber mit angepassten Zeitschätzungen:

1. **Phase 4 (Main) vorziehen?** - Niedrige Komplexität, aber Navigation-Hub
2. **Phase 2 (Settings) braucht mehr Zeit** - ViewModel-Extraktion zuerst

### Reihenfolge-Begründung

| Prinzip | Erklärung |
|---------|------------|
| **Einfach → Komplex** | Main (Navigation-Hub) vor Settings (komplexe Logik) |
| **Navigation früh** | Vermeidet doppelte Arbeit mit Bridge-Activities |
| **Service-bound zuletzt** | Timer mit Foreground Service ist am komplexesten |
| **Hilt nach Screens** | DI-Migration ist invasiv, separate Phase |

---

## 🔧 Phase 0: Vorbereitung & Setup + Navigation Shell

### 0.0 Interop-Strategie (XML ↔ Compose)

Während der Migration existieren beide UI-Systeme parallel:

```kotlin
// Compose in XML-Activity einbetten
class OldActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_old)
        
        val composeView = findViewById<ComposeView>(R.id.compose_container)
        composeView.setContent {
            RepsTheme {
                NewComposeComponent()
            }
        }
    }
}

// XML in Compose einbetten (selten nötig)
@Composable
fun LegacyViewWrapper() {
    AndroidView(
        factory = { context ->
            OldCustomView(context)
        },
        update = { view ->
            view.updateData(newData)
        }
    )
}
```

**Regeln:**
- ✅ Neue Screens: Compose
- ✅ Neue Komponenten in alten Screens: ComposeView
- ❌ Keine halben Screen-Migrationen (ganz oder gar nicht)

### 0.0.1 Design-Regeln (Brutalistisches Monochrom-Design)

**Farben zentral speichern:**
```
Color.kt           → Alle Farbwerte definieren
Theme.kt           → ColorScheme mit den Farben aufbauen  
UI Components      → NUR MaterialTheme.colorScheme.xxx verwenden
```

**Verboten in UI-Komponenten:**
```kotlin
// ❌ FALSCH - Hardcoded Farben
Color(0xFF90EE90)
Color.Green

// ✅ RICHTIG - Theme-Farben
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.primary
```

**Brutalistisches Farbschema:**
- `primary` = Weiß (LightSurface) - keine blauen Akzente
- `onSurface` = Weiß - Haupttext
- `onSurfaceVariant` = Grau - Sekundärtext
- `surface` = Dunkelgrau (#1E1E1E)
- `background` = Fast-Schwarz (#121212)
- `dynamicColor = false` - Android 12+ Systemfarben deaktiviert

---

### 0.1 Gradle Dependencies hinzufügen

**app/build.gradle:**
```gradle
android {
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM (Bill of Materials) - vereinfacht Versionierung
    def composeBom = platform('androidx.compose:compose-bom:2024.02.00')
    implementation composeBom
    androidTestImplementation composeBom
    
    // Core Compose
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    // Activity Compose Integration
    implementation 'androidx.activity:activity-compose:1.8.2'
    
    // ViewModel Compose
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.7.0'
    
    // Navigation Compose (stabil seit 2021)
    implementation 'androidx.navigation:navigation-compose:2.7.7'
    
    // Debug Tools
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
    
    // Testing
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
}
```

### 0.2 Theme erstellen

**Datei:** `ui/theme/Theme.kt`
```kotlin
@Composable
fun RepsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF90CAF9),      // Blau
            secondary = Color(0xFF80CBC4),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF26A69A),
            background = Color(0xFFF5F5F5),
            surface = Color.White
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepsTypography,
        content = content
    )
}
```

### 0.3 Navigation Shell vorbereiten

```kotlin
// RepsNavHost.kt - Leere Shell, wird pro Phase gefüllt
@Composable
fun RepsNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "stats" // Erster Compose-Screen
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Phase 1: Stats
        composable("stats") {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        // Phase 2-6: Weitere Screens hier ergänzen
    }
}
```

---

### 0.4 Checkliste Phase 0

- [x] Compose Dependencies in build.gradle
- [x] Sync & Build erfolgreich
- [x] Theme.kt erstellt
- [x] Color.kt erstellt  
- [x] Typography.kt erstellt
- [x] Erster @Preview funktioniert
- [x] Navigation Compose Dependency
- [x] RepsNavHost.kt Shell erstellt
- [ ] ComposeView in XML getestet (Interop)
- [x] **Tests:** Build-Zeit gemessen (Baseline: ~9s inkrementell, ~2min clean)

---

## ~~📊 Phase 1: Erster Compose Screen (Stats/Statistik)~~

> ❌ **ENTFERNT (16.01.2026):** Das Stats-Feature wurde aus der App entfernt.
> Alle zugehörigen Dateien wurden gelöscht:
> - `ui/stats/StatsActivity.kt`
> - `ui/stats/StatsScreen.kt`
> - `ui/stats/StatsViewModel.kt`
> - `ui/stats/StatsUiState.kt`
> - `ui/components/StatCard.kt`
> - Tests: `StatsViewModelTest.kt`, `StatsUiStateTest.kt`

---

## ⚙️ Phase 2: Settings Screen

### 2.1 Komponenten

```
ui/
  settings/
    SettingsScreen.kt
    components/
      SettingsItem.kt
      LanguageSelector.kt
      ThemeSelector.kt
```

### 2.2 SettingsScreen Beispiel

```kotlin
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { BackButton(onNavigateBack) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsSection(title = "Allgemein") {
                    LanguageSettingsItem(
                        currentLanguage = settings.language,
                        onLanguageChange = viewModel::setLanguage
                    )
                    ThemeSettingsItem(
                        currentTheme = settings.theme,
                        onThemeChange = viewModel::setTheme
                    )
                }
            }
            
            item {
                SettingsSection(title = "Timer") {
                    SliderSettingsItem(
                        title = "Standard-Pausenzeit",
                        value = settings.defaultRestTime,
                        range = 30f..300f,
                        onValueChange = viewModel::setDefaultRestTime
                    )
                }
            }
        }
    }
}
```

### 2.3 Checkliste Phase 2

> ⚠️ **Achtung:** SettingsActivity ist mit 487 Zeilen komplexer als erwartet!
> Enthält: Import/Export, Theme-Wechsel, Sprache, SharedPreferences, 6+ AlertDialogs
> **Empfehlung:** Erst SettingsViewModel extrahieren, dann UI migrieren.

**Phase 2a: ViewModel-Extraktion (Vorarbeit)**
- [ ] SettingsUiState.kt (Sealed Class) erstellt
- [ ] SettingsViewModel.kt mit StateFlow erstellt
- [ ] SharedPreferences-Logik in ViewModel verschoben
- [ ] Import/Export-Logik in ViewModel verschoben

**Phase 2b: Compose UI**
- [ ] SettingsScreen.kt erstellt
- [ ] SettingsItem Komponenten erstellt
- [ ] Switch-Settings (Vibration, Sound, KeepScreenOn)
- [ ] Dialog-Settings (Pausenzeit, Vibrationsdauer, Theme, Sprache)
- [ ] Export-Funktionalität (CSV/JSON)
- [ ] Import-Funktionalität (JSON)
- [ ] Daten löschen mit Bestätigungsdialog

**Phase 2c: Integration & Cleanup**
- [ ] Sprache-Wechsel funktioniert
- [ ] Theme-Wechsel funktioniert
- [ ] In RepsNavHost registriert
- [ ] Alte SettingsActivity gelöscht
- [ ] **Tests:** SettingsViewModelTest.kt
- [ ] **Tests:** SettingsScreenTest.kt
- [ ] **Review:** Code Review durchgeführt

---

## 📜 Phase 3: History Screen

### 3.1 Warum History vor WorkoutInput?
- Mittlere Komplexität (Liste + Interaktion)
- Gute Übung für LazyColumn + State
- Kein komplexes Formular-Handling

### 3.2 Komponenten

```
ui/
  history/
    HistoryScreen.kt
    HistoryViewModel.kt
    components/
      WorkoutHistoryItem.kt
      DateHeader.kt
      FilterChips.kt
      SwipeToDelete.kt
```

### 3.3 HistoryScreen mit Gruppierung

```kotlin
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verlauf") },
                navigationIcon = { BackButton(onNavigateBack) },
                actions = {
                    FilterChipsRow(viewModel::setFilter)
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            uiState.workoutsByDate.forEach { (date, workouts) ->
                stickyHeader {
                    DateHeader(date = date)
                }
                items(
                    items = workouts,
                    key = { it.id }
                ) { workout ->
                    SwipeToDeleteContainer(
                        onDelete = { viewModel.deleteWorkout(workout.id) }
                    ) {
                        WorkoutHistoryItem(workout = workout)
                    }
                }
            }
        }
    }
}
```

### 3.4 Checkliste Phase 3

- [ ] HistoryViewModel.kt erstellt
- [ ] HistoryScreen.kt erstellt
- [ ] LazyColumn mit stickyHeader
- [ ] Gruppierung nach Datum
- [ ] Filter-Funktionalität
- [ ] Swipe-to-Delete
- [ ] In RepsNavHost registriert
- [ ] Alte HistoryActivity gelöscht
- [ ] **Tests:** HistoryViewModelTest.kt
- [ ] **Tests:** HistoryScreenTest.kt
- [ ] **Review:** Code Review durchgeführt

---

## 🏠 Phase 4: Main/Dashboard Screen

### 4.1 Komponenten

```
ui/
  main/
    MainScreen.kt
    MainViewModel.kt
    components/
      QuickStartCard.kt
      RecentWorkoutsList.kt
      StreakBadge.kt
```

### 4.2 MainScreen als zentraler Hub

```kotlin
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToWorkout: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REPS") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Einstellungen")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToWorkout,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Neues Workout") }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                QuickStartCard(
                    lastExercise = uiState.lastExercise,
                    onQuickStart = { viewModel.quickStart(it) }
                )
            }
            item {
                RecentWorkoutsSection(
                    workouts = uiState.recentWorkouts,
                    onViewAll = onNavigateToHistory
                )
            }
        }
    }
}
```

### 4.3 Checkliste Phase 4

- [x] MainScreen.kt erstellt
- [ ] MainViewModel.kt erstellt (optional - aktuell stateless)
- [ ] QuickStart Funktionalität
- [ ] Letzte Workouts anzeigen
- [x] Navigation zu allen Screens
- [ ] Als startDestination in NavHost
- [x] MainActivity zu ComponentActivity konvertiert
- [ ] activity_main.xml löschen
- [ ] **Tests:** MainScreenTest.kt
- [ ] **Review:** Code Review durchgeführt

> ✅ **16.01.2026:** MainScreen.kt erstellt, MainActivity nutzt jetzt Compose!

---

## 🏋️ Phase 5: WorkoutInput Screen (Komplex)

### 5.1 Warum so spät?
Dies ist der **komplexeste Screen** mit:
- Exercise Type Selector (REPS/TIME)
- Dynamische Input-Felder
- Suggestion Chips
- Formular-Validierung
- Keyboard-Handling

> ⚠️ Erst nach Compose-Erfahrung aus Phase 1-4 angehen!

### 5.2 Komponenten

```
ui/
  workout/
    input/
      WorkoutInputScreen.kt
      WorkoutInputViewModel.kt
      components/
        ExerciseTypeSelector.kt
        RepsWeightInput.kt
        TimeInput.kt
        SuggestionChips.kt
        ExerciseDropdown.kt
```

### 5.3 ExerciseTypeSelector (Material 3)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTypeSelector(
    selectedType: ExerciseType,
    onTypeSelected: (ExerciseType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedType == ExerciseType.Reps,
            onClick = { onTypeSelected(ExerciseType.Reps) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { Icon(Icons.Default.Repeat, null) }
        ) {
            Text("Wiederholungen")
        }
        SegmentedButton(
            selected = selectedType == ExerciseType.Time,
            onClick = { onTypeSelected(ExerciseType.Time) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { Icon(Icons.Default.Timer, null) }
        ) {
            Text("Zeit")
        }
    }
}
```

### 5.4 Checkliste Phase 5

- [ ] WorkoutInputViewModel.kt erstellt
- [ ] ExerciseType Sealed Class
- [ ] ExerciseTypeSelector.kt
- [ ] Dynamische Inputs (Reps vs Time)
- [ ] Suggestion Chips
- [ ] Formular-Validierung
- [ ] Keyboard-Handling (ImeAction)
- [ ] In RepsNavHost registriert
- [ ] Alte WorkoutInputActivity gelöscht
- [ ] **Tests:** WorkoutInputViewModelTest.kt
- [ ] **Tests:** WorkoutInputScreenTest.kt
- [ ] **Review:** Code Review durchgeführt

---

## ⏱️ Phase 6: Timer Screen (Service-bound)

### 6.1 Warum zuletzt?
Der Timer ist der **technisch anspruchsvollste Screen**:
- Foreground Service Anbindung
- Zwei Modi: Countdown (REPS) / Stopwatch (TIME)
- Screen Wake Lock
- Notification Updates
- Process Death Handling

> ⚠️ Compose-Erfahrung aus allen vorherigen Phasen nutzen!

### 6.2 Komponenten

```
ui/
  timer/
    TimerScreen.kt
    TimerViewModel.kt          // bereits vorhanden ✅
    components/
      TimerDisplay.kt
      SetProgressIndicator.kt
      TimerControls.kt
```

### 4.3 TimerScreen Beispiel

```kotlin
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel(),
    onWorkoutComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Screen Wake Lock
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    // Back-Handler mit Bestätigung
    var showCancelDialog by remember { mutableStateOf(false) }
    BackHandler { showCancelDialog = true }
    
    Scaffold { padding ->
        when (val state = uiState) {
            is TimerUiState.Loading -> LoadingIndicator()
            is TimerUiState.Running -> TimerContent(
                state = state,
                onSetComplete = viewModel::completeSet,
                modifier = Modifier.padding(padding)
            )
            is TimerUiState.WorkoutCompleted -> {
                LaunchedEffect(Unit) { onWorkoutComplete() }
            }
            is TimerUiState.Error -> ErrorScreen(state.message)
        }
    }
    
    if (showCancelDialog) {
        CancelWorkoutDialog(
            onConfirm = onCancel,
            onDismiss = { showCancelDialog = false }
        )
    }
}
```

### 6.4 Checkliste Phase 6

- [ ] TimerScreen.kt erstellt
- [ ] TimerDisplay Komponente (Countdown + Stopwatch)
- [ ] SetProgressIndicator Komponente
- [ ] Service-Integration (bindService in LaunchedEffect)
- [ ] Screen Wake Lock (DisposableEffect)
- [ ] Back-Handler mit Bestätigungs-Dialog
- [ ] In RepsNavHost registriert
- [ ] Alte TimerActivity gelöscht
- [ ] **Tests:** TimerViewModelTest.kt (bereits vorhanden ✅)
- [ ] **Tests:** TimerScreenTest.kt
- [ ] **Review:** Service-Lifecycle geprüft

---

## 💉 Phase 7: Hilt Integration

### 7.1 NavHost Setup (String Routes - Stabil)

```kotlin
@Composable
fun RepsNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToWorkout = { navController.navigate("workout") },
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("workout") {
            WorkoutInputScreen(
                onNavigateToTimer = { exerciseId, sets, isTimeBased ->
                    navController.navigate("timer/$exerciseId/$sets/$isTimeBased")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "timer/{exerciseId}/{sets}/{isTimeBased}",
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.LongType },
                navArgument("sets") { type = NavType.IntType },
                navArgument("isTimeBased") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            TimerScreen(
                exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0,
                sets = backStackEntry.arguments?.getInt("sets") ?: 3,
                isTimeBased = backStackEntry.arguments?.getBoolean("isTimeBased") ?: false,
                onWorkoutComplete = { 
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("stats") {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
```

### 7.2 Checkliste Phase 7

- [ ] Navigation Routes als Konstanten definiert
- [ ] NavHost implementiert
- [ ] Argument-Passing funktioniert
- [ ] Deep Links (optional)
- [ ] Alle startActivity() entfernt

---

### 7.1 Warum nach allen Screens?
Hilt-Integration ist **invasiv** und betrifft alle ViewModels. Besser nach der UI-Migration.

### 7.2 Dependencies

```gradle
plugins {
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
}

dependencies {
    implementation 'com.google.dagger:hilt-android:2.50'
    ksp 'com.google.dagger:hilt-compiler:2.50'
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
}
```

### 8.2 Application Setup

```kotlin
@HiltAndroidApp
class RepsApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "workout_database"
        ).build()
    }
    
    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }
}
```

### 8.3 ViewModel mit Hilt

```kotlin
@HiltViewModel
class WorkoutInputViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // ...
}

// In Composable
@Composable
fun WorkoutInputScreen(
    viewModel: WorkoutInputViewModel = hiltViewModel()  // statt viewModel()
) {
    // ...
}
```

### 7.4 Checkliste Phase 7

- [x] Hilt Dependencies hinzugefügt
- [x] @HiltAndroidApp in Application
- [x] DatabaseModule erstellt
- [x] RepositoryModule erstellt
- [ ] UseCaseModule erstellt (optional)
- [x] Alle ViewModels auf @HiltViewModel
- [x] hiltViewModel() in allen Composables
- [ ] **Tests:** Hilt Testing Setup
- [ ] **Tests:** Alle Tests grün nach Umstellung

---

## 🧹 Phase 8: Cleanup & Polish

### 8.1 Aufräumen

- [ ] Alle XML Layout-Dateien gelöscht
- [ ] Alle alten Activities gelöscht
- [ ] View Binding Dependencies entfernt
- [ ] Ungenutzte Imports entfernt
- [ ] Code-Formatierung (ktlint)

### 8.2 Testing

- [ ] Compose UI Tests für kritische Flows
- [ ] Screenshot Tests (Paparazzi)
- [ ] Accessibility Tests (TalkBack)
- [ ] Performance Profiling (Baseline Profiles)
- [ ] Regressionstests (manuell)

### 8.3 Dokumentation

- [ ] README.md aktualisiert
- [ ] Architecture.md erstellt
- [ ] Code-Kommentare geprüft

---

## 🏗️ Phase 9: Architektur-Verbesserungen (v2.0)

> **Hinzugefügt:** 16.01.2026 nach Code Review
> **Priorität:** Nach funktionaler Migration
> **Ziel:** Clean Architecture + Robustheit

### 9.1 Warum Phase 9?
Die aktuelle Implementierung ist funktional, aber hat architektonische Schulden:
- Tight Coupling zwischen ViewModel und Service
- Fehlende Error Handling
- Keine Persistenz bei Process Death

### 9.2 Repository-Pattern für Timer

**Aktuell (v1.0):**
```kotlin
// ViewModel kennt Service direkt - Tight Coupling
class TimerViewModel {
    private var timerService: TimerService? = null
    private val serviceConnection = object : ServiceConnection { ... }
}
```

**Ziel (v2.0):**
```kotlin
// Repository abstrahiert Service-Logik
interface TimerRepository {
    fun startTimer(config: TimerConfig)
    fun stopTimer()
    fun observeTimerState(): Flow<TimerState>
}

class TimerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TimerRepository {
    // Service-Binding hier
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {
    // Kein Service-Code mehr
}
```

### 9.3 WakeLock für Xiaomi/Redmi

**Problem:** Trotz Foreground Service kann MIUI die CPU in Doze schicken.

**Lösung:**
```kotlin
// In TimerService.kt
private var wakeLock: PowerManager.WakeLock? = null

override fun onCreate() {
    super.onCreate()
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = pm.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "REPS::TimerWakeLock"
    )
}

private fun startTimer() {
    wakeLock?.acquire(pauseTimeSeconds * 1000L + 5000L) // Timer + Buffer
    // ... Timer-Logik
}

override fun onDestroy() {
    wakeLock?.release()
    super.onDestroy()
}
```

**Permission benötigt:**
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 9.4 Back-Handler mit Bestätigung

**Aktuell:** Kein Abbruch-Dialog - Service läuft weiter wenn User zurück drückt.

**Ziel:**
```kotlin
@Composable
fun TimerScreen(...) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    BackHandler {
        showCancelDialog = true
    }
    
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.cancel_workout_title)) },
            text = { Text(stringResource(R.string.cancel_workout_message)) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.stopAndCleanup()
                    onCancel()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}
```

### 9.5 Error Handling bei Service-Start

**Aktuell:** Kein Try-Catch - App kann crashen auf manchen ROMs.

**Ziel:**
```kotlin
fun startTimerService(): Result<Unit> {
    return try {
        TimerService.startTimer(context, ...)
        Result.success(Unit)
    } catch (e: SecurityException) {
        Log.e(TAG, "Foreground Service blocked", e)
        Result.failure(e)
    }
}

// Fallback im ViewModel
if (timerRepository.startTimerService().isFailure) {
    // Fallback zu internem CountDownTimer (ohne Hintergrund-Support)
    startInternalTimer()
    _state.update { it.copy(backgroundModeAvailable = false) }
}
```

### 9.6 Timer-State Persistenz

**Problem:** Bei Process Death (selten, aber möglich) geht Timer-State verloren.

**Lösung:**
```kotlin
// TimerStateStore.kt
class TimerStateStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("timer_state", Context.MODE_PRIVATE)
    
    fun saveState(state: TimerState) {
        prefs.edit()
            .putLong("time_left", state.timeLeftInMillis)
            .putInt("current_set", state.currentSet)
            .putLong("started_at", System.currentTimeMillis())
            .apply()
    }
    
    fun restoreState(): TimerState? {
        val startedAt = prefs.getLong("started_at", 0)
        if (startedAt == 0L) return null
        
        val elapsed = System.currentTimeMillis() - startedAt
        val savedTimeLeft = prefs.getLong("time_left", 0)
        val adjustedTimeLeft = (savedTimeLeft - elapsed).coerceAtLeast(0)
        
        return TimerState(
            timeLeftInMillis = adjustedTimeLeft,
            currentSet = prefs.getInt("current_set", 1)
        )
    }
    
    fun clear() = prefs.edit().clear().apply()
}
```

### 9.7 Checkliste Phase 9

- [x] TimerRepository Interface erstellen
- [x] TimerRepositoryImpl mit Service-Binding
- [x] Hilt Module für Repository
- [x] WakeLock in TimerService
- [x] WAKE_LOCK Permission
- [x] Back-Handler mit Dialog
- [x] String Resources für Dialog
- [x] Error Handling bei Service-Start
- [ ] Fallback zu internem Timer (optional)
- [ ] TimerStateStore für Persistenz (optional)
- [ ] Process Death Recovery (optional)
- [ ] **Tests:** TimerRepositoryTest.kt
- [ ] **Tests:** TimerStateStoreTest.kt
- [ ] **Review:** Architektur-Review durchgeführt

### 9.8 Abhängigkeiten

Phase 9 setzt voraus:
- ✅ Phase 6 (Timer Compose Migration)
- ✅ Phase 7 (Hilt Integration) - für Repository Injection

---

## 📚 Lernressourcen

### Offizielle Dokumentation
- [Jetpack Compose Basics](https://developer.android.com/jetpack/compose/tutorial)
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Material 3 Components](https://developer.android.com/jetpack/compose/designsystems/material3)

### Codelabs
- [Compose Basics Codelab](https://developer.android.com/codelabs/jetpack-compose-basics)
- [State in Compose Codelab](https://developer.android.com/codelabs/jetpack-compose-state)
- [Navigation Codelab](https://developer.android.com/codelabs/jetpack-compose-navigation)

### Videos
- [Now in Android (Sample App)](https://github.com/android/nowinandroid)
- [Compose Camp on YouTube](https://www.youtube.com/playlist?list=PLWz5rJ2EKKc94tpHND8pW8Qt8ZfT1a4cq)

---

## 🎯 Erfolgskriterien

| Kriterium | Ziel | Messmethode |
|-----------|------|-------------|
| **Build-Zeit** | Max. 20% langsamer | Gradle Build Scan |
| **APK-Größe** | Max. 2MB größer | APK Analyzer |
| **Performance** | 60 FPS auf allen Screens | Android Profiler |
| **Code-Reduktion** | Min. 30% weniger UI-Code | Zeilen-Vergleich |
| **Test-Abdeckung** | 80%+ für ViewModels | JaCoCo |
| **Crash-Rate** | 0 neue Crashes | Firebase Crashlytics |
| **Zeitplan** | ≤ 8 Wochen | Projektboard |

---

## 📝 Entscheidungslog

| Datum | Entscheidung | Begründung |
|-------|--------------|------------|
| 14.01.2026 | XML für Time-Based Feature | Schnelle Lieferung, Compose später |
| 14.01.2026 | Stats Screen als Pilot | Niedrigstes Risiko, gutes Lernprojekt |
| 14.01.2026 | Navigation früh (Phase 0) | Vermeidet doppelte Arbeit mit Bridge-Activities |
| 14.01.2026 | WorkoutInput spät (Phase 5) | Komplexester Screen, braucht Erfahrung |
| 14.01.2026 | Timer zuletzt (Phase 6) | Service-bound, höchste technische Komplexität |
| 14.01.2026 | Hilt nach Screens (Phase 7) | DI-Migration ist invasiv, separate Phase |
| 14.01.2026 | Zeitschätzung 6-8 Wochen | Realistisch mit Lernkurve |
| 14.01.2026 | String Routes statt Type-Safe | Stabilität > Modernität, Type-Safe erst 14 Monate alt |
| 16.01.2026 | Standard Icons statt Extended | APK-Größe: +5MB vermeiden, Details in `migration_nach_compose_issues.md` |
| 16.01.2026 | Stats-Feature entfernt | Feature nicht benötigt, Phase 1 obsolet, 760 Zeilen Code entfernt |
| 16.01.2026 | Foreground Service statt AlarmManager | 99% Zuverlässigkeit auf allen ROMs inkl. Xiaomi/Redmi |
| 16.01.2026 | Phase 9 (v2.0) hinzugefügt | Code Review: Repository-Pattern, WakeLock, Error Handling, Persistenz |
| 17.01.2026 | Phase 3 (History) komplett | WorkoutHistoryActivity → ComponentActivity, EditWorkoutBottomSheet Fragment → Compose ModalBottomSheet |
| 17.01.2026 | Brutalistisches Monochrom-Design | Alle Farben zentral in Color.kt, primary=Weiß, keine blauen Akzente, dynamicColor=false |
| 17.01.2026 | Phase 7+9 komplett | Hilt DI integriert, TimerRepository implementiert, WakeLock + Back-Handler für Zuverlässigkeit |
| 18.01.2026 | Multi-Activity statt Navigation Compose | RepsNavHost.kt + Routes.kt gelöscht - Multi-Activity ist simpler, funktioniert mit Foreground Service |
| 18.01.2026 | Alle XML-Layouts gelöscht | 5 Layouts (activity_timer, activity_tracking, dialog_edit_workout, item_date_header, empty_state_history) entfernt |
| 18.01.2026 | Stabile Übungs-Lokalisierung | nameResKey statt nameResId - R.string IDs sind nicht stabil zwischen Builds |
| 18.01.2026 | Phase 8 (Cleanup) abgeschlossen | Unused Code gelöscht, Migration 100% komplett |

---

## ⚠️ Risiken & Mitigationen

| Risiko | Wahrscheinlichkeit | Mitigation |
|--------|-------------------|------------|
| ~~Längere Build-Zeiten~~ | ~~Hoch~~ | ~~Compose Compiler Cache nutzen~~ → Kein Problem: ~50s Build |
| ~~Lernkurve~~ | ~~Mittel~~ | Abgeschlossen ✅ |
| ~~Breaking Changes~~ | ~~Niedrig~~ | Compose BOM für konsistente Versionen ✅ |
| ~~Regressions~~ | ~~Mittel~~ | Alle alten Screens gelöscht ✅ |

---

## 🏁 Abgeschlossen

1. ✅ Time-Based Feature mit XML fertigstellen
2. ✅ **Phase 0:** Compose Setup + Theme
3. ❌ ~~**Phase 1:** Stats Screen~~ - Entfernt (16.01.2026)
4. ✅ **Phase 4:** Main Screen
5. ✅ **Phase 3:** History Screen
6. ✅ **Phase 2:** Settings Screen
7. ✅ **Phase 5:** WorkoutInput Screen
8. ✅ **Phase 6:** Timer Screen + Foreground Service
9. ✅ **Phase 7:** Hilt Integration
10. ✅ **Phase 8:** Cleanup & Polish
11. ✅ **Phase 9:** v2.0 Architektur-Verbesserungen

> 🎉 **18.01.2026:** Migration 100% abgeschlossen!

---

## 📊 Fortschritts-Tracker

| Phase | Fortschritt | Notizen |
|-------|-------------|--------|
| 0 | ⬛⬛⬛⬛⬛ 100% | Theme erstellt, Compose Dependencies ✅ |
| 1 | ❌ Entfernt | Stats-Feature nicht mehr benötigt |
| 2 | ⬛⬛⬛⬛⬛ 100% | SettingsScreen + ViewModel + Activity + monochrome Switches ✅ |
| 3 | ⬛⬛⬛⬛⬛ 100% | HistoryScreen + EditWorkoutSheet + monochrome Volumen-Anzeige ✅ |
| 4 | ⬛⬛⬛⬛⬛ 100% | MainScreen.kt erstellt, Navigation funktioniert ✅ |
| 5 | ⬛⬛⬛⬛⬛ 100% | WorkoutInputScreen + ViewModel + imePadding für Keyboard ✅ |
| 6 | ⬛⬛⬛⬛⬛ 100% | TimerScreen + TimerService + Back-Handler + WakeLock ✅ |
| 7 | ⬛⬛⬛⬛⬛ 100% | Hilt DI: RepsApplication, DatabaseModule, RepositoryModule ✅ |
| 8 | ⬛⬛⬛⬛⬛ 100% | RepsNavHost, Routes.kt, 5 XML-Layouts gelöscht ✅ |
| 9 | ⬛⬛⬛⬛⬛ 100% | TimerRepository, WakeLock, Back-Handler, Error Handling ✅ |

---

## 🏆 Architektur-Entscheidung: Multi-Activity vs. Navigation Compose

**Entscheidung (18.01.2026):** Multi-Activity-Architektur beibehalten

| Aspekt | Multi-Activity | Navigation Compose |
|--------|----------------|-------------------|
| **Foreground Service** | ✅ Natürliche Integration | ⚠️ Komplexere Lifecycle-Behandlung |
| **Komplexität** | ✅ Einfacher | ⚠️ Mehr Boilerplate |
| **Status** | ✅ Funktioniert bereits | ❌ Müsste neu implementiert werden |
| **State Recovery** | ✅ Android-Standard | ⚠️ SavedStateHandle nötig |

**Gelöschte Dateien:**
- `ui/navigation/RepsNavHost.kt` - War nur Platzhalter, nie verwendet
- `ui/navigation/Routes.kt` - Ungenutzte Route-Konstanten

---

*Letzte Aktualisierung: 18.01.2026*  
*Migration abgeschlossen! 🎉*
