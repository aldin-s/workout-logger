package com.asstudio.berlin.reps.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asstudio.berlin.reps.data.model.CompletedSet

@Database(
    entities = [CompletedSet::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    
    abstract fun completedSetDao(): CompletedSetDao
    
    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null
        
        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}