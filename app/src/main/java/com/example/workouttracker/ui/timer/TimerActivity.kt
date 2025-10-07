package com.example.workouttracker.ui.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.ui.settings.SettingsActivity
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
    private var isTimerRunning: Boolean = false
    
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
        
        // Restore state or initialize fresh
        if (savedInstanceState != null) {
            currentSet = savedInstanceState.getInt(KEY_CURRENT_SET, 1)
            timeLeftInMillis = savedInstanceState.getLong(KEY_TIME_LEFT, pauseTimeSeconds * 1000L)
            isTimerRunning = savedInstanceState.getBoolean(KEY_TIMER_RUNNING, false)
        } else {
            currentSet = 1
            timeLeftInMillis = pauseTimeSeconds * 1000L
            isTimerRunning = true // Auto-start for first set
        }
        
        // Initialize display
        exerciseNameTextView.text = exerciseName.uppercase()
        weightTextView.text = String.format(getString(R.string.weight_format), weight)
        updateSetsDisplay()
        updateTimer()
        
        // Start timer if it was running
        if (isTimerRunning) {
            startTimer()
            setButtonDisabled()
        } else {
            setButtonEnabled()
        }

        doneButton.setOnClickListener {
            markSetAsCompleted()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_SET, currentSet)
        outState.putLong(KEY_TIME_LEFT, timeLeftInMillis)
        outState.putBoolean(KEY_TIMER_RUNNING, isTimerRunning)
    }

    private fun startTimer() {
        isTimerRunning = true
        setButtonDisabled()
        
        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                isTimerRunning = false
                timerTextView.text = "00:00"
                setButtonEnabled()
                triggerVibration()
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
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_400))
    }
    
    private fun setButtonEnabled() {
        doneButton.isEnabled = true
        doneButton.text = getString(R.string.set_done)
        doneButton.alpha = 1.0f
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_900))
    }

    private fun markSetAsCompleted() {
        // Log completed set to database
        logCompletedSet()
        
        if (currentSet >= totalSets) {
            finishWorkout()
        } else {
            currentSet++
            updateSetsDisplay()
            timeLeftInMillis = pauseTimeSeconds * 1000L // Reset timer for next set
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
            try {
                database.completedSetDao().insert(completedSet)
            } catch (e: Exception) {
                android.util.Log.e("TimerActivity", "Error logging completed set", e)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    androidx.appcompat.app.AlertDialog.Builder(this@TimerActivity)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_save_workout)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
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
    
    private fun triggerVibration() {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val vibrationEnabled = prefs.getBoolean(SettingsActivity.PREF_VIBRATION_ENABLED, true)
        
        if (!vibrationEnabled) return
        
        val duration = prefs.getInt(SettingsActivity.PREF_VIBRATION_DURATION, 500).toLong()
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    companion object {
        private const val KEY_CURRENT_SET = "current_set"
        private const val KEY_TIME_LEFT = "time_left"
        private const val KEY_TIMER_RUNNING = "timer_running"
    }
}