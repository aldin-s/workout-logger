# 📱 Prompt to Create Android Workout Tracker App (Modern Architecture - V2)

Create a complete Android workout tracker app with modern architecture patterns, clean code principles, and industry best practices.

## App Requirements:

### Main Screen (Dashboard):
- Display large timer showing "00:00"
- Display set counter showing "0/0"
- Stats cards showing:
  - Today's workout count
  - Current streak
  - Weekly summary
- Floating Action Button (FAB) for "Start Workout"
- Recent workouts preview (last 3)
- Quick action chips for frequent exercises

### Workflow:
1. When user clicks FAB "Start Workout", navigate to input screen
2. Input screen (in German) titled "Übung eingeben" with Material Design fields:
   - Exercise name with autocomplete from history (TextInputLayout)
   - Weight in kg with +/- buttons (Gewicht)
   - Number of repetitions with number picker (Wiederholungen)
   - Pause time with preset chips: 30s, 60s, 90s, 120s (Pausenzeit)
   - Total number of sets with number picker (Anzahl Sätze)
   - "Start" FAB at bottom
3. Navigate to Timer screen showing:
   - Large circular progress indicator with countdown timer
   - Center displays time remaining (MM:SS format)
   - Set counter below timer: "1/[total sets]"
   - Exercise name and weight displayed at top
   - Large "DONE" FAB (only enabled when timer reaches 00:00)
   - Color changes: green → yellow → red as time decreases
4. When user completes exercise and presses "DONE":
   - Log completed set to database via Repository
   - Increment set counter (e.g., "2/3")
   - Start countdown timer again automatically
   - Haptic feedback on completion
   - Disable "DONE" button until timer reaches 00:00
5. After all sets completed:
   - Show completion screen with summary
   - Option to log additional notes
   - Navigate back to dashboard
6. History screen shows:
   - Grouped by date (Today, Yesterday, This Week, etc.)
   - Cards with exercise details and charts
   - Progress graphs per exercise
   - Personal records (PR) highlights

### Database Schema:
**Entity: CompletedSetEntity**
```kotlin
@Entity(tableName = "completed_sets")
data class CompletedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val weightKg: Double,
    val plannedReps: Int,
    val completedReps: Int,
    val setNumber: Int,
    val timestamp: Long,
    val notes: String? = null
)
```

### Modern Architecture (MVVM + Clean Architecture):

#### **Project Structure:**
```
app/
├── data/                          # Data Layer
│   ├── local/
│   │   ├── entity/
│   │   │   └── CompletedSetEntity.kt
│   │   ├── dao/
│   │   │   └── CompletedSetDao.kt
│   │   └── database/
│   │       ├── WorkoutDatabase.kt
│   │       └── Converters.kt
│   ├── repository/
│   │   └── WorkoutRepositoryImpl.kt
│   └── mapper/
│       └── WorkoutMapper.kt       # Converts Entity ↔ Domain Model
│
├── domain/                        # Business Logic Layer
│   ├── model/
│   │   ├── WorkoutConfig.kt      # Domain models (not DB entities)
│   │   ├── CompletedSet.kt
│   │   └── WorkoutSession.kt
│   ├── repository/
│   │   └── WorkoutRepository.kt  # Interface (abstraction)
│   └── usecase/
│       ├── LogCompletedSetUseCase.kt
│       ├── GetWorkoutHistoryUseCase.kt
│       ├── GetWorkoutStatsUseCase.kt
│       └── StartTimerUseCase.kt
│
├── presentation/                  # UI Layer
│   ├── MainActivity.kt           # Single Activity
│   ├── timer/
│   │   ├── TimerFragment.kt
│   │   ├── TimerViewModel.kt
│   │   └── TimerState.kt
│   ├── input/
│   │   ├── WorkoutInputFragment.kt
│   │   ├── WorkoutInputViewModel.kt
│   │   └── WorkoutInputState.kt
│   ├── dashboard/
│   │   ├── DashboardFragment.kt
│   │   ├── DashboardViewModel.kt
│   │   └── DashboardState.kt
│   └── history/
│       ├── HistoryFragment.kt
│       ├── HistoryViewModel.kt
│       ├── HistoryAdapter.kt
│       └── HistoryState.kt
│
├── di/                           # Dependency Injection
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
└── util/
    ├── Constants.kt
    ├── Extensions.kt
    └── DateFormatter.kt
```

### Technical Requirements:

**Core Technologies:**
- Language: Kotlin 1.9.20
- Min SDK: 21
- Target SDK: 34
- Compile SDK: 34
- Gradle: 8.5
- Android Gradle Plugin: 8.1.4
- JVM Target: 17

**Architecture Components:**
- Architecture: MVVM + Clean Architecture
- Single Activity with Navigation Component
- ViewModels for each screen
- Repository pattern for data access
- Use Cases for business logic
- StateFlow for reactive UI updates
- Hilt for Dependency Injection

