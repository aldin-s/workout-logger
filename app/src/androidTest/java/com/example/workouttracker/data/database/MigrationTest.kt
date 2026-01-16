package com.example.workouttracker.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests for Room database migrations.
 * Ensures data integrity when upgrading the database schema.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WorkoutDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_createsExercisesTable() {
        // Create database with version 1 schema
        helper.createDatabase(testDbName, 1).apply {
            // Insert some workout data in version 1
            val values = ContentValues().apply {
                put("exerciseName", "Kreuzheben")
                put("weight", 100.0)
                put("completedReps", 5)
                put("plannedReps", 5)
                put("setNumber", 1)
                put("timestamp", System.currentTimeMillis())
            }
            insert("completed_sets", SQLiteDatabase.CONFLICT_REPLACE, values)
            close()
        }

        // Run the migration to version 2
        val db = helper.runMigrationsAndValidate(
            testDbName, 
            2, 
            true, 
            WorkoutDatabase.MIGRATION_1_2
        )

        // Verify the exercises table was created
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='exercises'"
        )
        assertTrue("Exercises table should exist", cursor.moveToFirst())
        cursor.close()

        // Verify the exercises table has the correct columns
        val columnCursor = db.query("PRAGMA table_info(exercises)")
        val columns = mutableListOf<String>()
        while (columnCursor.moveToNext()) {
            columns.add(columnCursor.getString(columnCursor.getColumnIndex("name")))
        }
        columnCursor.close()

        assertTrue("Should have id column", columns.contains("id"))
        assertTrue("Should have name column", columns.contains("name"))
        assertTrue("Should have nameResId column", columns.contains("nameResId"))
        assertTrue("Should have sortOrder column", columns.contains("sortOrder"))
        assertTrue("Should have createdAt column", columns.contains("createdAt"))

        // Verify existing workout data is preserved
        val workoutCursor = db.query("SELECT * FROM completed_sets")
        assertTrue("Workout data should be preserved", workoutCursor.moveToFirst())
        assertEquals("Kreuzheben", workoutCursor.getString(workoutCursor.getColumnIndex("exerciseName")))
        workoutCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_exercisesTableIsEmpty() {
        // Create database with version 1 schema
        helper.createDatabase(testDbName, 1).apply {
            close()
        }

        // Run the migration to version 2
        val db = helper.runMigrationsAndValidate(
            testDbName,
            2,
            true,
            WorkoutDatabase.MIGRATION_1_2
        )

        // Verify the exercises table is empty (predefined exercises are added at runtime)
        val cursor = db.query("SELECT COUNT(*) FROM exercises")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0))
        cursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun allMigrations() {
        // Create database with version 1 schema
        helper.createDatabase(testDbName, 1).apply {
            close()
        }

        // Open the database with Room to verify all migrations work
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            testDbName
        ).addMigrations(
            WorkoutDatabase.MIGRATION_1_2
        ).build()

        // Trigger database opening
        db.openHelper.writableDatabase
        
        db.close()
    }
}
