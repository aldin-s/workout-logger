package com.asstudio.berlin.reps.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.asstudio.berlin.reps.data.model.CompletedSet
import com.asstudio.berlin.reps.data.model.CustomExercise

@Database(
    entities = [CompletedSet::class, CustomExercise::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    
    abstract fun completedSetDao(): CompletedSetDao
    abstract fun customExerciseDao(): CustomExerciseDao
    
    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_exercises (
                        name TEXT PRIMARY KEY NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastUsed INTEGER NOT NULL,
                        usageCount INTEGER NOT NULL,
                        isHidden INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}