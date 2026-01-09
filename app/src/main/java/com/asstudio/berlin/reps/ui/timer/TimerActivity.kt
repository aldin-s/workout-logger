package com.asstudio.berlin.reps.ui.timer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import com.asstudio.berlin.reps.data.model.CompletedSet
import com.asstudio.berlin.reps.service.TimerService
import com.asstudio.berlin.reps.ui.settings.SettingsActivity
import com.asstudio.berlin.reps.ui.tracking.TrackingActivity
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
                val isRunning = service.isRunning()
                
                // Button should be disabled when timer is running OR when we're waiting for first timer to start
                if (isRunning) {
                    setButtonDisabled()
                } else if (currentSet == 1) {
                    // First set: keep button disabled, timer is starting
                    setButtonDisabled()
                } else {
                    // Timer finished, enable button
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
    
    // Permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Start service regardless of permission result
        // Service will work, just without notification on denied
        startTimerServiceInternal()
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
        
        // Disable button initially - will be enabled once service connects and timer state is known
        setButtonDisabled()
        
        // Check notification permission and start service
        checkNotificationPermissionAndStartService()
        bindTimerService()

        doneButton.setOnClickListener {
            markSetAsCompleted()
        }
    }
    
    private fun checkNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires POST_NOTIFICATIONS permission
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted, start service
                    startTimerServiceInternal()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Below Android 13, no runtime permission needed
            startTimerServiceInternal()
        }
    }
    
    private fun startTimerServiceInternal() {
        try {
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
        } catch (e: SecurityException) {
            // Android 14+ security exception
            android.util.Log.e("TimerActivity", "SecurityException starting service", e)
            showServiceErrorDialog()
        } catch (e: IllegalStateException) {
            // Xiaomi/Samsung specific restrictions or background limitations
            android.util.Log.e("TimerActivity", "IllegalStateException starting service", e)
            showServiceErrorDialog()
        } catch (e: Exception) {
            // Catch any other exception (ForegroundServiceStartNotAllowedException on Android 12+)
            android.util.Log.e("TimerActivity", "Failed to start foreground service", e)
            showServiceErrorDialog()
        }
    }
    
    private fun showServiceErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Service-Fehler")
            .setMessage("Timer-Service konnte nicht gestartet werden.\n\nBitte erlaube:\n• Benachrichtigungen\n• Hintergrund-Aktivitäten\n• Autostart\n\nin den App-Einstellungen.")
            .setPositiveButton("Einstellungen") { _, _ ->
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Einstellungen nicht verfügbar", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    @Deprecated("Use checkNotificationPermissionAndStartService instead")
    private fun startTimerService() {
        startTimerServiceInternal()
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
