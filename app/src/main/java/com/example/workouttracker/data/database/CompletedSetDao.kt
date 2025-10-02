package com.example.workouttracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workouttracker.data.model.CompletedSet

@Dao
interface CompletedSetDao {
    
    @Insert
    suspend fun insert(completedSet: CompletedSet)
    
    @Query("SELECT * FROM completed_sets ORDER BY timestamp DESC")
    suspend fun getAllSets(): List<CompletedSet>
    
    @Query("SELECT * FROM completed_sets WHERE exerciseName = :exerciseName ORDER BY timestamp DESC")
    suspend fun getSetsByExercise(exerciseName: String): List<CompletedSet>
    
    @Query("SELECT DISTINCT exerciseName FROM completed_sets ORDER BY exerciseName")
    suspend fun getAllExerciseNames(): List<String>
}