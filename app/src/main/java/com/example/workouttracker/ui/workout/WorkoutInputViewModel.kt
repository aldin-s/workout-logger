package com.example.workouttracker.ui.workout

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.R
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.getDisplayName
import com.example.workouttracker.data.repository.ExerciseRepository
import com.example.workouttracker.ui.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutInputState(
    val selectedExercise: Exercise? = null,
    val weight: String = "",
    val reps: String = "",
    val pauseTime: String = "120",
    val sets: String = "5",
    
    // Dialogs
    val showAddExerciseDialog: Boolean = false,
    val showDeleteConfirmDialog: Exercise? = null,
    
    // Validation errors
    val exerciseError: String? = null,
    val weightError: String? = null,
    val repsError: String? = null,
    val pauseTimeError: String? = null,
    val setsError: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class WorkoutInputViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferences,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WorkoutInputState())
    val state: StateFlow<WorkoutInputState> = _state.asStateFlow()
    
    // Eagerly: Startet sofort beim ViewModel-Init, nicht erst bei Collector
    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    init {
        // Load defaults from settings
        val defaultPauseTime = prefs.getInt(SettingsViewModel.PREF_DEFAULT_PAUSE_TIME, 120)
        val defaultSets = prefs.getInt(SettingsViewModel.PREF_DEFAULT_SETS, 5)
        val defaultReps = prefs.getInt(SettingsViewModel.PREF_DEFAULT_REPS, 8)
        _state.update { 
            it.copy(
                pauseTime = defaultPauseTime.toString(),
                sets = defaultSets.toString(),
                reps = defaultReps.toString()
            ) 
        }
        
        // Initialize predefined exercises on first app start
        viewModelScope.launch {
            exerciseRepository.initializePredefined()
        }
    }
    
    fun selectExercise(exercise: Exercise) {
        _state.update { 
            it.copy(
                selectedExercise = exercise,
                exerciseError = null
            ) 
        }
    }
    
    fun setWeight(weight: String) {
        _state.update { it.copy(weight = weight, weightError = null) }
    }
    
    fun setReps(reps: String) {
        _state.update { it.copy(reps = reps, repsError = null) }
    }
    
    fun setPauseTime(pauseTime: String) {
        _state.update { it.copy(pauseTime = pauseTime, pauseTimeError = null) }
    }
    
    fun setSets(sets: String) {
        _state.update { it.copy(sets = sets, setsError = null) }
    }
    
    // Dialog controls
    fun showAddExerciseDialog() {
        _state.update { it.copy(showAddExerciseDialog = true) }
    }
    
    fun hideAddExerciseDialog() {
        _state.update { it.copy(showAddExerciseDialog = false, errorMessage = null) }
    }
    
    fun showDeleteConfirmDialog(exercise: Exercise) {
        _state.update { it.copy(showDeleteConfirmDialog = exercise) }
    }
    
    fun hideDeleteConfirmDialog() {
        _state.update { it.copy(showDeleteConfirmDialog = null) }
    }
    
    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    // Exercise CRUD operations
    fun addExercise(name: String) {
        viewModelScope.launch {
            exerciseRepository.addExercise(name)
                .onSuccess {
                    _state.update { it.copy(showAddExerciseDialog = false, errorMessage = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun deleteExercise(id: String) {
        viewModelScope.launch {
            // Clear selection if deleted exercise was selected
            if (_state.value.selectedExercise?.id == id) {
                _state.update { it.copy(selectedExercise = null) }
            }
            exerciseRepository.deleteExercise(id)
            _state.update { it.copy(showDeleteConfirmDialog = null) }
        }
    }
    
    fun reorderExercises(exercises: List<Exercise>) {
        viewModelScope.launch {
            exerciseRepository.reorderExercises(exercises)
        }
    }
    
    fun validate(): WorkoutData? {
        val currentState = _state.value
        var hasError = false
        
        // Validate exercise
        if (currentState.selectedExercise == null) {
            _state.update { it.copy(exerciseError = application.getString(R.string.error_select_exercise)) }
            hasError = true
        }
        
        // Validate weight
        val weight = currentState.weight.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            _state.update { it.copy(weightError = application.getString(R.string.error_weight_invalid)) }
            hasError = true
        }
        
        // Validate reps
        val reps = currentState.reps.toIntOrNull()
        if (reps == null || reps <= 0) {
            _state.update { it.copy(repsError = application.getString(R.string.error_reps_invalid)) }
            hasError = true
        }
        
        // Validate pause time
        val pauseTime = currentState.pauseTime.toIntOrNull()
        if (pauseTime == null || pauseTime <= 0) {
            _state.update { it.copy(pauseTimeError = application.getString(R.string.error_pause_invalid)) }
            hasError = true
        }
        
        // Validate sets
        val sets = currentState.sets.toIntOrNull()
        if (sets == null || sets <= 0) {
            _state.update { it.copy(setsError = application.getString(R.string.error_sets_invalid)) }
            hasError = true
        }
        
        return if (!hasError && weight != null && reps != null && pauseTime != null && sets != null) {
            WorkoutData(
                // Use getDisplayName() for proper localization of predefined exercises
                exerciseName = currentState.selectedExercise!!.getDisplayName(application),
                weight = weight,
                reps = reps,
                pauseTime = pauseTime,
                totalSets = sets
            )
        } else {
            null
        }
    }
}

data class WorkoutData(
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val pauseTime: Int,
    val totalSets: Int
)
