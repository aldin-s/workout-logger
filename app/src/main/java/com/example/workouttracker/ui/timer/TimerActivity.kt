package com.example.workouttracker.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.service.TimerService
import com.example.workouttracker.ui.settings.SettingsActivity
import com.example.workouttracker.ui.tracking.TrackingActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TimerActivity : AppCompatActivity(), TimerService.TimerUpdateListener {

    private lateinit var timerTextView: TextView
    private lateinit var setsTextView: TextView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var doneButton: MaterialButton
    
    private var timerService: TimerService? = null
    private var serviceBound = false
    
    private var currentSet: Int = 1
    
    // Workout data from intent
    private lateinit var exerciseName: String
    private var weight: Double = 0.0
    private var plannedReps: Int = 0
    private var pauseTimeSeconds: Int = 60
    private var totalSets: Int = 0
    
    private lateinit var database: WorkoutDatabase
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            timerService?.setTimerUpdateListener(this@TimerActivity)
            serviceBound = true
            
            // Update UI with current timer state
            timerService?.let { service ->
                updateTimer(service.getTimeLeftInMillis())
                if (service.isRunning()) {
                    setButtonDisabled()
                } else {
                    setButtonEnabled()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService?.setTimerUpdateListener(null)
            timerService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        
        // Apply keep screen on setting
        applyKeepScreenOnSetting()

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
        } else {
            currentSet = 1
        }
        
        // Initialize display
        exerciseNameTextView.text = exerciseName.uppercase()
        weightTextView.text = String.format(getString(R.string.weight_format), weight)
        updateSetsDisplay()
        
        // Start and bind to service
        startTimerService()
        bindTimerService()

        doneButton.setOnClickListener {
            markSetAsCompleted()
        }
    }
    
    private fun startTimerService() {
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, pauseTimeSeconds)
            putExtra(TimerService.EXTRA_CURRENT_SET, currentSet)
            putExtra(TimerService.EXTRA_TOTAL_SETS, totalSets)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, exerciseName)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    private fun bindTimerService() {
        val bindIntent = Intent(this, TimerService::class.java)
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_SET, currentSet)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            timerService?.setTimerUpdateListener(null)
            unbindService(serviceConnection)
            serviceBound = false
        }
        
        // Stop service when activity is destroyed
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        startService(serviceIntent)
    }
    
    override fun onTimerTick(timeLeftInMillis: Long) {
        runOnUiThread {
            updateTimer(timeLeftInMillis)
        }
    }
    
    override fun onTimerFinish() {
        runOnUiThread {
            timerTextView.text = "00:00"
            setButtonEnabled()
        }
    }

    private fun updateTimer(timeLeft: Long) {
        val seconds = (timeLeft / 1000).toInt()
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
        // Stop any playing sound from service
        timerService?.stopSound()
        
        // Log completed set to database
        logCompletedSet()
        
        if (currentSet >= totalSets) {
            finishWorkout()
        } else {
            currentSet++
            updateSetsDisplay()
            // Start new timer for next set via service
            timerService?.resetTimer(pauseTimeSeconds, currentSet)
            setButtonDisabled()
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
        val intent = Intent(this, TrackingActivity::class.java)
        intent.putExtra("SETS_COMPLETED", currentSet)
        intent.putExtra("EXERCISE_NAME", exerciseName)
        intent.putExtra("WEIGHT", weight)
        startActivity(intent)
        finish()
    }
    
    private fun applyKeepScreenOnSetting() {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val keepScreenOn = prefs.getBoolean(SettingsActivity.PREF_KEEP_SCREEN_ON, false)
        
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    companion object {
        private const val KEY_CURRENT_SET = "current_set"
    }
}
