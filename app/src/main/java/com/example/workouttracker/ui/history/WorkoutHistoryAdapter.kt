package com.example.workouttracker.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.R
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryAdapter : ListAdapter<HistoryItem, RecyclerView.ViewHolder>(DiffCallback()) {

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
            is HistoryItem.WorkoutItem -> (holder as WorkoutViewHolder).bind(item)
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

        fun bind(item: HistoryItem.WorkoutItem) {
            val completedSet = item.completedSet
            
            // Exercise name in uppercase
            exerciseNameTextView.text = completedSet.exerciseName.uppercase()
            
            // Format as: "80.0 kg × 10 · Satz 1"
            workoutDetailsTextView.text = String.format(
                "%.1f kg × %d Wdh · Satz %d",
                completedSet.weight,
                completedSet.completedReps,
                completedSet.setNumber
            )
            
            // Time format HH:mm
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timestampTextView.text = timeFormat.format(completedSet.timestamp)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return when {
                oldItem is HistoryItem.DateHeader && newItem is HistoryItem.DateHeader ->
                    oldItem.dateLabel == newItem.dateLabel
                oldItem is HistoryItem.WorkoutItem && newItem is HistoryItem.WorkoutItem ->
                    oldItem.completedSet.id == newItem.completedSet.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}