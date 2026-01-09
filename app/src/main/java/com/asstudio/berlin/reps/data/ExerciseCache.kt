package com.asstudio.berlin.reps.data

import com.asstudio.berlin.reps.data.model.CustomExercise

/**
 * Simple singleton cache for preloaded exercises.
 * Exercises are loaded in MainActivity before the user opens WorkoutInputActivity.
 */
object ExerciseCache {
    
    @Volatile
    var exercises: List<CustomExercise>? = null
        private set
    
    @Volatile
    var isLoading: Boolean = false
        private set
    
    fun setExercises(list: List<CustomExercise>) {
        exercises = list
        isLoading = false
    }
    
    fun startLoading() {
        isLoading = true
    }
    
    fun clear() {
        exercises = null
        isLoading = false
    }
    
    fun hasData(): Boolean = exercises != null
}
