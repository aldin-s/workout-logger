package com.example.workouttracker.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.BuildConfig
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.data.model.CompletedSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class SettingsState(
    val vibrationEnabled: Boolean = true,
    val vibrationDuration: Int = 500,
    val soundEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val defaultPauseTime: Int = 120,
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
    data class Success(val count: Int) : ImportResult()
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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val database = WorkoutDatabase.getDatabase(application)
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _state.update { current ->
            current.copy(
                vibrationEnabled = prefs.getBoolean(PREF_VIBRATION_ENABLED, true),
                vibrationDuration = prefs.getInt(PREF_VIBRATION_DURATION, 500),
                soundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, false),
                keepScreenOn = prefs.getBoolean(PREF_KEEP_SCREEN_ON, false),
                defaultPauseTime = prefs.getInt(PREF_DEFAULT_PAUSE_TIME, 120),
                language = prefs.getString(PREF_LANGUAGE, "de") ?: "de",
                appVersion = BuildConfig.VERSION_NAME
            )
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
    
    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEEP_SCREEN_ON, enabled).apply()
        _state.update { it.copy(keepScreenOn = enabled) }
    }
    
    fun setDefaultPauseTime(pauseTime: PauseTime) {
        prefs.edit().putInt(PREF_DEFAULT_PAUSE_TIME, pauseTime.seconds).apply()
        _state.update { it.copy(defaultPauseTime = pauseTime.seconds) }
    }
    
    fun setLanguage(language: String) {
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
        _state.update { it.copy(language = language) }
    }
    
    fun exportToCsv() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResult = null) }
            
            try {
                val sets = withContext(Dispatchers.IO) {
                    database.completedSetDao().getAllSets()
                }
                val csv = generateCsv(sets)
                val fileName = "reps_export_${getCurrentDate()}.csv"
                val file = File(getApplication<Application>().cacheDir, fileName)
                
                withContext(Dispatchers.IO) {
                    file.writeText(csv)
                }
                
                val intent = createShareIntent(file, "text/csv")
                _state.update { it.copy(isExporting = false, exportResult = ExportResult.Success(intent)) }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, exportResult = ExportResult.Error(e.message ?: "Export failed")) }
            }
        }
    }
    
    fun exportToJson() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResult = null) }
            
            try {
                val sets = withContext(Dispatchers.IO) {
                    database.completedSetDao().getAllSets()
                }
                val json = generateJson(sets)
                val fileName = "reps_export_${getCurrentDate()}.json"
                val file = File(getApplication<Application>().cacheDir, fileName)
                
                withContext(Dispatchers.IO) {
                    file.writeText(json)
                }
                
                val intent = createShareIntent(file, "application/json")
                _state.update { it.copy(isExporting = false, exportResult = ExportResult.Success(intent)) }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, exportResult = ExportResult.Error(e.message ?: "Export failed")) }
            }
        }
    }
    
    fun importFromJson(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, importResult = null) }
            
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        ?: throw Exception("Could not read file")
                }
                
                val json = JSONObject(jsonString)
                val workoutsArray = json.getJSONArray("workouts")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                var importedCount = 0
                
                withContext(Dispatchers.IO) {
                    for (i in 0 until workoutsArray.length()) {
                        val setJson = workoutsArray.getJSONObject(i)
                        val set = CompletedSet(
                            exerciseName = setJson.getString("exerciseName"),
                            weight = setJson.getDouble("weight"),
                            completedReps = setJson.getInt("completedReps"),
                            plannedReps = setJson.getInt("plannedReps"),
                            setNumber = setJson.getInt("setNumber"),
                            timestamp = dateFormat.parse(setJson.getString("timestamp")) ?: Date()
                        )
                        database.completedSetDao().insert(set)
                        importedCount++
                    }
                }
                
                _state.update { it.copy(isImporting = false, importResult = ImportResult.Success(importedCount)) }
            } catch (e: Exception) {
                _state.update { it.copy(isImporting = false, importResult = ImportResult.Error(e.message ?: "Import failed")) }
            }
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, deleteResult = null) }
            
            try {
                withContext(Dispatchers.IO) {
                    database.completedSetDao().deleteAll()
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
    
    private fun generateCsv(sets: List<CompletedSet>): String {
        val sb = StringBuilder()
        sb.append("Date,Exercise,Weight,Reps,Set,Timestamp\n")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        sets.forEach { set ->
            sb.append("${dateFormat.format(set.timestamp)},")
            sb.append("${set.exerciseName},")
            sb.append("${set.weight},")
            sb.append("${set.completedReps},")
            sb.append("${set.setNumber},")
            sb.append("${timeFormat.format(set.timestamp)}\n")
        }
        
        return sb.toString()
    }
    
    private fun generateJson(sets: List<CompletedSet>): String {
        val json = JSONObject()
        json.put("exportDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()))
        json.put("appVersion", BuildConfig.VERSION_NAME)
        
        val workoutsArray = JSONArray()
        sets.forEach { set ->
            val setJson = JSONObject()
            setJson.put("exerciseName", set.exerciseName)
            setJson.put("weight", set.weight)
            setJson.put("completedReps", set.completedReps)
            setJson.put("plannedReps", set.plannedReps)
            setJson.put("setNumber", set.setNumber)
            setJson.put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(set.timestamp))
            workoutsArray.put(setJson)
        }
        
        json.put("workouts", workoutsArray)
        return json.toString(2)
    }
    
    private fun createShareIntent(file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    companion object {
        const val PREFS_NAME = "reps_settings"
        const val PREF_VIBRATION_ENABLED = "vibration_enabled"
        const val PREF_VIBRATION_DURATION = "vibration_duration"
        const val PREF_SOUND_ENABLED = "sound_enabled"
        const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
        const val PREF_DEFAULT_PAUSE_TIME = "default_pause_time"
        const val PREF_LANGUAGE = "language"
    }
}
