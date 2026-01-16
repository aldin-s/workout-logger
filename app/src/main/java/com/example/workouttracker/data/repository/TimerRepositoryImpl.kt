package com.example.workouttracker.data.repository

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.workouttracker.service.TimerService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementierung des TimerRepository.
 * Kapselt die Service-Bindung und Kommunikation.
 */
@Singleton
class TimerRepositoryImpl @Inject constructor(
    private val application: Application
) : TimerRepository {
    
    companion object {
        private const val TAG = "TimerRepository"
    }
    
    private var timerService: TimerService? = null
    private var serviceBound = false
    private var serviceConnection: ServiceConnection? = null
    
    override fun startTimer(
        exerciseName: String,
        pauseTimeSeconds: Int,
        currentSet: Int,
        totalSets: Int
    ): Result<Unit> {
        return try {
            TimerService.startTimer(
                context = application,
                exerciseName = exerciseName,
                pauseTimeSeconds = pauseTimeSeconds,
                currentSet = currentSet,
                totalSets = totalSets
            )
            Result.success(Unit)
        } catch (e: SecurityException) {
            Log.e(TAG, "Foreground Service blocked by system", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start timer service", e)
            Result.failure(e)
        }
    }
    
    override fun stopTimer() {
        try {
            TimerService.stopTimer(application)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop timer service", e)
        }
    }
    
    override fun nextSet(setNumber: Int) {
        try {
            TimerService.nextSet(application, setNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger next set", e)
        }
    }
    
    override fun observeTimerUpdates(): Flow<TimerServiceUpdate> = callbackFlow {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? TimerService.TimerBinder
                timerService = binder?.getService()
                serviceBound = true
                
                timerService?.setTimerUpdateListener(object : TimerService.TimerUpdateListener {
                    override fun onTimerTick(timeLeftInMillis: Long) {
                        trySend(TimerServiceUpdate.TimerTick(timeLeftInMillis))
                    }
                    
                    override fun onTimerFinish() {
                        trySend(TimerServiceUpdate.TimerComplete)
                    }
                })
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                timerService?.setTimerUpdateListener(null)
                timerService = null
                serviceBound = false
            }
        }
        
        serviceConnection = connection
        
        try {
            val intent = android.content.Intent(application, TimerService::class.java)
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to timer service", e)
            send(TimerServiceUpdate.Error(e.message ?: "Service binding failed"))
        }
        
        awaitClose {
            if (serviceBound) {
                timerService?.setTimerUpdateListener(null)
                try {
                    application.unbindService(connection)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to unbind service", e)
                }
            }
            serviceConnection = null
            serviceBound = false
        }
    }
    
    override fun isBackgroundModeAvailable(): Boolean {
        // Auf den meisten Geräten ist der Foreground Service verfügbar
        // Auf Xiaomi/MIUI kann er durch Battery Saver blockiert werden
        return true // Optimistisch, Error Handling beim Start
    }
}
