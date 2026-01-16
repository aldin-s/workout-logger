package com.example.workouttracker.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    private var settingsViewModelRef: SettingsViewModel? = null
    
    private val soundPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            settingsViewModelRef?.setSoundUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            settingsViewModelRef = settingsViewModel
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
                            val summary = result.summary
                            val message = if (summary.exercisesImported > 0) {
                                getString(
                                    R.string.import_success_with_exercises,
                                    summary.workoutsImported,
                                    summary.exercisesImported,
                                    summary.workoutsSkipped
                                )
                            } else {
                                getString(
                                    R.string.import_success,
                                    summary.workoutsImported,
                                    summary.workoutsSkipped
                                )
                            }
                            Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
                            settingsViewModel.clearImportResult()
                        }
                        is ImportResult.Error -> {
                            Toast.makeText(this@SettingsActivity, result.message, Toast.LENGTH_LONG).show()
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
                    onSoundSelectionClick = { openSoundPicker(state.soundUri) },
                    onKeepScreenOnChange = settingsViewModel::setKeepScreenOn,
                    onPauseTimeChange = settingsViewModel::setDefaultPauseTime,
                    onDefaultSetsChange = settingsViewModel::setDefaultSets,
                    onDefaultRepsChange = settingsViewModel::setDefaultReps,
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
    
    private fun openSoundPicker(currentSoundUri: String?) {
        val existingUri = if (currentSoundUri != null) {
            Uri.parse(currentSoundUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.sound_picker_title))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        }
        soundPickerLauncher.launch(intent)
    }
}
