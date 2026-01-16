package com.example.workouttracker.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.database.WorkoutDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel für den Stats Screen.
 * Lädt Statistiken aus der Room-Datenbank.
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = WorkoutDatabase.getDatabase(application)
    private val dao = database.completedSetDao()
    
    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
    }
    
    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState.Loading
            
            try {
                val allSets = dao.getAllSets()
                
                if (allSets.isEmpty()) {
                    _uiState.value = StatsUiState.Success(
                        totalWorkouts = 0,
                        weeklyWorkouts = 0,
                        totalWeight = 0.0,
                        totalSets = 0,
                        favoriteExercise = null,
                        lastWorkoutDate = null
                    )
                    return@launch
                }
                
                // Gesamtgewicht berechnen (Gewicht × Wiederholungen)
                val totalWeight = allSets.sumOf { it.weight * it.completedReps }
                
                // Gesamtanzahl Sets
                val totalSets = allSets.size
                
                // Workouts zählen (gruppiert nach Datum - nur Datum, nicht Zeit)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val workoutDates = allSets.map { dateFormat.format(it.timestamp) }.distinct()
                val totalWorkouts = workoutDates.size
                
                // Workouts diese Woche
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.time
                
                val weeklyWorkouts = allSets
                    .filter { it.timestamp >= weekStart }
                    .map { dateFormat.format(it.timestamp) }
                    .distinct()
                    .size
                
                // Häufigste Übung
                val favoriteExercise = allSets
                    .groupingBy { it.exerciseName }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
                
                // Letztes Workout-Datum
                val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val lastWorkoutDate = allSets.maxOfOrNull { it.timestamp }
                    ?.let { displayFormat.format(it) }
                
                _uiState.value = StatsUiState.Success(
                    totalWorkouts = totalWorkouts,
                    weeklyWorkouts = weeklyWorkouts,
                    totalWeight = totalWeight,
                    totalSets = totalSets,
                    favoriteExercise = favoriteExercise,
                    lastWorkoutDate = lastWorkoutDate
                )
                
            } catch (e: Exception) {
                _uiState.value = StatsUiState.Error(
                    message = e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }
}
