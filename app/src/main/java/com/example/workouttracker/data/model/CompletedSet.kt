package com.example.workouttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "completed_sets")
data class CompletedSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val weight: Double,
    val plannedReps: Int,
    val completedReps: Int,
    val setNumber: Int,
    val timestamp: Date
)