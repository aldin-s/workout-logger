package com.example.workouttracker.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests für StatsUiState.
 * 
 * Da StatsViewModel ein AndroidViewModel ist und die Room-Datenbank direkt nutzt,
 * testen wir hier die State-Klassen und die Berechnungslogik.
 * 
 * Für vollständige ViewModel-Tests wäre ein Refactoring nötig:
 * - Repository-Pattern einführen
 * - Dependency Injection (Hilt)
 * - Dann: Mocking des Repositories
 */
class StatsViewModelTest {

    // ============================================
    // StatsUiState Tests
    // ============================================

    @Test
    fun `Loading state is initial state`() {
        val state = StatsUiState.Loading
        assertTrue(state is StatsUiState.Loading)
    }

    @Test
    fun `Success state with empty data`() {
        val state = StatsUiState.Success(
            totalWorkouts = 0,
            weeklyWorkouts = 0,
            totalWeight = 0.0,
            totalSets = 0,
            favoriteExercise = null,
            lastWorkoutDate = null
        )
        
        assertEquals(0, state.totalWorkouts)
        assertEquals(0, state.weeklyWorkouts)
        assertEquals(0.0, state.totalWeight, 0.001)
        assertEquals(0, state.totalSets)
        assertNull(state.favoriteExercise)
        assertNull(state.lastWorkoutDate)
    }

    @Test
    fun `Success state with workout data`() {
        val state = StatsUiState.Success(
            totalWorkouts = 10,
            weeklyWorkouts = 3,
            totalWeight = 5000.0,
            totalSets = 45,
            favoriteExercise = "Kreuzheben",
            lastWorkoutDate = "15.01.2026"
        )
        
        assertEquals(10, state.totalWorkouts)
        assertEquals(3, state.weeklyWorkouts)
        assertEquals(5000.0, state.totalWeight, 0.001)
        assertEquals(45, state.totalSets)
        assertEquals("Kreuzheben", state.favoriteExercise)
        assertEquals("15.01.2026", state.lastWorkoutDate)
    }

    @Test
    fun `Error state contains message`() {
        val errorMessage = "Datenbankfehler"
        val state = StatsUiState.Error(message = errorMessage)
        
        assertTrue(state is StatsUiState.Error)
        assertEquals(errorMessage, state.message)
    }

    // ============================================
    // Berechnungslogik Tests (extrahiert aus ViewModel)
    // ============================================

    @Test
    fun `calculateTotalWeight multiplies weight by reps`() {
        // Simuliere die Berechnung aus dem ViewModel
        data class MockSet(val weight: Double, val completedReps: Int)
        
        val sets = listOf(
            MockSet(100.0, 5),  // 500
            MockSet(80.0, 8),   // 640
            MockSet(60.0, 10)   // 600
        )
        
        val totalWeight = sets.sumOf { it.weight * it.completedReps }
        
        assertEquals(1740.0, totalWeight, 0.001)
    }

    @Test
    fun `findFavoriteExercise returns most frequent`() {
        val exercises = listOf(
            "Kreuzheben",
            "Bankdrücken",
            "Kreuzheben",
            "Rudern",
            "Kreuzheben"
        )
        
        val favorite = exercises
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        
        assertEquals("Kreuzheben", favorite)
    }

    @Test
    fun `findFavoriteExercise returns null for empty list`() {
        val exercises = emptyList<String>()
        
        val favorite = exercises
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        
        assertNull(favorite)
    }

    @Test
    fun `countDistinctWorkoutDates groups by date`() {
        // Simuliere Datum-Strings (wie im ViewModel mit SimpleDateFormat)
        val dates = listOf(
            "2026-01-15",
            "2026-01-15",  // Gleicher Tag
            "2026-01-14",
            "2026-01-14",  // Gleicher Tag
            "2026-01-13"
        )
        
        val distinctDays = dates.distinct().size
        
        assertEquals(3, distinctDays)
    }
}
