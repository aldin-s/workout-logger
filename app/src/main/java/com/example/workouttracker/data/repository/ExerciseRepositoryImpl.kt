package com.example.workouttracker.data.repository

import android.content.Context
import com.example.workouttracker.R
import com.example.workouttracker.data.database.ExerciseDao
import com.example.workouttracker.data.model.Exercise
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExerciseRepository with validation.
 */
@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    @ApplicationContext private val context: Context
) : ExerciseRepository {
    
    override fun getAllExercises(): Flow<List<Exercise>> = dao.getAll()
    
    override suspend fun initializePredefined() {
        if (dao.count() == 0) {
            dao.insertAll(listOf(
                Exercise(
                    id = "predefined_deadlift",
                    name = context.getString(R.string.exercise_deadlift),
                    nameResKey = "exercise_deadlift",
                    sortOrder = 0
                ),
                Exercise(
                    id = "predefined_bench",
                    name = context.getString(R.string.exercise_bench_press),
                    nameResKey = "exercise_bench_press",
                    sortOrder = 1
                ),
                Exercise(
                    id = "predefined_rowing",
                    name = context.getString(R.string.exercise_rowing),
                    nameResKey = "exercise_rowing",
                    sortOrder = 2
                ),
                Exercise(
                    id = "predefined_squat",
                    name = context.getString(R.string.exercise_squat),
                    nameResKey = "exercise_squat",
                    sortOrder = 3
                )
            ))
        }
    }
    
    override suspend fun addExercise(name: String): Result<Unit> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException(
                context.getString(R.string.error_exercise_name_empty)
            ))
        }
        if (dao.existsByName(trimmed)) {
            return Result.failure(IllegalArgumentException(
                context.getString(R.string.error_exercise_exists)
            ))
        }
        val maxOrder = dao.getAll().first().maxOfOrNull { it.sortOrder } ?: 0
        dao.insert(Exercise(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            sortOrder = maxOrder + 1
        ))
        return Result.success(Unit)
    }
    
    override suspend fun renameExercise(id: String, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException(
                context.getString(R.string.error_exercise_name_empty)
            ))
        }
        if (dao.existsByNameExcluding(trimmed, id)) {
            return Result.failure(IllegalArgumentException(
                context.getString(R.string.error_exercise_exists)
            ))
        }
        dao.updateName(id, trimmed)
        return Result.success(Unit)
    }
    
    override suspend fun deleteExercise(id: String) = dao.delete(id)
    
    override suspend fun reorderExercises(exercises: List<Exercise>) {
        val updated = exercises.mapIndexed { index, exercise ->
            exercise.copy(sortOrder = index)
        }
        dao.updateAll(updated)
    }
    
    override suspend fun getAllExercisesSnapshot(): List<Exercise> = dao.getAllSnapshot()
    
    override suspend fun importExercises(exercises: List<Exercise>) {
        exercises.forEach { exercise ->
            if (!dao.existsByName(exercise.name)) {
                dao.insert(exercise)
            }
        }
    }
    
    override suspend fun deleteAllAndReinitialize() {
        dao.deleteAll()
        initializePredefined()
    }
}
