package com.example.workouttracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.workouttracker.data.model.CompletedSet

@Dao
interface CompletedSetDao {
    
    @Insert
    suspend fun insert(completedSet: CompletedSet)
    
    @Insert
    suspend fun insertAll(sets: List<CompletedSet>)
    
    @Update
    suspend fun update(completedSet: CompletedSet)
    
    @Delete
    suspend fun delete(completedSet: CompletedSet)
    
    @Query("DELETE FROM completed_sets WHERE id = :setId")
    suspend fun deleteById(setId: Long)
    
    @Query("SELECT * FROM completed_sets ORDER BY timestamp DESC")
    suspend fun getAllSets(): List<CompletedSet>
    
    @Query("SELECT * FROM completed_sets WHERE exerciseName = :exerciseName ORDER BY timestamp DESC")
    suspend fun getSetsByExercise(exerciseName: String): List<CompletedSet>
    
    @Query("SELECT DISTINCT exerciseName FROM completed_sets ORDER BY exerciseName")
    suspend fun getAllExerciseNames(): List<String>
    
    @Query("DELETE FROM completed_sets")
    suspend fun deleteAll()
    
    /**
     * Check if a set with the same exerciseName, timestamp, and setNumber already exists.
     * Used for duplicate detection during import.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM completed_sets 
            WHERE exerciseName = :exerciseName 
            AND timestamp = :timestamp 
            AND setNumber = :setNumber
        )
    """)
    suspend fun existsByKey(exerciseName: String, timestamp: Long, setNumber: Int): Boolean
    
    /**
     * Import sets atomically - all or nothing.
     */
    @Transaction
    suspend fun importSets(sets: List<CompletedSet>) {
        insertAll(sets)
    }
}