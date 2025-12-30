package com.example.workouttracker.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

enum class AppTheme(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    AUTO("auto");
    
    companion object {
        fun fromString(value: String): AppTheme {
            return values().find { it.value == value } ?: AUTO
        }
    }
}

object ThemeManager {
    
    private const val PREF_THEME = "app_theme"
    
    fun applyTheme(context: Context, theme: AppTheme) {
        // Save preference
        saveThemePreference(context, theme)
        
        // Apply theme
        when (theme) {
            AppTheme.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            AppTheme.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            AppTheme.AUTO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    
    fun getCurrentTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val themeValue = prefs.getString(PREF_THEME, AppTheme.AUTO.value) ?: AppTheme.AUTO.value
        return AppTheme.fromString(themeValue)
    }
    
    fun isSystemInDarkMode(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
    
    fun getCurrentThemeDisplayName(context: Context): String {
        val theme = getCurrentTheme(context)
        return when (theme) {
            AppTheme.LIGHT -> "Hell"
            AppTheme.DARK -> "Dunkel"
            AppTheme.AUTO -> "Automatisch"
        }
    }
    
    private fun saveThemePreference(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_THEME, theme.value).apply()
    }
    
    fun initializeTheme(context: Context) {
        val savedTheme = getCurrentTheme(context)
        applyTheme(context, savedTheme)
    }
}
