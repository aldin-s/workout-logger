package com.example.workouttracker.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

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

/**
 * Locale manager using AndroidX Per-App Language API.
 * Works on Android 13+ natively, AndroidX handles backwards compatibility.
 * 
 * This is the modern, Google-recommended approach since AndroidX AppCompat 1.6.0+
 * No Activity recreation needed - AppCompatDelegate handles it automatically.
 */
object LocaleManager {
    
    /**
     * Set app language. Takes effect immediately without app restart.
     * @param languageTag BCP 47 language tag (e.g., "de", "en")
     */
    fun setLocale(languageTag: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    /**
     * Get currently set app language.
     * @return BCP 47 language tag or empty string if system default
     */
    fun getLocale(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "" else locales.toLanguageTags()
    }
    
    /**
     * Reset to system default language.
     */
    fun resetToSystemDefault() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }
}
