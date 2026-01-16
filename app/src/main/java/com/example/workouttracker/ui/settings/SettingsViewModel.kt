package com.example.workouttracker.ui.settings

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.BuildConfig
import com.example.workouttracker.R
import com.example.workouttracker.data.database.CompletedSetDao
import com.example.workouttracker.data.export.ExportService
import com.example.workouttracker.data.export.ImportService
import com.example.workouttracker.data.export.ImportSummary
import com.example.workouttracker.data.export.UnsupportedSchemaVersionException
import com.example.workouttracker.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsState(
    val vibrationEnabled: Boolean = true,
    val vibrationDuration: Int = 500,
    val soundEnabled: Boolean = false,
    val soundUri: String? = null,
    val soundName: String = "",
    val keepScreenOn: Boolean = true,
    // Workout defaults
    val defaultPauseTime: Int = 120,
    val defaultSets: Int = 5,
    val defaultReps: Int = 8,
    val language: String = "de",
    val appVersion: String = "",
    
    // Operation states
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isDeleting: Boolean = false,
    val exportResult: ExportResult? = null,
    val importResult: ImportResult? = null,
    val deleteResult: DeleteResult? = null
)

sealed class ExportResult {
    data class Success(val intent: Intent) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val summary: ImportSummary) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

sealed class DeleteResult {
    object Success : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}

enum class VibrationDuration(val ms: Int, val labelResId: Int) {
    SHORT(200, com.example.workouttracker.R.string.vibration_short),
    MEDIUM(500, com.example.workouttracker.R.string.vibration_medium),
    LONG(1000, com.example.workouttracker.R.string.vibration_long);
    
    companion object {
        fun fromMs(ms: Int): VibrationDuration = entries.find { it.ms == ms } ?: MEDIUM
    }
}

enum class PauseTime(val seconds: Int) {
    SEC_30(30),
    SEC_60(60),
    SEC_90(90),
    SEC_120(120),
    SEC_180(180),
    SEC_300(300);
    
