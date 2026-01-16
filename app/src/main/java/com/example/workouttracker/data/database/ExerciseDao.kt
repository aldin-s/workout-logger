package com.example.workouttracker.data.database

import androidx.room.*
import com.example.workouttracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exercise entity.
 */
@Dao
interface ExerciseDao {
    
    @Query("SELECT * FROM exercises ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<Exercise>>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: Exercise): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>)
    
    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("UPDATE exercises SET name = :newName WHERE id = :id")
    suspend fun updateName(id: String, newName: String)
    
    @Query("UPDATE exercises SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)
    
    @Update
    suspend fun updateAll(exercises: List<Exercise>)
    
    @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE LOWER(name) = LOWER(:name))")
    suspend fun existsByName(name: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE LOWER(name) = LOWER(:name) AND id != :excludeId)")
    suspend fun existsByNameExcluding(name: String, excludeId: String): Boolean
    
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
    
    @Query("SELECT * FROM exercises ORDER BY sortOrder ASC")
    suspend fun getAllSnapshot(): List<Exercise>
    
    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}
