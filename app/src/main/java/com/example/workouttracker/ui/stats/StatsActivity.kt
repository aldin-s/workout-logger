package com.example.workouttracker.ui.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.workouttracker.ui.theme.RepsTheme

/**
 * Bridge-Activity für den Stats Screen.
 * Verwendet Jetpack Compose mit RepsTheme.
 * 
 * Diese Activity wird entfernt, sobald die gesamte App
 * auf Navigation Compose migriert ist (Phase 7).
 */
class StatsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            RepsTheme {
                StatsScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