**Database:**
- Room 2.6.1 with KSP (not KAPT)
- Suspend functions for all DAO operations
- Flow for observable queries
- TypeConverters for Date objects

**UI Framework:**
- Material Design 3 components
- ViewBinding for type-safe view access
- Navigation Component for fragment navigation
- RecyclerView with ListAdapter and DiffUtil

**Async Operations:**
- Kotlin Coroutines 1.6.4
- Dispatchers.IO for database operations
- Dispatchers.Main for UI updates
- ViewModelScope for lifecycle-aware coroutines

### Dependencies:

```gradle
dependencies {
    // AndroidX Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Navigation Component
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
    
    // Lifecycle & ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    ksp 'androidx.room:room-compiler:2.6.1'
    
    // Hilt Dependency Injection
    implementation 'com.google.dagger:hilt-android:2.48'
    ksp 'com.google.dagger:hilt-compiler:2.48'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    
    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // ViewBinding
    // Enabled in build.gradle
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
    testImplementation 'androidx.arch.core:core-testing:2.2.0'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

### Build Configuration:

**Root build.gradle (Modern plugins DSL):**
```gradle
plugins {
    id 'com.android.application' version '8.1.4' apply false
    id 'com.android.library' version '8.1.4' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
    id 'com.google.devtools.ksp' version '1.9.20-1.0.14' apply false
    id 'com.google.dagger.hilt.android' version '2.48' apply false
}

tasks.register('clean', Delete) {
    delete rootProject.buildDir
}
```

**App build.gradle:**
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.devtools.ksp'
    id 'com.google.dagger.hilt.android'
}

android {
    namespace 'com.example.workouttracker'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.workouttracker"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        
        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
    
    buildFeatures {
        viewBinding true
    }
}
```

**gradle.properties:**
```properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64
android.nonTransitiveRClass=true
```

### Key Code Patterns:

**1. ViewModel Example:**
```kotlin
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val logCompletedSetUseCase: LogCompletedSetUseCase,
    private val startTimerUseCase: StartTimerUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimerState>(TimerState.Idle)
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()
    
    fun startTimer(config: WorkoutConfig) {
        viewModelScope.launch {
            startTimerUseCase(config.pauseTimeSeconds).collect { timeRemaining ->
                _uiState.value = TimerState.Running(
                    timeRemaining = timeRemaining,
                    currentSet = _currentSet,
                    totalSets = config.totalSets
                )
            }
        }
    }
    
    fun completeSet(config: WorkoutConfig) {
        viewModelScope.launch {
            logCompletedSetUseCase(config, _currentSet)
            _currentSet++
            if (_currentSet <= config.totalSets) {
                startTimer(config)
            } else {
                _uiState.value = TimerState.Completed
            }
        }
    }
}

sealed class TimerState {
    object Idle : TimerState()
    data class Running(
        val timeRemaining: Int,
        val currentSet: Int,
        val totalSets: Int
    ) : TimerState()
    object Completed : TimerState()
}
```

**2. Repository Pattern:**
```kotlin
interface WorkoutRepository {
    suspend fun logCompletedSet(completedSet: CompletedSet)
    fun getAllWorkouts(): Flow<List<CompletedSet>>
    suspend fun getWorkoutsByExercise(exerciseName: String): List<CompletedSet>
    suspend fun getWorkoutStats(): WorkoutStats
}

class WorkoutRepositoryImpl @Inject constructor(
    private val dao: CompletedSetDao,
    private val mapper: WorkoutMapper
) : WorkoutRepository {
    
    override suspend fun logCompletedSet(completedSet: CompletedSet) {
        dao.insert(mapper.toEntity(completedSet))
    }
    
    override fun getAllWorkouts(): Flow<List<CompletedSet>> {
        return dao.getAllSetsFlow().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    override suspend fun getWorkoutsByExercise(exerciseName: String): List<CompletedSet> {
        return dao.getSetsByExercise(exerciseName).map { mapper.toDomain(it) }
    }
}
```

**3. Use Case Example:**
```kotlin
class LogCompletedSetUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        config: WorkoutConfig,
        setNumber: Int
    ) {
        val completedSet = CompletedSet(
            exerciseName = config.exerciseName,
            weightKg = config.weight,
            plannedReps = config.reps,
            completedReps = config.reps,
            setNumber = setNumber,
            timestamp = System.currentTimeMillis()
        )
        repository.logCompletedSet(completedSet)
    }
}
```

