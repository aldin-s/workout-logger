package com.example.workouttracker.ui.tracking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.example.workouttracker.MainActivity
import com.example.workouttracker.ui.history.WorkoutHistoryActivity
import com.example.workouttracker.ui.theme.RepsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        val setsCompleted = intent.getIntExtra("SETS_COMPLETED", 0)
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "Übung"
        val weight = intent.getDoubleExtra("WEIGHT", 0.0)

        setContent {
            RepsTheme(darkTheme = true, dynamicColor = false) {
                TrackingScreen(
                    state = TrackingState(
                        exerciseName = exerciseName,
                        weight = weight,
                        setsCompleted = setsCompleted
                    ),
                    onBackToMain = {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onViewHistory = {
                        val intent = Intent(this, WorkoutHistoryActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}