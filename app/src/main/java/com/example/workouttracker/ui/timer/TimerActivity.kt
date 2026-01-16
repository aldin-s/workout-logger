package com.example.workouttracker.ui.timer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.workouttracker.R
import com.example.workouttracker.service.TimerService
import com.example.workouttracker.ui.settings.SettingsViewModel
import com.example.workouttracker.ui.theme.RepsTheme
import com.example.workouttracker.ui.tracking.TrackingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerActivity : AppCompatActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result - service will work regardless, but notification may not show
        if (!isGranted) {
            android.util.Log.w("TimerActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        requestNotificationPermissionIfNeeded()

        // Apply keep screen on setting
        applyKeepScreenOnSetting()

        // Get workout data from intent
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: ""
        val weight = intent.getDoubleExtra("WEIGHT", 0.0)
        val plannedReps = intent.getIntExtra("REPS", 0)
        val pauseTimeSeconds = intent.getIntExtra("PAUSE_TIME", 60)
        val totalSets = intent.getIntExtra("TOTAL_SETS", 1)

        setContent {
            val timerViewModel: TimerViewModel = hiltViewModel()
            val state by timerViewModel.state.collectAsState()
            
            // State für Cancel-Dialog
            var showCancelDialog by remember { mutableStateOf(false) }
            
            // Back-Handler mit Bestätigungsdialog
            BackHandler {
                showCancelDialog = true
            }

            // Bind to service when composable is active
            DisposableEffect(Unit) {
                timerViewModel.bindService(this@TimerActivity)
                onDispose {
                    timerViewModel.unbindService(this@TimerActivity)
                }
            }

            // Initialize ViewModel with intent data
            LaunchedEffect(Unit) {
                timerViewModel.initialize(
                    exerciseName = exerciseName,
                    weight = weight,
                    plannedReps = plannedReps,
                    pauseTimeSeconds = pauseTimeSeconds,
                    totalSets = totalSets
                )
            }

            // Navigate to TrackingActivity when workout is complete
            LaunchedEffect(state.isWorkoutCompleted) {
                if (state.isWorkoutCompleted) {
                    val intent = Intent(this@TimerActivity, TrackingActivity::class.java).apply {
                        putExtra("SETS_COMPLETED", state.currentSet)
                        putExtra("EXERCISE_NAME", state.exerciseName)
                        putExtra("WEIGHT", state.weight)
                    }
                    startActivity(intent)
                    finish()
                }
            }

            RepsTheme(darkTheme = true, dynamicColor = false) {
                TimerScreen(
                    state = state,
                    onSetCompleted = timerViewModel::markSetAsCompleted
                )
                
                // Cancel Workout Dialog
                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { showCancelDialog = false },
                        title = { Text(stringResource(R.string.cancel_workout_title)) },
                        text = { Text(stringResource(R.string.cancel_workout_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showCancelDialog = false
                                timerViewModel.stopAndCleanup()
                                finish()
                            }) {
                                Text(stringResource(R.string.yes))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelDialog = false }) {
                                Text(stringResource(R.string.no))
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun applyKeepScreenOnSetting() {
        val prefs = getSharedPreferences(SettingsViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        val keepScreenOn = prefs.getBoolean(SettingsViewModel.PREF_KEEP_SCREEN_ON, true)

        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't stop the service here - let it run in background
        // Service will be stopped when workout completes or user explicitly cancels
    }
}