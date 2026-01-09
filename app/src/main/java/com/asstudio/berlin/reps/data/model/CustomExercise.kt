package com.asstudio.berlin.reps.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0
)
