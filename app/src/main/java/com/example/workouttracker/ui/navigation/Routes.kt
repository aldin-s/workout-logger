package com.example.workouttracker.ui.navigation

/**
 * Navigation Routes für die REPS App.
 * String-based Routes (stabil seit 2021).
 */
object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val WORKOUT_INPUT = "workout_input"
    
    // Timer mit Argumenten
    const val TIMER = "timer/{exerciseId}/{sets}/{isTimeBased}"
    
    fun timer(exerciseId: Long, sets: Int, isTimeBased: Boolean): String {
        return "timer/$exerciseId/$sets/$isTimeBased"
    }
}
