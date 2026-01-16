package com.example.workouttracker.data.model

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.workouttracker.R

/**
 * Exercise entity representing both predefined and custom exercises.
 * All exercises are stored in the database and can be reordered, renamed, or deleted.
 * 
 * For predefined exercises, we store a stable string key (e.g., "exercise_deadlift")
 * instead of an integer resource ID, because R.string.xxx values can change between builds.
 */
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    @Deprecated("Use nameResKey instead - resource IDs are unstable across builds")
    val nameResId: Int? = null,
    val nameResKey: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Maps stable string keys to resource IDs at runtime.
 * This ensures the correct string is always displayed, regardless of build.
 */
private val RESOURCE_KEY_MAP: Map<String, Int> = mapOf(
    "exercise_deadlift" to R.string.exercise_deadlift,
    "exercise_bench_press" to R.string.exercise_bench_press,
    "exercise_rowing" to R.string.exercise_rowing,
    "exercise_squat" to R.string.exercise_squat
)

/**
 * Returns the localized display name for an exercise (Composable version).
 * - For predefined exercises: Uses nameResKey for stable, dynamic localization
 * - For custom exercises: Uses the stored name
 * 
 * This ensures predefined exercises update when the app language changes,
 * and are immune to resource ID changes between builds.
 */
@Composable
fun Exercise.displayName(): String {
    val context = LocalContext.current
    return getDisplayName(context)
}

/**
 * Returns the localized display name for an exercise (non-Composable version).
 * Use this in ViewModels, Services, or other non-UI code.
 */
fun Exercise.getDisplayName(context: Context): String {
    // First try the stable key (new approach)
    if (!nameResKey.isNullOrEmpty()) {
        val resId = RESOURCE_KEY_MAP[nameResKey]
        if (resId != null) {
            return context.getString(resId)
        }
    }
    // Fallback to stored name for custom exercises
    return name
}
