package com.example.workouttracker.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.R
import com.example.workouttracker.ui.timer.TimerActivity

class TrackingActivity : AppCompatActivity() {

    private lateinit var currentSetTextView: TextView
    private lateinit var repetitionsTextView: TextView
    private lateinit var nextSetButton: Button
    private var currentSet: Int = 1
    private var totalRepetitions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        currentSetTextView = findViewById(R.id.currentSetTextView)
        repetitionsTextView = findViewById(R.id.repetitionsTextView)
        nextSetButton = findViewById(R.id.nextSetButton)

        // Initialize with the first set
        updateUI()

        nextSetButton.setOnClickListener {
            // Logic to track the next set
            currentSet++
            totalRepetitions += 10 // Assuming 10 repetitions per set for simplicity
            updateUI()
        }
    }

    private fun updateUI() {
        currentSetTextView.text = "Set: $currentSet"
        repetitionsTextView.text = "Total Repetitions: $totalRepetitions"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, TimerActivity::class.java))
        finish()
    }
}