package com.example.workouttracker.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val state by settingsViewModel.state.collectAsState()
            
            RepsTheme(darkTheme = true, dynamicColor = false) {
                // Handle export result
                LaunchedEffect(state.exportResult) {
                    when (val result = state.exportResult) {
                        is ExportResult.Success -> {
                            startActivity(result.intent)
                            settingsViewModel.clearExportResult()
                        }
                        is ExportResult.Error -> {
                            Toast.makeText(this@SettingsActivity, R.string.export_error, Toast.LENGTH_SHORT).show()
                            settingsViewModel.clearExportResult()
                        }
                        null -> { /* no-op */ }
                    }
                }
                
                // Handle import result
                LaunchedEffect(state.importResult) {
                    when (val result = state.importResult) {
                        is ImportResult.Success -> {
                            Toast.makeText(
                                this@SettingsActivity, 
                                getString(R.string.import_success, result.count), 
                                Toast.LENGTH_SHORT
                            ).show()
                            settingsViewModel.clearImportResult()
                        }
                        is ImportResult.Error -> {
                            Toast.makeText(this@SettingsActivity, R.string.import_error, Toast.LENGTH_SHORT).show()
                            settingsViewModel.clearImportResult()
                        }
                        null -> { /* no-op */ }
                    }
                }
                
                // Handle delete result
                LaunchedEffect(state.deleteResult) {
                    when (val result = state.deleteResult) {
                        is DeleteResult.Success -> {
                            Toast.makeText(this@SettingsActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
                            settingsViewModel.clearDeleteResult()
                        }
                        is DeleteResult.Error -> {
                            Toast.makeText(this@SettingsActivity, result.message, Toast.LENGTH_SHORT).show()
                            settingsViewModel.clearDeleteResult()
                        }
                        null -> { /* no-op */ }
                    }
                }
                
                SettingsScreen(
                    state = state,
                    onNavigateBack = { finish() },
                    onVibrationEnabledChange = settingsViewModel::setVibrationEnabled,
                    onVibrationDurationChange = settingsViewModel::setVibrationDuration,
                    onSoundEnabledChange = settingsViewModel::setSoundEnabled,
                    onKeepScreenOnChange = settingsViewModel::setKeepScreenOn,
                    onPauseTimeChange = settingsViewModel::setDefaultPauseTime,
                    onLanguageChange = settingsViewModel::setLanguage,
                    onExportCsv = settingsViewModel::exportToCsv,
                    onExportJson = settingsViewModel::exportToJson,
                    onImportJson = settingsViewModel::importFromJson,
                    onDeleteAllData = settingsViewModel::deleteAllData,
                    onClearExportResult = settingsViewModel::clearExportResult,
                    onClearImportResult = settingsViewModel::clearImportResult,
                    onClearDeleteResult = settingsViewModel::clearDeleteResult,
                    onFinish = { finish() }
                )
            }
        }
    }
}
