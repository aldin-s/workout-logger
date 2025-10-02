package com.example.workouttracker.ui.workout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.R
import com.example.workouttracker.ui.timer.TimerActivity

class WorkoutInputActivity : AppCompatActivity() {
    
    private lateinit var exerciseNameEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var repsEditText: EditText
    private lateinit var pauseTimeEditText: EditText
    private lateinit var setsEditText: EditText
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_input)

        initViews()
        setupClickListeners()
        setDefaultValues()
    }

    private fun initViews() {
        exerciseNameEditText = findViewById(R.id.exerciseNameEditText)
        weightEditText = findViewById(R.id.weightEditText)
        repsEditText = findViewById(R.id.repsEditText)
        pauseTimeEditText = findViewById(R.id.pauseTimeEditText)
        setsEditText = findViewById(R.id.setsEditText)
        startButton = findViewById(R.id.startButton)
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            if (validateInput()) {
                startWorkout()
            }
        }
    }

    private fun setDefaultValues() {
        pauseTimeEditText.setText("60") // Default 60 seconds pause
        setsEditText.setText("3") // Default 3 sets
    }

    private fun validateInput(): Boolean {
        val exerciseName = exerciseNameEditText.text.toString().trim()
        val weightText = weightEditText.text.toString().trim()
        val repsText = repsEditText.text.toString().trim()
        val pauseTimeText = pauseTimeEditText.text.toString().trim()
        val setsText = setsEditText.text.toString().trim()

        when {
            exerciseName.isEmpty() -> {
                exerciseNameEditText.error = "Übung eingeben"
                return false
            }
            weightText.isEmpty() -> {
                weightEditText.error = "Gewicht eingeben"
                return false
            }
            repsText.isEmpty() -> {
                repsEditText.error = "Wiederholungen eingeben"
                return false
            }
            pauseTimeText.isEmpty() -> {
                pauseTimeEditText.error = "Pausenzeit eingeben"
                return false
            }
            setsText.isEmpty() -> {
                setsEditText.error = "Anzahl Sätze eingeben"
                return false
            }
        }

        return true
    }

    private fun startWorkout() {
        val exerciseName = exerciseNameEditText.text.toString().trim()
        val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
        val reps = repsEditText.text.toString().toIntOrNull() ?: 0
        val pauseTime = pauseTimeEditText.text.toString().toIntOrNull() ?: 60
        val totalSets = setsEditText.text.toString().toIntOrNull() ?: 1

        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("EXERCISE_NAME", exerciseName)
            putExtra("WEIGHT", weight)
            putExtra("REPS", reps)
            putExtra("PAUSE_TIME", pauseTime)
            putExtra("TOTAL_SETS", totalSets)
        }
        
        startActivity(intent)
        finish()
    }
}