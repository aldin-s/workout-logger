package com.asstudio.berlin.reps.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asstudio.berlin.reps.R
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryAdapter(
    private val onItemLongClick: ((WorkoutSession) -> Unit)? = null
) : ListAdapter<HistoryItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryItem.DateHeader -> VIEW_TYPE_HEADER
            is HistoryItem.WorkoutItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_history_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout_history, parent, false)
                WorkoutViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is HistoryItem.WorkoutItem -> (holder as WorkoutViewHolder).bind(item, onItemLongClick)
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateHeaderTextView: TextView = itemView.findViewById(R.id.dateHeaderTextView)

        fun bind(header: HistoryItem.DateHeader) {
            dateHeaderTextView.text = header.dateLabel
        }
    }

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNameTextView: TextView = itemView.findViewById(R.id.exerciseNameTextView)
        private val workoutDetailsTextView: TextView = itemView.findViewById(R.id.workoutDetailsTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(item: HistoryItem.WorkoutItem, onItemLongClick: ((WorkoutSession) -> Unit)? = null) {
            val session = item.session
            val context = itemView.context
            
            // Exercise name in uppercase
            exerciseNameTextView.text = session.exerciseName.uppercase()
            
            // Format workout details with sets info
            val setsText = context.resources.getQuantityString(
                com.asstudio.berlin.reps.R.plurals.sets_count,
                session.totalSets,
                session.totalSets
            )
            
            workoutDetailsTextView.text = String.format(
                "%.1f kg × %d Wdh · %s",
                session.weight,
                session.reps,
                setsText
            )
            
            // Format timestamp with date based on age
            timestampTextView.text = formatTimestamp(session.startTime, session.endTime, context)
            
            // Long-click listener for edit/delete
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(session)
                true
            }
        }
        
        private fun formatTimestamp(startTime: java.util.Date, endTime: java.util.Date, context: android.content.Context): String {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startTimeStr = timeFormat.format(startTime)
            val endTimeStr = timeFormat.format(endTime)
            
            val timeRange = if (startTimeStr == endTimeStr) {
                startTimeStr
            } else {
                "$startTimeStr - $endTimeStr"
            }
            
            // Determine date format based on age
            val calendar = java.util.Calendar.getInstance()
            val today = calendar.timeInMillis
            
            calendar.time = startTime
            val itemDate = calendar.timeInMillis
            val daysDiff = ((today - itemDate) / (1000 * 60 * 60 * 24)).toInt()
            
            return when {
                daysDiff == 0 -> timeRange // HEUTE: nur Zeit
                daysDiff == 1 -> timeRange // GESTERN: nur Zeit
                daysDiff in 2..6 -> {
                    // DIESE WOCHE: Wochentag
                    val weekdayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    "${weekdayFormat.format(startTime)} · $timeRange"
                }
                daysDiff in 7..13 -> {
                    // LETZTE WOCHE: Wochentag
                    val weekdayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    "${weekdayFormat.format(startTime)} · $timeRange"
                }
                daysDiff in 14..29 -> {
                    // DIESEN MONAT: Tag + Monat
                    val dateFormat = SimpleDateFormat("d. MMM", Locale.getDefault())
                    "${dateFormat.format(startTime)} · $timeRange"
                }
                daysDiff in 30..59 -> {
                    // LETZTER MONAT: Tag + Monat
                    val dateFormat = SimpleDateFormat("d. MMM", Locale.getDefault())
                    "${dateFormat.format(startTime)} · $timeRange"
                }
                else -> {
                    // ÄLTER: Kompaktes Datum
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    "${dateFormat.format(startTime)} · $timeRange"
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return when {
                oldItem is HistoryItem.DateHeader && newItem is HistoryItem.DateHeader ->
                    oldItem.dateLabel == newItem.dateLabel
                oldItem is HistoryItem.WorkoutItem && newItem is HistoryItem.WorkoutItem ->
                    oldItem.session.startTime == newItem.session.startTime &&
                    oldItem.session.exerciseName == newItem.session.exerciseName
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}