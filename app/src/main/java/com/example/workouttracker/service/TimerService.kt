package com.example.workouttracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.workouttracker.R
import com.example.workouttracker.ui.settings.SettingsActivity
import com.example.workouttracker.ui.timer.TimerActivity

class TimerService : Service() {

    private val binder = TimerBinder()
    private var countdownTimer: CountDownTimer? = null
    private var currentRingtone: android.media.Ringtone? = null
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
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }
    
    private fun startForegroundService() {
        val notification = createNotification(timeLeftInMillis)
        startForeground(NOTIFICATION_ID, notification)
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
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(timeLeft: Long): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val seconds = (timeLeft / 1000).toInt()
        val timeText = if (timeLeft == 0L && !isTimerRunning) {
            "Bereit"
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
                
                // Update notification
                val notification = createNotification(timeLeftInMillis)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(NOTIFICATION_ID, notification)
            }

            override fun onFinish() {
                isTimerRunning = false
                timeLeftInMillis = 0
                timerUpdateListener?.onTimerFinish()
                triggerVibration()
                triggerSound()
                
                // Update notification one last time
                val notification = createNotification(0)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(NOTIFICATION_ID, notification)
            }
        }.start()
    }
    
    fun stopTimer() {
        countdownTimer?.cancel()
        isTimerRunning = false
        stopSound()
    }
    
    fun resetTimer(newPauseTime: Int, set: Int) {
        stopTimer()
        pauseTimeSeconds = newPauseTime
        currentSet = set
        timeLeftInMillis = pauseTimeSeconds * 1000L
        startTimer()
    }
    
    fun prepareNextSet(set: Int) {
        stopTimer()
        currentSet = set
        timeLeftInMillis = pauseTimeSeconds * 1000L
        // Do NOT start timer - wait for user to press button
    }
    
    fun getTimeLeftInMillis(): Long = timeLeftInMillis
    
    fun isRunning(): Boolean = isTimerRunning
    
    private fun triggerVibration() {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(SettingsActivity.PREF_VIBRATION_ENABLED, true)
        
        if (!isEnabled) return
        
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
    
    private fun triggerSound() {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(SettingsActivity.PREF_SOUND_ENABLED, true)
        
        if (!isEnabled) return
        
        try {
            stopSound()
            
            val uriString = prefs.getString(SettingsActivity.PREF_SOUND_URI, null)
            val soundUri = if (uriString != null) {
                Uri.parse(uriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            currentRingtone = RingtoneManager.getRingtone(applicationContext, soundUri)
            currentRingtone?.play()
        } catch (e: Exception) {
            android.util.Log.e("TimerService", "Error playing sound", e)
        }
    }
    
    fun stopSound() {
        try {
            currentRingtone?.stop()
            currentRingtone = null
        } catch (e: Exception) {
            android.util.Log.e("TimerService", "Error stopping sound", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
    
    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        
        const val ACTION_START_TIMER = "com.example.workouttracker.ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "com.example.workouttracker.ACTION_STOP_TIMER"
        
        const val EXTRA_PAUSE_TIME = "pause_time"
        const val EXTRA_CURRENT_SET = "current_set"
        const val EXTRA_TOTAL_SETS = "total_sets"
        const val EXTRA_EXERCISE_NAME = "exercise_name"
    }
}
