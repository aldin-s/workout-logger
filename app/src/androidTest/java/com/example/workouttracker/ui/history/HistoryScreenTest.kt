package com.example.workouttracker.ui.history

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
import java.util.Date

class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createSampleSession(
        exerciseName: String = "Kreuzheben",
        weight: Double = 100.0,
        reps: Int = 5,
        totalSets: Int = 3
    ) = WorkoutSession(
        exerciseName = exerciseName,
        weight = weight,
        reps = reps,
        totalSets = totalSets,
        startTime = Date(),
        endTime = Date(),
        sets = emptyList()
    )

    @Test
    fun historyScreen_emptyState_displaysEmptyMessage() {
        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = emptyList(),
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Noch keine Workouts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Starte dein erstes Training").assertIsDisplayed()
    }

    @Test
    fun historyScreen_withData_displaysDateHeader() {
        val items = listOf(
            HistoryItem.DateHeader("HEUTE"),
            HistoryItem.WorkoutItem(createSampleSession())
        )

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("HEUTE").assertIsDisplayed()
    }

    @Test
    fun historyScreen_withData_displaysExerciseName() {
        val items = listOf(
            HistoryItem.DateHeader("HEUTE"),
            HistoryItem.WorkoutItem(createSampleSession(exerciseName = "Bankdrücken"))
        )

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("BANKDRÜCKEN").assertIsDisplayed()
    }

    @Test
    fun historyScreen_withData_displaysWorkoutDetails() {
        val items = listOf(
            HistoryItem.DateHeader("HEUTE"),
            HistoryItem.WorkoutItem(createSampleSession(weight = 80.0, reps = 8, totalSets = 4))
        )

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("80,0 kg × 8 Wdh · 4 Sätze").assertIsDisplayed()
    }

    @Test
    fun historyScreen_backButton_triggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = emptyList(),
                    onNavigateBack = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Zurück").performClick()
        assert(backClicked) { "onNavigateBack callback was not triggered" }
    }

    @Test
    fun historyScreen_longClickOnItem_triggersCallback() {
        var longClickedSession: WorkoutSession? = null
        val session = createSampleSession(exerciseName = "Kniebeugen")
        val items = listOf(
            HistoryItem.DateHeader("HEUTE"),
            HistoryItem.WorkoutItem(session)
        )

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = {},
                    onSessionLongClick = { longClickedSession = it }
                )
            }
        }

        composeTestRule.onNodeWithText("KNIEBEUGEN").performTouchInput { longClick() }
        assert(longClickedSession == session) { "onSessionLongClick callback was not triggered with correct session" }
    }

    @Test
    fun historyScreen_multipleItems_displaysAll() {
        val items = listOf(
            HistoryItem.DateHeader("HEUTE"),
            HistoryItem.WorkoutItem(createSampleSession(exerciseName = "Kreuzheben")),
            HistoryItem.WorkoutItem(createSampleSession(exerciseName = "Bankdrücken")),
            HistoryItem.DateHeader("GESTERN"),
            HistoryItem.WorkoutItem(createSampleSession(exerciseName = "Kniebeugen"))
        )

        composeTestRule.setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("HEUTE").assertIsDisplayed()
        composeTestRule.onNodeWithText("KREUZHEBEN").assertIsDisplayed()
        composeTestRule.onNodeWithText("BANKDRÜCKEN").assertIsDisplayed()
        composeTestRule.onNodeWithText("GESTERN").assertIsDisplayed()
        composeTestRule.onNodeWithText("KNIEBEUGEN").assertIsDisplayed()
    }
}
