package com.example.workouttracker.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests für StatsUiState Sealed Class.
 * Stellt sicher, dass alle States korrekt erstellt werden können.
 */
class StatsUiStateTest {

    @Test
    fun `all state types are distinct`() {
        val loading: StatsUiState = StatsUiState.Loading
        val success: StatsUiState = StatsUiState.Success(0, 0, 0.0, 0, null, null)
        val error: StatsUiState = StatsUiState.Error("test")
        
        assertTrue(loading is StatsUiState.Loading)
        assertTrue(success is StatsUiState.Success)
        assertTrue(error is StatsUiState.Error)
        
        // Type checks - these verify the sealed class hierarchy
        assertFalse(loading is StatsUiState.Success)
        assertFalse(loading is StatsUiState.Error)
    }

    @Test
    fun `Success state data class equality works`() {
        val state1 = StatsUiState.Success(
            totalWorkouts = 5,
            weeklyWorkouts = 2,
            totalWeight = 1000.0,
            totalSets = 20,
            favoriteExercise = "Deadlift",
            lastWorkoutDate = "01.01.2026"
        )
        
        val state2 = StatsUiState.Success(
            totalWorkouts = 5,
            weeklyWorkouts = 2,
            totalWeight = 1000.0,
            totalSets = 20,
            favoriteExercise = "Deadlift",
            lastWorkoutDate = "01.01.2026"
        )
        
        assertEquals(state1, state2)
    }

    @Test
    fun `Success state copy works correctly`() {
        val original = StatsUiState.Success(
            totalWorkouts = 5,
            weeklyWorkouts = 2,
            totalWeight = 1000.0,
            totalSets = 20,
            favoriteExercise = "Deadlift",
            lastWorkoutDate = "01.01.2026"
        )
        
        val modified = original.copy(totalWorkouts = 10)
        
        assertEquals(10, modified.totalWorkouts)
        assertEquals(2, modified.weeklyWorkouts) // Unverändert
    }

    @Test
    fun `Error state preserves message`() {
        val message = "Something went wrong"
        val error = StatsUiState.Error(message)
        
        assertEquals(message, error.message)
    }

    @Test
    fun `Loading is singleton object`() {
        val loading1 = StatsUiState.Loading
        val loading2 = StatsUiState.Loading
        
        assertTrue(loading1 === loading2)
    }
}
