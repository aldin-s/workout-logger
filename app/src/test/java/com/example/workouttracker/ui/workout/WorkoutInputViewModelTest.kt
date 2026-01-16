package com.example.workouttracker.ui.workout

import android.app.Application
import android.content.SharedPreferences
import com.example.workouttracker.R
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.repository.ExerciseRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutInputViewModelTest {

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var prefs: SharedPreferences

    @MockK
    private lateinit var exerciseRepository: ExerciseRepository

    private lateinit var viewModel: WorkoutInputViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock SharedPreferences
        every { prefs.getInt("default_pause_time", 120) } returns 120

        // Mock exercise repository
        every { exerciseRepository.getAllExercises() } returns flowOf(emptyList())
        coEvery { exerciseRepository.initializePredefined() } just Runs

        // Mock string resources
        every { application.getString(R.string.error_select_exercise) } returns "Please select an exercise"
        every { application.getString(R.string.error_weight_invalid) } returns "Invalid weight"
        every { application.getString(R.string.error_reps_invalid) } returns "Invalid reps"
        every { application.getString(R.string.error_pause_invalid) } returns "Invalid pause time"
        every { application.getString(R.string.error_sets_invalid) } returns "Invalid sets"

        viewModel = WorkoutInputViewModel(application, prefs, exerciseRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default pause time from preferences`() = runTest {
        testScheduler.advanceUntilIdle()
        assertEquals("120", viewModel.state.value.pauseTime)
    }

    @Test
    fun `selectExercise updates state`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)

        viewModel.selectExercise(exercise)

        assertEquals(exercise, viewModel.state.value.selectedExercise)
        assertNull(viewModel.state.value.exerciseError)
    }

    @Test
    fun `setWeight updates state and clears error`() = runTest {
        viewModel.setWeight("100")

        assertEquals("100", viewModel.state.value.weight)
        assertNull(viewModel.state.value.weightError)
    }

    @Test
    fun `setReps updates state and clears error`() = runTest {
        viewModel.setReps("5")

        assertEquals("5", viewModel.state.value.reps)
        assertNull(viewModel.state.value.repsError)
    }

    @Test
    fun `setPauseTime updates state and clears error`() = runTest {
        viewModel.setPauseTime("90")

        assertEquals("90", viewModel.state.value.pauseTime)
        assertNull(viewModel.state.value.pauseTimeError)
    }

    @Test
    fun `setSets updates state and clears error`() = runTest {
        viewModel.setSets("3")

        assertEquals("3", viewModel.state.value.sets)
        assertNull(viewModel.state.value.setsError)
    }

    @Test
    fun `validate returns null when no exercise selected`() = runTest {
        viewModel.setWeight("100")
        viewModel.setReps("5")

        val result = viewModel.validate()

        assertNull(result)
        assertNotNull(viewModel.state.value.exerciseError)
    }

    @Test
    fun `validate returns null when weight is invalid`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.selectExercise(exercise)
        viewModel.setWeight("abc")
        viewModel.setReps("5")

        val result = viewModel.validate()

        assertNull(result)
        assertNotNull(viewModel.state.value.weightError)
    }

    @Test
    fun `validate returns null when reps is zero`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.selectExercise(exercise)
        viewModel.setWeight("100")
        viewModel.setReps("0")

        val result = viewModel.validate()

        assertNull(result)
        assertNotNull(viewModel.state.value.repsError)
    }

    @Test
    fun `validate returns WorkoutData when all fields are valid`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.selectExercise(exercise)
        viewModel.setWeight("100")
        viewModel.setReps("5")
        viewModel.setPauseTime("120")
        viewModel.setSets("3")

        val result = viewModel.validate()

        assertNotNull(result)
        assertEquals("Test", result?.exerciseName)
        assertEquals(100.0, result?.weight)
        assertEquals(5, result?.reps)
        assertEquals(120, result?.pauseTime)
        assertEquals(3, result?.totalSets)
    }

    @Test
    fun `showAddExerciseDialog updates state`() = runTest {
        viewModel.showAddExerciseDialog()

        assertTrue(viewModel.state.value.showAddExerciseDialog)
    }

    @Test
    fun `hideAddExerciseDialog updates state and clears error`() = runTest {
        viewModel.showAddExerciseDialog()
        viewModel.hideAddExerciseDialog()

        assertFalse(viewModel.state.value.showAddExerciseDialog)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `showBottomSheet updates state with exercise`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)

        viewModel.showBottomSheet(exercise)

        assertEquals(exercise, viewModel.state.value.showBottomSheet)
    }

    @Test
    fun `hideBottomSheet clears bottom sheet state`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.showBottomSheet(exercise)
        viewModel.hideBottomSheet()

        assertNull(viewModel.state.value.showBottomSheet)
    }

    @Test
    fun `showRenameDialog sets rename dialog and hides bottom sheet`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.showBottomSheet(exercise)

        viewModel.showRenameDialog(exercise)

        assertEquals(exercise, viewModel.state.value.showRenameDialog)
        assertNull(viewModel.state.value.showBottomSheet)
    }

    @Test
    fun `showDeleteConfirmDialog sets delete dialog and hides bottom sheet`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        viewModel.showBottomSheet(exercise)

        viewModel.showDeleteConfirmDialog(exercise)

        assertEquals(exercise, viewModel.state.value.showDeleteConfirmDialog)
        assertNull(viewModel.state.value.showBottomSheet)
    }

    @Test
    fun `addExercise calls repository and hides dialog on success`() = runTest {
        coEvery { exerciseRepository.addExercise("New") } returns Result.success(Unit)

        viewModel.showAddExerciseDialog()
        viewModel.addExercise("New")
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showAddExerciseDialog)
        coVerify { exerciseRepository.addExercise("New") }
    }

    @Test
    fun `addExercise shows error message on failure`() = runTest {
        coEvery { exerciseRepository.addExercise("Existing") } returns 
            Result.failure(IllegalArgumentException("Already exists"))

        viewModel.showAddExerciseDialog()
        viewModel.addExercise("Existing")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showAddExerciseDialog)
        assertEquals("Already exists", viewModel.state.value.errorMessage)
    }

    @Test
    fun `deleteExercise clears selection if deleted exercise was selected`() = runTest {
        val exercise = Exercise(id = "1", name = "Test", sortOrder = 0)
        coEvery { exerciseRepository.deleteExercise("1") } just Runs

        viewModel.selectExercise(exercise)
        viewModel.deleteExercise("1")
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.selectedExercise)
        coVerify { exerciseRepository.deleteExercise("1") }
    }

    @Test
    fun `reorderExercises calls repository`() = runTest {
        val exercises = listOf(
            Exercise(id = "1", name = "A", sortOrder = 0),
            Exercise(id = "2", name = "B", sortOrder = 1)
        )
        coEvery { exerciseRepository.reorderExercises(exercises) } just Runs

        viewModel.reorderExercises(exercises)
        testScheduler.advanceUntilIdle()

        coVerify { exerciseRepository.reorderExercises(exercises) }
    }
}
