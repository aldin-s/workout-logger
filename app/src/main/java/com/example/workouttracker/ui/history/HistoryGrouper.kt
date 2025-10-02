package com.example.workouttracker.ui.history

import android.content.Context
import com.example.workouttracker.R
import com.example.workouttracker.data.model.CompletedSet
import java.util.Calendar
import java.util.Date

object HistoryGrouper {
    
    fun groupByDate(completedSets: List<CompletedSet>, context: Context): List<HistoryItem> {
        if (completedSets.isEmpty()) return emptyList()
        
        val grouped = mutableListOf<HistoryItem>()
        val sortedSets = completedSets.sortedByDescending { it.timestamp }
        
        var currentDateLabel: String? = null
        
        for (set in sortedSets) {
            val dateLabel = getDateLabel(set.timestamp, context)
            
            if (dateLabel != currentDateLabel) {
                grouped.add(HistoryItem.DateHeader(dateLabel))
                currentDateLabel = dateLabel
            }
            
            grouped.add(HistoryItem.WorkoutItem(set))
        }
        
        return grouped
    }
    
    private fun getDateLabel(date: Date, context: Context): String {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        calendar.time = date
        val itemDate = calendar.timeInMillis
        
        val daysDiff = ((today - itemDate) / (1000 * 60 * 60 * 24)).toInt()
        
        return when {
            daysDiff == 0 -> context.getString(R.string.date_today)
            daysDiff == 1 -> context.getString(R.string.date_yesterday)
            daysDiff in 2..6 -> context.getString(R.string.date_this_week)
            daysDiff in 7..13 -> context.getString(R.string.date_last_week)
            daysDiff in 14..29 -> context.getString(R.string.date_this_month)
            daysDiff in 30..59 -> context.getString(R.string.date_last_month)
            else -> context.getString(R.string.date_older)
        }
    }
}
