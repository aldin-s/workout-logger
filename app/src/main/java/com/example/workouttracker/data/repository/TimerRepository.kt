package com.example.workouttracker.data.repository

import com.example.workouttracker.ui.timer.TimerState
import kotlinx.coroutines.flow.Flow

/**
 * Repository für Timer-Operationen.
 * Abstrahiert die Foreground Service Logik vom ViewModel.
 */
interface TimerRepository {
    /**
     * Startet den Timer mit den gegebenen Parametern.
     * @return Result.success() bei Erfolg, Result.failure() bei Fehler (z.B. SecurityException)
     */
    fun startTimer(
        exerciseName: String,
        pauseTimeSeconds: Int,
        currentSet: Int,
        totalSets: Int
    ): Result<Unit>
    
    /**
     * Stoppt den Timer und beendet den Foreground Service.
     */
    fun stopTimer()
    
    /**
     * Geht zum nächsten Set über.
     */
    fun nextSet(setNumber: Int)
    
    /**
     * Bindet an den TimerService und gibt Updates zurück.
     * @return Flow mit Timer-Updates (timeLeft, isComplete, etc.)
     */
    fun observeTimerUpdates(): Flow<TimerServiceUpdate>
    
    /**
     * Prüft, ob der Foreground Service verfügbar ist.
     * Auf manchen ROMs (z.B. Xiaomi mit MIUI) kann der Service blockiert werden.
     */
    fun isBackgroundModeAvailable(): Boolean
}

/**
 * Updates vom TimerService.
 */
sealed class TimerServiceUpdate {
    data class TimerTick(val timeLeftInMillis: Long) : TimerServiceUpdate()
    object TimerComplete : TimerServiceUpdate()
    data class Error(val message: String) : TimerServiceUpdate()
}
