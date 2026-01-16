package com.example.workouttracker.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.example.workouttracker.ui.theme.RepsTheme
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_displaysTimerAndSets() {
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    timerDisplay = "02:30",
                    setsDisplay = "3/5",
                    onStartWorkout = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("02:30").assertIsDisplayed()
        composeTestRule.onNodeWithText("3/5").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysDefaultValues() {
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/0").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysHeader() {
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("WORKOUT TRACKER").assertIsDisplayed()
    }

    @Test
    fun mainScreen_startWorkoutButtonClick_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = { clicked = true },
                    onNavigateToHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Workout starten").performClick()
        assert(clicked) { "onStartWorkout callback was not triggered" }
    }

    @Test
    fun mainScreen_historyButtonClick_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = {},
                    onNavigateToHistory = { clicked = true },
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Verlauf").performClick()
        assert(clicked) { "onNavigateToHistory callback was not triggered" }
    }

    @Test
    fun mainScreen_settingsButtonClick_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Einstellungen").performClick()
        assert(clicked) { "onNavigateToSettings callback was not triggered" }
    }

    @Test
    fun mainScreen_timerLongPress_triggersDebugMenu() {
        var debugMenuRequested = false
        
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    onStartWorkout = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = {},
                    onDebugMenuRequest = { debugMenuRequested = true }
                )
            }
        }

        composeTestRule.onNodeWithText("00:00").performTouchInput { longClick() }
        assert(debugMenuRequested) { "onDebugMenuRequest callback was not triggered" }
    }
}
