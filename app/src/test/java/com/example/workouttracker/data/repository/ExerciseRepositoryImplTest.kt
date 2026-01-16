package com.example.workouttracker.data.repository

import android.content.Context
import com.example.workouttracker.R
import com.example.workouttracker.data.database.ExerciseDao
import com.example.workouttracker.data.model.Exercise
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExerciseRepositoryImplTest {

    @MockK
    private lateinit var dao: ExerciseDao

    @MockK
    private lateinit var context: Context

    private lateinit var repository: ExerciseRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock string resources
        every { context.getString(R.string.error_exercise_name_empty) } returns "Name cannot be empty"
        every { context.getString(R.string.error_exercise_exists) } returns "Exercise already exists"
        every { context.getString(R.string.exercise_deadlift) } returns "Kreuzheben"
        every { context.getString(R.string.exercise_bench_press) } returns "Bankdrücken"
        every { context.getString(R.string.exercise_rowing) } returns "Rudern"
        every { context.getString(R.string.exercise_squat) } returns "Kniebeuge"
        
        repository = ExerciseRepositoryImpl(dao, context)
    }

    @Test
    fun `getAllExercises returns flow from dao`() = runTest {
        val exercises = listOf(
            Exercise(id = "1", name = "Test", sortOrder = 0)
        )
        every { dao.getAll() } returns flowOf(exercises)

        repository.getAllExercises()

        // Verify dao.getAll() was called
        verify { dao.getAll() }
    }

    @Test
    fun `initializePredefined inserts default exercises when count is 0`() = runTest {
        coEvery { dao.count() } returns 0
        coEvery { dao.insertAll(any()) } just Runs

        repository.initializePredefined()

        coVerify { dao.insertAll(match { it.size == 4 }) }
    }

    @Test
    fun `initializePredefined does nothing when exercises exist`() = runTest {
        coEvery { dao.count() } returns 4

        repository.initializePredefined()

        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `addExercise fails with empty name`() = runTest {
        val result = repository.addExercise("   ")

        assertTrue(result.isFailure)
        assertEquals("Name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `addExercise fails when exercise already exists`() = runTest {
        coEvery { dao.existsByName("Test") } returns true

        val result = repository.addExercise("Test")

        assertTrue(result.isFailure)
        assertEquals("Exercise already exists", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `addExercise succeeds with valid name`() = runTest {
        val exercises = listOf(
            Exercise(id = "1", name = "Existing", sortOrder = 0)
        )
        coEvery { dao.existsByName("New Exercise") } returns false
        every { dao.getAll() } returns flowOf(exercises)
        coEvery { dao.insert(any()) } returns 1L

        val result = repository.addExercise("New Exercise")

        assertTrue(result.isSuccess)
        coVerify { 
            dao.insert(match { 
                it.name == "New Exercise" && it.sortOrder == 1 
            }) 
        }
    }

    @Test
    fun `renameExercise fails with empty name`() = runTest {
        val result = repository.renameExercise("1", "   ")

        assertTrue(result.isFailure)
        assertEquals("Name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.updateName(any(), any()) }
    }

    @Test
    fun `renameExercise fails when name already exists for different exercise`() = runTest {
        coEvery { dao.existsByNameExcluding("Existing", "1") } returns true

        val result = repository.renameExercise("1", "Existing")

        assertTrue(result.isFailure)
        assertEquals("Exercise already exists", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.updateName(any(), any()) }
    }

    @Test
    fun `renameExercise succeeds with valid name`() = runTest {
        coEvery { dao.existsByNameExcluding("New Name", "1") } returns false
        coEvery { dao.updateName("1", "New Name") } just Runs

        val result = repository.renameExercise("1", "New Name")

        assertTrue(result.isSuccess)
        coVerify { dao.updateName("1", "New Name") }
    }

    @Test
    fun `deleteExercise calls dao delete`() = runTest {
        coEvery { dao.delete("1") } just Runs

        repository.deleteExercise("1")

        coVerify { dao.delete("1") }
    }

    @Test
    fun `reorderExercises updates sortOrder for all exercises`() = runTest {
        val exercises = listOf(
            Exercise(id = "2", name = "Second", sortOrder = 1),
            Exercise(id = "1", name = "First", sortOrder = 0)
        )
        coEvery { dao.updateAll(any()) } just Runs

        repository.reorderExercises(exercises)

        coVerify { 
            dao.updateAll(match { 
                it[0].id == "2" && it[0].sortOrder == 0 &&
                it[1].id == "1" && it[1].sortOrder == 1 
            }) 
        }
    }

    @Test
    fun `importExercises skips existing exercises`() = runTest {
        val exercises = listOf(
            Exercise(id = "1", name = "Existing", sortOrder = 0),
            Exercise(id = "2", name = "New", sortOrder = 1)
        )
        coEvery { dao.existsByName("Existing") } returns true
        coEvery { dao.existsByName("New") } returns false
        coEvery { dao.insert(any()) } returns 1L

        repository.importExercises(exercises)

        // Only "New" should be inserted
        coVerify(exactly = 1) { dao.insert(match { it.name == "New" }) }
        coVerify(exactly = 0) { dao.insert(match { it.name == "Existing" }) }
    }

    @Test
    fun `deleteAllAndReinitialize deletes all and reinitializes`() = runTest {
        coEvery { dao.deleteAll() } just Runs
        coEvery { dao.count() } returns 0
        coEvery { dao.insertAll(any()) } just Runs

        repository.deleteAllAndReinitialize()

        coVerifyOrder {
            dao.deleteAll()
            dao.count()
            dao.insertAll(any())
        }
    }
}
