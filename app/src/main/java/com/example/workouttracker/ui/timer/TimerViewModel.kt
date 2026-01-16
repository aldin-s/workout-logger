package com.example.workouttracker.ui.timer

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.database.CompletedSetDao
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.data.repository.TimerRepository
import com.example.workouttracker.data.repository.TimerServiceUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class TimerState(
    val exerciseName: String = "",
    val weight: Double = 0.0,
    val plannedReps: Int = 0,
    val pauseTimeSeconds: Int = 60,
    val totalSets: Int = 1,
    val currentSet: Int = 1,
    val timeLeftInMillis: Long = 0,
    val isTimerRunning: Boolean = false,
    val isWorkoutCompleted: Boolean = false,
    val isServiceBound: Boolean = false,
    val backgroundModeAvailable: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val completedSetDao: CompletedSetDao,
    private val timerRepository: TimerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()
    
    // Verhindert doppelte Initialisierung bei Configuration Changes (Rotation)
    private var isInitialized = false

    fun initialize(
        exerciseName: String,
        weight: Double,
        plannedReps: Int,
        pauseTimeSeconds: Int,
        totalSets: Int
    ) {
        // Bei Rotation: ViewModel überlebt, initialize() wird erneut aufgerufen
        // → Skip wenn bereits initialisiert
        if (isInitialized) return
        isInitialized = true
        
        // Restore from SavedState if available
        val restoredCurrentSet = savedStateHandle.get<Int>(KEY_CURRENT_SET) ?: 1
        val restoredTimeLeft = savedStateHandle.get<Long>(KEY_TIME_LEFT) ?: (pauseTimeSeconds * 1000L)

        _state.update {
            it.copy(
                exerciseName = exerciseName,
                weight = weight,
                plannedReps = plannedReps,
                pauseTimeSeconds = pauseTimeSeconds,
                totalSets = totalSets,
                currentSet = restoredCurrentSet,
                timeLeftInMillis = restoredTimeLeft
            )
        }
        
        // Start the foreground service via repository
        startTimerService()
    }
    
    /**
     * Bindet an den TimerService und empfängt Updates.
     * Muss von der Activity aufgerufen werden.
     */
    fun bindService(context: Context) {
        viewModelScope.launch {
            timerRepository.observeTimerUpdates().collect { update ->
                when (update) {
                    is TimerServiceUpdate.TimerTick -> {
                        _state.update { 
                            it.copy(
                                timeLeftInMillis = update.timeLeftInMillis,
                                isTimerRunning = true,
                                isServiceBound = true
                            )
                        }
                        savedStateHandle[KEY_TIME_LEFT] = update.timeLeftInMillis
                    }
                    is TimerServiceUpdate.TimerComplete -> {
                        _state.update { 
                            it.copy(
                                timeLeftInMillis = 0,
                                isTimerRunning = false
                            )
                        }
                        savedStateHandle[KEY_TIME_LEFT] = 0L
                        savedStateHandle[KEY_TIMER_RUNNING] = false
                    }
                    is TimerServiceUpdate.Error -> {
                        _state.update { 
                            it.copy(
                                error = update.message,
                                backgroundModeAvailable = false
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun unbindService(context: Context) {
        // Der Flow wird automatisch gecancelled wenn der ViewModel cleared wird
        _state.update { it.copy(isServiceBound = false) }
    }
    
    private fun startTimerService() {
        val currentState = _state.value
        
        val result = timerRepository.startTimer(
            exerciseName = currentState.exerciseName,
            pauseTimeSeconds = currentState.pauseTimeSeconds,
            currentSet = currentState.currentSet,
            totalSets = currentState.totalSets
        )
        
        result.fold(
            onSuccess = {
                _state.update { it.copy(isTimerRunning = true) }
                savedStateHandle[KEY_TIMER_RUNNING] = true
            },
            onFailure = { e ->
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to start timer",
                        backgroundModeAvailable = false
                    )
                }
            }
        )
    }

    fun markSetAsCompleted() {
        logCompletedSet()

        val currentState = _state.value
        if (currentState.currentSet >= currentState.totalSets) {
            // Workout complete - stop service
            stopTimerService()
            _state.update { it.copy(isWorkoutCompleted = true) }
        } else {
            // Move to next set
            val newSet = currentState.currentSet + 1
            val newTimeLeft = currentState.pauseTimeSeconds * 1000L
            
            _state.update {
                it.copy(
                    currentSet = newSet,
                    timeLeftInMillis = newTimeLeft,
                    isTimerRunning = true
                )
            }
            saveState()
            
            // Tell service to start next set
            timerRepository.nextSet(newSet)
        }
    }
    
    private fun stopTimerService() {
        timerRepository.stopTimer()
        _state.update { it.copy(isTimerRunning = false) }
    }

    private fun logCompletedSet() {
        val currentState = _state.value
        val completedSet = CompletedSet(
            exerciseName = currentState.exerciseName,
            weight = currentState.weight,
            plannedReps = currentState.plannedReps,
            completedReps = currentState.plannedReps,
            setNumber = currentState.currentSet,
            timestamp = Date()
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                completedSetDao.insert(completedSet)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun saveState() {
        savedStateHandle[KEY_CURRENT_SET] = _state.value.currentSet
        savedStateHandle[KEY_TIME_LEFT] = _state.value.timeLeftInMillis
        savedStateHandle[KEY_TIMER_RUNNING] = _state.value.isTimerRunning
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun stopAndCleanup() {
        stopTimerService()
    }

    companion object {
        private const val KEY_CURRENT_SET = "current_set"
        private const val KEY_TIME_LEFT = "time_left"
        private const val KEY_TIMER_RUNNING = "timer_running"
    }
}
