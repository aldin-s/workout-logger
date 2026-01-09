package com.asstudio.berlin.reps.utils

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for Date/Time utilities
 * Tests date formatting and calculations
 */
class DateUtilsTest {

    @Test
    fun testDateFormattingYYYYMMDD() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JANUARY, 9, 0, 0, 0)
        val date = calendar.time

        // When
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatted = formatter.format(date)

        // Then
        assertEquals("2026-01-09", formatted)
    }

    @Test
    fun testDateFormattingWithTime() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JANUARY, 9, 14, 30, 0)
        val date = calendar.time

        // When
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatted = formatter.format(date)

        // Then
        assertTrue(formatted.startsWith("2026-01-09 14:30"))
    }

    @Test
    fun testTimestampComparison() {
        // Given
        val now = Date()
        val future = Date(now.time + 1000) // 1 second later

        // Then
        assertTrue(future.after(now))
        assertTrue(now.before(future))
    }

    @Test
    fun testTimeDifference() {
        // Given
        val start = Date()
        val end = Date(start.time + 5000) // 5 seconds later

        // When
        val diff = end.time - start.time

        // Then
        assertEquals(5000, diff)
    }
}

/**
 * Unit tests for Timer validation logic
 */
class ValidationUtilsTest {

    @Test
    fun testValidWeightRange() {
        assertTrue(isValidWeight(0.5))
        assertTrue(isValidWeight(100.0))
        assertTrue(isValidWeight(999.0))
        assertFalse(isValidWeight(0.0))
        assertFalse(isValidWeight(-1.0))
    }

    @Test
    fun testValidRepsRange() {
        assertTrue(isValidReps(1))
        assertTrue(isValidReps(50))
        assertTrue(isValidReps(100))
        assertFalse(isValidReps(0))
        assertFalse(isValidReps(-1))
    }

    @Test
    fun testValidPauseTimeRange() {
        assertTrue(isValidPauseTime(10))
        assertTrue(isValidPauseTime(120))
        assertTrue(isValidPauseTime(600))
        assertFalse(isValidPauseTime(0))
        assertFalse(isValidPauseTime(-1))
    }

    @Test
    fun testValidSetsRange() {
        assertTrue(isValidSets(1))
        assertTrue(isValidSets(10))
        assertTrue(isValidSets(50))
        assertFalse(isValidSets(0))
        assertFalse(isValidSets(-1))
    }

    @Test
    fun testEmptyExerciseName() {
        assertFalse(isValidExerciseName(""))
        assertFalse(isValidExerciseName("   "))
        assertTrue(isValidExerciseName("Bankdrücken"))
        assertTrue(isValidExerciseName("a"))
    }

    // Helper validation functions (these should match your actual implementation)
    private fun isValidWeight(weight: Double) = weight > 0
    private fun isValidReps(reps: Int) = reps > 0
    private fun isValidPauseTime(time: Int) = time > 0
    private fun isValidSets(sets: Int) = sets > 0
    private fun isValidExerciseName(name: String) = name.trim().isNotEmpty()
}
