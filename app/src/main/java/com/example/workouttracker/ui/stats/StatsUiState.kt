package com.example.workouttracker.ui.stats

/**
 * UI State für den Stats Screen.
 * Sealed Class für typsichere State-Verwaltung.
 */
sealed class StatsUiState {
    
    /**
     * Daten werden geladen.
     */
    data object Loading : StatsUiState()
    
    /**
     * Daten erfolgreich geladen.
     */
    data class Success(
        val totalWorkouts: Int,
        val weeklyWorkouts: Int,
        val totalWeight: Double,
        val totalSets: Int,
        val favoriteExercise: String?,
        val lastWorkoutDate: String?
    ) : StatsUiState()
    
    /**
     * Fehler beim Laden.
     */
    data class Error(val message: String) : StatsUiState()
}
