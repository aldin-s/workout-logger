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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.service.TimerService
import com.asstudio.berlin.reps.ui.settings.SettingsActivity
import com.asstudio.berlin.reps.ui.tracking.TrackingActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Timer-Screen für das Workout.
 * 
 * Diese Activity ist nur für UI-Rendering zuständig.
 * Alle Logik und State-Verwaltung erfolgt im TimerViewModel.
 */
class TimerActivity : AppCompatActivity(), TimerService.TimerUpdateListener {

    // ViewModel - Single Source of Truth für den UI-Zustand
    private val viewModel: TimerViewModel by viewModels()
    
    // UI-Elemente
    private lateinit var timerTextView: TextView
    private lateinit var setsTextView: TextView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var doneButton: MaterialButton
    
    // Service-Verbindung
    private var timerService: TimerService? = null
    private var serviceBound = false
    
    // Flag um zu erkennen, ob Activity durch Rotation zerstört wird
    private var isConfigurationChange = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            timerService?.setTimerUpdateListener(this@TimerActivity)
            serviceBound = true
            
            // Zustand vom Service an ViewModel übergeben
            timerService?.let { svc ->
                val timeLeft = svc.getTimeLeftInMillis()
                val isRunning = svc.isRunning()
                viewModel.restoreState(timeLeft, isRunning)
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
    ) { _ ->
        // Start service regardless of permission result
        startTimerServiceInternal()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        
        applyKeepScreenOnSetting()
        initializeViews()
        
        // ViewModel initialisieren (nur beim ersten Start)
        if (savedInstanceState == null) {
            initializeViewModel()
            checkNotificationPermissionAndStartService()
        }
        
        bindTimerService()
        setupClickListeners()
        observeUiState()
    }
    
    private fun initializeViews() {
        timerTextView = findViewById(R.id.timerTextView)
        setsTextView = findViewById(R.id.setsTextView)
        exerciseNameTextView = findViewById(R.id.exerciseNameTextView)
        weightTextView = findViewById(R.id.weightTextView)
        doneButton = findViewById(R.id.doneButton)
    }
    
    private fun initializeViewModel() {
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: ""
        val weight = intent.getDoubleExtra("WEIGHT", 0.0)
        val plannedReps = intent.getIntExtra("REPS", 0)
        val pauseTimeSeconds = intent.getIntExtra("PAUSE_TIME", 60)
        val totalSets = intent.getIntExtra("TOTAL_SETS", 1)
        
        viewModel.initializeWorkout(
            exerciseName = exerciseName,
            weight = weight,
            plannedReps = plannedReps,
            pauseTimeSeconds = pauseTimeSeconds,
            totalSets = totalSets
        )
    }
    
    private fun setupClickListeners() {
        doneButton.setOnClickListener {
            handleSetCompleted()
        }
        
        // Back-Button mit Bestätigungsdialog
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }
    
