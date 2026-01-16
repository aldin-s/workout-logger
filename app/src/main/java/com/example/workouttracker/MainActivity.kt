package com.example.workouttracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.ui.history.WorkoutHistoryActivity
import com.example.workouttracker.ui.main.MainScreen
import com.example.workouttracker.ui.settings.SettingsActivity
import com.example.workouttracker.ui.theme.RepsTheme
import com.example.workouttracker.ui.workout.WorkoutInputActivity
import com.example.workouttracker.utils.TestDataGenerator

class MainActivity : ComponentActivity() {

    private lateinit var database: WorkoutDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = WorkoutDatabase.getDatabase(this)
        
        setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                MainScreen(
                    timerDisplay = "00:00",
                    setsDisplay = "0/0",
                    onStartWorkout = {
                        startActivity(Intent(this, WorkoutInputActivity::class.java))
                    },
                    onNavigateToHistory = {
                        startActivity(Intent(this, WorkoutHistoryActivity::class.java))
                    },
                    onNavigateToSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onDebugMenuRequest = {
                        showDebugMenu()
                    }
                )
            }
        }
    }
    
    private fun showDebugMenu() {
        AlertDialog.Builder(this)
            .setTitle("Debug Menu")
            .setItems(arrayOf("Generate Test Data", "Clear All Data")) { _, which ->
                when (which) {
                    0 -> {
                        TestDataGenerator.generateTestData(database)
                        Toast.makeText(this, "Test data generated! Check History.", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        TestDataGenerator.clearAllData(database)
                        Toast.makeText(this, "All data cleared!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}