package com.example.workouttracker.utils

import android.os.CountDownTimer
import java.util.concurrent.TimeUnit

object TimerUtils {
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0

    fun startTimer(durationInMillis: Long, onTick: (Long) -> Unit, onFinish: () -> Unit) {
        timeLeftInMillis = durationInMillis
        timer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                onTick(millisUntilFinished)
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    fun stopTimer() {
        timer?.cancel()
    }

    fun getTimeLeft(): Long {
        return timeLeftInMillis
    }

    fun formatTime(millis: Long): String {
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
    }
}