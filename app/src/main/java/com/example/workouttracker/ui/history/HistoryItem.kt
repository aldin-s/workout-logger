package com.example.workouttracker.ui.history

sealed class HistoryItem {
    data class DateHeader(val dateLabel: String) : HistoryItem()
    data class WorkoutItem(val session: WorkoutSession) : HistoryItem()
}
