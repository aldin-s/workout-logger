package com.asstudio.berlin.reps.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.asstudio.berlin.reps.data.model.CompletedSet

@Dao
interface CompletedSetDao {
    
    @Insert
    suspend fun insert(completedSet: CompletedSet)
    
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
}