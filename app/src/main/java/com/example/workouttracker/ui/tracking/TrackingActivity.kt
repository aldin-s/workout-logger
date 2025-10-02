package com.example.workouttracker.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.MainActivity
import com.example.workouttracker.R
import com.example.workouttracker.ui.history.WorkoutHistoryActivity

class TrackingActivity : AppCompatActivity() {

    private lateinit var completionTitleTextView: TextView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var setsCompletedTextView: TextView
    private lateinit var backToMainButton: Button
    private lateinit var viewHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        completionTitleTextView = findViewById(R.id.completionTitleTextView)
        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        weightTextView = findViewById(R.id.weightTextView)
        setsCompletedTextView = findViewById(R.id.setsCompletedTextView)
        backToMainButton = findViewById(R.id.backToMainButton)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)

        // Get data from intent
        val setsCompleted = intent.getIntExtra("SETS_COMPLETED", 0)
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "Übung"
        val weight = intent.getDoubleExtra("WEIGHT", 0.0)

        // Update UI with completion data
        exerciseNameTextView.text = exerciseName.uppercase()
        weightTextView.text = String.format(getString(R.string.weight_format), weight)
        setsCompletedTextView.text = resources.getQuantityString(R.plurals.sets_completed, setsCompleted, setsCompleted)

        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        viewHistoryButton.setOnClickListener {
            val intent = Intent(this, WorkoutHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}