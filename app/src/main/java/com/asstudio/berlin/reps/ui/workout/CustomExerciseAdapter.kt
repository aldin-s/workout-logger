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
import com.asstudio.berlin.reps.data.model.CustomExercise
import com.google.android.material.card.MaterialCardView

class CustomExerciseAdapter(
    private val onExerciseClick: (String) -> Unit,
    private val onDeleteClick: (CustomExercise) -> Unit
) : ListAdapter<CustomExercise, CustomExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.exerciseCard)
        private val nameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(exercise: CustomExercise) {
            nameText.text = exercise.name
            deleteButton.visibility = View.VISIBLE
            
            card.setOnClickListener {
                onExerciseClick(exercise.name)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(exercise)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CustomExercise>() {
        override fun areItemsTheSame(oldItem: CustomExercise, newItem: CustomExercise): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CustomExercise, newItem: CustomExercise): Boolean {
            return oldItem == newItem
        }
    }
}
