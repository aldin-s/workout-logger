package com.example.workouttracker.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.R
import com.example.workouttracker.data.model.CompletedSet
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryAdapter : ListAdapter<CompletedSet, WorkoutHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNameTextView: TextView = itemView.findViewById(R.id.exerciseNameTextView)
        private val weightRepsTextView: TextView = itemView.findViewById(R.id.weightRepsTextView)
        private val setInfoTextView: TextView = itemView.findViewById(R.id.setInfoTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(completedSet: CompletedSet) {
            exerciseNameTextView.text = completedSet.exerciseName
            weightRepsTextView.text = "${completedSet.weight}kg × ${completedSet.completedReps} reps"
            setInfoTextView.text = "Set ${completedSet.setNumber}"
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            timestampTextView.text = dateFormat.format(completedSet.timestamp)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CompletedSet>() {
        override fun areItemsTheSame(oldItem: CompletedSet, newItem: CompletedSet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CompletedSet, newItem: CompletedSet): Boolean {
            return oldItem == newItem
        }
    }
}