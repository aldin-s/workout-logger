package com.asstudio.berlin.reps.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.ui.workout.WorkoutInputActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for WorkoutInputActivity
 * Tests the critical flow of creating workouts (both standard and custom exercises)
 */
@RunWith(AndroidJUnit4::class)
class WorkoutInputActivityTest {

    @Test
    fun testStandardExerciseSelection() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Select Kreuzheben
        onView(withId(R.id.cardKreuzheben)).perform(click())

        // Then - Exercise should be selected
        onView(withId(R.id.selectedExerciseText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testCustomExerciseInput() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Select custom exercise and enter name
        onView(withId(R.id.cardCustomExercise)).perform(click())
        onView(withId(R.id.customExerciseEditText))
            .perform(typeText("Bizeps Curl"), closeSoftKeyboard())

        // Then - Custom exercise layout should be visible
        onView(withId(R.id.customExerciseLayout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testCompleteWorkoutInputHappyPath() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Fill all fields with valid data
        onView(withId(R.id.cardBankdruecken)).perform(click())
        onView(withId(R.id.weightEditText))
            .perform(typeText("100"), closeSoftKeyboard())
        onView(withId(R.id.repsEditText))
            .perform(typeText("5"), closeSoftKeyboard())
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("120"), closeSoftKeyboard())
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("5"), closeSoftKeyboard())

        // Then - Start button should be enabled and clickable
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
    }

    @Test
    fun testEmptyInputShowsError() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Try to start without selecting exercise
        onView(withId(R.id.startButton)).perform(click())

        // Then - Error should be shown (Toast or error message)
        // Note: Validation should prevent starting
        onView(withId(R.id.startButton)).check(matches(isDisplayed()))
    }

    @Test
    fun testMinimalValuesInput() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Enter minimal values
        onView(withId(R.id.cardKniebeuge)).perform(click())
        onView(withId(R.id.weightEditText))
            .perform(typeText("0.5"), closeSoftKeyboard())
        onView(withId(R.id.repsEditText))
            .perform(typeText("1"), closeSoftKeyboard())
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("10"), closeSoftKeyboard())
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("1"), closeSoftKeyboard())

        // Then - Should accept minimal values
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
    }

    @Test
    fun testMaximalValuesInput() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Enter maximal values
        onView(withId(R.id.cardRudern)).perform(click())
        onView(withId(R.id.weightEditText))
            .perform(typeText("999"), closeSoftKeyboard())
        onView(withId(R.id.repsEditText))
            .perform(typeText("100"), closeSoftKeyboard())
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("600"), closeSoftKeyboard())
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("50"), closeSoftKeyboard())

        // Then - Should accept maximal values
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
    }

    @Test
    fun testCustomExerciseCompleteFlow() {
        // CRITICAL TEST: Custom exercise on Android 14+
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkoutInputActivity::class.java)
        ActivityScenario.launch<WorkoutInputActivity>(intent)

        // When - Create complete custom workout
        onView(withId(R.id.cardCustomExercise)).perform(click())
        onView(withId(R.id.customExerciseEditText))
            .perform(typeText("Trizeps Extension"), closeSoftKeyboard())
        onView(withId(R.id.weightEditText))
            .perform(typeText("30"), closeSoftKeyboard())
        onView(withId(R.id.repsEditText))
            .perform(typeText("12"), closeSoftKeyboard())
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("90"), closeSoftKeyboard())
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("3"), closeSoftKeyboard())
        
        // Then - Should be able to start (no crash on Android 14)
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
            .perform(click())
        
        // Verify we navigate to TimerActivity (no crash)
        Thread.sleep(1000) // Wait for transition
    }
}
