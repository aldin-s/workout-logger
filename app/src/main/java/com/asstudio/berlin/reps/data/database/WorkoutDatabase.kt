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
        
        private const val PREFS_NAME = "workout_db_prefs"
        private const val KEY_STANDARD_EXERCISES_INITIALIZED = "standard_exercises_initialized"
        
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
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE custom_exercises ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Checks if standard exercises need to be initialized.
         * Returns true only ONCE per installation.
         */
        fun shouldInitializeStandardExercises(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val initialized = prefs.getBoolean(KEY_STANDARD_EXERCISES_INITIALIZED, false)
            if (!initialized) {
                prefs.edit().putBoolean(KEY_STANDARD_EXERCISES_INITIALIZED, true).apply()
                return true
            }
            return false
        }
    }
}