package com.asstudio.berlin.reps.ui.timer

/**
 * Sealed Class für alle möglichen Timer-UI-Zustände.
 * Single Source of Truth für den gesamten Timer-Screen.
 */
sealed class TimerUiState {
    
    /**
     * Initialer Zustand - Warten auf Service-Verbindung
     */
    data object Loading : TimerUiState()
    
    /**
     * Timer läuft - Pausenzeit zwischen Sätzen
     */
    data class Running(
        val exerciseName: String,
        val weight: Double,
        val currentSet: Int,
        val totalSets: Int,
        val timeLeftMillis: Long,
        val pauseTimeSeconds: Int
    ) : TimerUiState() {
        val timeLeftFormatted: String
            get() {
                val seconds = (timeLeftMillis / 1000).toInt()
                return String.format("%02d:%02d", seconds / 60, seconds % 60)
            }
        
        val setsFormatted: String
            get() = "Satz $currentSet/$totalSets"
        
        val weightFormatted: String
            get() = String.format("%.1f kg", weight)
    }
    
    /**
     * Timer abgelaufen - Warten auf User-Aktion (Satz fertig)
     */
    data class WaitingForSetComplete(
        val exerciseName: String,
        val weight: Double,
        val currentSet: Int,
        val totalSets: Int,
        val pauseTimeSeconds: Int
    ) : TimerUiState() {
        val setsFormatted: String
            get() = "Satz $currentSet/$totalSets"
        
        val weightFormatted: String
            get() = String.format("%.1f kg", weight)
    }
    
    /**
     * Workout komplett abgeschlossen
     */
    data class WorkoutCompleted(
        val exerciseName: String,
        val weight: Double,
        val totalSetsCompleted: Int
    ) : TimerUiState()
    
    /**
     * Fehler beim Service-Start
     */
    data class Error(val message: String) : TimerUiState()
}
