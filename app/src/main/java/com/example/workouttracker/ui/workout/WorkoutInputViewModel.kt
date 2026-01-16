package com.example.workouttracker.ui.workout

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WorkoutInputState(
    val selectedExercise: Exercise? = null,
    val customExerciseName: String = "",
    val weight: String = "",
    val reps: String = "",
    val pauseTime: String = "120",
    val sets: String = "5",
    
    // Validation errors
    val exerciseError: String? = null,
    val weightError: String? = null,
    val repsError: String? = null,
    val pauseTimeError: String? = null,
    val setsError: String? = null
)

sealed class Exercise(val nameResId: Int) {
    object Deadlift : Exercise(R.string.exercise_deadlift)
    object BenchPress : Exercise(R.string.exercise_bench_press)
    object Rowing : Exercise(R.string.exercise_rowing)
    object Squat : Exercise(R.string.exercise_squat)
    data class Custom(val name: String) : Exercise(0)
    
    companion object {
        val predefinedExercises = listOf(Deadlift, BenchPress, Rowing, Squat)
    }
}

class WorkoutInputViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = application.getSharedPreferences(SettingsViewModel.PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _state = MutableStateFlow(WorkoutInputState())
    val state: StateFlow<WorkoutInputState> = _state.asStateFlow()
    
    init {
        // Load default pause time from settings
        val defaultPauseTime = prefs.getInt(SettingsViewModel.PREF_DEFAULT_PAUSE_TIME, 120)
        _state.update { it.copy(pauseTime = defaultPauseTime.toString()) }
    }
    
    fun selectExercise(exercise: Exercise) {
        _state.update { 
            it.copy(
                selectedExercise = exercise,
                exerciseError = null
            ) 
        }
    }
    
    fun setCustomExerciseName(name: String) {
        _state.update { 
            it.copy(
                customExerciseName = name,
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
    
    fun validate(): WorkoutData? {
        val currentState = _state.value
        var hasError = false
        
        // Get exercise name
        val exerciseName = when (val exercise = currentState.selectedExercise) {
            is Exercise.Custom -> currentState.customExerciseName.trim()
            is Exercise -> getApplication<Application>().getString(exercise.nameResId)
            null -> ""
        }
        
        // Validate exercise
        if (currentState.selectedExercise == null) {
            _state.update { it.copy(exerciseError = getApplication<Application>().getString(R.string.error_select_exercise)) }
            hasError = true
        } else if (currentState.selectedExercise is Exercise.Custom && currentState.customExerciseName.isBlank()) {
            _state.update { it.copy(exerciseError = getApplication<Application>().getString(R.string.error_enter_exercise)) }
            hasError = true
        }
        
        // Validate weight
        val weight = currentState.weight.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            _state.update { it.copy(weightError = getApplication<Application>().getString(R.string.error_weight_invalid)) }
            hasError = true
        }
        
        // Validate reps
        val reps = currentState.reps.toIntOrNull()
        if (reps == null || reps <= 0) {
            _state.update { it.copy(repsError = getApplication<Application>().getString(R.string.error_reps_invalid)) }
            hasError = true
        }
        
        // Validate pause time
        val pauseTime = currentState.pauseTime.toIntOrNull()
        if (pauseTime == null || pauseTime <= 0) {
            _state.update { it.copy(pauseTimeError = getApplication<Application>().getString(R.string.error_pause_invalid)) }
            hasError = true
        }
        
        // Validate sets
        val sets = currentState.sets.toIntOrNull()
        if (sets == null || sets <= 0) {
            _state.update { it.copy(setsError = getApplication<Application>().getString(R.string.error_sets_invalid)) }
            hasError = true
        }
        
        return if (!hasError && weight != null && reps != null && pauseTime != null && sets != null) {
            WorkoutData(
                exerciseName = exerciseName,
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
