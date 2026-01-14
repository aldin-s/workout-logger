package com.asstudio.berlin.reps.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import com.asstudio.berlin.reps.data.model.CompletedSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel für den Timer-Screen.
 * 
 * Zentrale State-Verwaltung nach dem Single Source of Truth Prinzip.
 * Alle UI-Entscheidungen werden hier getroffen, nicht in der Activity.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = WorkoutDatabase.getDatabase(application)
    
    // Single Source of Truth für den UI-Zustand
    private val _uiState = MutableStateFlow<TimerUiState>(TimerUiState.Loading)
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    // Workout-Konfiguration (wird einmal gesetzt)
    private var exerciseName: String = ""
    private var weight: Double = 0.0
    private var plannedReps: Int = 0
    private var pauseTimeSeconds: Int = 60
    private var totalSets: Int = 1
    private var currentSet: Int = 1
    
    /**
     * Initialisiert das Workout mit den übergebenen Parametern.
     * Wird einmal beim Start aufgerufen.
     */
    fun initializeWorkout(
        exerciseName: String,
        weight: Double,
        plannedReps: Int,
        pauseTimeSeconds: Int,
        totalSets: Int,
        restoredCurrentSet: Int? = null
    ) {
        this.exerciseName = exerciseName
        this.weight = weight
        this.plannedReps = plannedReps
        this.pauseTimeSeconds = pauseTimeSeconds
        this.totalSets = totalSets
        this.currentSet = restoredCurrentSet ?: 1
        
        // Starte im Running-Zustand mit voller Zeit
        startTimer()
    }
    
    /**
     * Wird vom Service aufgerufen bei jedem Timer-Tick.
     */
    fun onTimerTick(timeLeftMillis: Long) {
        _uiState.value = TimerUiState.Running(
            exerciseName = exerciseName,
            weight = weight,
            currentSet = currentSet,
            totalSets = totalSets,
            timeLeftMillis = timeLeftMillis,
            pauseTimeSeconds = pauseTimeSeconds
        )
    }
    
    /**
     * Wird vom Service aufgerufen wenn der Timer abgelaufen ist.
     */
    fun onTimerFinished() {
        _uiState.value = TimerUiState.WaitingForSetComplete(
            exerciseName = exerciseName,
            weight = weight,
            currentSet = currentSet,
            totalSets = totalSets,
            pauseTimeSeconds = pauseTimeSeconds
        )
    }
    
    /**
     * User hat "Satz fertig" geklickt.
     * Speichert den Satz und startet nächsten Timer oder beendet Workout.
     */
    fun onSetCompleted(): SetCompletedResult {
        // Speichere abgeschlossenen Satz in Datenbank
        logCompletedSet()
        
        return if (currentSet >= totalSets) {
            // Workout beendet
            _uiState.value = TimerUiState.WorkoutCompleted(
                exerciseName = exerciseName,
                weight = weight,
                totalSetsCompleted = currentSet
            )
            SetCompletedResult.WorkoutFinished
        } else {
            // Nächster Satz
            currentSet++
            startTimer()
            SetCompletedResult.NextSet(currentSet, pauseTimeSeconds)
        }
    }
    
    /**
     * Service-Fehler aufgetreten.
     */
    fun onServiceError(message: String) {
        _uiState.value = TimerUiState.Error(message)
    }
    
    /**
     * Stellt den Zustand nach einer Rotation wieder her.
     */
    fun restoreState(timeLeftMillis: Long, isRunning: Boolean) {
        if (isRunning || timeLeftMillis > 0) {
            _uiState.value = TimerUiState.Running(
                exerciseName = exerciseName,
                weight = weight,
                currentSet = currentSet,
                totalSets = totalSets,
                timeLeftMillis = timeLeftMillis,
                pauseTimeSeconds = pauseTimeSeconds
            )
        } else {
            onTimerFinished()
        }
    }
    
    /**
     * Prüft ob aktuell auf Satz-Abschluss gewartet wird (Button aktiv).
     */
    fun isWaitingForSetComplete(): Boolean {
        return _uiState.value is TimerUiState.WaitingForSetComplete
    }
    
    /**
     * Gibt die aktuelle Satz-Nummer zurück (für Service-Updates).
     */
    fun getCurrentSet(): Int = currentSet
    
    /**
     * Gibt die Pausenzeit zurück (für Service-Restart).
     */
    fun getPauseTimeSeconds(): Int = pauseTimeSeconds
    
    /**
     * Gibt den Übungsnamen zurück.
     */
    fun getExerciseName(): String = exerciseName
    
    /**
     * Gibt das Gewicht zurück.
     */
    fun getWeight(): Double = weight
    
    /**
     * Gibt die Gesamtanzahl der Sätze zurück.
     */
    fun getTotalSets(): Int = totalSets
    
    private fun startTimer() {
        _uiState.value = TimerUiState.Running(
            exerciseName = exerciseName,
            weight = weight,
            currentSet = currentSet,
            totalSets = totalSets,
            timeLeftMillis = pauseTimeSeconds * 1000L,
            pauseTimeSeconds = pauseTimeSeconds
        )
    }
    
    private fun logCompletedSet() {
        val completedSet = CompletedSet(
            exerciseName = exerciseName,
            weight = weight,
            plannedReps = plannedReps,
            completedReps = plannedReps,
            setNumber = currentSet,
            timestamp = Date()
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.completedSetDao().insert(completedSet)
            } catch (e: Exception) {
                android.util.Log.e("TimerViewModel", "Error logging completed set", e)
            }
        }
    }
    
    /**
     * Ergebnis nach Satz-Abschluss.
     */
    sealed class SetCompletedResult {
        data class NextSet(val setNumber: Int, val pauseTimeSeconds: Int) : SetCompletedResult()
        data object WorkoutFinished : SetCompletedResult()
    }
}
