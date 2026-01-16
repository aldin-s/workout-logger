package com.example.workouttracker.data.export

import kotlinx.serialization.Serializable

/**
 * Root export data structure with schema versioning.
 * Schema version 1: Initial version with workouts and exercises.
 */
@Serializable
data class ExportData(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportDate: Long,
    val appVersion: String,
    val workouts: List<WorkoutExport>,
    val exercises: List<ExerciseExport>
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

/**
 * Exported workout set data.
 * All timestamps are in milliseconds (Long) for consistency.
 */
@Serializable
data class WorkoutExport(
    val exerciseName: String,
    val weight: Double,
    val completedReps: Int,
    val plannedReps: Int,
    val setNumber: Int,
    val timestamp: Long  // Milliseconds since epoch
)

/**
 * Exported exercise data.
 * Uses nameResKey for stable localization across builds.
 */
@Serializable
data class ExerciseExport(
    val id: String,
    val name: String,
    @Deprecated("Use nameResKey instead - resource IDs are unstable")
    val nameResId: Int? = null,
    val nameResKey: String? = null,
    val sortOrder: Int,
    val createdAt: Long  // Milliseconds since epoch
)

/**
 * Import result summary.
 */
data class ImportSummary(
    val workoutsImported: Int,
    val workoutsSkipped: Int,
    val exercisesImported: Int,
    val exercisesSkipped: Int
) {
    val totalImported: Int get() = workoutsImported + exercisesImported
    val totalSkipped: Int get() = workoutsSkipped + exercisesSkipped
}

/**
 * Exception for schema version mismatch.
 */
class UnsupportedSchemaVersionException(
    val foundVersion: Int,
    val supportedVersion: Int
) : Exception("Unsupported schema version: $foundVersion. Supported: $supportedVersion")
