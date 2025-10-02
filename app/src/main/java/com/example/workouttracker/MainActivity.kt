package com.example.workouttracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.ui.history.WorkoutHistoryActivity
import com.example.workouttracker.ui.workout.WorkoutInputActivity

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var setsTextView: TextView
    private lateinit var startWorkoutButton: Button
    private lateinit var historyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initializeDisplays()
        setupClickListeners()
    }

    private fun initViews() {
        timerTextView = findViewById(R.id.timerTextView)
        setsTextView = findViewById(R.id.setsTextView)
        startWorkoutButton = findViewById(R.id.startWorkoutButton)
        historyButton = findViewById(R.id.historyButton)
    }

    private fun initializeDisplays() {
        timerTextView.text = "00:00"
        setsTextView.text = "0/0"
    }

    private fun setupClickListeners() {
        startWorkoutButton.setOnClickListener {
            val intent = Intent(this, WorkoutInputActivity::class.java)
            startActivity(intent)
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, WorkoutHistoryActivity::class.java)
            startActivity(intent)
        }
    }
}