package com.example.workouttracker

import android.app.Application
import com.example.workouttracker.data.repository.ExerciseRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for REPS workout tracker.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class RepsApplication : Application() {
    
    @Inject
    lateinit var exerciseRepository: ExerciseRepository
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Warm up: Initialize predefined + fetch all exercises into Room cache
        applicationScope.launch {
            exerciseRepository.initializePredefined()
            // Trigger Room query to warm up SQLite cache
            exerciseRepository.getAllExercisesSnapshot()
        }
    }
}
