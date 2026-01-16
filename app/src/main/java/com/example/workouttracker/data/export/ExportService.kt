package com.example.workouttracker.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.workouttracker.BuildConfig
import com.example.workouttracker.data.database.CompletedSetDao
import com.example.workouttracker.data.model.CompletedSet
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.repository.ExerciseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for exporting workout data to CSV and JSON formats.
 * Implements proper CSV escaping and uses kotlinx.serialization for JSON.
 */
@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val completedSetDao: CompletedSetDao,
    private val exerciseRepository: ExerciseRepository
) {
    
    private val json = Json { 
        prettyPrint = true
        encodeDefaults = true
    }
    
    /**
     * Export all data to JSON format with schema versioning.
     * @return Intent for sharing the exported file.
     */
    suspend fun exportToJson(): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val sets = completedSetDao.getAllSets()
            val exercises = exerciseRepository.getAllExercisesSnapshot()
            
            val exportData = ExportData(
                schemaVersion = ExportData.CURRENT_SCHEMA_VERSION,
                exportDate = System.currentTimeMillis(),
                appVersion = BuildConfig.VERSION_NAME,
                workouts = sets.map { it.toExport() },
                exercises = exercises.map { it.toExport() }
            )
            
            val jsonString = json.encodeToString(exportData)
            val fileName = "reps_export_${getCurrentDate()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonString)
            
            Result.success(createShareIntent(file, "application/json"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export workout data to CSV format with proper escaping.
     * Note: CSV only exports workouts, not exercises (CSV is for spreadsheet compatibility).
     * @return Intent for sharing the exported file.
     */
    suspend fun exportToCsv(): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val sets = completedSetDao.getAllSets()
            val csv = generateCsv(sets)
            val fileName = "reps_export_${getCurrentDate()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csv)
            
            Result.success(createShareIntent(file, "text/csv"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate CSV with proper escaping for special characters.
     * Handles commas, quotes, and newlines in field values.
     * 
     * Date format: Localized based on device settings (e.g., "16.01.2026, 14:35" for German).
     * Uses java.time API for modern, thread-safe date formatting.
     */
    private fun generateCsv(sets: List<CompletedSet>): String {
        val sb = StringBuilder()
        // English headers for universal CSV compatibility
        sb.append("Date,Exercise,Weight (kg),Reps,Planned,Set\n")
        
        // Localized date/time format based on device locale
        val dateFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        
        sets.forEach { set ->
            val formattedDate = dateFormatter.format(Instant.ofEpochMilli(set.timestamp.time))
            sb.append("${escapeCsv(formattedDate)},")
            sb.append("${escapeCsv(set.exerciseName)},")
            sb.append("${set.weight},")
            sb.append("${set.completedReps},")
            sb.append("${set.plannedReps},")
            sb.append("${set.setNumber}\n")
        }
        
        return sb.toString()
    }
    
    /**
     * Escape a string for CSV format according to RFC 4180.
     * - If the field contains comma, double-quote, or newline, wrap in quotes
     * - Double-quotes within the field are escaped by doubling them
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    private fun createShareIntent(file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun getCurrentDate(): String {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
            Instant.now().atZone(ZoneId.systemDefault())
        )
    }
    
    // Extension functions for conversion
    private fun CompletedSet.toExport() = WorkoutExport(
        exerciseName = exerciseName,
        weight = weight,
        completedReps = completedReps,
        plannedReps = plannedReps,
        setNumber = setNumber,
        timestamp = timestamp.time
    )
    
    private fun Exercise.toExport() = ExerciseExport(
        id = id,
        name = name,
        nameResKey = nameResKey,
        sortOrder = sortOrder,
        createdAt = createdAt
    )
}
