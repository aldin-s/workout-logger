package com.asstudio.berlin.reps.service

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import com.asstudio.berlin.reps.service.TimerService
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Tests for TimerService - Critical component for Android 14+
 * Tests Foreground Service lifecycle, Notification, and Timer functionality
 */
@RunWith(AndroidJUnit4::class)
class TimerServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testServiceStartsSuccessfully() {
        // Given
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, 60)
            putExtra(TimerService.EXTRA_CURRENT_SET, 1)
            putExtra(TimerService.EXTRA_TOTAL_SETS, 5)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, "Test Exercise")
        }

        // When
        val binder = serviceRule.bindService(serviceIntent)

        // Then
        assertNotNull("Service binder should not be null", binder)
        val service = (binder as TimerService.TimerBinder).getService()
        assertNotNull("Service should not be null", service)
    }

    @Test
    fun testServiceBindsCorrectly() {
        // Given
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, 120)
            putExtra(TimerService.EXTRA_CURRENT_SET, 1)
            putExtra(TimerService.EXTRA_TOTAL_SETS, 3)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, "Bankdrücken")
        }

        // When
        val binder = serviceRule.bindService(serviceIntent)
        val service = (binder as TimerService.TimerBinder).getService()

        // Then
        assertTrue("Timer should be running", service.isRunning())
        assertTrue("Time left should be > 0", service.getTimeLeftInMillis() > 0)
    }

    @Test
    fun testTimerCountsDown() {
        // Given
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, 5) // 5 seconds
            putExtra(TimerService.EXTRA_CURRENT_SET, 1)
            putExtra(TimerService.EXTRA_TOTAL_SETS, 1)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, "Quick Test")
        }

        // When
        val binder = serviceRule.bindService(serviceIntent)
        val service = (binder as TimerService.TimerBinder).getService()
        val initialTime = service.getTimeLeftInMillis()
        
        // Wait 2 seconds
        Thread.sleep(2000)
        val afterTime = service.getTimeLeftInMillis()

        // Then
        assertTrue("Timer should count down", afterTime < initialTime)
        assertTrue("Timer should still be running", service.isRunning())
    }

    @Test
    fun testServiceStopsCorrectly() {
        // Given
        val startIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, 60)
            putExtra(TimerService.EXTRA_CURRENT_SET, 1)
            putExtra(TimerService.EXTRA_TOTAL_SETS, 1)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, "Test")
        }
        val binder = serviceRule.bindService(startIntent)
        val service = (binder as TimerService.TimerBinder).getService()

        // When
        service.stopTimer()

        // Then
        assertFalse("Timer should be stopped", service.isRunning())
    }

    @Test
    fun testResetTimerFunctionality() {
        // Given
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_PAUSE_TIME, 60)
            putExtra(TimerService.EXTRA_CURRENT_SET, 1)
            putExtra(TimerService.EXTRA_TOTAL_SETS, 5)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, "Test")
        }
        val binder = serviceRule.bindService(serviceIntent)
        val service = (binder as TimerService.TimerBinder).getService()

        // Wait for timer to count down
        Thread.sleep(2000)

        // When - Reset for next set
        service.resetTimer(60, 2)

        // Then
        assertTrue("Timer should be running after reset", service.isRunning())
        assertTrue("Time should be reset", service.getTimeLeftInMillis() > 50000)
    }
}
