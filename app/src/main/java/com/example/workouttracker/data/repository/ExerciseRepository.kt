package com.example.workouttracker.data.repository

import com.example.workouttracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing exercises.
 */
interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getAllExercisesSnapshot(): List<Exercise>
    suspend fun initializePredefined()
    suspend fun addExercise(name: String): Result<Unit>
    suspend fun renameExercise(id: String, newName: String): Result<Unit>
    suspend fun deleteExercise(id: String)
    suspend fun reorderExercises(exercises: List<Exercise>)
    suspend fun importExercises(exercises: List<Exercise>)
    suspend fun deleteAllAndReinitialize()
}
