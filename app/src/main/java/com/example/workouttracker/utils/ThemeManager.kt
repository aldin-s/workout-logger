package com.example.workouttracker.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/**
 * Theme manager that enforces dark mode only.
 * The app uses a brutalist dark theme optimized for OLED displays.
 */
object ThemeManager {
    
    /**
     * Apply dark theme. Call this in Application.onCreate() or Activity.onCreate()
     */
    fun applyDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    fun isSystemInDarkMode(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
