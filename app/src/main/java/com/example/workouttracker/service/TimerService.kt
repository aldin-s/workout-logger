package com.example.workouttracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.workouttracker.R
import com.example.workouttracker.ui.settings.SettingsViewModel
import com.example.workouttracker.ui.timer.TimerActivity

/**
 * Foreground Service for reliable workout timer.
 * Ensures timer continues running and vibration/sound triggers even when:
 * - Screen is locked
 * - App is in background
 * - Device is in Doze mode
 * 
 * Uses WakeLock for additional reliability on aggressive OEM skins (Xiaomi/MIUI, Samsung, etc.)
 */
class TimerService : Service() {

    private val binder = TimerBinder()
    private var countdownTimer: CountDownTimer? = null
    private var currentRingtone: android.media.Ringtone? = null
    
    // WakeLock für zuverlässige Timer-Ausführung auf aggressiven OEM-Skins
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Timer state
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning: Boolean = false
    private var pauseTimeSeconds: Int = 60
    private var currentSet: Int = 1
    private var totalSets: Int = 1
    private var exerciseName: String = ""
    
    private var timerUpdateListener: TimerUpdateListener? = null
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    
    interface TimerUpdateListener {
        fun onTimerTick(timeLeftInMillis: Long)
        fun onTimerFinish()
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    /**
     * Erwirbt WakeLock für zuverlässige Timer-Ausführung.
     * PARTIAL_WAKE_LOCK hält die CPU wach, auch wenn der Bildschirm aus ist.
     * Wichtig für Xiaomi/MIUI und andere aggressive Batterie-Optimierungen.
     */
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "REPS::TimerWakeLock"
            ).apply {
                // Timeout als Sicherheit - etwas länger als max Timer-Dauer (5min) * max Sets
                // Wird bei onDestroy manuell freigegeben
                acquire(30 * 60 * 1000L) // 30 Minuten max
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }
    
    /**
     * Gibt WakeLock frei.
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START_TIMER -> {
                    pauseTimeSeconds = intent.getIntExtra(EXTRA_PAUSE_TIME, 60)
                    currentSet = intent.getIntExtra(EXTRA_CURRENT_SET, 1)
                    totalSets = intent.getIntExtra(EXTRA_TOTAL_SETS, 1)
                    exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: ""
                    timeLeftInMillis = pauseTimeSeconds * 1000L
                    startForegroundService()
                    startTimer()
                }
                ACTION_STOP_TIMER -> {
                    stopTimer()
                    stopForegroundCompat()
                    stopSelf()
                }
                ACTION_NEXT_SET -> {
                    val newSet = intent.getIntExtra(EXTRA_CURRENT_SET, currentSet + 1)
                    prepareNextSet(newSet)
                    startTimer()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }
    
    private fun startForegroundService() {
        try {
            val notification = createNotification(timeLeftInMillis)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ requires explicit foreground service type
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException starting foreground", e)
            throw e
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error starting foreground service", e)
            throw e
        }
    }
    
    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.timer_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.timer_notification_channel_description)
                setShowBadge(false)
            }
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(timeLeft: Long): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val seconds = (timeLeft / 1000).toInt()
        val timeText = if (timeLeft == 0L && !isTimerRunning) {
            getString(R.string.timer_ready)
        } else {
            String.format("%02d:%02d", seconds / 60, seconds % 60)
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(exerciseName)
            .setContentText(getString(R.string.timer_notification_text, timeText, currentSet, totalSets))
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    fun setTimerUpdateListener(listener: TimerUpdateListener?) {
        timerUpdateListener = listener
    }
    
    fun startTimer() {
        if (isTimerRunning) return
        
        isTimerRunning = true
        
        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                timerUpdateListener?.onTimerTick(timeLeftInMillis)
                updateNotification()
            }

            override fun onFinish() {
                isTimerRunning = false
                timeLeftInMillis = 0
                timerUpdateListener?.onTimerFinish()
                triggerVibration()
                triggerSound()
                updateNotification()
            }
        }.start()
    }
    
    fun stopTimer() {
        countdownTimer?.cancel()
        isTimerRunning = false
        stopSound()
    }
    
    fun prepareNextSet(set: Int) {
        stopTimer()
        currentSet = set
        timeLeftInMillis = pauseTimeSeconds * 1000L
    }
    
    private fun updateNotification() {
        val notification = createNotification(timeLeftInMillis)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    fun getTimeLeftInMillis(): Long = timeLeftInMillis
    fun isRunning(): Boolean = isTimerRunning
    fun getCurrentSet(): Int = currentSet
    
    private fun triggerVibration() {
        val prefs = getSharedPreferences(SettingsViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(SettingsViewModel.PREF_VIBRATION_ENABLED, true)
        
        if (!isEnabled) return
        
        val duration = prefs.getInt(SettingsViewModel.PREF_VIBRATION_DURATION, 500).toLong()
        
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
    
    private fun triggerSound() {
        val prefs = getSharedPreferences(SettingsViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(SettingsViewModel.PREF_SOUND_ENABLED, false)
        
        if (!isEnabled) return
        
        try {
            stopSound()
            
            // Use user-selected sound or default
            val savedSoundUri = prefs.getString(SettingsViewModel.PREF_SOUND_URI, null)
            val notificationUri = if (savedSoundUri != null) {
                android.net.Uri.parse(savedSoundUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            currentRingtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
            currentRingtone?.play()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error playing sound", e)
        }
    }
    
    private fun stopSound() {
        try {
            currentRingtone?.stop()
            currentRingtone = null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error stopping sound", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopSound()
        releaseWakeLock()
    }
    
    companion object {
        private const val TAG = "TimerService"
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_TIMER = "com.example.workouttracker.START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.workouttracker.STOP_TIMER"
        const val ACTION_NEXT_SET = "com.example.workouttracker.NEXT_SET"
        
        const val EXTRA_PAUSE_TIME = "pause_time"
        const val EXTRA_CURRENT_SET = "current_set"
        const val EXTRA_TOTAL_SETS = "total_sets"
        const val EXTRA_EXERCISE_NAME = "exercise_name"
        
        fun startTimer(
            context: Context,
            exerciseName: String,
            pauseTimeSeconds: Int,
            currentSet: Int,
            totalSets: Int
        ) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
                putExtra(EXTRA_PAUSE_TIME, pauseTimeSeconds)
                putExtra(EXTRA_CURRENT_SET, currentSet)
                putExtra(EXTRA_TOTAL_SETS, totalSets)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            context.startService(intent)
        }
        
        fun nextSet(context: Context, newSet: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_NEXT_SET
                putExtra(EXTRA_CURRENT_SET, newSet)
            }
            context.startService(intent)
        }
    }
}
