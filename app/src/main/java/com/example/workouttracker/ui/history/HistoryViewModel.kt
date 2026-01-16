package com.example.workouttracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.database.WorkoutDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val items: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = WorkoutDatabase.getDatabase(application)
    
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            
            try {
                val items = withContext(Dispatchers.IO) {
                    val completedSets = database.completedSetDao().getAllSets()
                    HistoryGrouper.groupByDate(completedSets, getApplication())
                }
                _uiState.value = HistoryUiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                session.sets.forEach { set ->
                    database.completedSetDao().delete(set)
                }
                loadHistory() // Refresh
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Delete failed")
            }
        }
    }
    
    fun updateSession(session: WorkoutSession, newWeight: Double, newReps: Int, newSets: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSetCount = session.sets.size
                
                when {
                    newSets == currentSetCount -> {
                        // Same number of sets: update existing
                        session.sets.forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                    }
                    newSets < currentSetCount -> {
                        // Fewer sets: update first N, delete rest
                        session.sets.take(newSets).forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                        session.sets.drop(newSets).forEach { set ->
                            database.completedSetDao().delete(set)
                        }
                    }
                    else -> {
                        // More sets: update existing, create new
                        session.sets.forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                        val templateSet = session.sets.first()
                        repeat(newSets - currentSetCount) { index ->
                            database.completedSetDao().insert(
                                templateSet.copy(
                                    id = 0,
                                    setNumber = currentSetCount + index + 1,
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                    }
                }
                
                loadHistory() // Refresh
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Update failed")
            }
        }
    }
}
