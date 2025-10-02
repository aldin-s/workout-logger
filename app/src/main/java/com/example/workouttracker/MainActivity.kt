package com.example.workouttracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.ui.history.WorkoutHistoryActivity
import com.example.workouttracker.ui.workout.WorkoutInputActivity
import com.example.workouttracker.utils.TestDataGenerator

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var setsTextView: TextView
    private lateinit var startWorkoutButton: Button
    private lateinit var historyButton: Button
    private lateinit var database: WorkoutDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = WorkoutDatabase.getDatabase(this)
        
        initViews()
        initializeDisplays()
        setupClickListeners()
        setupDebugMenu()
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
    
    private fun setupDebugMenu() {
        // Long-click on timer display to show debug menu
        timerTextView.setOnLongClickListener {
            showDebugMenu()
            true
        }
    }
    
    private fun showDebugMenu() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Debug Menu")
        builder.setItems(arrayOf("Generate Test Data", "Clear All Data")) { _, which ->
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
        builder.show()
    }
}