    /**
     * Beobachtet den UI-State und aktualisiert die Views entsprechend.
     * Dies ist die einzige Stelle, die UI-Updates durchführt.
     */
    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }
    
    /**
     * Rendert den UI-State in die Views.
     * Klare Trennung: Jeder State führt zu einem definierten UI-Zustand.
     */
    private fun renderState(state: TimerUiState) {
        when (state) {
            is TimerUiState.Loading -> {
                // Warten auf Service-Verbindung
                setButtonDisabled()
            }
            
            is TimerUiState.Running -> {
                exerciseNameTextView.text = state.exerciseName.uppercase()
                weightTextView.text = state.weightFormatted
                timerTextView.text = state.timeLeftFormatted
                setsTextView.text = state.setsFormatted
                setButtonDisabled()
            }
            
            is TimerUiState.WaitingForSetComplete -> {
                exerciseNameTextView.text = state.exerciseName.uppercase()
                weightTextView.text = state.weightFormatted
                timerTextView.text = "00:00"
                setsTextView.text = state.setsFormatted
                setButtonEnabled()
            }
            
            is TimerUiState.WorkoutCompleted -> {
                navigateToTrackingScreen(state)
            }
            
            is TimerUiState.Error -> {
                showServiceErrorDialog(state.message)
            }
        }
    }
    
    private fun handleSetCompleted() {
        // Sound stoppen
        timerService?.stopSound()
        
        // ViewModel aktualisieren und Ergebnis verarbeiten
        when (val result = viewModel.onSetCompleted()) {
            is TimerViewModel.SetCompletedResult.NextSet -> {
                // Neuen Timer im Service starten
                timerService?.resetTimer(result.pauseTimeSeconds, result.setNumber)
            }
            is TimerViewModel.SetCompletedResult.WorkoutFinished -> {
                // Wird durch State-Observation behandelt (navigateToTrackingScreen)
            }
        }
    }
    
    // ============ Service-Kommunikation ============
    
    override fun onTimerTick(timeLeftInMillis: Long) {
        runOnUiThread {
            viewModel.onTimerTick(timeLeftInMillis)
        }
    }
    
    override fun onTimerFinish() {
        runOnUiThread {
            viewModel.onTimerFinished()
        }
    }
    
    // ============ Service Lifecycle ============
    
    private fun checkNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startTimerServiceInternal()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startTimerServiceInternal()
        }
    }
    
    private fun startTimerServiceInternal() {
        try {
            val serviceIntent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_START_TIMER
                putExtra(TimerService.EXTRA_PAUSE_TIME, viewModel.getPauseTimeSeconds())
                putExtra(TimerService.EXTRA_CURRENT_SET, viewModel.getCurrentSet())
                putExtra(TimerService.EXTRA_TOTAL_SETS, viewModel.getTotalSets())
                putExtra(TimerService.EXTRA_EXERCISE_NAME, viewModel.getExerciseName())
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("TimerActivity", "Failed to start service", e)
            viewModel.onServiceError("Timer-Service konnte nicht gestartet werden")
        }
    }
    
    private fun bindTimerService() {
        val bindIntent = Intent(this, TimerService::class.java)
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isConfigurationChange = true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            timerService?.setTimerUpdateListener(null)
            unbindService(serviceConnection)
            serviceBound = false
        }
        
        // Service NUR stoppen wenn Activity wirklich beendet wird
        if (!isConfigurationChange && !isChangingConfigurations) {
            stopTimerService()
        }
    }
    
    // ============ UI Helpers ============
    
    private fun setButtonDisabled() {
        doneButton.isEnabled = false
        doneButton.text = getString(R.string.pause_running)
        doneButton.alpha = 0.5f
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.gray_400)
        )
    }
    
    private fun setButtonEnabled() {
        doneButton.isEnabled = true
        doneButton.text = getString(R.string.set_done)
        doneButton.alpha = 1.0f
        doneButton.strokeColor = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.gray_900)
        )
    }
    
    private fun navigateToTrackingScreen(state: TimerUiState.WorkoutCompleted) {
        // WICHTIG: Service explizit stoppen damit Notification verschwindet
        stopTimerService()
        
        val intent = Intent(this, TrackingActivity::class.java).apply {
            putExtra("SETS_COMPLETED", state.totalSetsCompleted)
            putExtra("EXERCISE_NAME", state.exerciseName)
            putExtra("WEIGHT", state.weight)
        }
        startActivity(intent)
        finish()
    }
    
    /**
     * Stoppt den Timer-Service und entfernt die Notification.
     */
    private fun stopTimerService() {
        val serviceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        startService(serviceIntent)
    }
    
    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.cancel_workout_title)
            .setMessage(R.string.cancel_workout_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                stopTimerService()
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun showServiceErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Service-Fehler")
            .setMessage("$message\n\nBitte erlaube:\n• Benachrichtigungen\n• Hintergrund-Aktivitäten")
            .setPositiveButton("Einstellungen") { _, _ ->
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Einstellungen nicht verfügbar", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen") { _, _ -> finish() }
            .setCancelable(false)
            .show()
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
}
