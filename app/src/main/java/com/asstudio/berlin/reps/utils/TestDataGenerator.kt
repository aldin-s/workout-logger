package com.asstudio.berlin.reps.utils

import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import com.asstudio.berlin.reps.data.model.CompletedSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

object TestDataGenerator {
    
    fun generateTestData(database: WorkoutDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance()
            
            // HEUTE - 3 Workouts (vor 2 Stunden)
            calendar.add(Calendar.HOUR, -2)
            insertWorkout(database, "Bankdrücken", 80.0, 10, 3, calendar.time)
            
            calendar.add(Calendar.MINUTE, 15)
            insertWorkout(database, "Kreuzheben", 100.0, 8, 4, calendar.time)
            
            calendar.add(Calendar.MINUTE, 20)
            insertWorkout(database, "Kniebeuge", 120.0, 12, 5, calendar.time)
            
            // GESTERN - 2 Workouts
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 15)
            insertWorkout(database, "Rudern", 60.0, 12, 4, calendar.time)
            
            calendar.add(Calendar.MINUTE, 25)
            insertWorkout(database, "Bankdrücken", 82.5, 10, 3, calendar.time)
            
            // VOR 3 TAGEN (Diese Woche)
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            calendar.set(Calendar.HOUR_OF_DAY, 14)
            calendar.set(Calendar.MINUTE, 30)
            insertWorkout(database, "Kreuzheben", 95.0, 8, 4, calendar.time)
            
            calendar.add(Calendar.MINUTE, 20)
            insertWorkout(database, "Kniebeuge", 115.0, 12, 4, calendar.time)
            
            // VOR 5 TAGEN (Diese Woche)
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            insertWorkout(database, "Bankdrücken", 77.5, 10, 4, calendar.time)
            
            calendar.add(Calendar.MINUTE, 18)
            insertWorkout(database, "Rudern", 57.5, 12, 3, calendar.time)
            
            // VOR 10 TAGEN (Letzter Monat)
            calendar.add(Calendar.DAY_OF_MONTH, -5)
            calendar.set(Calendar.HOUR_OF_DAY, 15)
            calendar.set(Calendar.MINUTE, 45)
            insertWorkout(database, "Kreuzheben", 90.0, 8, 3, calendar.time)
            
            calendar.add(Calendar.MINUTE, 15)
            insertWorkout(database, "Kniebeuge", 110.0, 12, 3, calendar.time)
            
            // VOR 15 TAGEN (Letzter Monat)
            calendar.add(Calendar.DAY_OF_MONTH, -5)
            calendar.set(Calendar.HOUR_OF_DAY, 11)
            calendar.set(Calendar.MINUTE, 20)
            insertWorkout(database, "Bankdrücken", 75.0, 10, 3, calendar.time)
            
            // VOR 30 TAGEN (Älter)
            calendar.add(Calendar.DAY_OF_MONTH, -15)
            calendar.set(Calendar.HOUR_OF_DAY, 16)
            calendar.set(Calendar.MINUTE, 0)
            insertWorkout(database, "Rudern", 55.0, 12, 4, calendar.time)
            
            calendar.add(Calendar.MINUTE, 22)
            insertWorkout(database, "Kreuzheben", 85.0, 8, 4, calendar.time)
        }
    }
    
    private suspend fun insertWorkout(
        database: WorkoutDatabase,
        exercise: String,
        weight: Double,
        reps: Int,
        sets: Int,
        baseTime: Date
    ) {
        val calendar = Calendar.getInstance()
        calendar.time = baseTime
        
        for (setNumber in 1..sets) {
            val completedSet = CompletedSet(
                exerciseName = exercise,
                weight = weight,
                plannedReps = reps,
                completedReps = reps,
                setNumber = setNumber,
                timestamp = calendar.time
            )
            database.completedSetDao().insert(completedSet)
            
            // Pause zwischen Sätzen (2 Minuten)
            calendar.add(Calendar.MINUTE, 2)
        }
    }
    
    fun clearAllData(database: WorkoutDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            database.completedSetDao().deleteAll()
        }
    }
}
