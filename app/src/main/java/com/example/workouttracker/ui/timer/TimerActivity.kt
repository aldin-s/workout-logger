package com.example.workouttracker.ui.timer

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.ui.tracking.TrackingActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TimerActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var setsTextView: TextView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var doneButton: MaterialButton
    private var countdownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var currentSet: Int = 1
    
    // Workout data from intent
    private lateinit var exerciseName: String
    private var weight: Double = 0.0
    private var plannedReps: Int = 0
    private var pauseTimeSeconds: Int = 60
    private var totalSets: Int = 0
    
    private lateinit var database: WorkoutDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerTextView = findViewById(R.id.timerTextView)
        setsTextView = findViewById(R.id.setsTextView)
        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        weightTextView = findViewById(R.id.weightTextView)
        doneButton = findViewById(R.id.doneButton)

        // Get workout data from intent
        exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: ""
        weight = intent.getDoubleExtra("WEIGHT", 0.0)
        plannedReps = intent.getIntExtra("REPS", 0)
        pauseTimeSeconds = intent.getIntExtra("PAUSE_TIME", 60)
        totalSets = intent.getIntExtra("TOTAL_SETS", 1)
        
        database = WorkoutDatabase.getDatabase(this)
        
        // Initialize display
        exerciseNameTextView.text = exerciseName.uppercase()
        weightTextView.text = String.format(getString(R.string.weight_format), weight)
        updateSetsDisplay()
        timerTextView.text = String.format("%02d:%02d", pauseTimeSeconds / 60, pauseTimeSeconds % 60)
        
        // Start timer automatically for first set
        startTimer()
        setButtonDisabled()

        doneButton.setOnClickListener {
            markSetAsCompleted()
        }
    }

    private fun startTimer() {
        timeLeftInMillis = pauseTimeSeconds * 1000L
        setButtonDisabled()
        
        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                setButtonEnabled()
            }
        }.start()
    }

    private fun updateTimer() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        timerTextView.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
    }

    private fun updateSetsDisplay() {
        setsTextView.text = String.format(getString(R.string.set_format), currentSet, totalSets)
    }
    
    private fun setButtonDisabled() {
        doneButton.isEnabled = false
        doneButton.text = getString(R.string.pause_running)
        doneButton.alpha = 0.5f
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.gray_400))
    }
    
    private fun setButtonEnabled() {
        doneButton.isEnabled = true
        doneButton.text = getString(R.string.set_done)
        doneButton.alpha = 1.0f
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.gray_900))
    }

    private fun markSetAsCompleted() {
        // Log completed set to database
        logCompletedSet()
        
        if (currentSet >= totalSets) {
            finishWorkout()
        } else {
            currentSet++
            updateSetsDisplay()
            startTimer() // Start pause timer for next set
        }
    }

    private fun logCompletedSet() {
        val completedSet = CompletedSet(
            exerciseName = exerciseName,
            weight = weight,
            plannedReps = plannedReps,
            completedReps = plannedReps, // User completed planned reps when DONE pressed
            setNumber = currentSet,
            timestamp = Date()
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            database.completedSetDao().insert(completedSet)
        }
    }

    private fun finishWorkout() {
        countdownTimer?.cancel()
        val intent = Intent(this, TrackingActivity::class.java)
        intent.putExtra("SETS_COMPLETED", currentSet)
        intent.putExtra("EXERCISE_NAME", exerciseName)
        intent.putExtra("WEIGHT", weight)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}