package com.asstudio.berlin.reps.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asstudio.berlin.reps.R
import com.google.android.material.card.MaterialCardView

class RecentExerciseAdapter(
    private val onExerciseClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<RecentExercise, RecentExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun submitList(list: List<RecentExercise>?) {
        // Disable animation for instant update
        super.submitList(list, null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.exerciseCard)
        private val nameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(exercise: RecentExercise) {
            nameText.text = exercise.name
            deleteButton.visibility = if (exercise.isCustom) View.VISIBLE else View.GONE
            
            card.setOnClickListener {
                onExerciseClick(exercise.name)
            }
            
            if (exercise.isCustom) {
                deleteButton.setOnClickListener {
                    onDeleteClick(exercise.name)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecentExercise>() {
        override fun areItemsTheSame(oldItem: RecentExercise, newItem: RecentExercise): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecentExercise, newItem: RecentExercise): Boolean {
            return oldItem == newItem
        }
    }
}