**4. Dependency Injection:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideDao(database: WorkoutDatabase): CompletedSetDao {
        return database.completedSetDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        impl: WorkoutRepositoryImpl
    ): WorkoutRepository
}
```

**5. Data Classes (Type Safety):**
```kotlin
@Parcelize
data class WorkoutConfig(
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val pauseTimeSeconds: Int,
    val totalSets: Int
) : Parcelable {
    
    companion object {
        fun validate(config: WorkoutConfig): Result<WorkoutConfig> {
            return when {
                config.exerciseName.isBlank() -> 
                    Result.failure(Exception("Exercise name required"))
                config.weight <= 0 -> 
                    Result.failure(Exception("Weight must be positive"))
                config.reps <= 0 -> 
                    Result.failure(Exception("Reps must be positive"))
                config.pauseTimeSeconds < 0 -> 
                    Result.failure(Exception("Pause time must be positive"))
                config.totalSets <= 0 -> 
                    Result.failure(Exception("Sets must be positive"))
                else -> Result.success(config)
            }
        }
    }
}
```

### Code Quality Requirements:

**Documentation:**
- KDoc comments for all public functions and classes
- Meaningful variable and function names
- No magic numbers (use Constants object)

**Testing:**
- Unit tests for ViewModels
- Unit tests for Use Cases
- Repository tests with fake DAO
- Minimum 70% code coverage

**Constants:**
```kotlin
object WorkoutConstants {
    const val DEFAULT_PAUSE_TIME_SECONDS = 60
    const val MIN_WEIGHT_KG = 0.0
    const val MAX_WEIGHT_KG = 500.0
    const val MIN_REPS = 1
    const val MAX_REPS = 100
    const val MIN_SETS = 1
    const val MAX_SETS = 10
    
    const val DATABASE_NAME = "workout_database"
    const val DATABASE_VERSION = 1
}
```

### Material Design 3 Theme:

**colors.xml:**
```xml
<resources>
    <!-- Fitness Athletic Theme -->
    <color name="md_theme_primary">#FF6B35</color>
    <color name="md_theme_onPrimary">#FFFFFF</color>
    <color name="md_theme_secondary">#004E89</color>
    <color name="md_theme_onSecondary">#FFFFFF</color>
    <color name="md_theme_tertiary">#00D9FF</color>
    <color name="md_theme_error">#FF3D00</color>
    <color name="md_theme_background">#F7F7F7</color>
    <color name="md_theme_surface">#FFFFFF</color>
</resources>
```

### Navigation Graph:

**nav_graph.xml:**
```xml
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/dashboardFragment">

    <fragment
        android:id="@+id/dashboardFragment"
        android:name="com.example.workouttracker.presentation.dashboard.DashboardFragment"
        android:label="Dashboard">
        <action
            android:id="@+id/action_dashboard_to_input"
            app:destination="@id/workoutInputFragment" />
    </fragment>

    <fragment
        android:id="@+id/workoutInputFragment"
        android:name="com.example.workouttracker.presentation.input.WorkoutInputFragment"
        android:label="Übung eingeben">
        <action
            android:id="@+id/action_input_to_timer"
            app:destination="@id/timerFragment" />
        <argument
            android:name="workoutConfig"
            app:argType="com.example.workouttracker.domain.model.WorkoutConfig" />
    </fragment>

    <fragment
        android:id="@+id/timerFragment"
        android:name="com.example.workouttracker.presentation.timer.TimerFragment"
        android:label="Timer">
        <argument
            android:name="workoutConfig"
            app:argType="com.example.workouttracker.domain.model.WorkoutConfig" />
    </fragment>
</navigation>
```

### Installation & Build:

```bash
# Build debug APK
./gradlew assembleDebug

# Install to device
adb install app/build/outputs/apk/debug/app-debug.apk

# Combined build and install
./gradlew installDebug

# Run tests
./gradlew test

# Generate code coverage
./gradlew jacocoTestReport
```

### .gitignore:

```gitignore
# Built files
*.apk
*.ap_
*.aab
*.dex
*.class
bin/
gen/
out/
build/
.gradle/

# Android Studio
*.iml
.idea/
.DS_Store
/captures
.externalNativeBuild
.cxx
local.properties

# Keystore files
*.jks
*.keystore

# Room schemas (optional - commit if needed)
# app/schemas/
```

### Expected Behavior:

1. **Code Quality:**
   - No God objects (classes > 300 lines)
   - Single Responsibility Principle
   - Dependency Inversion (interfaces, not implementations)
   - Testable code with dependency injection

2. **Performance:**
   - Smooth 60fps animations
   - No ANR (Application Not Responding)
   - Efficient database queries with indexes
   - Proper coroutine cancellation

3. **User Experience:**
   - Material Design animations
   - Haptic feedback on actions
   - Loading states for async operations
   - Error messages in German
   - Proper keyboard handling

4. **Maintainability:**
   - Clear package structure
   - Consistent naming conventions
   - Comprehensive documentation
   - Easy to add new features

---

**Version:** 2.0 (Modern Architecture)  
**Created:** October 2, 2025  
**Author:** AI-assisted development with GitHub Copilot  
**Architecture:** MVVM + Clean Architecture + Hilt DI  
**License:** Open Source
