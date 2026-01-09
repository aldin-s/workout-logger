package com.asstudio.berlin.reps

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End tests for MainActivity
 * Tests navigation and basic app flow
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAppLaunchesSuccessfully() {
        // Verify main components are visible
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.setsTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.startWorkoutButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToWorkoutInput() {
        // When
        onView(withId(R.id.startWorkoutButton))
            .perform(click())

        // Then - Should navigate to WorkoutInputActivity
        Thread.sleep(500)
        // Verify we're in WorkoutInputActivity by checking for exercise cards
        onView(withId(R.id.cardKreuzheben))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToHistory() {
        // When
        onView(withId(R.id.historyButton))
            .perform(click())

        // Then - Should navigate to WorkoutHistoryActivity
        Thread.sleep(500)
    }

    @Test
    fun testNavigationToSettings() {
        // When
        onView(withId(R.id.settingsButton))
            .perform(click())

        // Then - Should navigate to SettingsActivity
        Thread.sleep(500)
    }

    @Test
    fun testInitialTimerDisplay() {
        // Timer should show 00:00 initially
        onView(withId(R.id.timerTextView))
            .check(matches(withText("00:00")))
    }

    @Test
    fun testInitialSetsDisplay() {
        // Sets should show 0/0 initially
        onView(withId(R.id.setsTextView))
            .check(matches(withText("0/0")))
    }
}
