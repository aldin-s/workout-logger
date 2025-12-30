package com.example.workouttracker.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.workouttracker.BuildConfig
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import com.example.workouttracker.data.model.CompletedSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var database: WorkoutDatabase
    
    // UI Elements
    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var vibrationDurationLayout: LinearLayout
    private lateinit var vibrationDurationValue: TextView
    private lateinit var soundSwitch: SwitchCompat
    private lateinit var soundSelectionLayout: LinearLayout
    private lateinit var soundNameValue: TextView
    private lateinit var keepScreenOnSwitch: SwitchCompat
    private lateinit var defaultPauseTimeLayout: LinearLayout
    private lateinit var defaultPauseTimeValue: TextView
    private lateinit var languageLayout: LinearLayout
    private lateinit var languageValue: TextView
    private lateinit var exportHistoryButton: TextView
    private lateinit var importHistoryButton: TextView
    private lateinit var deleteAllDataButton: LinearLayout
    private lateinit var appVersionText: TextView

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                importFromJson(uri)
            }
        }
    }
    
    private val soundPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            saveSoundUri(uri)
            updateSoundName(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        database = WorkoutDatabase.getDatabase(this)

        initViews()
        loadSettings()
        setupClickListeners()
    }

    private fun initViews() {
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        vibrationDurationLayout = findViewById(R.id.vibrationDurationLayout)
        vibrationDurationValue = findViewById(R.id.vibrationDurationValue)
        soundSwitch = findViewById(R.id.soundSwitch)
        soundSelectionLayout = findViewById(R.id.soundSelectionLayout)
        soundNameValue = findViewById(R.id.soundNameValue)
        keepScreenOnSwitch = findViewById(R.id.keepScreenOnSwitch)
        defaultPauseTimeLayout = findViewById(R.id.defaultPauseTimeLayout)
        defaultPauseTimeValue = findViewById(R.id.defaultPauseTimeValue)
        languageLayout = findViewById(R.id.languageLayout)
        languageValue = findViewById(R.id.languageValue)
        exportHistoryButton = findViewById(R.id.exportHistoryButton)
        importHistoryButton = findViewById(R.id.importHistoryButton)
        deleteAllDataButton = findViewById(R.id.deleteAllDataButton)
        appVersionText = findViewById(R.id.appVersionText)
    }

    private fun loadSettings() {
        // Vibration
        val vibrationEnabled = prefs.getBoolean(PREF_VIBRATION_ENABLED, true)
        vibrationSwitch.isChecked = vibrationEnabled
        vibrationDurationLayout.visibility = if (vibrationEnabled) View.VISIBLE else View.GONE

        val vibrationDuration = prefs.getInt(PREF_VIBRATION_DURATION, 500)
        vibrationDurationValue.text = when (vibrationDuration) {
            200 -> getString(R.string.vibration_short)
            1000 -> getString(R.string.vibration_long)
            else -> getString(R.string.vibration_medium)
        }

        // Sound
        val soundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, false)
        soundSwitch.isChecked = soundEnabled
        soundSelectionLayout.visibility = if (soundEnabled) View.VISIBLE else View.GONE
        
        // Load saved sound name
        val soundUriString = prefs.getString(PREF_SOUND_URI, null)
        val soundUri = if (soundUriString != null) Uri.parse(soundUriString) else null
        updateSoundName(soundUri)
        
        // Keep Screen On
        val keepScreenOnEnabled = prefs.getBoolean(PREF_KEEP_SCREEN_ON, false)
        keepScreenOnSwitch.isChecked = keepScreenOnEnabled
        val pauseTime = prefs.getInt(PREF_DEFAULT_PAUSE_TIME, 120)
        defaultPauseTimeValue.text = getString(R.string.pause_time_seconds, pauseTime)

        // Language
        val language = prefs.getString(PREF_LANGUAGE, "de") ?: "de"
        languageValue.text = if (language == "de") {
            getString(R.string.language_german)
        } else {
            getString(R.string.language_english)
        }

        // App Version
        appVersionText.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
    }

    private fun setupClickListeners() {
        // Vibration Toggle
        findViewById<RelativeLayout>(R.id.vibrationToggleLayout).setOnClickListener {
            vibrationSwitch.isChecked = !vibrationSwitch.isChecked
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PREF_VIBRATION_ENABLED, isChecked).apply()
            vibrationDurationLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Vibration Duration
        vibrationDurationLayout.setOnClickListener {
            showVibrationDurationDialog()
        }

        // Sound Toggle
        findViewById<RelativeLayout>(R.id.soundToggleLayout).setOnClickListener {
            soundSwitch.isChecked = !soundSwitch.isChecked
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PREF_SOUND_ENABLED, isChecked).apply()
            soundSelectionLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Sound Selection
        soundSelectionLayout.setOnClickListener {
            openSystemSoundPicker()
        }
        
        keepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PREF_KEEP_SCREEN_ON, isChecked).apply()
        }

        // Default Pause Time
        defaultPauseTimeLayout.setOnClickListener {
            showPauseTimeDialog()
        }

        // Language
        languageLayout.setOnClickListener {
            showLanguageDialog()
        }

        // Export
        exportHistoryButton.setOnClickListener {
            showExportFormatDialog()
        }

        // Import
        importHistoryButton.setOnClickListener {
            openFilePicker()
        }

        // Delete All
        deleteAllDataButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showVibrationDurationDialog() {
        val options = arrayOf(
            getString(R.string.vibration_short),
            getString(R.string.vibration_medium),
            getString(R.string.vibration_long)
        )
        val values = intArrayOf(200, 500, 1000)
        val current = prefs.getInt(PREF_VIBRATION_DURATION, 500)
        val selected = values.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(R.string.vibration_duration)
            .setSingleChoiceItems(options, selected) { dialog, which ->
                prefs.edit().putInt(PREF_VIBRATION_DURATION, values[which]).apply()
                vibrationDurationValue.text = options[which]
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showPauseTimeDialog() {
        val options = arrayOf("30s", "60s", "90s", "120s", "180s", "300s")
        val values = intArrayOf(30, 60, 90, 120, 180, 300)
        val current = prefs.getInt(PREF_DEFAULT_PAUSE_TIME, 120)
        val selected = values.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(R.string.default_pause_time)
            .setSingleChoiceItems(options, selected) { dialog, which ->
                prefs.edit().putInt(PREF_DEFAULT_PAUSE_TIME, values[which]).apply()
                defaultPauseTimeValue.text = getString(R.string.pause_time_seconds, values[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showLanguageDialog() {
        val options = arrayOf(
            getString(R.string.language_german),
            getString(R.string.language_english)
        )
        val values = arrayOf("de", "en")
        val current = prefs.getString(PREF_LANGUAGE, "de") ?: "de"
        val selected = values.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(options, selected) { dialog, which ->
                prefs.edit().putString(PREF_LANGUAGE, values[which]).apply()
                dialog.dismiss()
                // Restart app to apply language
                Toast.makeText(this, "Please restart the app", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showExportFormatDialog() {
        val options = arrayOf(
            getString(R.string.export_csv),
            getString(R.string.export_json)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.export_format_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToCsv()
                    1 -> exportToJson()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun exportToCsv() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sets = database.completedSetDao().getAllSets()
                val csv = generateCsv(sets)
                val fileName = "reps_export_${getCurrentDate()}.csv"
                val file = File(cacheDir, fileName)
                file.writeText(csv)

                withContext(Dispatchers.Main) {
                    shareFile(file, "text/csv")
                    Toast.makeText(this@SettingsActivity, R.string.export_success, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Export CSV error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, R.string.export_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exportToJson() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sets = database.completedSetDao().getAllSets()
                val json = generateJson(sets)
                val fileName = "reps_export_${getCurrentDate()}.json"
                val file = File(cacheDir, fileName)
                file.writeText(json)

                withContext(Dispatchers.Main) {
                    shareFile(file, "application/json")
                    Toast.makeText(this@SettingsActivity, R.string.export_success, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Export JSON error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, R.string.export_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_history)))
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        importFileLauncher.launch(intent)
    }

    private fun importFromJson(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonString = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: throw Exception("Could not read file")

                val json = JSONObject(jsonString)
                val workoutsArray = json.getJSONArray("workouts")
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                var importedCount = 0

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

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsActivity,
                        getString(R.string.import_success, importedCount),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Import error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, R.string.import_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteAllData()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAllData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                database.completedSetDao().deleteAll()
                prefs.edit().clear().apply()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Delete all error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, R.string.error_generic, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    private fun openSystemSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.sound_picker_title))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getCurrentSoundUri())
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        }
        soundPickerLauncher.launch(intent)
    }
    
    private fun getCurrentSoundUri(): Uri? {
        val uriString = prefs.getString(PREF_SOUND_URI, null)
        return if (uriString != null) {
            Uri.parse(uriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }
    
    private fun saveSoundUri(uri: Uri?) {
        prefs.edit().putString(PREF_SOUND_URI, uri?.toString()).apply()
    }
    
    private fun updateSoundName(uri: Uri?) {
        val name = if (uri == null) {
            getString(R.string.sound_silent)
        } else {
            try {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                ringtone.getTitle(this)
            } catch (e: Exception) {
                getString(R.string.sound_silent)
            }
        }
        soundNameValue.text = name
    }

    companion object {
        const val PREFS_NAME = "reps_settings"
        const val PREF_VIBRATION_ENABLED = "vibration_enabled"
        const val PREF_VIBRATION_DURATION = "vibration_duration"
        const val PREF_SOUND_ENABLED = "sound_enabled"
        const val PREF_SOUND_URI = "sound_uri"
        const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
        const val PREF_DEFAULT_PAUSE_TIME = "default_pause_time"
        const val PREF_LANGUAGE = "language"
    }
}
