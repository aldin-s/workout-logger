package com.asstudio.berlin.reps.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asstudio.berlin.reps.data.model.CompletedSet
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Instrumented test for Room Database operations
 * Tests Insert, Query, Update, Delete operations on CompletedSet
 */
@RunWith(AndroidJUnit4::class)
class CompletedSetDaoTest {

    private lateinit var database: WorkoutDatabase
    private lateinit var dao: CompletedSetDao

    @Before
    fun setup() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutDatabase::class.java
        ).allowMainThreadQueries().build()
        
        dao = database.completedSetDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveCompletedSet() = runBlocking {
        // Given
        val completedSet = CompletedSet(
            id = 0,
            exerciseName = "Bankdrücken",
            weight = 100.0,
            plannedReps = 5,
            completedReps = 5,
            setNumber = 1,
            timestamp = Date()
        )

        // When
        dao.insert(completedSet)
        val allSets = dao.getAllSets()

        // Then
        assertEquals(1, allSets.size)
        assertEquals("Bankdrücken", allSets[0].exerciseName)
        assertEquals(100.0, allSets[0].weight, 0.01)
    }

    @Test
    fun insertMultipleSetsAndRetrieve() = runBlocking {
        // Given
        val sets = listOf(
            CompletedSet(0, "Kreuzheben", 120.0, 5, 5, 1, Date()),
            CompletedSet(0, "Kreuzheben", 120.0, 5, 5, 2, Date()),
            CompletedSet(0, "Kreuzheben", 120.0, 5, 5, 3, Date())
        )

        // When
        sets.forEach { dao.insert(it) }
        val allSets = dao.getAllSets()

        // Then
        assertEquals(3, allSets.size)
        assertTrue(allSets.all { it.exerciseName == "Kreuzheben" })
    }

    @Test
    fun deleteAllSets() = runBlocking {
        // Given
        dao.insert(CompletedSet(0, "Test", 50.0, 10, 10, 1, Date()))
        dao.insert(CompletedSet(0, "Test2", 60.0, 8, 8, 1, Date()))

        // When
        dao.deleteAll()
        val allSets = dao.getAllSets()

        // Then
        assertEquals(0, allSets.size)
    }

    @Test
    fun updateCompletedSet() = runBlocking {
        // Given
        val set = CompletedSet(0, "Rudern", 80.0, 8, 8, 1, Date())
        dao.insert(set)
        val insertedSet = dao.getAllSets()[0]

        // When
        val updatedSet = insertedSet.copy(completedReps = 10)
        dao.update(updatedSet)
        val result = dao.getAllSets()[0]

        // Then
        assertEquals(10, result.completedReps)
        assertEquals(8, result.plannedReps) // Should remain unchanged
    }

    @Test
    fun deleteSpecificSet() = runBlocking {
        // Given
        val set1 = CompletedSet(0, "Exercise1", 50.0, 10, 10, 1, Date())
        val set2 = CompletedSet(0, "Exercise2", 60.0, 8, 8, 1, Date())
        dao.insert(set1)
        dao.insert(set2)

        // When
        val setToDelete = dao.getAllSets()[0]
        dao.delete(setToDelete)
        val remaining = dao.getAllSets()

        // Then
        assertEquals(1, remaining.size)
    }

    @Test
    fun testMinimalValues() = runBlocking {
        // Test edge case: minimum values
        val minSet = CompletedSet(
            id = 0,
            exerciseName = "Test",
            weight = 0.5,
            plannedReps = 1,
            completedReps = 1,
            setNumber = 1,
            timestamp = Date()
        )

        dao.insert(minSet)
        val result = dao.getAllSets()[0]

        assertEquals(0.5, result.weight, 0.01)
        assertEquals(1, result.completedReps)
    }

    @Test
    fun testMaximalValues() = runBlocking {
        // Test edge case: maximum values
        val maxSet = CompletedSet(
            id = 0,
            exerciseName = "Heavy Deadlift",
            weight = 999.0,
            plannedReps = 100,
            completedReps = 100,
            setNumber = 50,
            timestamp = Date()
        )

        dao.insert(maxSet)
        val result = dao.getAllSets()[0]

        assertEquals(999.0, result.weight, 0.01)
        assertEquals(100, result.completedReps)
        assertEquals(50, result.setNumber)
    }
}
