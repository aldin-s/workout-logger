package com.asstudio.berlin.reps.ui.history

import android.content.Context
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.model.CompletedSet
import java.util.Calendar
import java.util.Date

object HistoryGrouper {
    
    fun groupByDate(completedSets: List<CompletedSet>, context: Context): List<HistoryItem> {
        if (completedSets.isEmpty()) return emptyList()
        
        val grouped = mutableListOf<HistoryItem>()
        val sortedSets = completedSets.sortedByDescending { it.timestamp }
        
        // First, group sets into workout sessions
        val sessions = groupIntoSessions(sortedSets)
        
        var currentDateLabel: String? = null
        
        for (session in sessions) {
            val dateLabel = getDateLabel(session.startTime, context)
            
            if (dateLabel != currentDateLabel) {
                grouped.add(HistoryItem.DateHeader(dateLabel))
                currentDateLabel = dateLabel
            }
            
            grouped.add(HistoryItem.WorkoutItem(session))
        }
        
        return grouped
    }
    
    /**
     * Groups consecutive sets of the same exercise into workout sessions
     */
    private fun groupIntoSessions(sets: List<CompletedSet>): List<WorkoutSession> {
        if (sets.isEmpty()) return emptyList()
        
        val sessions = mutableListOf<WorkoutSession>()
        var currentSets = mutableListOf<CompletedSet>()
        var currentExercise: String? = null
        var currentWeight: Double? = null
        
        for (set in sets) {
            // Check if this set belongs to current session
            // Same exercise and weight, and within 10 minutes of last set
            val belongsToCurrentSession = if (currentSets.isEmpty()) {
                false
            } else {
                set.exerciseName == currentExercise &&
                set.weight == currentWeight &&
                isWithinTimeWindow(set.timestamp, currentSets.last().timestamp, 10)
            }
            
            if (belongsToCurrentSession) {
                currentSets.add(set)
            } else {
                // Save previous session if exists
                if (currentSets.isNotEmpty()) {
                    sessions.add(createSession(currentSets))
                }
                
                // Start new session
                currentSets = mutableListOf(set)
                currentExercise = set.exerciseName
                currentWeight = set.weight
            }
        }
        
        // Don't forget the last session
        if (currentSets.isNotEmpty()) {
            sessions.add(createSession(currentSets))
        }
        
        return sessions
    }
    
    private fun createSession(sets: List<CompletedSet>): WorkoutSession {
        val sortedSets = sets.sortedBy { it.timestamp }
        return WorkoutSession(
            exerciseName = sortedSets.first().exerciseName,
            weight = sortedSets.first().weight,
            reps = sortedSets.first().completedReps,
            totalSets = sortedSets.size,
            startTime = sortedSets.first().timestamp,
            endTime = sortedSets.last().timestamp,
            sets = sortedSets
        )
    }
    
    private fun isWithinTimeWindow(time1: Date, time2: Date, minutes: Int): Boolean {
        val diffInMillis = Math.abs(time1.time - time2.time)
        val diffInMinutes = diffInMillis / (1000 * 60)
        return diffInMinutes <= minutes
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
