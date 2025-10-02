package com.example.workouttracker.ui.history

import com.example.workouttracker.data.model.CompletedSet
import java.util.Date

/**
 * Represents a workout session - all sets of the same exercise done consecutively
 */
data class WorkoutSession(
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val totalSets: Int,
    val startTime: Date,
    val endTime: Date,
    val sets: List<CompletedSet>
)
