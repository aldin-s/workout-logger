package com.asstudio.berlin.reps.data.database

import androidx.room.*
import com.asstudio.berlin.reps.data.model.CustomExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomExerciseDao {
    
    @Query("""
        SELECT ce.* FROM custom_exercises ce
        LEFT JOIN completed_sets cs ON ce.name = cs.exerciseName
        WHERE ce.isHidden = 0 AND (cs.timestamp IS NOT NULL OR ce.lastUsed > 0)
        GROUP BY ce.name
        ORDER BY MAX(COALESCE(cs.timestamp, ce.lastUsed)) DESC
        LIMIT :limit
    """)
    fun getRecentlyUsed(limit: Int = 10): Flow<List<CustomExercise>>
    
    @Query("SELECT * FROM custom_exercises WHERE isHidden = 0 AND createdAt > 0 ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllCustomExercises(): Flow<List<CustomExercise>>
    
    @Query("SELECT * FROM custom_exercises WHERE isHidden = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllVisibleExercises(): Flow<List<CustomExercise>>
    
    @Query("SELECT * FROM custom_exercises WHERE isHidden = 0 ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllVisibleExercisesOnce(): List<CustomExercise>
    
    @Query("""
        SELECT ce.* FROM custom_exercises ce
        LEFT JOIN completed_sets cs ON ce.name = cs.exerciseName
        WHERE ce.isHidden = 0 AND (cs.timestamp IS NOT NULL OR ce.lastUsed > 0)
        GROUP BY ce.name
        ORDER BY MAX(COALESCE(cs.timestamp, ce.lastUsed)) DESC
        LIMIT :limit
    """)
    suspend fun getRecentlyUsedOnce(limit: Int = 10): List<CustomExercise>
    
    @Query("SELECT * FROM custom_exercises WHERE name = :name AND isHidden = 0")
    suspend fun getExerciseByName(name: String): CustomExercise?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: CustomExercise)
    
    @Query("UPDATE custom_exercises SET lastUsed = :timestamp, usageCount = usageCount + 1 WHERE name = :name")
    suspend fun updateUsage(name: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE custom_exercises SET isHidden = 1 WHERE name = :name")
    suspend fun hideExercise(name: String)
    
    @Query("UPDATE custom_exercises SET sortOrder = :sortOrder WHERE name = :name")
    suspend fun updateSortOrder(name: String, sortOrder: Int)
    
    @Query("SELECT MAX(sortOrder) FROM custom_exercises WHERE isHidden = 0")
    suspend fun getMaxSortOrder(): Int?
    
    @Transaction
    suspend fun updateAllSortOrders(exercises: List<CustomExercise>) {
        exercises.forEach { exercise ->
            updateSortOrder(exercise.name, exercise.sortOrder)
        }
    }
    
    @Query("DELETE FROM custom_exercises WHERE name = :name")
    suspend fun deleteExercise(name: String)
}
