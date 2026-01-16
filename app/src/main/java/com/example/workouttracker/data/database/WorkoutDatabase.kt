package com.example.workouttracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.data.model.Exercise

/**
 * Room database for workout data.
 * 
 * Note: Database instance is provided by Hilt (see DatabaseModule).
 * Do NOT use a manual singleton here - Hilt handles the @Singleton lifecycle.
 */
@Database(
    entities = [CompletedSet::class, Exercise::class],
    version = 3,
    exportSchema = true  // Required for migration testing
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    
    abstract fun completedSetDao(): CompletedSetDao
    abstract fun exerciseDao(): ExerciseDao
    
    companion object {
        /**
         * Migration from version 1 to 2: Add exercises table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercises (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        nameResId INTEGER,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 2 to 3: Add nameResKey column for stable localization.
         * 
         * Resource IDs (R.string.xxx) can change between builds, causing wrong strings
         * to be displayed. We now use a stable string key that maps to resources at runtime.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new stable key column
                db.execSQL("ALTER TABLE exercises ADD COLUMN nameResKey TEXT")
                
                // Migrate existing predefined exercises to use stable keys
                db.execSQL("UPDATE exercises SET nameResKey = 'exercise_deadlift' WHERE id = 'predefined_deadlift'")
                db.execSQL("UPDATE exercises SET nameResKey = 'exercise_bench_press' WHERE id = 'predefined_bench'")
                db.execSQL("UPDATE exercises SET nameResKey = 'exercise_rowing' WHERE id = 'predefined_rowing'")
                db.execSQL("UPDATE exercises SET nameResKey = 'exercise_squat' WHERE id = 'predefined_squat'")
            }
        }
    }
}