    companion object {
        fun fromSeconds(seconds: Int): PauseTime = entries.find { it.seconds == seconds } ?: SEC_120
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferences,
    private val completedSetDao: CompletedSetDao,
    private val exerciseRepository: ExerciseRepository,
    private val exportService: ExportService,
    private val importService: ImportService
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val soundUriString = prefs.getString(PREF_SOUND_URI, null)
        val soundName = getSoundNameFromUri(soundUriString)
        
        _state.update { current ->
            current.copy(
                vibrationEnabled = prefs.getBoolean(PREF_VIBRATION_ENABLED, true),
                vibrationDuration = prefs.getInt(PREF_VIBRATION_DURATION, 500),
                soundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, false),
                soundUri = soundUriString,
                soundName = soundName,
                keepScreenOn = prefs.getBoolean(PREF_KEEP_SCREEN_ON, true),
                defaultPauseTime = prefs.getInt(PREF_DEFAULT_PAUSE_TIME, 120),
                defaultSets = prefs.getInt(PREF_DEFAULT_SETS, 5),
                defaultReps = prefs.getInt(PREF_DEFAULT_REPS, 8),
                language = prefs.getString(PREF_LANGUAGE, "de") ?: "de",
                appVersion = BuildConfig.VERSION_NAME
            )
        }
    }
    
    private fun getSoundNameFromUri(uriString: String?): String {
        if (uriString == null) {
            return application.getString(R.string.sound_default)
        }
        return try {
            val uri = Uri.parse(uriString)
            val ringtone = android.media.RingtoneManager.getRingtone(application, uri)
            ringtone?.getTitle(application) ?: application.getString(R.string.sound_default)
        } catch (e: Exception) {
            application.getString(R.string.sound_default)
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_VIBRATION_ENABLED, enabled).apply()
        _state.update { it.copy(vibrationEnabled = enabled) }
    }
    
    fun setVibrationDuration(duration: VibrationDuration) {
        prefs.edit().putInt(PREF_VIBRATION_DURATION, duration.ms).apply()
        _state.update { it.copy(vibrationDuration = duration.ms) }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_SOUND_ENABLED, enabled).apply()
        _state.update { it.copy(soundEnabled = enabled) }
    }
    
    fun setSoundUri(uri: Uri?) {
        prefs.edit().putString(PREF_SOUND_URI, uri?.toString()).apply()
        val soundName = getSoundNameFromUri(uri?.toString())
        _state.update { it.copy(soundUri = uri?.toString(), soundName = soundName) }
    }
    
    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEEP_SCREEN_ON, enabled).apply()
        _state.update { it.copy(keepScreenOn = enabled) }
    }
    
    fun setDefaultPauseTime(seconds: Int) {
        prefs.edit().putInt(PREF_DEFAULT_PAUSE_TIME, seconds).apply()
        _state.update { it.copy(defaultPauseTime = seconds) }
    }
    
    fun setDefaultSets(sets: Int) {
        prefs.edit().putInt(PREF_DEFAULT_SETS, sets).apply()
        _state.update { it.copy(defaultSets = sets) }
    }
    
    fun setDefaultReps(reps: Int) {
        prefs.edit().putInt(PREF_DEFAULT_REPS, reps).apply()
        _state.update { it.copy(defaultReps = reps) }
    }
    
    fun setLanguage(language: String) {
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
        _state.update { it.copy(language = language) }
        
        // Apply locale change immediately using Per-App Language API
        com.example.workouttracker.utils.LocaleManager.setLocale(language)
    }
    
    fun exportToCsv() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResult = null) }
            
            val result = withContext(Dispatchers.IO) {
                exportService.exportToCsv()
            }
            
            result.fold(
                onSuccess = { intent ->
                    _state.update { it.copy(isExporting = false, exportResult = ExportResult.Success(intent)) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isExporting = false, exportResult = ExportResult.Error(error.message ?: "Export failed")) }
                }
            )
        }
    }
    
    fun exportToJson() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResult = null) }
            
            val result = withContext(Dispatchers.IO) {
                exportService.exportToJson()
            }
            
            result.fold(
                onSuccess = { intent ->
                    _state.update { it.copy(isExporting = false, exportResult = ExportResult.Success(intent)) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isExporting = false, exportResult = ExportResult.Error(error.message ?: "Export failed")) }
                }
            )
        }
    }
    
    fun importFromJson(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, importResult = null) }
            
            val result = withContext(Dispatchers.IO) {
                importService.importFromJson(uri)
            }
            
            result.fold(
                onSuccess = { summary ->
                    _state.update { it.copy(isImporting = false, importResult = ImportResult.Success(summary)) }
                },
                onFailure = { error ->
                    val message = when (error) {
                        is UnsupportedSchemaVersionException -> 
                            application.getString(R.string.import_error_schema_version, error.foundVersion)
                        else -> error.message ?: "Import failed"
                    }
                    _state.update { it.copy(isImporting = false, importResult = ImportResult.Error(message)) }
                }
            )
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, deleteResult = null) }
            
            try {
                withContext(Dispatchers.IO) {
                    completedSetDao.deleteAll()
                    exerciseRepository.deleteAllAndReinitialize()
                    prefs.edit().clear().apply()
                }
                
                // Reload default settings after clearing
                loadSettings()
                _state.update { it.copy(isDeleting = false, deleteResult = DeleteResult.Success) }
            } catch (e: Exception) {
                _state.update { it.copy(isDeleting = false, deleteResult = DeleteResult.Error(e.message ?: "Delete failed")) }
            }
        }
    }
    
    fun clearExportResult() {
        _state.update { it.copy(exportResult = null) }
    }
    
    fun clearImportResult() {
        _state.update { it.copy(importResult = null) }
    }
    
    fun clearDeleteResult() {
        _state.update { it.copy(deleteResult = null) }
    }
    
    companion object {
        const val PREFS_NAME = "reps_settings"
        const val PREF_VIBRATION_ENABLED = "vibration_enabled"
        const val PREF_VIBRATION_DURATION = "vibration_duration"
        const val PREF_SOUND_ENABLED = "sound_enabled"
        const val PREF_SOUND_URI = "sound_uri"
        const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
        const val PREF_DEFAULT_PAUSE_TIME = "default_pause_time"
        const val PREF_DEFAULT_SETS = "default_sets"
        const val PREF_DEFAULT_REPS = "default_reps"
        const val PREF_LANGUAGE = "language"
    }
}
