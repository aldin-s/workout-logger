package com.example.workouttracker.ui.history

import com.example.workouttracker.data.model.CompletedSet

sealed class HistoryItem {
    data class DateHeader(val dateLabel: String) : HistoryItem()
    data class WorkoutItem(val completedSet: CompletedSet) : HistoryItem()
}
