# 🔍 Code Review & Technical Debt

**Datum:** 14.01.2026  
**Reviewer:** GitHub Copilot (Senior Android Developer Perspektive)

---

## Zusammenfassung

| Kategorie | Status |
|-----------|--------|
| ✅ Gut | Room, Coroutines, KSP, Foreground Service |
| 🟠 Verbesserungswürdig | Architektur, UI-Binding, Dependencies |
| 🔴 Veraltet | Gradle Syntax, keine DI, keine ViewModels |

---

## 🔴 Kritische Punkte

### 1. Gradle Groovy statt Kotlin DSL
- **Dateien:** `build.gradle`, `app/build.gradle`
- **Problem:** Groovy ist veraltet, Kotlin DSL ist der Standard
- **Lösung:** Migration zu `build.gradle.kts`

### 2. Alte Plugin-Syntax
- **Datei:** `app/build.gradle`
- **Problem:** `apply plugin: 'xxx'` statt `plugins {}` Block
- **Lösung:** 
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
```

### 3. Kein MVVM / ViewModel
- **Dateien:** Alle Activities
- **Problem:** Direkte DB-Zugriffe in Activities, keine Trennung von UI und Logik
- **Lösung:** ViewModels pro Screen einführen
- **Beispiel:**
```kotlin
// Aktuell (schlecht):
class MainActivity : AppCompatActivity() {
    private lateinit var database: WorkoutDatabase
    // DB-Zugriff direkt in Activity
}

// Besser:
class MainViewModel(private val repository: WorkoutRepository) : ViewModel() {
    val exercises = repository.exercises.asLiveData()
}
```

### 4. Keine Dependency Injection
- **Problem:** `WorkoutDatabase.getDatabase(this)` wird überall aufgerufen
- **Lösung:** Hilt oder Koin einführen
- **Aufwand:** ~1 Tag

### 5. Hardcoded Colors in Layouts
- **Dateien:** `activity_tracking.xml`, andere Layouts
- **Problem:** `#121212`, `#FFFFFF` direkt im XML
- **Lösung:** Theme-Attribute nutzen (`?attr/colorSurface`, `?attr/colorOnSurface`)

---

## 🟠 Verbesserungswürdig

### 1. Singleton Cache (ExerciseCache.kt)
- **Problem:** Manueller Singleton-Cache ist fragil und nicht reaktiv
- **Lösung:** StateFlow in Repository-Klasse

### 2. findViewById statt ViewBinding
- **Dateien:** Alle Activities
- **Problem:** Nicht typsicher, Boilerplate, Runtime-Crashes möglich
- **Lösung:** ViewBinding aktivieren in `build.gradle`:
```groovy
buildFeatures {
    viewBinding = true
}
```

### 3. Deprecated APIs
| API | Datei | Ersatz |
|-----|-------|--------|
| `overridePendingTransition` | MainActivity | `overrideActivityTransition` (API 34+) |
| `android.app.AlertDialog` | MainActivity | `MaterialAlertDialogBuilder` |

### 4. SharedPreferences direkt
- **Datei:** SettingsActivity.kt
- **Problem:** SharedPreferences ist synchron und nicht typsicher
- **Lösung:** Jetpack DataStore (Preferences)

### 5. Material 2 statt Material 3
- **Aktuell:** `com.google.android.material:material:1.12.0`
- **Modern:** Material 3 mit Dynamic Colors

---

## ✅ Was gut gemacht ist

- **Room Database** mit korrekten Migrationen
- **KSP** statt KAPT für Annotation Processing
- **Coroutines + Flow** für asynchrone Operationen
- **Foreground Service** korrekt mit Notification Channel
- **Notification Permission** für Android 13+ implementiert
- **Test Setup** mit JUnit, Mockito, Espresso vorhanden
- **ProGuard/R8** für Release-Builds aktiviert
- **Lokalisierung** (de/en) vorhanden

---

## 📋 Empfohlene Roadmap

### Phase 1: Quick Wins (1-2 Tage)
- [ ] ViewBinding aktivieren
- [ ] Hardcoded Colors → Theme-Attribute
- [ ] `MaterialAlertDialogBuilder` überall nutzen
- [ ] Deprecated API Warnings fixen

### Phase 2: Architektur (1 Woche)
- [ ] ViewModels für alle Activities erstellen
- [ ] Repository-Pattern für Datenzugriff
- [ ] Hilt für Dependency Injection einführen
- [ ] ExerciseCache durch Repository mit StateFlow ersetzen

### Phase 3: Modernisierung (Optional, langfristig)
- [ ] Gradle Kotlin DSL Migration
- [ ] Version Catalog (`libs.versions.toml`)
- [ ] Navigation Component
- [ ] Jetpack Compose für neue Screens
- [ ] Material 3 Theme

---

## Referenzen

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
